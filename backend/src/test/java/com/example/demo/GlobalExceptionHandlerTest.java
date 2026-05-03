package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Unit tests for {@link GlobalExceptionHandler}. */
class GlobalExceptionHandlerTest {

  /** Handler under test. */
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void shouldReturn409ForIllegalState() {
    final ProblemDetail result = handler.handleIllegalState(new IllegalStateException("bad state"));

    assertEquals(HttpStatus.CONFLICT.value(), result.getStatus(), "status should be 409");
    assertEquals("Conflict", result.getTitle(), "title should be Conflict");
    assertEquals("bad state", result.getDetail(), "detail should contain message");
    assertEquals(URI.create("about:blank"), result.getType(), "type should be about:blank");
  }

  @Test
  void shouldReturn400ForIllegalArgument() {
    final ProblemDetail result =
        handler.handleIllegalArgument(new IllegalArgumentException("bad arg"));

    assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus(), "status should be 400");
    assertEquals("Bad Request", result.getTitle(), "title should be Bad Request");
    assertEquals("bad arg", result.getDetail(), "detail should contain message");
    assertEquals(URI.create("about:blank"), result.getType(), "type should be about:blank");
  }

  @Test
  void shouldReturn500ForUnhandledException() {
    final ProblemDetail result = handler.handleException(new RuntimeException("unexpected"));

    assertEquals(
        HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus(), "status should be 500");
    assertEquals(
        "Internal Server Error", result.getTitle(), "title should be Internal Server Error");
    assertEquals(URI.create("about:blank"), result.getType(), "type should be about:blank");
  }
}
