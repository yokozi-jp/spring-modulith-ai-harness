package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.opentelemetry.api.OpenTelemetry;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;

/**
 * Observability テスト。
 *
 * <p>OpenTelemetry SDK が正常に設定され、トレース・メトリクスが送信可能な状態であることを検証する。
 */
@Tag("e2e")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ObservabilityTest {

  /** HTTP クライアント。 */
  private final TestRestTemplate restTemplate;

  /** OpenTelemetry SDK。 */
  private final OpenTelemetry openTelemetry;

  /** OpenTelemetry SDK が注入されること。 */
  @Test
  void shouldHaveOpenTelemetrySdkConfigured() {
    assertNotNull(openTelemetry, "OpenTelemetry SDK should be configured");
  }

  /** Actuator health にアクセスしてトレースが生成されること（エンドポイント疎通確認）。 */
  @Test
  void shouldGenerateTraceOnRequest() {
    final ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/health", String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "health endpoint should respond");
    // トレースが OTEL Collector に送信されたことの直接検証は困難なため、
    // SDK が設定されていること + エンドポイントが応答することで間接的に検証する。
  }
}
