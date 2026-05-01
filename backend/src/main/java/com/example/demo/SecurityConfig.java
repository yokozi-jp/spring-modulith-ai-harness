package com.example.demo;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 設定。
 *
 * <p>2 つの SecurityFilterChain を定義する:
 *
 * <ol>
 *   <li>Actuator / Swagger — Basic 認証（ステートレス）
 *   <li>それ以外 — OAuth2 Login (BFF) + セッション Cookie
 * </ol>
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties(ActuatorAuthProperties.class)
class SecurityConfig {

  /**
   * Actuator + Swagger 用の SecurityFilterChain。Basic 認証で保護する。
   *
   * <p>ステートレス構成にし、OAuth2 の AuthenticationEntryPoint を排除するため exceptionHandling で
   * HttpStatusEntryPoint(401) を明示設定する。httpBasic が WWW-Authenticate ヘッダーを付与する。
   *
   * <p>ECS タスクヘルスチェックおよび ALB ターゲットグループのヘルスチェックは認証不要で公開する。
   */
  @Bean
  @Order(1)
  @SuppressWarnings("PMD.SignatureDeclareThrowsException")
  /* default */ SecurityFilterChain actuatorSwaggerFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/actuator/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/actuator/health/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(
            basic ->
                basic.authenticationEntryPoint(
                    (request, response, authException) -> {
                      response.setStatus(401);
                      response.setHeader("WWW-Authenticate", "Basic realm=\"actuator\"");
                      response.flushBuffer();
                    }))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                    (request, response, authException) -> {
                      response.setStatus(401);
                      response.setHeader("WWW-Authenticate", "Basic realm=\"actuator\"");
                      response.flushBuffer();
                    }))
        .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }

  /**
   * メイン SecurityFilterChain。OAuth2 Login (BFF) でブラウザ認証を行う。
   *
   * <p>トークンはサーバーサイドセッション（Redis）に保持し、ブラウザには JSESSIONID Cookie のみ送信する。 CSRF は {@code .spa()} で SPA
   * 向けに設定する（CookieCsrfTokenRepository + BREACH 対策 + 認証後のトークン自動更新）。
   */
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

  /** Actuator / Swagger 用の Basic 認証ユーザー。資格情報は {@link ActuatorAuthProperties} で注入する。 */
  @Bean
  /* default */ UserDetailsService userDetailsService(ActuatorAuthProperties props) {
    final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    final InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
    manager.createUser(
        User.builder()
            .username(props.username())
            .password(encoder.encode(props.password()))
            .roles("ACTUATOR")
            .build());
    return manager;
  }
}
