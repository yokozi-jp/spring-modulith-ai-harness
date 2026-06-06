package com.example.demo.catalog.presentation.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.catalog.application.query.dto.ProductDetailDto;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProductDetailResponse}. */
class ProductDetailResponseTest {

  /** テスト用商品 ID 値。 */
  private static final String PRODUCT_ID_VALUE = "product-456";

  /** from() が DTO の id フィールドを正しく変換すること。 */
  @Test
  void shouldConvertFromDto() {
    final ProductDetailDto dto = new ProductDetailDto(PRODUCT_ID_VALUE);
    final ProductDetailResponse result = ProductDetailResponse.from(dto);
    assertEquals(PRODUCT_ID_VALUE, result.id(), "id should match dto id");
  }
}
