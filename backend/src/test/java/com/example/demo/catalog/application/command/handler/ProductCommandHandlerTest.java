package com.example.demo.catalog.application.command.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.demo.catalog.application.command.command.CreateProductCommand;
import com.example.demo.catalog.application.command.dto.CreatedProductDto;
import com.example.demo.catalog.domain.model.aggregate.Product;
import com.example.demo.catalog.domain.model.valueobject.Sku;
import com.example.demo.catalog.domain.model.valueobject.identifier.ProductId;
import com.example.demo.catalog.domain.repository.ProductRepository;
import com.example.demo.catalog.domain.service.ProductFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link ProductCommandHandler}. */
@ExtendWith(MockitoExtension.class)
class ProductCommandHandlerTest {

  /** テスト用商品 ID 値。 */
  private static final String PRODUCT_ID_VALUE = "test-product-id";

  /** テスト用 SKU 値。 */
  private static final String SKU_VALUE = "SKU-0000000000000000000000000001";

  /** Mock ファクトリ。 */
  @Mock private ProductFactory factory;

  /** Mock リポジトリ。 */
  @Mock private ProductRepository repository;

  /** テスト対象。 */
  @InjectMocks private ProductCommandHandler sut;

  /** 商品作成コマンドを処理して作成結果 ID を返すこと。 */
  @Test
  void shouldReturnCreatedIdOnCreate() {
    final Product product =
        new Product(new ProductId(PRODUCT_ID_VALUE), "テスト商品", "テスト説明", "cat-1", new Sku(SKU_VALUE));
    Mockito.when(factory.create(eq("テスト商品"), eq("テスト説明"), eq("cat-1"), any(Sku.class)))
        .thenReturn(product);

    final CreateProductCommand command =
        new CreateProductCommand("テスト商品", "テスト説明", "cat-1", SKU_VALUE, "system");
    final CreatedProductDto result = sut.handle(command);

    assertEquals(PRODUCT_ID_VALUE, result.id(), "returned id should match created product id");
    verify(repository).save(product, 0, "system");
  }
}
