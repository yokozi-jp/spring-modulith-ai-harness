package com.example.demo.catalog.presentation.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.catalog.application.query.dto.ProductSummaryDto;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProductSummaryResponse}. */
class ProductSummaryResponseTest {

  /** テスト用商品 ID 値。 */
  private static final String PRODUCT_ID_VALUE = "product-123";

  /** from() が DTO の id フィールドを正しく変換すること。 */
  @Test
  void shouldConvertFromDto() {
    final ProductSummaryDto dto = new ProductSummaryDto(PRODUCT_ID_VALUE);
    final ProductSummaryResponse result = ProductSummaryResponse.from(dto);
    assertEquals(PRODUCT_ID_VALUE, result.id(), "id should match dto id");
  }
}
