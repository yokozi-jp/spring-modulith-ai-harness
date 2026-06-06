package com.example.demo.category.infrastructure.db.repository;

import static com.example.demo.jooq.tables.Categories.CATEGORIES;
import static com.example.demo.jooq.tables.CategoryClosures.CATEGORY_CLOSURES;

import com.example.demo.category.domain.model.aggregate.Category;
import com.example.demo.category.domain.model.valueobject.identifier.CategoryId;
import com.example.demo.category.domain.repository.CategoryRepository;
import com.example.demo.jooq.SoftDeleteCondition;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Repository;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jspecify.annotations.Nullable;

/** Category リポジトリ実装。 */
@RequiredArgsConstructor
@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

  /** jOOQ DSL コンテキスト。 */
  private final DSLContext dsl;

  /** 時計。 */
  private final Clock clock;

  @Override
  public CategoryId generateId() {
    return new CategoryId(UUID.randomUUID().toString());
  }

  @Override
  public void save(final Category category, final int version, final String operatorId) {
    final UUID id = UUID.fromString(category.getId().value());
    @Nullable final UUID parentId =
        category.getParentCategoryId() != null
            ? UUID.fromString(category.getParentCategoryId().value())
            : null;
    final OffsetDateTime now = OffsetDateTime.now(clock);

    if (version == 0) {
      dsl.insertInto(CATEGORIES)
          .set(CATEGORIES.ID, id)
          .set(CATEGORIES.NAME, category.getName())
          .set(CATEGORIES.SORT_ORDER, category.getSortOrder())
          .set(CATEGORIES.PARENT_CATEGORY_ID, DSL.val(parentId, CATEGORIES.PARENT_CATEGORY_ID))
          .set(CATEGORIES.CREATED_AT, now)
          .set(CATEGORIES.UPDATED_AT, now)
          .set(CATEGORIES.CREATED_BY, operatorId)
          .set(CATEGORIES.UPDATED_BY, operatorId)
          .set(CATEGORIES.VERSION, 1)
          .execute();
      insertClosureEntries(id, parentId, operatorId, now);
    } else {
      dsl.update(CATEGORIES)
          .set(CATEGORIES.NAME, category.getName())
          .set(CATEGORIES.SORT_ORDER, category.getSortOrder())
          .set(CATEGORIES.UPDATED_AT, now)
          .set(CATEGORIES.UPDATED_BY, operatorId)
          .set(CATEGORIES.VERSION, version + 1)
          .where(CATEGORIES.ID.eq(id))
          .and(CATEGORIES.VERSION.eq(version))
          .and(SoftDeleteCondition.notDeleted(CATEGORIES))
          .execute();
    }
  }

  @Override
  public Optional<Category> findById(final CategoryId id) {
    final UUID uuid = UUID.fromString(id.value());
    return dsl.selectFrom(CATEGORIES)
        .where(CATEGORIES.ID.eq(uuid))
        .and(SoftDeleteCondition.notDeleted(CATEGORIES))
        .fetchOptional()
        .map(
            r -> {
              final CategoryId parentCatId =
                  r.getParentCategoryId() != null
                      ? new CategoryId(r.getParentCategoryId().toString())
                      : null;
              return Category.reconstitute(
                  new CategoryId(r.getId().toString()), r.getName(), r.getSortOrder(), parentCatId);
            });
  }

  @Override
  public int getVersion(final CategoryId id) {
    final UUID uuid = UUID.fromString(id.value());
    return dsl.select(CATEGORIES.VERSION)
        .from(CATEGORIES)
        .where(CATEGORIES.ID.eq(uuid))
        .and(SoftDeleteCondition.notDeleted(CATEGORIES))
        .fetchOptional(CATEGORIES.VERSION)
        .orElse(0);
  }

  @Override
  public void delete(final CategoryId id, final int version, final String operatorId) {
    final UUID uuid = UUID.fromString(id.value());
    final OffsetDateTime now = OffsetDateTime.now(clock);
    dsl.update(CATEGORIES)
        .set(CATEGORIES.DELETED_AT, now)
        .set(CATEGORIES.UPDATED_AT, now)
        .set(CATEGORIES.UPDATED_BY, operatorId)
        .set(CATEGORIES.VERSION, version + 1)
        .where(CATEGORIES.ID.eq(uuid))
        .and(CATEGORIES.VERSION.eq(version))
        .and(SoftDeleteCondition.notDeleted(CATEGORIES))
        .execute();
  }

  @Override
  public boolean existsChildCategories(final CategoryId id) {
    final UUID uuid = UUID.fromString(id.value());
    return dsl.fetchExists(
        dsl.selectOne()
            .from(CATEGORY_CLOSURES)
            .where(CATEGORY_CLOSURES.ANCESTOR_ID.eq(uuid))
            .and(CATEGORY_CLOSURES.DEPTH.eq(1))
            .and(SoftDeleteCondition.notDeleted(CATEGORY_CLOSURES)));
  }

  @Override
  public void move(final Category category, final int version, final String operatorId) {
    final UUID id = UUID.fromString(category.getId().value());
    @Nullable final UUID newParentId =
        category.getParentCategoryId() != null
            ? UUID.fromString(category.getParentCategoryId().value())
            : null;
    final OffsetDateTime now = OffsetDateTime.now(clock);

    // 1. Update parent_category_id in categories table
    dsl.update(CATEGORIES)
        .set(CATEGORIES.PARENT_CATEGORY_ID, DSL.val(newParentId, CATEGORIES.PARENT_CATEGORY_ID))
        .set(CATEGORIES.UPDATED_AT, now)
        .set(CATEGORIES.UPDATED_BY, operatorId)
        .set(CATEGORIES.VERSION, version + 1)
        .where(CATEGORIES.ID.eq(id))
        .and(CATEGORIES.VERSION.eq(version))
        .and(SoftDeleteCondition.notDeleted(CATEGORIES))
        .execute();

    // 2. Delete old closure entries (ancestors of id × descendants of id)
    //    but keep self-referencing rows of descendants
    dsl.deleteFrom(CATEGORY_CLOSURES)
        .where(
            CATEGORY_CLOSURES.DESCENDANT_ID.in(
                dsl.select(CATEGORY_CLOSURES.DESCENDANT_ID)
                    .from(CATEGORY_CLOSURES)
                    .where(CATEGORY_CLOSURES.ANCESTOR_ID.eq(id))))
        .and(
            CATEGORY_CLOSURES.ANCESTOR_ID.notIn(
                dsl.select(CATEGORY_CLOSURES.DESCENDANT_ID)
                    .from(CATEGORY_CLOSURES)
                    .where(CATEGORY_CLOSURES.ANCESTOR_ID.eq(id))))
        .execute();

    // 3. Insert new closure entries: new ancestors × subtree descendants
    if (newParentId != null) {
      dsl.execute(
          """
          INSERT INTO category_closures (ancestor_id, descendant_id, depth, created_at, updated_at, created_by, updated_by, version)
          SELECT a.ancestor_id, d.descendant_id, a.depth + d.depth + 1, {0}, {0}, {1}, {1}, 0
          FROM category_closures a
          CROSS JOIN category_closures d
          WHERE a.descendant_id = {2}
            AND d.ancestor_id = {3}
          """,
          now, operatorId, newParentId, id);
    }
  }

  @Override
  public boolean isDescendant(final CategoryId ancestorId, final CategoryId descendantId) {
    final UUID ancestor = UUID.fromString(ancestorId.value());
    final UUID descendant = UUID.fromString(descendantId.value());
    return dsl.fetchExists(
        dsl.selectOne()
            .from(CATEGORY_CLOSURES)
            .where(CATEGORY_CLOSURES.ANCESTOR_ID.eq(ancestor))
            .and(CATEGORY_CLOSURES.DESCENDANT_ID.eq(descendant))
            .and(CATEGORY_CLOSURES.DEPTH.gt(0)));
  }

  private void insertClosureEntries(
      final UUID id,
      @Nullable final UUID parentId,
      final String operatorId,
      final OffsetDateTime now) {
    // Self-reference (depth=0)
    dsl.insertInto(CATEGORY_CLOSURES)
        .set(CATEGORY_CLOSURES.ANCESTOR_ID, id)
        .set(CATEGORY_CLOSURES.DESCENDANT_ID, id)
        .set(CATEGORY_CLOSURES.DEPTH, 0)
        .set(CATEGORY_CLOSURES.CREATED_AT, now)
        .set(CATEGORY_CLOSURES.UPDATED_AT, now)
        .set(CATEGORY_CLOSURES.CREATED_BY, operatorId)
        .set(CATEGORY_CLOSURES.UPDATED_BY, operatorId)
        .set(CATEGORY_CLOSURES.VERSION, 0)
        .execute();

    // Ancestor entries (copy parent's ancestors + add parent→child)
    if (parentId != null) {
      dsl.execute(
          """
          INSERT INTO category_closures (ancestor_id, descendant_id, depth, created_at, updated_at, created_by, updated_by, version)
          SELECT ancestor_id, {0}, depth + 1, {1}, {1}, {2}, {2}, 0
          FROM category_closures
          WHERE descendant_id = {3}
          """,
          id, now, operatorId, parentId);
    }
  }
}
