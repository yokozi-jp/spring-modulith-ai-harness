package com.example.demo.category.infrastructure.db.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.category.domain.model.aggregate.Category;
import com.example.demo.category.domain.model.valueobject.identifier.CategoryId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Integration tests for {@link CategoryRepositoryImpl}. */
@Tag("integration")
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class CategoryRepositoryImplTest {

  /** テスト用操作者 ID。 */
  private static final String OPERATOR_ID = "test-operator";

  /** テスト対象リポジトリ。 */
  private final CategoryRepositoryImpl sut;

  /** ID を生成できること。 */
  @Test
  void shouldGenerateId() {
    final CategoryId id = sut.generateId();
    assertNotNull(id, "generated id should not be null");
    assertNotNull(id.value(), "generated id value should not be null");
  }

  /** 保存したカテゴリを ID で取得できること。 */
  @Test
  void shouldSaveAndFindById() {
    final CategoryId id = sut.generateId();
    final Category category = new Category(id, "テストカテゴリ", 1, null);
    sut.save(category, 0, OPERATOR_ID);

    final Optional<Category> found = sut.findById(id);

    assertTrue(found.isPresent(), "saved category should be found");
    assertEquals("テストカテゴリ", found.get().getName(), "name should match");
    assertEquals(1, found.get().getSortOrder(), "sortOrder should match");
  }

  /** 更新時にバージョンがインクリメントされること。 */
  @Test
  void shouldUpdateWithVersionIncrement() {
    final CategoryId id = sut.generateId();
    final Category category = new Category(id, "初期名", 1, null);
    sut.save(category, 0, OPERATOR_ID);

    final int versionAfterInsert = sut.getVersion(id);
    assertEquals(1, versionAfterInsert, "version after insert should be 1");

    final Category updated = category.update("更新名", 2);
    sut.save(updated, versionAfterInsert, OPERATOR_ID);

    final int versionAfterUpdate = sut.getVersion(id);
    assertEquals(2, versionAfterUpdate, "version after update should be 2");
  }

  /** 論理削除後に findById で取得できないこと。 */
  @Test
  void shouldSoftDelete() {
    final CategoryId id = sut.generateId();
    final Category category = new Category(id, "削除対象", 1, null);
    sut.save(category, 0, OPERATOR_ID);

    final int version = sut.getVersion(id);
    sut.delete(id, version, OPERATOR_ID);

    final Optional<Category> found = sut.findById(id);
    assertFalse(found.isPresent(), "deleted category should not be found");
  }

  /** 子カテゴリの存在を検出できること。 */
  @Test
  void shouldDetectChildCategories() {
    final CategoryId parentId = sut.generateId();
    final Category parent = new Category(parentId, "親", 1, null);
    sut.save(parent, 0, OPERATOR_ID);

    assertFalse(sut.existsChildCategories(parentId), "should have no children initially");

    final CategoryId childId = sut.generateId();
    final Category child = new Category(childId, "子", 1, parentId);
    sut.save(child, 0, OPERATOR_ID);

    assertTrue(sut.existsChildCategories(parentId), "should detect child category");
  }

  /** カテゴリをツリー内で移動できること。 */
  @Test
  void shouldMoveCategory() {
    final CategoryId parentAId = sut.generateId();
    final Category parentA = new Category(parentAId, "親A", 1, null);
    sut.save(parentA, 0, OPERATOR_ID);

    final CategoryId parentBId = sut.generateId();
    final Category parentB = new Category(parentBId, "親B", 2, null);
    sut.save(parentB, 0, OPERATOR_ID);

    final CategoryId childId = sut.generateId();
    final Category child = new Category(childId, "子", 1, parentAId);
    sut.save(child, 0, OPERATOR_ID);

    final int childVersion = sut.getVersion(childId);
    final Category moved = child.move(parentBId);
    sut.move(moved, childVersion, OPERATOR_ID);

    final Optional<Category> found = sut.findById(childId);
    assertTrue(found.isPresent(), "moved category should still exist");
    assertEquals(
        parentBId.value(),
        found.get().getParentCategoryId().value(),
        "parent should be parentB after move");
  }

  /** 子孫関係を検出できること。 */
  @Test
  void shouldDetectDescendant() {
    final CategoryId rootId = sut.generateId();
    final Category root = new Category(rootId, "ルート", 1, null);
    sut.save(root, 0, OPERATOR_ID);

    final CategoryId childId = sut.generateId();
    final Category child = new Category(childId, "子", 1, rootId);
    sut.save(child, 0, OPERATOR_ID);

    final CategoryId grandchildId = sut.generateId();
    final Category grandchild = new Category(grandchildId, "孫", 1, childId);
    sut.save(grandchild, 0, OPERATOR_ID);

    assertTrue(sut.isDescendant(rootId, childId), "child should be descendant of root");
    assertTrue(sut.isDescendant(rootId, grandchildId), "grandchild should be descendant of root");
    assertFalse(sut.isDescendant(childId, rootId), "root should not be descendant of child");
  }
}
