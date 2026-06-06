package com.example.demo.catalog.infrastructure.db.query;

import static com.example.demo.jooq.tables.Products.PRODUCTS;

import com.example.demo.catalog.application.query.dto.ProductDetailDto;
import com.example.demo.catalog.application.query.dto.ProductSummaryDto;
import com.example.demo.catalog.application.query.param.ProductListParam;
import com.example.demo.catalog.application.query.service.ProductQueryService;
import com.example.demo.jooq.SoftDeleteCondition;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/** Product クエリサービス実装。 */
@RequiredArgsConstructor
@Component
public class ProductQueryServiceImpl implements ProductQueryService {

  /** jOOQ DSL コンテキスト。 */
  private final DSLContext dsl;

  @Override
  public Page<ProductSummaryDto> findAll(final ProductListParam param, final Pageable pageable) {
    final Integer totalCount =
        dsl.selectCount()
            .from(PRODUCTS)
            .where(SoftDeleteCondition.notDeleted(PRODUCTS))
            .fetchOne(0, Integer.class);
    final int total = totalCount != null ? totalCount : 0;

    final List<ProductSummaryDto> content =
        dsl.select(PRODUCTS.ID)
            .from(PRODUCTS)
            .where(SoftDeleteCondition.notDeleted(PRODUCTS))
            .orderBy(PRODUCTS.CREATED_AT.desc())
            .limit(pageable.getPageSize())
            .offset((int) pageable.getOffset())
            .fetch(r -> new ProductSummaryDto(r.get(PRODUCTS.ID).toString()));

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Optional<ProductDetailDto> findById(final String id) {
    final UUID uuid = UUID.fromString(id);
    return dsl.selectFrom(PRODUCTS)
        .where(PRODUCTS.ID.eq(uuid))
        .and(SoftDeleteCondition.notDeleted(PRODUCTS))
        .fetchOptional()
        .map(r -> new ProductDetailDto(r.getId().toString()));
  }
}
