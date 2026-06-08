package com.example.demo.catalog.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.demo.catalog.domain.model.aggregate.Product;
import com.example.demo.catalog.domain.model.aggregate.ProductStatus;
import com.example.demo.catalog.domain.model.valueobject.Sku;
import com.example.demo.catalog.domain.model.valueobject.identifier.ProductId;
import com.example.demo.catalog.domain.repository.ProductRepository;
import com.example.demo.category.CategoryApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link ProductFactory}. */
@ExtendWith(MockitoExtension.class)
class ProductFactoryTest {

  /** テスト用生成 ID。 */
  private static final String GENERATED_ID = "gen-prod-001";

  /** テスト用カテゴリ ID。 */
  private static final String CATEGORY_ID = "cat-001";

  /** テスト用 SKU 値。 */
  private static final String SKU_VALUE = "SKU-1234567890123456789012345678";

  /** Mock リポジトリ。 */
  @Mock private ProductRepository repository;

  /** Mock カテゴリ API。 */
  @Mock private CategoryApi categoryApi;

  /** テスト対象。 */
  @InjectMocks private ProductFactory sut;

  @Test
  void shouldCreateProductWithGeneratedId() {
    when(repository.generateId()).thenReturn(new ProductId(GENERATED_ID));
    when(categoryApi.existsById(CATEGORY_ID)).thenReturn(true);

    final Product result = sut.create("テスト商品", "商品説明", CATEGORY_ID, new Sku(SKU_VALUE));

    assertEquals(GENERATED_ID, result.getId().value(), "id should match generated id");
    assertEquals("テスト商品", result.getName(), "name should match");
    assertEquals("商品説明", result.getDescription(), "description should match");
    assertEquals(CATEGORY_ID, result.getCategoryId(), "categoryId should match");
    assertEquals(SKU_VALUE, result.getSku().value(), "sku should match");
    assertEquals(ProductStatus.DRAFT, result.getStatus(), "status should be DRAFT");
  }

  @Test
  void shouldThrowWhenCategoryDoesNotExist() {
    when(categoryApi.existsById(CATEGORY_ID)).thenReturn(false);

    assertThrows(
        IllegalArgumentException.class,
        () -> sut.create("テスト商品", "商品説明", CATEGORY_ID, new Sku(SKU_VALUE)),
        "should throw when category does not exist");
  }
}
