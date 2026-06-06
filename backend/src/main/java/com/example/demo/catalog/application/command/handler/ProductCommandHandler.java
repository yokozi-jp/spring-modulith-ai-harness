package com.example.demo.catalog.application.command.handler;

import com.example.demo.catalog.application.command.command.CreateProductCommand;
import com.example.demo.catalog.application.command.dto.CreatedProductDto;
import com.example.demo.catalog.domain.model.aggregate.Product;
import com.example.demo.catalog.domain.model.valueobject.Sku;
import com.example.demo.catalog.domain.repository.ProductRepository;
import com.example.demo.catalog.domain.service.ProductFactory;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
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
}
