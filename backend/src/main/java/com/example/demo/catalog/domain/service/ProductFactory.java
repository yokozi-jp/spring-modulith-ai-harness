package com.example.demo.catalog.domain.service;

import com.example.demo.catalog.domain.model.aggregate.Product;
import com.example.demo.catalog.domain.model.valueobject.Sku;
import com.example.demo.catalog.domain.model.valueobject.identifier.ProductId;
import com.example.demo.catalog.domain.repository.ProductRepository;
import com.example.demo.category.CategoryApi;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Factory;
import org.springframework.stereotype.Component;

/** Product ファクトリ。 */
@RequiredArgsConstructor
@Factory
@Component
public class ProductFactory {

  /** リポジトリ。 */
  private final ProductRepository repository;

  /** カテゴリ API。 */
  private final CategoryApi categoryApi;

  /**
   * 新しい商品を生成する。
   *
   * @param name 商品名
   * @param description 商品説明
   * @param categoryId カテゴリ ID
   * @param sku SKU
   * @return 生成された商品
   */
  public Product create(
      final String name, final String description, final String categoryId, final Sku sku) {
    if (!categoryApi.existsById(categoryId)) {
      throw new IllegalArgumentException("Category does not exist: " + categoryId);
    }
    final ProductId id = repository.generateId();
    return new Product(id, name, description, categoryId, sku);
  }
}
