package com.example.demo;

import com.redis.testcontainers.RedisContainer;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/** ローカル開発・テスト用の Testcontainers 設定 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  /* default */ LgtmStackContainer grafanaLgtmContainer() {
    return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.26.0"));
  }

  @Bean
  @ServiceConnection
  /* default */ PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer(DockerImageName.parse("postgres:18.3"));
  }

  @Bean
  @ServiceConnection
  /* default */ RedisContainer redisContainer() {
    return new RedisContainer(DockerImageName.parse("redis:8.6.2"));
  }

  @Bean
  @SuppressWarnings("resource")
  /* default */ KeycloakContainer keycloakContainer() {
    return new KeycloakContainer("quay.io/keycloak/keycloak:26.6.0")
        .withRealmImportFile("/keycloak/demo-realm.json");
  }

  @Bean
  /* default */ DynamicPropertyRegistrar keycloakProperties(KeycloakContainer keycloak) {
    return registry -> {
      final String oidcBase = keycloak.getAuthServerUrl() + "/realms/demo/protocol/openid-connect";
      registry.add(
          "spring.security.oauth2.client.provider.keycloak.authorization-uri",
          () -> oidcBase + "/auth");
      registry.add(
          "spring.security.oauth2.client.provider.keycloak.token-uri", () -> oidcBase + "/token");
      registry.add(
          "spring.security.oauth2.client.provider.keycloak.jwk-set-uri", () -> oidcBase + "/certs");
      registry.add(
          "spring.security.oauth2.client.provider.keycloak.user-info-uri",
          () -> oidcBase + "/userinfo");
      registry.add(
          "spring.security.oauth2.client.registration.keycloak.client-id", () -> "demo-app");
      registry.add(
          "spring.security.oauth2.client.registration.keycloak.client-secret",
          () -> "demo-app-secret");
    };
  }
}
