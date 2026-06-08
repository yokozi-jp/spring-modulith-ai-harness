package com.example.demo.catalog.domain.model.aggregate;

import com.example.demo.catalog.domain.model.valueobject.Sku;
import com.example.demo.catalog.domain.model.valueobject.identifier.ProductId;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;

/** Product 集約ルート。 */
@Getter
@EqualsAndHashCode(of = "id")
public class Product implements AggregateRoot<Product, ProductId> {

  /** 商品名の最大文字数。 */
  public static final int MAX_NAME_LENGTH = 100;

  /** 商品説明の最大文字数。 */
  public static final int MAX_DESCRIPTION_LENGTH = 1000;

  /** 識別子。 */
  private final ProductId id;

  /** 商品名。 */
  private final String name;

  /** 商品説明。 */
  private final String description;

  /** カテゴリ ID。 */
  private final String categoryId;

  /** SKU。 */
  private final Sku sku;

  /** ステータス。 */
  private final ProductStatus status;

  /**
   * 新規作成用コンストラクタ（Factory から呼び出す）。
   *
   * @param id 識別子
   * @param name 商品名
   * @param description 商品説明
   * @param categoryId カテゴリ ID
   * @param sku SKU
   */
  public Product(
      final ProductId id,
      final String name,
      final String description,
      final String categoryId,
      final Sku sku) {
    Objects.requireNonNull(id, "id must not be null");
    validateName(name);
    validateDescription(description);
    Objects.requireNonNull(categoryId, "categoryId must not be null");
    Objects.requireNonNull(sku, "sku must not be null");
    this.id = id;
    this.name = name;
    this.description = description;
    this.categoryId = categoryId;
    this.sku = sku;
    this.status = ProductStatus.DRAFT;
  }

  /**
   * 永続化データから集約を再構築する。
   *
   * @param id 識別子
   * @param name 商品名
   * @param description 商品説明
   * @param categoryId カテゴリ ID
   * @param sku SKU
   * @param status ステータス
   * @return 再構築された Product
   */
  public static Product reconstitute(
      final ProductId id,
      final String name,
      final String description,
      final String categoryId,
      final Sku sku,
      final ProductStatus status) {
    return new Product(id, name, description, categoryId, sku, status);
  }

  /**
   * 商品を公開する。
   *
   * @return 公開済みの新しいインスタンス
   */
  public Product publish() {
    if (this.status == ProductStatus.ARCHIVED) {
      throw new IllegalStateException("Cannot publish an archived product");
    }
    return new Product(
        this.id, this.name, this.description, this.categoryId, this.sku, ProductStatus.PUBLISHED);
  }

  /**
   * 商品を非公開にする。
   *
   * @return 下書きに戻した新しいインスタンス
   */
  public Product unpublish() {
    if (this.status == ProductStatus.ARCHIVED) {
      throw new IllegalStateException("Cannot unpublish an archived product");
    }
    if (this.status == ProductStatus.DRAFT) {
      throw new IllegalStateException("Cannot unpublish a draft product");
    }
    return new Product(
        this.id, this.name, this.description, this.categoryId, this.sku, ProductStatus.DRAFT);
  }

  /**
   * 商品をアーカイブする。
   *
   * @return アーカイブ済みの新しいインスタンス
   */
  public Product archive() {
    if (this.status == ProductStatus.ARCHIVED) {
      throw new IllegalStateException("Product is already archived");
    }
    return new Product(
        this.id, this.name, this.description, this.categoryId, this.sku, ProductStatus.ARCHIVED);
  }

  /**
   * 商品情報を更新する。
   *
   * @param newName 新しい商品名
   * @param newDescription 新しい説明
   * @param newCategoryId 新しいカテゴリ ID
   * @return 更新された新しいインスタンス
   */
  public Product update(
      final String newName, final String newDescription, final String newCategoryId) {
    if (this.status == ProductStatus.ARCHIVED) {
      throw new IllegalStateException("Cannot update an archived product");
    }
    validateName(newName);
    validateDescription(newDescription);
    Objects.requireNonNull(newCategoryId, "categoryId must not be null");
    return new Product(this.id, newName, newDescription, newCategoryId, this.sku, this.status);
  }

  @Override
  public ProductId getId() {
    return id;
  }

  /** 全フィールドを受け取る private コンストラクタ（reconstitute 用）。 */
  private Product(
      final ProductId id,
      final String name,
      final String description,
      final String categoryId,
      final Sku sku,
      final ProductStatus status) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.categoryId = categoryId;
    this.sku = sku;
    this.status = status;
  }

  private static void validateName(final String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Product name must not be blank");
    }
    if (name.length() > MAX_NAME_LENGTH) {
      throw new IllegalArgumentException(
          "Product name must not exceed " + MAX_NAME_LENGTH + " characters");
    }
  }

  private static void validateDescription(final String description) {
    if (description == null || description.isBlank()) {
      throw new IllegalArgumentException("Product description must not be blank");
    }
    if (description.length() > MAX_DESCRIPTION_LENGTH) {
      throw new IllegalArgumentException(
          "Product description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters");
    }
  }
}
