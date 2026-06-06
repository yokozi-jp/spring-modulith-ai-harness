package com.example.demo.catalog.application.command.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.example.demo.OptimisticLockException;
import com.example.demo.catalog.application.command.command.ArchiveProductCommand;
import com.example.demo.catalog.application.command.command.CreateProductCommand;
import com.example.demo.catalog.application.command.command.DeleteProductCommand;
import com.example.demo.catalog.application.command.command.PublishProductCommand;
import com.example.demo.catalog.application.command.command.UnpublishProductCommand;
import com.example.demo.catalog.application.command.command.UpdateProductCommand;
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
@SuppressWarnings("PMD.TooManyMethods")
@ExtendWith(MockitoExtension.class)
class ProductCommandHandlerTest {

  /** テスト用商品 ID。 */
  private static final String PRODUCT_ID = "prod-1";

  /** テスト用オペレータ ID。 */
  private static final String OPERATOR_ID = "op-1";

  /** テスト用 SKU。 */
  private static final String SKU_VALUE = "SKU-0000000000000000000000000001";

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

  @Test
  void shouldReturnCreatedIdOnCreate() {
    final Product product = createDraftProduct();
    Mockito.when(factory.create(any(), any(), any(), any())).thenReturn(product);

    final CreatedProductDto result =
        sut.handle(new CreateProductCommand("テスト", "説明", "cat-1", SKU_VALUE, OPERATOR_ID));

    assertEquals(PRODUCT_ID, result.id(), "id should match");
  }

  @Test
  void shouldUpdateProduct() {
    final Product product = createDraftProduct();
    Mockito.when(repository.findById(new ProductId(PRODUCT_ID))).thenReturn(Optional.of(product));
    Mockito.when(repository.getVersion(new ProductId(PRODUCT_ID))).thenReturn(1);
    Mockito.when(categoryApi.existsById("cat-2")).thenReturn(true);

    sut.handle(new UpdateProductCommand(PRODUCT_ID, "新名前", "新説明", "cat-2", 1, OPERATOR_ID));

    verify(repository).save(any(Product.class), Mockito.eq(1), Mockito.eq(OPERATOR_ID));
  }

  @Test
  void shouldThrowWhenCategoryNotFoundOnUpdate() {
    final Product product = createDraftProduct();
    Mockito.when(repository.findById(new ProductId(PRODUCT_ID))).thenReturn(Optional.of(product));
    Mockito.when(repository.getVersion(new ProductId(PRODUCT_ID))).thenReturn(1);
    Mockito.when(categoryApi.existsById("bad-cat")).thenReturn(false);

    assertThrows(
        IllegalArgumentException.class,
        () -> sut.handle(new UpdateProductCommand(PRODUCT_ID, "名前", "説明", "bad-cat", 1, OPERATOR_ID)),
        "should throw when category not found");
  }

  @Test
  void shouldPublishProduct() {
    final Product product = createDraftProduct();
    Mockito.when(repository.findById(new ProductId(PRODUCT_ID))).thenReturn(Optional.of(product));
    Mockito.when(repository.getVersion(new ProductId(PRODUCT_ID))).thenReturn(1);

    sut.handle(new PublishProductCommand(PRODUCT_ID, 1, OPERATOR_ID));

    verify(repository).save(any(Product.class), Mockito.eq(1), Mockito.eq(OPERATOR_ID));
  }

  @Test
  void shouldUnpublishProduct() {
    final Product published = createDraftProduct().publish();
    Mockito.when(repository.findById(new ProductId(PRODUCT_ID))).thenReturn(Optional.of(published));
    Mockito.when(repository.getVersion(new ProductId(PRODUCT_ID))).thenReturn(1);

    sut.handle(new UnpublishProductCommand(PRODUCT_ID, 1, OPERATOR_ID));

    verify(repository).save(any(Product.class), Mockito.eq(1), Mockito.eq(OPERATOR_ID));
  }

  @Test
  void shouldArchiveProduct() {
    final Product product = createDraftProduct();
    Mockito.when(repository.findById(new ProductId(PRODUCT_ID))).thenReturn(Optional.of(product));
    Mockito.when(repository.getVersion(new ProductId(PRODUCT_ID))).thenReturn(1);

    sut.handle(new ArchiveProductCommand(PRODUCT_ID, 1, OPERATOR_ID));

    verify(repository).save(any(Product.class), Mockito.eq(1), Mockito.eq(OPERATOR_ID));
  }

  @Test
  void shouldDeleteAndPublishEvent() {
    final Product product = createDraftProduct();
    Mockito.when(repository.findById(new ProductId(PRODUCT_ID))).thenReturn(Optional.of(product));
    Mockito.when(repository.getVersion(new ProductId(PRODUCT_ID))).thenReturn(1);

    sut.handle(new DeleteProductCommand(PRODUCT_ID, 1, OPERATOR_ID));

    verify(repository).delete(new ProductId(PRODUCT_ID), 1, OPERATOR_ID);
    verify(eventPublisher).publishEvent(new ProductDeletedEvent(PRODUCT_ID));
  }

  @Test
  void shouldThrowWhenProductNotFound() {
    Mockito.when(repository.findById(new ProductId(PRODUCT_ID))).thenReturn(Optional.empty());

    assertThrows(
        ProductNotFoundException.class,
        () -> sut.handle(new PublishProductCommand(PRODUCT_ID, 1, OPERATOR_ID)),
        "should throw when not found");
  }

  @Test
  void shouldThrowOnVersionMismatch() {
    final Product product = createDraftProduct();
    Mockito.when(repository.findById(new ProductId(PRODUCT_ID))).thenReturn(Optional.of(product));
    Mockito.when(repository.getVersion(new ProductId(PRODUCT_ID))).thenReturn(2);

    assertThrows(
        OptimisticLockException.class,
        () -> sut.handle(new PublishProductCommand(PRODUCT_ID, 1, OPERATOR_ID)),
        "should throw on version mismatch");
  }

  private Product createDraftProduct() {
    return new Product(
        new ProductId(PRODUCT_ID), "テスト商品", "テスト説明", "cat-1", new Sku(SKU_VALUE));
  }
}
