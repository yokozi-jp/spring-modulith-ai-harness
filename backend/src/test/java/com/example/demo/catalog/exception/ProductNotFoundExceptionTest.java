package com.example.demo.catalog.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProductNotFoundException}. */
class ProductNotFoundExceptionTest {

  /** メッセージに引数が含まれること。 */
  @Test
  void shouldContainArgumentInMessage() {
    final ProductNotFoundException ex = new ProductNotFoundException("test-id");

    assertEquals("Product not found: test-id", ex.getMessage(), "message should contain id");
  }
}
