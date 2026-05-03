package com.example.demo;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 時計の Bean 定義。テストでは Clock.fixed() に差し替え可能。 */
@Configuration
class ClockConfig {

  /** UTC の Clock を提供する。 */
  @Bean
  /* default */ Clock clock() {
    return Clock.systemUTC();
  }
}
