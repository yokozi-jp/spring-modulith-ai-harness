package com.example.demo.pricing.presentation.controller;

import com.example.demo.pricing.exception.PricingNotFoundException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Pricing モジュールの例外ハンドラ。 */
@Slf4j
@RestControllerAdvice(basePackages = "com.example.demo.pricing")
public class PricingExceptionHandler {

  /** PricingNotFoundException を処理する。 */
  @ExceptionHandler(PricingNotFoundException.class)
  /* default */ ProblemDetail handleNotFound(final PricingNotFoundException ex) {
    log.warn("PricingNotFoundException: {}", ex.getMessage());
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Not Found");
    problem.setDetail(ex.getMessage());
    problem.setType(URI.create("about:blank"));
    return problem;
  }
}
