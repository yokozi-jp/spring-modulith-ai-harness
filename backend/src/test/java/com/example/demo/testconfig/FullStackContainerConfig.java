package com.example.demo.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/** 全コンテナを合成したフルスタック設定。@SpringBootTest で使用する。 */
@TestConfiguration(proxyBeanMethods = false)
@Import({
  PostgresContainerConfig.class,
  RedisContainerConfig.class,
  KeycloakContainerConfig.class,
  ObservabilityContainerConfig.class
})
public class FullStackContainerConfig {}
