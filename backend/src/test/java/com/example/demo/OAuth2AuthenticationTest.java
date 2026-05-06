package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.testconfig.FullStackContainerConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;

/**
 * OAuth2 認証フローテスト。
 *
 * <p>未認証で API にアクセスすると OAuth2 ログインページにリダイレクトされることを検証する。 Keycloak + Redis（セッション）が必要。
 */
@Tag("e2e")
@Import(FullStackContainerConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class OAuth2AuthenticationTest {

  /** HTTP クライアント（リダイレクトを追跡しない設定）。 */
  private final TestRestTemplate restTemplate;

  /** 未認証で API にアクセスすると 302 リダイレクトになること。 */
  @Test
  void shouldRedirectToLoginWhenUnauthenticated() {
    final ResponseEntity<String> response =
        restTemplate.getForEntity("/api/v1/samples", String.class);

    assertEquals(
        HttpStatus.FOUND,
        response.getStatusCode(),
        "unauthenticated request should redirect to login");
  }

  /** リダイレクト先が OAuth2 認可エンドポイントであること。 */
  @Test
  void shouldRedirectToKeycloakAuthEndpoint() {
    final ResponseEntity<String> response =
        restTemplate.getForEntity("/api/v1/samples", String.class);

    assertNotNull(response.getHeaders().getLocation(), "Location header should be present");
    assertTrue(
        response.getHeaders().getLocation().toString().contains("/oauth2/authorization/keycloak"),
        "should redirect to OAuth2 authorization endpoint");
  }
}
