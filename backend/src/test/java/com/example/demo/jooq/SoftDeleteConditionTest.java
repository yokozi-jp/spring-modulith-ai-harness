package com.example.demo.jooq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SoftDeleteCondition}. */
class SoftDeleteConditionTest {

  /** {@code notDeleted} が {@code deleted_at IS NULL} 条件を返すこと。 */
  @Test
  void shouldReturnDeletedAtIsNullCondition() {
    final Condition condition = SoftDeleteCondition.notDeleted(DSL.table(DSL.name("orders")));

    final Condition expected = DSL.field(DSL.name("orders", "deleted_at")).isNull();
    assertEquals(expected, condition, "should produce deleted_at IS NULL condition");
  }

  /** スキーマ付きテーブルでも正しい条件を生成すること。 */
  @Test
  void shouldHandleSchemaQualifiedTable() {
    final Condition condition =
        SoftDeleteCondition.notDeleted(DSL.table(DSL.name("demo", "products")));

    final Condition expected = DSL.field(DSL.name("products", "deleted_at")).isNull();
    assertEquals(expected, condition, "should use table name without schema for column reference");
  }
}
