package com.example.demo.category.infrastructure.db.query;

import static com.example.demo.jooq.tables.Categories.CATEGORIES;

import com.example.demo.category.CategoryApi;
import com.example.demo.jooq.SoftDeleteCondition;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

/** {@link CategoryApi} の jOOQ 実装。 */
@RequiredArgsConstructor
@Component
public class CategoryApiImpl implements CategoryApi {

  /** jOOQ DSL コンテキスト。 */
  private final DSLContext dsl;

  @Override
  public boolean existsById(final String categoryId) {
    final UUID uuid = UUID.fromString(categoryId);
    return dsl.fetchExists(
        dsl.selectOne()
            .from(CATEGORIES)
            .where(CATEGORIES.ID.eq(uuid))
            .and(SoftDeleteCondition.notDeleted(CATEGORIES)));
  }
}
