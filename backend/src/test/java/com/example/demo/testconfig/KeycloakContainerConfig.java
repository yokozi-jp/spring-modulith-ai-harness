package com.example.demo.testconfig;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

/** Keycloak コンテナ設定。OAuth2 認証フローを使用するテストで必要。 */
@TestConfiguration(proxyBeanMethods = false)
public class KeycloakContainerConfig {

  /** Keycloak コンテナ。demo レルムをインポートする。 */
  @Bean
  @SuppressWarnings("resource")
  /* default */ KeycloakContainer keycloakContainer() {
    return new KeycloakContainer("quay.io/keycloak/keycloak:26.6.0")
        .withRealmImportFile("/keycloak/demo-realm.json");
  }

  /** Keycloak の OAuth2 プロパティを動的に登録する。 */
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
