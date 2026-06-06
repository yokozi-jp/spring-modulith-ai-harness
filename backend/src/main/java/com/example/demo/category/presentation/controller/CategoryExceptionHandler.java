package com.example.demo.category.presentation.controller;

import com.example.demo.category.exception.CategoryNotFoundException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Category モジュールの例外ハンドラ。 */
@Slf4j
@RestControllerAdvice(basePackages = "com.example.demo.category")
public class CategoryExceptionHandler {

  /** CategoryNotFoundException を処理する。 */
  @ExceptionHandler(CategoryNotFoundException.class)
  /* default */ ProblemDetail handleNotFound(final CategoryNotFoundException ex) {
    log.warn("CategoryNotFoundException: {}", ex.getMessage());
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Not Found");
    problem.setDetail(ex.getMessage());
    problem.setType(URI.create("about:blank"));
    return problem;
  }
}
