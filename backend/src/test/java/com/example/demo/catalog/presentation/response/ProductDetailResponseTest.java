package com.example.demo.catalog.presentation.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.catalog.application.query.dto.ProductDetailDto;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProductDetailResponse}. */
class ProductDetailResponseTest {

  /** テスト用商品 ID 値。 */
  private static final String PRODUCT_ID_VALUE = "product-456";

  /** テスト用商品名。 */
  private static final String PRODUCT_NAME = "テスト商品";

  /** テスト用商品説明。 */
  private static final String PRODUCT_DESCRIPTION = "商品の説明文";

  /** テスト用カテゴリ ID。 */
  private static final String CATEGORY_ID = "category-789";

  /** テスト用 SKU。 */
  private static final String PRODUCT_SKU = "SKU-001";

  /** テスト用ステータス。 */
  private static final String PRODUCT_STATUS = "DRAFT";

  /** テスト用バージョン。 */
  private static final int VERSION = 1;

  /** from() が DTO のフィールドを正しく変換すること。 */
  @Test
  void shouldConvertFromDto() {
    final ProductDetailDto dto =
        new ProductDetailDto(
            PRODUCT_ID_VALUE,
            PRODUCT_NAME,
            PRODUCT_DESCRIPTION,
            CATEGORY_ID,
            PRODUCT_SKU,
            PRODUCT_STATUS,
            VERSION);
    final ProductDetailResponse result = ProductDetailResponse.from(dto);
    assertEquals(PRODUCT_ID_VALUE, result.id(), "id should match dto id");
    assertEquals(PRODUCT_NAME, result.name(), "name should match dto name");
    assertEquals(PRODUCT_DESCRIPTION, result.description(), "description should match");
    assertEquals(CATEGORY_ID, result.categoryId(), "categoryId should match");
    assertEquals(PRODUCT_SKU, result.sku(), "sku should match");
    assertEquals(PRODUCT_STATUS, result.status(), "status should match");
    assertEquals(VERSION, result.version(), "version should match");
  }
}
