package com.example.demo.pricing.domain.model.valueobject.identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link PricingId}. */
class PricingIdTest {

  @Test
  void shouldCreateWithValidValue() {
    final PricingId id = new PricingId("pricing-001");

    assertEquals("pricing-001", id.value(), "value should match");
  }

  @Test
  void shouldThrowWhenValueIsNull() {
    assertThrows(
        NullPointerException.class, () -> new PricingId(null), "should throw on null value");
  }

  @Test
  void shouldThrowWhenValueIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> new PricingId("  "), "should throw on blank value");
  }

  @Test
  void shouldThrowWhenValueIsEmpty() {
    assertThrows(
        IllegalArgumentException.class, () -> new PricingId(""), "should throw on empty value");
  }
}
