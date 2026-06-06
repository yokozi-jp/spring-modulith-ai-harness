package com.example.demo.catalog.infrastructure.db.repository;

import static com.example.demo.jooq.tables.Products.PRODUCTS;

import com.example.demo.OptimisticLockException;
import com.example.demo.catalog.domain.model.aggregate.Product;
import com.example.demo.catalog.domain.model.aggregate.ProductStatus;
import com.example.demo.catalog.domain.model.valueobject.Sku;
import com.example.demo.catalog.domain.model.valueobject.identifier.ProductId;
import com.example.demo.catalog.domain.repository.ProductRepository;
import com.example.demo.jooq.SoftDeleteCondition;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Repository;
import org.jooq.DSLContext;

/** Product リポジトリ実装。 */
@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepository {

  /** jOOQ DSL コンテキスト。 */
  private final DSLContext dsl;

  /** 時計。 */
  private final Clock clock;

  @Override
  public ProductId generateId() {
    return new ProductId(UUID.randomUUID().toString());
  }

  @Override
  public void save(final Product product, final int version, final String operatorId) {
    final UUID id = UUID.fromString(product.getId().value());
    final OffsetDateTime now = OffsetDateTime.now(clock);

    if (version == 0) {
      dsl.insertInto(PRODUCTS)
          .set(PRODUCTS.ID, id)
          .set(PRODUCTS.NAME, product.getName())
          .set(PRODUCTS.DESCRIPTION, product.getDescription())
          .set(PRODUCTS.CATEGORY_ID, UUID.fromString(product.getCategoryId()))
          .set(PRODUCTS.SKU, product.getSku().value())
          .set(PRODUCTS.STATUS, product.getStatus().name())
          .set(PRODUCTS.CREATED_AT, now)
          .set(PRODUCTS.UPDATED_AT, now)
          .set(PRODUCTS.CREATED_BY, operatorId)
          .set(PRODUCTS.UPDATED_BY, operatorId)
          .set(PRODUCTS.VERSION, 1)
          .execute();
    } else {
      final int affected =
          dsl.update(PRODUCTS)
              .set(PRODUCTS.NAME, product.getName())
              .set(PRODUCTS.DESCRIPTION, product.getDescription())
              .set(PRODUCTS.CATEGORY_ID, UUID.fromString(product.getCategoryId()))
              .set(PRODUCTS.SKU, product.getSku().value())
              .set(PRODUCTS.STATUS, product.getStatus().name())
              .set(PRODUCTS.UPDATED_AT, now)
              .set(PRODUCTS.UPDATED_BY, operatorId)
              .set(PRODUCTS.VERSION, version + 1)
              .where(PRODUCTS.ID.eq(id))
              .and(PRODUCTS.VERSION.eq(version))
              .and(SoftDeleteCondition.notDeleted(PRODUCTS))
              .execute();
      if (affected == 0) {
        throw new OptimisticLockException("Product", product.getId().value());
      }
    }
  }

  @Override
  public Optional<Product> findById(final ProductId id) {
    final UUID uuid = UUID.fromString(id.value());
    return dsl.selectFrom(PRODUCTS)
        .where(PRODUCTS.ID.eq(uuid))
        .and(SoftDeleteCondition.notDeleted(PRODUCTS))
        .fetchOptional()
        .map(
            r ->
                Product.reconstitute(
                    new ProductId(r.getId().toString()),
                    r.getName(),
                    r.getDescription(),
                    r.getCategoryId().toString(),
                    new Sku(r.getSku()),
                    ProductStatus.valueOf(r.getStatus())));
  }

  @Override
  public int getVersion(final ProductId id) {
    final UUID uuid = UUID.fromString(id.value());
    return dsl.select(PRODUCTS.VERSION)
        .from(PRODUCTS)
        .where(PRODUCTS.ID.eq(uuid))
        .and(SoftDeleteCondition.notDeleted(PRODUCTS))
        .fetchOptional(PRODUCTS.VERSION)
        .orElse(0);
  }

  @Override
  public void delete(final ProductId id, final int version, final String operatorId) {
    final UUID uuid = UUID.fromString(id.value());
    final OffsetDateTime now = OffsetDateTime.now(clock);
    final int affected =
        dsl.update(PRODUCTS)
            .set(PRODUCTS.DELETED_AT, now)
            .set(PRODUCTS.UPDATED_AT, now)
            .set(PRODUCTS.UPDATED_BY, operatorId)
            .set(PRODUCTS.VERSION, version + 1)
            .where(PRODUCTS.ID.eq(uuid))
            .and(PRODUCTS.VERSION.eq(version))
            .and(SoftDeleteCondition.notDeleted(PRODUCTS))
            .execute();
    if (affected == 0) {
      throw new OptimisticLockException("Product", id.value());
    }
  }
}
