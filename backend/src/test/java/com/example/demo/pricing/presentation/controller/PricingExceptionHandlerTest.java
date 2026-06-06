package com.example.demo.pricing.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.pricing.exception.PricingNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Unit tests for {@link PricingExceptionHandler}. */
class PricingExceptionHandlerTest {

  /** テスト対象。 */
  private final PricingExceptionHandler sut = new PricingExceptionHandler();

  /** 404 ステータスを返すこと。 */
  @Test
  void shouldReturn404Status() {
    final ProblemDetail result = sut.handleNotFound(new PricingNotFoundException("test-id"));

    assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus(), "status should be 404");
  }

  /** メッセージに ID が含まれること。 */
  @Test
  void shouldContainIdInDetail() {
    final ProblemDetail result = sut.handleNotFound(new PricingNotFoundException("test-id"));

    assertEquals("Pricing not found: test-id", result.getDetail(), "detail should contain id");
  }
}
