package com.example.demo;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** アプリケーション共通の例外ハンドラ。モジュール固有の例外は各モジュールのハンドラで処理する。 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /** 不正な状態遷移（例: 削除済みエンティティの更新）。 */
  @ExceptionHandler(IllegalStateException.class)
  /* default */ ProblemDetail handleIllegalState(final IllegalStateException ex) {
    if (log.isWarnEnabled()) {
      log.warn("Illegal state: {}", ex.getMessage());
    }
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problem.setTitle("Conflict");
    problem.setDetail(ex.getMessage());
    problem.setType(URI.create("about:blank"));
    return problem;
  }

  /** 不正な引数（バリデーション漏れ等）。 */
  @ExceptionHandler(IllegalArgumentException.class)
  /* default */ ProblemDetail handleIllegalArgument(final IllegalArgumentException ex) {
    if (log.isWarnEnabled()) {
      log.warn("Illegal argument: {}", ex.getMessage());
    }
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Bad Request");
    problem.setDetail(ex.getMessage());
    problem.setType(URI.create("about:blank"));
    return problem;
  }
}
