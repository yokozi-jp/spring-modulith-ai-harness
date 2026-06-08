package com.example.demo.category.presentation.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.category.application.query.dto.CategoryDetailDto;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CategoryDetailResponse}. */
class CategoryDetailResponseTest {

  /** テスト用カテゴリ ID。 */
  private static final String ID = "cat-001";

  /** テスト用カテゴリ名。 */
  private static final String NAME = "家電";

  /** from() が DTO のフィールドを正しく変換すること。 */
  @Test
  void shouldConvertFromDto() {
    final CategoryDetailDto.AncestorDto ancestor = new CategoryDetailDto.AncestorDto("root", "ルート");
    final CategoryDetailDto dto =
        new CategoryDetailDto(ID, NAME, 3, "parent-001", 2, List.of(ancestor));

    final CategoryDetailResponse result = CategoryDetailResponse.from(dto);

    assertEquals(ID, result.id(), "id should match");
    assertEquals(NAME, result.name(), "name should match");
    assertEquals(3, result.sortOrder(), "sortOrder should match");
    assertEquals("parent-001", result.parentCategoryId(), "parentCategoryId should match");
    assertEquals(2, result.version(), "version should match");
    assertEquals(1, result.ancestors().size(), "ancestors size should be 1");
    assertEquals("root", result.ancestors().getFirst().id(), "ancestor id should match");
    assertEquals("ルート", result.ancestors().getFirst().name(), "ancestor name should match");
  }
}
