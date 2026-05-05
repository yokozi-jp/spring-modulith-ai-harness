package com.example.demo.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.utility.DockerImageName;

/** Grafana LGTM コンテナ設定。OpenTelemetry エクスポートを検証するテストで必要。 */
@TestConfiguration(proxyBeanMethods = false)
public class ObservabilityContainerConfig {

  /** Grafana LGTM（Loki + Grafana + Tempo + Mimir）コンテナ。 */
  @Bean
  @ServiceConnection
  /* default */ LgtmStackContainer grafanaLgtmContainer() {
    return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.26.0"));
  }
}
