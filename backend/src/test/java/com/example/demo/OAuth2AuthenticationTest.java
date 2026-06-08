package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * OAuth2 認証フローテスト。
 *
 * <p>未認証で API にアクセスすると OAuth2 ログインページにリダイレクトされることを検証する。 Keycloak + Redis（セッション）が必要。
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OAuth2AuthenticationTest {

  /** テスト用サーバーポート。 */
  @LocalServerPort private int port;

  /** リダイレクト追跡しない HTTP クライアント。 */
  private final HttpClient httpClient =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

  /** 未認証で API にアクセスすると 302 リダイレクトになること。 */
  @Test
  void shouldRedirectToLoginWhenUnauthenticated() throws Exception {
    final HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/v1/samples"))
            .GET()
            .build();

    final HttpResponse<String> response =
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(302, response.statusCode(), "unauthenticated request should redirect to login");
  }

  /** リダイレクト先が OAuth2 認可エンドポイントであること。 */
  @Test
  void shouldRedirectToKeycloakAuthEndpoint() throws Exception {
    final HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/v1/samples"))
            .GET()
            .build();

    final HttpResponse<String> response =
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    final String location = response.headers().firstValue("Location").orElse(null);
    assertNotNull(location, "Location header should be present");
    assertTrue(
        location.contains("/oauth2/authorization/keycloak"),
        "should redirect to OAuth2 authorization endpoint, got: " + location);
  }
}
