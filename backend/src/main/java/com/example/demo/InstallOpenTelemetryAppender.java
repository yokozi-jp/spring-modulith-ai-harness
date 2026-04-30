package com.example.demo;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Logback の OpenTelemetryAppender に OpenTelemetry SDK インスタンスを接続する。
 *
 * <p>Spring Boot 4 は OpenTelemetry SDK を自動設定するが、Logback appender への接続は 手動で行う必要がある。このクラスが Bean 初期化時に
 * {@link OpenTelemetryAppender#install} を呼び出し、 ログの OTLP エクスポートを有効化する。
 */
@Component
final class InstallOpenTelemetryAppender implements InitializingBean {

  /** OpenTelemetry SDK インスタンス。 */
  private final OpenTelemetry openTelemetry;

  /* default */ InstallOpenTelemetryAppender(OpenTelemetry openTelemetry) {
    this.openTelemetry = openTelemetry;
  }

  @Override
  public void afterPropertiesSet() {
    OpenTelemetryAppender.install(this.openTelemetry);
  }
}
