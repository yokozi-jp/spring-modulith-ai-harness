package com.example.demo.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * メイン SecurityFilterChain。OAuth2 Login (BFF) でブラウザ認証を行う。
 *
 * <p>トークンはサーバーサイドセッション（Redis）に保持し、ブラウザには JSESSIONID Cookie のみ送信する。 CSRF は {@code .spa()} で SPA
 * 向けに設定する（CookieCsrfTokenRepository + BREACH 対策 + 認証後のトークン自動更新）。
 */
@Configuration(proxyBeanMethods = false)
class OAuth2SecurityConfig {

  /** OAuth2 Login (BFF) 用の SecurityFilterChain。 */
  @Bean
  @Order(2)
  @SuppressWarnings("PMD.SignatureDeclareThrowsException")
  /* default */ SecurityFilterChain oauth2LoginFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
        .oauth2Login(withDefaults())
        .oauth2Client(withDefaults())
        .csrf(CsrfConfigurer::spa);
    return http.build();
  }
}
