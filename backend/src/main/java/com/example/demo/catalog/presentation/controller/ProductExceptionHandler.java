package com.example.demo.catalog.presentation.controller;

import com.example.demo.catalog.exception.ProductNotFoundException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Product モジュールの例外ハンドラ。 */
@Slf4j
@RestControllerAdvice(basePackages = "com.example.demo.catalog")
public class ProductExceptionHandler {

  /** ProductNotFoundException を処理する。 */
  @ExceptionHandler(ProductNotFoundException.class)
  /* default */ ProblemDetail handleNotFound(final ProductNotFoundException ex) {
    log.warn("ProductNotFoundException: {}", ex.getMessage());
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Not Found");
    problem.setDetail(ex.getMessage());
    problem.setType(URI.create("about:blank"));
    return problem;
  }
}
