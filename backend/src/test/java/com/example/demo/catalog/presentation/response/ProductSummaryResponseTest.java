package com.example.demo.catalog.presentation.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.catalog.application.query.dto.ProductSummaryDto;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProductSummaryResponse}. */
class ProductSummaryResponseTest {

  /** テスト用商品 ID 値。 */
  private static final String PRODUCT_ID_VALUE = "product-123";

  /** テスト用商品名。 */
  private static final String PRODUCT_NAME = "テスト商品";

  /** テスト用ステータス。 */
  private static final String PRODUCT_STATUS = "DRAFT";

  /** テスト用カテゴリ ID。 */
  private static final String CATEGORY_ID = "category-456";

  /** from() が DTO のフィールドを正しく変換すること。 */
  @Test
  void shouldConvertFromDto() {
    final ProductSummaryDto dto =
        new ProductSummaryDto(PRODUCT_ID_VALUE, PRODUCT_NAME, PRODUCT_STATUS, CATEGORY_ID);
    final ProductSummaryResponse result = ProductSummaryResponse.from(dto);
    assertEquals(PRODUCT_ID_VALUE, result.id(), "id should match dto id");
    assertEquals(PRODUCT_NAME, result.name(), "name should match dto name");
    assertEquals(PRODUCT_STATUS, result.status(), "status should match dto status");
    assertEquals(CATEGORY_ID, result.categoryId(), "categoryId should match dto categoryId");
  }
}
