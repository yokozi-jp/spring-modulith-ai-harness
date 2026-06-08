package com.example.demo.category.presentation.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.category.application.query.dto.CategorySummaryDto;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CategorySummaryResponse}. */
class CategorySummaryResponseTest {

  /** from() が DTO のフィールドを正しく変換すること。 */
  @Test
  void shouldConvertFromDto() {
    final CategorySummaryDto dto = new CategorySummaryDto("cat-001", "家電", 5);

    final CategorySummaryResponse result = CategorySummaryResponse.from(dto);

    assertEquals("cat-001", result.id(), "id should match");
    assertEquals("家電", result.name(), "name should match");
    assertEquals(5, result.sortOrder(), "sortOrder should match");
  }
}
