package com.example.demo;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * アプリケーション共通の例外ハンドラ。
 *
 * <p>{@link ResponseEntityExceptionHandler} を継承しているため、Spring MVC の標準例外 （{@code
 * MethodArgumentNotValidException}、{@code HttpRequestMethodNotSupportedException} 等）は 親クラスが自動的に適切な
 * HTTP ステータスの {@code ProblemDetail} に変換する。
 *
 * <p>本クラスでは、親クラスがカバーしない汎用ビジネス例外（{@code IllegalStateException} → 409、 {@code
 * IllegalArgumentException} → 400）を追加でハンドリングする。
 *
 * <p>例外ハンドラの解決順序:
 *
 * <ol>
 *   <li>モジュール固有ハンドラ（{@code @RestControllerAdvice(basePackages = "...")} でスコープ限定）
 *   <li>本クラス（グローバル、スコープ未指定）
 *   <li>{@code ResponseEntityExceptionHandler} の親メソッド群
 * </ol>
 *
 * <p>モジュール固有の業務例外（{@code XxxNotFoundException} 等）は各モジュールの {@code presentation/controller/}
 * 配下にハンドラを作成し、そちらで処理すること。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /** 不正な状態遷移（例: 削除済みエンティティの更新）。 */
  @ExceptionHandler(IllegalStateException.class)
  /* default */ ProblemDetail handleIllegalState(final IllegalStateException ex) {
    log.warn("Illegal state: {}", ex.getMessage());
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problem.setTitle("Conflict");
    problem.setDetail(ex.getMessage());
    problem.setType(URI.create("about:blank"));
    return problem;
  }

  /** 楽観ロックの競合。 */
  @ExceptionHandler(OptimisticLockException.class)
  /* default */ ProblemDetail handleOptimisticLock(final OptimisticLockException ex) {
    log.warn("Optimistic lock conflict: {}", ex.getMessage());
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problem.setTitle("Conflict");
    problem.setDetail(ex.getMessage());
    problem.setType(URI.create("about:blank"));
    return problem;
  }

  /** 不正な引数（バリデーション漏れ等）。 */
  @ExceptionHandler(IllegalArgumentException.class)
  /* default */ ProblemDetail handleIllegalArgument(final IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Bad Request");
    problem.setDetail(ex.getMessage());
    problem.setType(URI.create("about:blank"));
    return problem;
  }
}
