package com.example.demo.catalog.application.command.handler;

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
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Product コマンドハンドラ。 */
@RequiredArgsConstructor
@Component
public class ProductCommandHandler {

  /** ファクトリ。 */
  private final ProductFactory factory;

  /** リポジトリ。 */
  private final ProductRepository repository;

  /** カテゴリ API。 */
  private final CategoryApi categoryApi;

  /** イベント発行。 */
  private final ApplicationEventPublisher eventPublisher;

  /** 商品を作成する。 */
  @Transactional
  @CommandHandler
  public CreatedProductDto handle(final CreateProductCommand command) {
    final Product product =
        factory.create(
            command.name(), command.description(), command.categoryId(), new Sku(command.sku()));
    repository.save(product, 0, command.operatorId());
    return new CreatedProductDto(product.getId().value());
  }

  /** 商品を更新する。 */
  @Transactional
  @CommandHandler
  public void handle(final UpdateProductCommand command) {
    final ProductId id = new ProductId(command.id());
    final Product product =
        repository.findById(id).orElseThrow(() -> new ProductNotFoundException(command.id()));
    verifyVersion(id, command.version());
    if (!categoryApi.existsById(command.categoryId())) {
      throw new IllegalArgumentException("Category not found: " + command.categoryId());
    }
    final Product updated =
        product.update(command.name(), command.description(), command.categoryId());
    repository.save(updated, command.version(), command.operatorId());
  }

  /** 商品を公開する。 */
  @Transactional
  @CommandHandler
  public void handle(final PublishProductCommand command) {
    final ProductId id = new ProductId(command.id());
    final Product product =
        repository.findById(id).orElseThrow(() -> new ProductNotFoundException(command.id()));
    verifyVersion(id, command.version());
    final Product published = product.publish();
    repository.save(published, command.version(), command.operatorId());
  }

  /** 商品を非公開にする。 */
  @Transactional
  @CommandHandler
  public void handle(final UnpublishProductCommand command) {
    final ProductId id = new ProductId(command.id());
    final Product product =
        repository.findById(id).orElseThrow(() -> new ProductNotFoundException(command.id()));
    verifyVersion(id, command.version());
    final Product unpublished = product.unpublish();
    repository.save(unpublished, command.version(), command.operatorId());
  }

  /** 商品をアーカイブする。 */
  @Transactional
  @CommandHandler
  public void handle(final ArchiveProductCommand command) {
    final ProductId id = new ProductId(command.id());
    final Product product =
        repository.findById(id).orElseThrow(() -> new ProductNotFoundException(command.id()));
    verifyVersion(id, command.version());
    final Product archived = product.archive();
    repository.save(archived, command.version(), command.operatorId());
  }

  /** 商品を削除する。 */
  @Transactional
  @CommandHandler
  public void handle(final DeleteProductCommand command) {
    final ProductId id = new ProductId(command.id());
    if (repository.findById(id).isEmpty()) {
      throw new ProductNotFoundException(command.id());
    }
    verifyVersion(id, command.version());
    repository.delete(id, command.version(), command.operatorId());
    eventPublisher.publishEvent(new ProductDeletedEvent(command.id()));
  }

  private void verifyVersion(final ProductId id, final int expectedVersion) {
    final int currentVersion = repository.getVersion(id);
    if (currentVersion != expectedVersion) {
      throw new OptimisticLockException("Product", id.value());
    }
  }
}
