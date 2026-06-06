package com.example.demo.category.infrastructure.db.query;

import static com.example.demo.jooq.tables.Categories.CATEGORIES;
import static com.example.demo.jooq.tables.CategoryClosures.CATEGORY_CLOSURES;

import com.example.demo.category.application.query.dto.CategoryDetailDto;
import com.example.demo.category.application.query.dto.CategorySummaryDto;
import com.example.demo.category.application.query.param.CategoryListParam;
import com.example.demo.category.application.query.service.CategoryQueryService;
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

/** Category クエリサービス実装。 */
@RequiredArgsConstructor
@Component
public class CategoryQueryServiceImpl implements CategoryQueryService {

  /** jOOQ DSL コンテキスト。 */
  private final DSLContext dsl;

  @Override
  public Page<CategorySummaryDto> findAll(final CategoryListParam param, final Pageable pageable) {
    final Integer totalCount =
        dsl.selectCount()
            .from(CATEGORIES)
            .where(CATEGORIES.PARENT_CATEGORY_ID.isNull())
            .and(SoftDeleteCondition.notDeleted(CATEGORIES))
            .fetchOne(0, Integer.class);
    final int total = totalCount != null ? totalCount : 0;

    final List<CategorySummaryDto> content =
        dsl.select(CATEGORIES.ID, CATEGORIES.NAME, CATEGORIES.SORT_ORDER)
            .from(CATEGORIES)
            .where(CATEGORIES.PARENT_CATEGORY_ID.isNull())
            .and(SoftDeleteCondition.notDeleted(CATEGORIES))
            .orderBy(CATEGORIES.SORT_ORDER.asc(), CATEGORIES.NAME.asc())
            .limit(pageable.getPageSize())
            .offset((int) pageable.getOffset())
            .fetch(
                r ->
                    new CategorySummaryDto(
                        r.get(CATEGORIES.ID).toString(),
                        r.get(CATEGORIES.NAME),
                        r.get(CATEGORIES.SORT_ORDER)));

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Optional<CategoryDetailDto> findById(final String id) {
    final UUID uuid = UUID.fromString(id);
    return dsl.selectFrom(CATEGORIES)
        .where(CATEGORIES.ID.eq(uuid))
        .and(SoftDeleteCondition.notDeleted(CATEGORIES))
        .fetchOptional()
        .map(
            r -> {
              final List<CategoryDetailDto.AncestorDto> ancestors = fetchAncestors(uuid);
              final String parentId =
                  r.getParentCategoryId() != null ? r.getParentCategoryId().toString() : null;
              return new CategoryDetailDto(
                  r.getId().toString(),
                  r.getName(),
                  r.getSortOrder(),
                  parentId,
                  r.getVersion(),
                  ancestors);
            });
  }

  @Override
  public List<CategorySummaryDto> findChildrenById(final String id) {
    final UUID uuid = UUID.fromString(id);
    return dsl.select(CATEGORIES.ID, CATEGORIES.NAME, CATEGORIES.SORT_ORDER)
        .from(CATEGORIES)
        .join(CATEGORY_CLOSURES)
        .on(CATEGORY_CLOSURES.DESCENDANT_ID.eq(CATEGORIES.ID))
        .where(CATEGORY_CLOSURES.ANCESTOR_ID.eq(uuid))
        .and(CATEGORY_CLOSURES.DEPTH.eq(1))
        .and(SoftDeleteCondition.notDeleted(CATEGORIES))
        .orderBy(CATEGORIES.SORT_ORDER.asc(), CATEGORIES.NAME.asc())
        .fetch(
            r ->
                new CategorySummaryDto(
                    r.get(CATEGORIES.ID).toString(),
                    r.get(CATEGORIES.NAME),
                    r.get(CATEGORIES.SORT_ORDER)));
  }

  private List<CategoryDetailDto.AncestorDto> fetchAncestors(final UUID categoryId) {
    return dsl.select(CATEGORIES.ID, CATEGORIES.NAME)
        .from(CATEGORIES)
        .join(CATEGORY_CLOSURES)
        .on(CATEGORY_CLOSURES.ANCESTOR_ID.eq(CATEGORIES.ID))
        .where(CATEGORY_CLOSURES.DESCENDANT_ID.eq(categoryId))
        .and(CATEGORY_CLOSURES.DEPTH.gt(0))
        .and(SoftDeleteCondition.notDeleted(CATEGORIES))
        .orderBy(CATEGORY_CLOSURES.DEPTH.desc())
        .fetch(
            r ->
                new CategoryDetailDto.AncestorDto(
                    r.get(CATEGORIES.ID).toString(), r.get(CATEGORIES.NAME)));
  }
}
