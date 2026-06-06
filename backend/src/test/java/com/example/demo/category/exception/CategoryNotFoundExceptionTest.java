package com.example.demo.category.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link CategoryNotFoundException}. */
class CategoryNotFoundExceptionTest {

  /** メッセージに引数が含まれること。 */
  @Test
  void shouldContainArgumentInMessage() {
    final CategoryNotFoundException ex = new CategoryNotFoundException("test-id");

    assertEquals("Category not found: test-id", ex.getMessage(), "message should contain id");
  }
}
