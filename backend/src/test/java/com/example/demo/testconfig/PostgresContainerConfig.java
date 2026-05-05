package com.example.demo.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/** PostgreSQL コンテナ設定。DB のみのテスト（@JooqTest 等）で使用する。 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainerConfig {

  /** PostgreSQL コンテナ。initScript で demo スキーマを作成する。 */
  @Bean
  @ServiceConnection
  @SuppressWarnings("resource")
  /* default */ PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer(DockerImageName.parse("postgres:18.3"))
        .withInitScript("initdb/01-create-schema.sql");
  }
}
