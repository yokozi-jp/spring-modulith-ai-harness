package com.example.demo;

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
  @SuppressWarnings("resource")
  /* default */ KeycloakContainer keycloakContainer() {
    return new KeycloakContainer("quay.io/keycloak/keycloak:26.6.0")
        .withRealmImportFile("/keycloak/demo-realm.json");
  }

  @Bean
  /* default */ DynamicPropertyRegistrar keycloakProperties(KeycloakContainer keycloak) {
    return registry ->
        registry.add(
            "spring.security.oauth2.resourceserver.jwt.issuer-uri",
            () -> keycloak.getAuthServerUrl() + "/realms/demo");
  }
}
