package com.example.demo.category.domain.model.valueobject.identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link CategoryId}. */
class CategoryIdTest {

  /** テスト用 ID 値。 */
  private static final String ID_VALUE = "cat-001";

  @Test
  void shouldCreateWithValidValue() {
    final CategoryId id = new CategoryId(ID_VALUE);

    assertEquals(ID_VALUE, id.value(), "value should match");
  }

  @Test
  void shouldThrowWhenValueIsNull() {
    assertThrows(
        NullPointerException.class, () -> new CategoryId(null), "should throw on null value");
  }

  @Test
  void shouldThrowWhenValueIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> new CategoryId("  "), "should throw on blank value");
  }

  @Test
  void shouldBeEqualForSameValue() {
    final CategoryId id1 = new CategoryId(ID_VALUE);
    final CategoryId id2 = new CategoryId(ID_VALUE);

    assertEquals(id1, id2, "same value should be equal");
  }
}
