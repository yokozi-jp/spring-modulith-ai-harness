package com.example.demo.pricing.domain.model.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Price} value object. */
class PriceTest {

  @Test
  void shouldCreateWithPositiveValue() {
    final Price price = new Price(new BigDecimal("1000.50"));

    assertEquals(new BigDecimal("1000.50"), price.value(), "value should match");
  }

  @Test
  void shouldThrowWhenValueIsNull() {
    assertThrows(NullPointerException.class, () -> new Price(null), "should throw on null value");
  }

  @Test
  void shouldThrowWhenValueIsZero() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Price(BigDecimal.ZERO),
        "should throw on zero value");
  }

  @Test
  void shouldThrowWhenValueIsNegative() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Price(new BigDecimal("-100")),
        "should throw on negative value");
  }

  @Test
  void shouldAcceptSmallPositiveValue() {
    final Price price = new Price(new BigDecimal("0.0001"));

    assertEquals(
        new BigDecimal("0.0001"), price.value(), "small positive value should be accepted");
  }
}
