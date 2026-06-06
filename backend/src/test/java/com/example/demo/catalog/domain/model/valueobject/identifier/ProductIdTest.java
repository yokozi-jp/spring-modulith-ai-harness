package com.example.demo.catalog.domain.model.valueobject.identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProductId}. */
class ProductIdTest {

  /** テスト用 ID 値。 */
  private static final String ID_VALUE = "prod-001";

  @Test
  void shouldCreateWithValidValue() {
    final ProductId id = new ProductId(ID_VALUE);

    assertEquals(ID_VALUE, id.value(), "value should match");
  }

  @Test
  void shouldThrowWhenValueIsNull() {
    assertThrows(
        NullPointerException.class, () -> new ProductId(null), "should throw on null value");
  }

  @Test
  void shouldThrowWhenValueIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> new ProductId("  "), "should throw on blank value");
  }

  @Test
  void shouldBeEqualForSameValue() {
    final ProductId id1 = new ProductId(ID_VALUE);
    final ProductId id2 = new ProductId(ID_VALUE);

    assertEquals(id1, id2, "same value should be equal");
  }
}
