package com.example.demo.pricing.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link PricingNotFoundException}. */
class PricingNotFoundExceptionTest {

  /** メッセージに引数が含まれること。 */
  @Test
  void shouldContainArgumentInMessage() {
    final PricingNotFoundException ex = new PricingNotFoundException("test-id");

    assertEquals("Pricing not found: test-id", ex.getMessage(), "message should contain id");
  }
}
