package com.example.demo.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** {@link Clock} Bean の構成。テストでは {@code Clock.fixed()} に差し替え可能。 */
@Configuration
class ClockConfig {

  /** UTC の Clock を提供する。 */
  @Bean
  /* default */ Clock clock() {
    return Clock.systemUTC();
  }
}
