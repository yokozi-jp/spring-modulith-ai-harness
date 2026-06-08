package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;

/**
 * Actuator エンドポイントの Basic 認証テスト。
 *
 * <p>/actuator/health は認証不要で公開。その他の Actuator エンドポイントは Basic 認証が必要。
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ActuatorSecurityTest {

  /** HTTP クライアント。 */
  private final TestRestTemplate restTemplate;

  /** /actuator/health は認証不要でアクセスできること。 */
  @Test
  void shouldAllowHealthWithoutAuth() {
    final ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/health", String.class);

    assertEquals(
        HttpStatus.OK, response.getStatusCode(), "health should be accessible without auth");
  }

  /** /actuator/info は認証なしで 401 になること。 */
  @Test
  void shouldRequireAuthForActuatorInfo() {
    final ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/info", String.class);

    assertEquals(
        HttpStatus.UNAUTHORIZED,
        response.getStatusCode(),
        "actuator/info should require authentication");
  }

  /** /actuator/info は Basic 認証で 200 になること。 */
  @Test
  void shouldAllowActuatorInfoWithBasicAuth() {
    final TestRestTemplate authed = restTemplate.withBasicAuth("admin", "admin");
    final ResponseEntity<String> response = authed.getForEntity("/actuator/info", String.class);

    assertEquals(
        HttpStatus.OK,
        response.getStatusCode(),
        "actuator/info should be accessible with basic auth");
  }
}
