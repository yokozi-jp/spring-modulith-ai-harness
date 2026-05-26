package com.example.demo.jooq;

import org.jooq.Condition;
import org.jooq.Table;
import org.jooq.impl.DSL;

/**
 * 論理削除フィルタの共通ヘルパー。
 *
 * <p>全テーブルに存在する {@code deleted_at} カラムを使い、有効レコードのみを取得する条件を生成する。
 * クエリ時は必ずこのヘルパーを使用すること。削除済みを含めて取得する場合はヘルパーを使わず、コメントで意図を明記する。
 */
public final class SoftDeleteCondition {

  /** 論理削除カラム名。 */
  private static final String DELETED_AT = "deleted_at";

  private SoftDeleteCondition() {}

  /**
   * 指定テーブルの有効レコード条件（{@code deleted_at IS NULL}）を返す。
   *
   * @param table 対象テーブル
   * @return {@code table.deleted_at IS NULL} に相当する条件
   */
  public static Condition notDeleted(final Table<?> table) {
    return DSL.field(DSL.name(table.getName(), DELETED_AT)).isNull();
  }
}
