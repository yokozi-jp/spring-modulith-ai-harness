package com.example.demo.category.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.category.exception.CategoryNotFoundException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Unit tests for {@link CategoryExceptionHandler}. */
class CategoryExceptionHandlerTest {

  /** テスト対象。 */
  private final CategoryExceptionHandler sut = new CategoryExceptionHandler();

  /** テスト用カテゴリ ID。 */
  private static final String CATEGORY_ID = "cat-001";

  /** 404 ステータスを返すこと。 */
  @Test
  void shouldReturn404Status() {
    final ProblemDetail result = sut.handleNotFound(new CategoryNotFoundException(CATEGORY_ID));

    assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus(), "status should be 404");
  }

  /** タイトルが Not Found であること。 */
  @Test
  void shouldReturnNotFoundTitle() {
    final ProblemDetail result = sut.handleNotFound(new CategoryNotFoundException(CATEGORY_ID));

    assertEquals("Not Found", result.getTitle(), "title should be Not Found");
  }

  /** 詳細メッセージに ID が含まれること。 */
  @Test
  void shouldContainIdInDetail() {
    final ProblemDetail result = sut.handleNotFound(new CategoryNotFoundException(CATEGORY_ID));

    assertEquals("Category not found: cat-001", result.getDetail(), "detail should contain id");
  }

  /** type が about:blank であること。 */
  @Test
  void shouldHaveBlankType() {
    final ProblemDetail result = sut.handleNotFound(new CategoryNotFoundException(CATEGORY_ID));

    assertEquals(URI.create("about:blank"), result.getType(), "type should be about:blank");
  }
}
