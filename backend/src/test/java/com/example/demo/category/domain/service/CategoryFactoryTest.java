package com.example.demo.category.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.example.demo.category.domain.model.aggregate.Category;
import com.example.demo.category.domain.model.valueobject.identifier.CategoryId;
import com.example.demo.category.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link CategoryFactory}. */
@ExtendWith(MockitoExtension.class)
class CategoryFactoryTest {

  /** テスト用 ID 値。 */
  private static final String GENERATED_ID = "gen-cat-001";

  /** Mock リポジトリ。 */
  @Mock private CategoryRepository repository;

  /** テスト対象。 */
  @InjectMocks private CategoryFactory sut;

  @Test
  void shouldCreateRootCategory() {
    when(repository.generateId()).thenReturn(new CategoryId(GENERATED_ID));

    final Category result = sut.create("家電", 0, null);

    assertEquals(GENERATED_ID, result.getId().value(), "id should match generated id");
    assertEquals("家電", result.getName(), "name should match");
    assertEquals(0, result.getSortOrder(), "sortOrder should match");
    assertNull(result.getParentCategoryId(), "root should have null parent");
  }

  @Test
  void shouldCreateChildCategory() {
    when(repository.generateId()).thenReturn(new CategoryId(GENERATED_ID));
    final CategoryId parentId = new CategoryId("parent-001");

    final Category result = sut.create("スマートフォン", 2, parentId);

    assertEquals(GENERATED_ID, result.getId().value(), "id should match generated id");
    assertEquals("スマートフォン", result.getName(), "name should match");
    assertEquals(2, result.getSortOrder(), "sortOrder should match");
    assertEquals(parentId, result.getParentCategoryId(), "parent should match");
  }
}
