package com.example.demo.pricing.domain.model.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link PricingLevel} enum. */
class PricingLevelTest {

  @Test
  void shouldHaveTwoValues() {
    assertEquals(2, PricingLevel.values().length, "should have 2 levels");
  }

  @Test
  void shouldContainRegion() {
    assertEquals(PricingLevel.REGION, PricingLevel.valueOf("REGION"), "REGION should exist");
  }

  @Test
  void shouldContainPrefecture() {
    assertEquals(
        PricingLevel.PREFECTURE, PricingLevel.valueOf("PREFECTURE"), "PREFECTURE should exist");
  }
}
