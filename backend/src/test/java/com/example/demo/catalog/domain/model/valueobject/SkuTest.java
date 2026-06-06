package com.example.demo.catalog.domain.model.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link Sku} value object. */
class SkuTest {

  /** 有効な SKU 値。 */
  private static final String VALID_SKU = "SKU-1234567890123456789012345678";

  @Test
  void shouldCreateWithValidFormat() {
    final Sku sku = new Sku(VALID_SKU);

    assertEquals(VALID_SKU, sku.value(), "value should match");
  }

  @Test
  void shouldThrowWhenValueIsNull() {
    assertThrows(NullPointerException.class, () -> new Sku(null), "should throw on null value");
  }

  @Test
  void shouldThrowWhenMissingPrefix() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Sku("1234567890123456789012345678"),
        "should throw when missing SKU- prefix");
  }

  @Test
  void shouldThrowWhenTooFewDigits() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Sku("SKU-123456789012345678901234567"),
        "should throw when fewer than 28 digits");
  }

  @Test
  void shouldThrowWhenTooManyDigits() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Sku("SKU-12345678901234567890123456789"),
        "should throw when more than 28 digits");
  }

  @Test
  void shouldThrowWhenContainsLettersAfterPrefix() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Sku("SKU-123456789012345678901234567a"),
        "should throw when contains non-digit characters");
  }

  @Test
  void shouldBeEqualForSameValue() {
    final Sku sku1 = new Sku(VALID_SKU);
    final Sku sku2 = new Sku(VALID_SKU);

    assertEquals(sku1, sku2, "same value should be equal");
  }
}
