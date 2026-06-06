package com.example.demo.catalog.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.catalog.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Unit tests for {@link ProductExceptionHandler}. */
class ProductExceptionHandlerTest {

  /** テスト対象。 */
  private final ProductExceptionHandler sut = new ProductExceptionHandler();

  /** 404 ステータスを返すこと。 */
  @Test
  void shouldReturn404Status() {
    final ProblemDetail result = sut.handleNotFound(new ProductNotFoundException("id-1"));

    assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus(), "status should be 404");
  }
}
