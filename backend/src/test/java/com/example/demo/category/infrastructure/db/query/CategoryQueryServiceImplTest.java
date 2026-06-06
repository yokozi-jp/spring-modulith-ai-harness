package com.example.demo.category.infrastructure.db.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.category.application.query.dto.CategoryDetailDto;
import com.example.demo.category.application.query.dto.CategorySummaryDto;
import com.example.demo.category.application.query.param.CategoryListParam;
import com.example.demo.jooq.tables.Categories;
import com.example.demo.jooq.tables.CategoryClosures;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Integration tests for {@link CategoryQueryServiceImpl}. */
@Tag("integration")
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class CategoryQueryServiceImplTest {

  /** テスト対象クエリサービス。 */
  private final CategoryQueryServiceImpl sut;

  /** テストデータ投入用 DSL コンテキスト。 */
  private final DSLContext dsl;

  /** テスト用操作者 ID。 */
  private static final String OPERATOR_ID = "test-operator";

  /** ルートカテゴリ一覧を取得できること。 */
  @Test
  void shouldReturnRootCategories() {
    final UUID rootId1 = UUID.randomUUID();
    final UUID rootId2 = UUID.randomUUID();
    final UUID childId = UUID.randomUUID();
    final OffsetDateTime now = OffsetDateTime.now(java.time.ZoneOffset.UTC);

    insertCategory(rootId1, "ルート1", 1, null, now);
    insertCategory(rootId2, "ルート2", 2, null, now);
    insertCategory(childId, "子", 1, rootId1, now);

    final Page<CategorySummaryDto> result =
        sut.findAll(new CategoryListParam(), PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements(), "should return only root categories");
    assertEquals("ルート1", result.getContent().get(0).name(), "first root should be sorted");
  }

  /** ID で詳細を取得し祖先情報が含まれること。 */
  @Test
  void shouldFindByIdWithAncestors() {
    final UUID grandparentId = UUID.randomUUID();
    final UUID parentId = UUID.randomUUID();
    final UUID childId = UUID.randomUUID();
    final OffsetDateTime now = OffsetDateTime.now(java.time.ZoneOffset.UTC);

    insertCategory(grandparentId, "祖父", 1, null, now);
    insertCategory(parentId, "親", 1, grandparentId, now);
    insertCategory(childId, "子", 1, parentId, now);

    insertClosure(grandparentId, grandparentId, 0, now);
    insertClosure(parentId, parentId, 0, now);
    insertClosure(grandparentId, parentId, 1, now);
    insertClosure(childId, childId, 0, now);
    insertClosure(parentId, childId, 1, now);
    insertClosure(grandparentId, childId, 2, now);

    final Optional<CategoryDetailDto> result = sut.findById(childId.toString());

    assertTrue(result.isPresent(), "child category should be found");
    final CategoryDetailDto detail = result.get();
    assertEquals("子", detail.name(), "name should match");
    assertNotNull(detail.ancestors(), "ancestors should not be null");
    assertEquals(2, detail.ancestors().size(), "should have 2 ancestors");
    assertEquals("祖父", detail.ancestors().get(0).name(), "first ancestor should be grandparent");
    assertEquals("親", detail.ancestors().get(1).name(), "second ancestor should be parent");
  }

  /** 子カテゴリ一覧を取得できること。 */
  @Test
  void shouldReturnChildren() {
    final UUID parentId = UUID.randomUUID();
    final UUID child1Id = UUID.randomUUID();
    final UUID child2Id = UUID.randomUUID();
    final OffsetDateTime now = OffsetDateTime.now(java.time.ZoneOffset.UTC);

    insertCategory(parentId, "親", 1, null, now);
    insertCategory(child1Id, "子1", 2, parentId, now);
    insertCategory(child2Id, "子2", 1, parentId, now);

    insertClosure(parentId, parentId, 0, now);
    insertClosure(child1Id, child1Id, 0, now);
    insertClosure(parentId, child1Id, 1, now);
    insertClosure(child2Id, child2Id, 0, now);
    insertClosure(parentId, child2Id, 1, now);

    final List<CategorySummaryDto> result = sut.findChildrenById(parentId.toString());

    assertEquals(2, result.size(), "should return 2 children");
    assertEquals("子2", result.get(0).name(), "first child should be sorted by sortOrder");
    assertEquals("子1", result.get(1).name(), "second child should be sorted by sortOrder");
  }

  private void insertCategory(
      final UUID id,
      final String name,
      final int sortOrder,
      final UUID parentId,
      final OffsetDateTime now) {
    dsl.insertInto(Categories.CATEGORIES)
        .set(Categories.CATEGORIES.ID, id)
        .set(Categories.CATEGORIES.NAME, name)
        .set(Categories.CATEGORIES.SORT_ORDER, sortOrder)
        .set(Categories.CATEGORIES.PARENT_CATEGORY_ID, parentId)
        .set(Categories.CATEGORIES.CREATED_AT, now)
        .set(Categories.CATEGORIES.UPDATED_AT, now)
        .set(Categories.CATEGORIES.CREATED_BY, OPERATOR_ID)
        .set(Categories.CATEGORIES.UPDATED_BY, OPERATOR_ID)
        .set(Categories.CATEGORIES.VERSION, 1)
        .execute();
  }

  private void insertClosure(
      final UUID ancestorId, final UUID descendantId, final int depth, final OffsetDateTime now) {
    dsl.insertInto(CategoryClosures.CATEGORY_CLOSURES)
        .set(CategoryClosures.CATEGORY_CLOSURES.ANCESTOR_ID, ancestorId)
        .set(CategoryClosures.CATEGORY_CLOSURES.DESCENDANT_ID, descendantId)
        .set(CategoryClosures.CATEGORY_CLOSURES.DEPTH, depth)
        .set(CategoryClosures.CATEGORY_CLOSURES.CREATED_AT, now)
        .set(CategoryClosures.CATEGORY_CLOSURES.UPDATED_AT, now)
        .set(CategoryClosures.CATEGORY_CLOSURES.CREATED_BY, OPERATOR_ID)
        .set(CategoryClosures.CATEGORY_CLOSURES.UPDATED_BY, OPERATOR_ID)
        .set(CategoryClosures.CATEGORY_CLOSURES.VERSION, 0)
        .execute();
  }
}
