package com.example.demo.catalog.application.command.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.example.demo.catalog.application.command.command.CreateProductCommand;
import com.example.demo.catalog.application.command.command.DeleteProductCommand;
import com.example.demo.catalog.application.command.command.PublishProductCommand;
import com.example.demo.catalog.application.command.dto.CreatedProductDto;
import com.example.demo.catalog.domain.model.aggregate.Product;
import com.example.demo.catalog.domain.model.valueobject.Sku;
import com.example.demo.catalog.domain.model.valueobject.identifier.ProductId;
import com.example.demo.catalog.domain.repository.ProductRepository;
import com.example.demo.catalog.domain.service.ProductFactory;
import com.example.demo.catalog.event.ProductDeletedEvent;
import com.example.demo.catalog.exception.ProductNotFoundException;
import com.example.demo.category.CategoryApi;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/** Unit tests for {@link ProductCommandHandler}. */
@ExtendWith(MockitoExtension.class)
class ProductCommandHandlerTest {

  /** テスト用商品 ID。 */
  private static final String PRODUCT_ID = "prod-1";

  /** テスト用オペレータ ID。 */
  private static final String OPERATOR_ID = "operator-1";

  /** ファクトリモック。 */
  @Mock private ProductFactory factory;

  /** リポジトリモック。 */
  @Mock private ProductRepository repository;

  /** カテゴリ API モック。 */
  @Mock private CategoryApi categoryApi;

  /** イベント発行モック。 */
  @Mock private ApplicationEventPublisher eventPublisher;

  /** テスト対象。 */
  @InjectMocks private ProductCommandHandler sut;

  /** 商品作成で ID を返すこと。 */
  @Test
  void shouldReturnCreatedIdOnCreate() {
    final Product product =
        new Product(
            new ProductId(PRODUCT_ID),
            "テスト",
            "説明",
            "cat-1",
            new Sku("SKU-0000000000000000000000000001"));
    Mockito.when(factory.create(any(), any(), any(), any())).thenReturn(product);

    final CreatedProductDto result =
        sut.handle(
            new CreateProductCommand(
                "テスト", "説明", "cat-1", "SKU-0000000000000000000000000001", OPERATOR_ID));

    assertEquals(PRODUCT_ID, result.id(), "id should match");
  }

  /** 存在しない商品の公開で例外を投げること。 */
  @Test
  void shouldThrowWhenProductNotFoundOnPublish() {
    final ProductId id = new ProductId(PRODUCT_ID);
    Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        ProductNotFoundException.class,
        () -> sut.handle(new PublishProductCommand(PRODUCT_ID, 1, OPERATOR_ID)),
        "should throw when product not found");
  }

  /** 削除時に ProductDeletedEvent を発行すること。 */
  @Test
  void shouldPublishEventOnDelete() {
    final ProductId id = new ProductId(PRODUCT_ID);
    final Product product =
        new Product(id, "テスト", "説明", "cat-1", new Sku("SKU-0000000000000000000000000001"));
    Mockito.when(repository.findById(id)).thenReturn(Optional.of(product));
    Mockito.when(repository.getVersion(id)).thenReturn(1);

    sut.handle(new DeleteProductCommand(PRODUCT_ID, 1, OPERATOR_ID));

    verify(repository).delete(id, 1, OPERATOR_ID);
    verify(eventPublisher).publishEvent(new ProductDeletedEvent(PRODUCT_ID));
  }
}
