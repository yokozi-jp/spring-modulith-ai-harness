package com.example.demo.catalog.infrastructure.db.query;

import static com.example.demo.jooq.tables.Products.PRODUCTS;

import com.example.demo.catalog.CatalogApi;
import com.example.demo.jooq.SoftDeleteCondition;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

/** {@link CatalogApi} の jOOQ 実装。 */
@RequiredArgsConstructor
@Component
public class CatalogApiImpl implements CatalogApi {

  /** jOOQ DSL コンテキスト。 */
  private final DSLContext dsl;

  @Override
  public boolean existsProductByCategoryId(final String categoryId) {
    final UUID uuid = UUID.fromString(categoryId);
    return dsl.fetchExists(
        dsl.selectOne()
            .from(PRODUCTS)
            .where(PRODUCTS.CATEGORY_ID.eq(uuid))
            .and(SoftDeleteCondition.notDeleted(PRODUCTS)));
  }

  @Override
  public boolean existsProductById(final String productId) {
    final UUID uuid = UUID.fromString(productId);
    return dsl.fetchExists(
        dsl.selectOne()
            .from(PRODUCTS)
            .where(PRODUCTS.ID.eq(uuid))
            .and(SoftDeleteCondition.notDeleted(PRODUCTS)));
  }
}
