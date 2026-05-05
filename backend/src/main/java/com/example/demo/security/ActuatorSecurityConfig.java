package com.example.demo.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Actuator / Swagger 用の Security 設定。Basic 認証で保護する。
 *
 * <p>ステートレス構成にし、OAuth2 の {@code AuthenticationEntryPoint} を排除するため {@link
 * FlushingBasicAuthEntryPoint} でレスポンスを即座にコミットする。
 *
 * <p>ECS タスクヘルスチェックおよび ALB ターゲットグループのヘルスチェックは認証不要で公開する。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ActuatorAuthProperties.class)
class ActuatorSecurityConfig {

  /** Actuator + Swagger 用の SecurityFilterChain。 */
  @Bean
  @Order(1)
  @SuppressWarnings("PMD.SignatureDeclareThrowsException")
  /* default */ SecurityFilterChain actuatorSwaggerFilterChain(HttpSecurity http) throws Exception {
    final FlushingBasicAuthEntryPoint entryPoint = new FlushingBasicAuthEntryPoint("actuator");

    http.securityMatcher("/actuator/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/actuator/health/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(basic -> basic.authenticationEntryPoint(entryPoint))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
        .csrf(AbstractHttpConfigurer::disable);
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
