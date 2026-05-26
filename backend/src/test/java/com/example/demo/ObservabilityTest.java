package com.example.demo;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.api.OpenTelemetry;
import java.net.URI;
import java.time.Instant;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;
import org.springframework.web.client.RestClient;

/**
 * Observability E2E テスト。
 *
 * <p>OpenTelemetry SDK が正常に設定され、トレース・メトリクス・ログが grafana/otel-lgtm コンテナに送信されていることを バックエンド API（Tempo,
 * Prometheus, Loki）を通じて検証する。
 */
@Tag("e2e")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
class ObservabilityTest {

  /** ポーリング最大待機秒数（メトリクス export interval を考慮）。 */
  private static final int POLL_TIMEOUT_SECONDS = 90;

  /** ポーリング間隔秒数。 */
  private static final int POLL_INTERVAL_SECONDS = 3;

  /** アプリケーションの service.name（spring.application.name から自動設定される）。 */
  private static final String SERVICE_NAME = "demo";

  /** HTTP クライアント（アプリケーションへのリクエスト用）。 */
  private final TestRestTemplate restTemplate;

  /** OpenTelemetry SDK。 */
  private final OpenTelemetry openTelemetry;

  /** OTEL トレースエンドポイント（ホスト名の導出に使用）。 */
  private final String tracesEndpoint;

  /** テスト用コンストラクタ。 */
  /* default */ ObservabilityTest(
      final TestRestTemplate restTemplate,
      final OpenTelemetry openTelemetry,
      @Value("${management.opentelemetry.tracing.export.otlp.endpoint}")
          final String tracesEndpoint) {
    this.restTemplate = restTemplate;
    this.openTelemetry = openTelemetry;
    this.tracesEndpoint = tracesEndpoint;
  }

  /** OpenTelemetry SDK が noop でないこと。 */
  @Test
  void shouldHaveOpenTelemetrySdkConfigured() {
    assertNotNull(openTelemetry, "OpenTelemetry SDK should be configured");
    assertNotEquals(
        OpenTelemetry.noop(), openTelemetry, "OpenTelemetry should not be noop instance");
  }

  /** トレースが OTEL Collector 経由で Tempo に到達すること。 */
  @Test
  void shouldExportTracesToTempo() {
    final RestClient client = buildNoErrorClient();
    final String tempoBaseUrl = deriveBaseUrl(tracesEndpoint, 3200);

    // OTLP HTTP エンドポイントにテストトレースを直接送信
    final long now = Instant.now().toEpochMilli() * 1_000_000L;
    final String payload =
        """
        {"resourceSpans":[{"resource":{"attributes":[{"key":"service.name",\
        "value":{"stringValue":"e2e-test"}}]},"scopeSpans":[{"spans":[{\
        "traceId":"a1b2c3d4e5f6a7b8a1b2c3d4e5f6a7b8",\
        "spanId":"b1c2d3e4f5a6b7c8","name":"e2e-verify",\
        "startTimeUnixNano":"%d","endTimeUnixNano":"%d"}]}]}]}\
        """
            .formatted(now - 1_000_000_000L, now);

    final ResponseEntity<String> otlpResponse =
        client
            .post()
            .uri(URI.create(tracesEndpoint))
            .header("Content-Type", "application/json")
            .body(payload)
            .retrieve()
            .toEntity(String.class);
    assertTrue(
        otlpResponse.getStatusCode().is2xxSuccessful(), "OTLP endpoint should accept trace data");

    // Tempo でトレースが取得可能であること
    final URI traceUri = URI.create(tempoBaseUrl + "/api/traces/a1b2c3d4e5f6a7b8a1b2c3d4e5f6a7b8");

    Awaitility.await()
        .atMost(POLL_TIMEOUT_SECONDS, SECONDS)
        .pollInterval(POLL_INTERVAL_SECONDS, SECONDS)
        .untilAsserted(
            () -> {
              final ResponseEntity<String> response =
                  client.get().uri(traceUri).retrieve().toEntity(String.class);

              assertTrue(
                  response.getStatusCode().is2xxSuccessful(), "Tempo should return the trace");
              assertNotNull(response.getBody(), "Tempo response body should not be null");
              assertTrue(
                  response.getBody().contains("e2e-verify"), "Tempo should contain the test span");
            });
  }

  /** アプリ固有メトリクスが Prometheus に記録されること。 */
  @Test
  void shouldExportMetricsToPrometheus() {
    // アプリにリクエストを送り HTTP メトリクスを生成させる
    restTemplate.getForEntity("/actuator/health", String.class);

    final RestClient client = buildNoErrorClient();
    final URI prometheusUri =
        URI.create(
            deriveBaseUrl(tracesEndpoint, 9090)
                + "/api/v1/query?query=http_server_request_duration_seconds_count%7Bjob%3D%22"
                + SERVICE_NAME
                + "%22%7D");

    Awaitility.await()
        .atMost(POLL_TIMEOUT_SECONDS, SECONDS)
        .pollInterval(POLL_INTERVAL_SECONDS, SECONDS)
        .untilAsserted(
            () -> {
              final ResponseEntity<String> response =
                  client.get().uri(prometheusUri).retrieve().toEntity(String.class);

              final String body = response.getBody();
              assertNotNull(body, "Prometheus response body should not be null");
              assertTrue(
                  body.contains("\"success\""), "Prometheus query should return success status");
              assertTrue(
                  body.contains("\"result\":[{"),
                  "Prometheus should contain at least one metric result");
            });
  }

  /** アプリのログが Loki に記録されること。 */
  @Test
  void shouldExportLogsToLoki() {
    // アプリにリクエストを送りログを生成させる
    restTemplate.getForEntity("/actuator/health", String.class);

    final RestClient client = buildNoErrorClient();
    final String lokiBaseUrl = deriveBaseUrl(tracesEndpoint, 3100);
    final long startNanos = Instant.now().minusSeconds(300).toEpochMilli() * 1_000_000L;
    final long endNanos = Instant.now().plusSeconds(60).toEpochMilli() * 1_000_000L;
    final URI lokiUri =
        URI.create(
            lokiBaseUrl
                + "/loki/api/v1/query_range?query=%7Bservice_name%3D%22"
                + SERVICE_NAME
                + "%22%7D&start="
                + startNanos
                + "&end="
                + endNanos
                + "&limit=1");

    Awaitility.await()
        .atMost(POLL_TIMEOUT_SECONDS, SECONDS)
        .pollInterval(POLL_INTERVAL_SECONDS, SECONDS)
        .untilAsserted(
            () -> {
              final ResponseEntity<String> response =
                  client.get().uri(lokiUri).retrieve().toEntity(String.class);

              final String body = response.getBody();
              assertNotNull(body, "Loki response body should not be null");
              assertTrue(body.contains("\"success\""), "Loki query should return success status");
              assertTrue(body.contains("\"values\""), "Loki should contain log entries");
            });
  }

  /** Actuator health エンドポイントが正常応答すること。 */
  @Test
  void shouldRespondOnHealthEndpoint() {
    final ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/health", String.class);

    assertTrue(
        response.getStatusCode().is2xxSuccessful(), "health endpoint should return 2xx status");
  }

  /**
   * エラーレスポンスで例外をスローしない RestClient を構築する。
   *
   * @return RestClient インスタンス
   */
  private static RestClient buildNoErrorClient() {
    return RestClient.builder().defaultStatusHandler(status -> true, (req, res) -> {}).build();
  }

  /**
   * OTEL エンドポイント URL からホスト名を抽出し、指定ポートのベース URL を構築する。
   *
   * @param endpoint OTEL エンドポイント（例: http://grafana-lgtm-e2e:4318/v1/traces）
   * @param port 対象サービスのポート番号
   * @return ベース URL（例: http://grafana-lgtm-e2e:3200）
   */
  private static String deriveBaseUrl(final String endpoint, final int port) {
    final URI uri = URI.create(endpoint);
    return uri.getScheme() + "://" + uri.getHost() + ":" + port;
  }
}
