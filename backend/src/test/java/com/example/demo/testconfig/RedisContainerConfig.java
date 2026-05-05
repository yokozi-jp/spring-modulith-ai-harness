package com.example.demo.testconfig;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.utility.DockerImageName;

/** Redis コンテナ設定。Spring Session を使用するテストで必要。 */
@TestConfiguration(proxyBeanMethods = false)
public class RedisContainerConfig {

  /** Redis コンテナ。 */
  @Bean
  @ServiceConnection
  /* default */ RedisContainer redisContainer() {
    return new RedisContainer(DockerImageName.parse("redis:8.6.2"));
  }
}
