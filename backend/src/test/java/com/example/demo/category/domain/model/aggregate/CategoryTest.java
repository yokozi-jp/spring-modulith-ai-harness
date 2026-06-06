package com.example.demo.category.domain.model.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.category.domain.model.valueobject.identifier.CategoryId;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Category} aggregate. */
class CategoryTest {

  /** テスト用カテゴリ ID。 */
  private static final String ID_VALUE = "cat-001";

  /** テスト用カテゴリ名。 */
  private static final String NAME = "家電";

  /** テスト用親カテゴリ ID。 */
  private static final String PARENT_ID_VALUE = "cat-parent";

  @Test
  void shouldCreateRootCategory() {
    final Category category = new Category(new CategoryId(ID_VALUE), NAME, 0, null);

    assertEquals(ID_VALUE, category.getId().value(), "id should match");
    assertEquals(NAME, category.getName(), "name should match");
    assertEquals(0, category.getSortOrder(), "sortOrder should be 0");
    assertNull(category.getParentCategoryId(), "root category should have null parent");
  }

  @Test
  void shouldCreateChildCategory() {
    final CategoryId parentId = new CategoryId(PARENT_ID_VALUE);
    final Category category = new Category(new CategoryId(ID_VALUE), NAME, 3, parentId);

    assertEquals(parentId, category.getParentCategoryId(), "parent should match");
    assertEquals(3, category.getSortOrder(), "sortOrder should be 3");
  }

  @Test
  void shouldThrowWhenNameIsBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Category(new CategoryId(ID_VALUE), "  ", 0, null),
        "should throw on blank name");
  }

  @Test
  void shouldThrowWhenNameIsNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Category(new CategoryId(ID_VALUE), null, 0, null),
        "should throw on null name");
  }

  @Test
  void shouldThrowWhenNameExceedsMaxLength() {
    final String longName = "a".repeat(Category.MAX_NAME_LENGTH + 1);
    assertThrows(
        IllegalArgumentException.class,
        () -> new Category(new CategoryId(ID_VALUE), longName, 0, null),
        "should throw on name exceeding max length");
  }

  @Test
  void shouldUpdateNameAndSortOrder() {
    final Category category = new Category(new CategoryId(ID_VALUE), NAME, 0, null);
    final Category updated = category.update("スマートフォン", 5);

    assertEquals("スマートフォン", updated.getName(), "name should be updated");
    assertEquals(5, updated.getSortOrder(), "sortOrder should be updated");
    assertEquals(ID_VALUE, updated.getId().value(), "id should remain the same");
  }

  @Test
  void shouldMoveToNewParent() {
    final Category category = new Category(new CategoryId(ID_VALUE), NAME, 0, null);
    final CategoryId newParent = new CategoryId("new-parent");
    final Category moved = category.move(newParent);

    assertEquals(newParent, moved.getParentCategoryId(), "parent should be updated");
    assertEquals(ID_VALUE, moved.getId().value(), "id should remain the same");
  }

  @Test
  void shouldMoveToRoot() {
    final CategoryId parentId = new CategoryId(PARENT_ID_VALUE);
    final Category category = new Category(new CategoryId(ID_VALUE), NAME, 0, parentId);
    final Category moved = category.move(null);

    assertNull(moved.getParentCategoryId(), "should be root after move");
  }

  @Test
  void shouldReconstitute() {
    final CategoryId parentId = new CategoryId(PARENT_ID_VALUE);
    final Category category = Category.reconstitute(new CategoryId(ID_VALUE), NAME, 2, parentId);

    assertEquals(ID_VALUE, category.getId().value(), "id should match");
    assertEquals(NAME, category.getName(), "name should match");
    assertEquals(2, category.getSortOrder(), "sortOrder should match");
    assertEquals(parentId, category.getParentCategoryId(), "parent should match");
  }
}
