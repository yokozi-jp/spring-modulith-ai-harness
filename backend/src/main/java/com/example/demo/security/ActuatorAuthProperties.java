package com.example.demo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Actuator / Swagger 用 Basic 認証の資格情報。 */
@ConfigurationProperties(prefix = "actuator")
record ActuatorAuthProperties(String username, String password) {}
