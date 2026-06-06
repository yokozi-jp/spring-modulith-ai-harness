package com.example.demo;

/**
 * 楽観ロックの競合を示す例外。
 *
 * <p>更新対象のエンティティが他のトランザクションによって変更済みの場合にスローされる。 {@link GlobalExceptionHandler} が {@code 409
 * Conflict} レスポンスに変換する。
 */
public class OptimisticLockException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * エンティティ種別と ID を含むメッセージで例外を生成する。
   *
   * @param entityType エンティティの種別名
   * @param entityId エンティティの識別子
   */
  public OptimisticLockException(final String entityType, final String entityId) {
    super(entityType + " with id " + entityId + " has been modified by another transaction");
  }
}
