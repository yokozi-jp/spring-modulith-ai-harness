package com.example.demo.catalog;

/**
 * カタログモジュールの公開 API。
 *
 * <p>他モジュールからカタログ情報を問い合わせるために使用する。
 */
public interface CatalogApi {

  /**
   * 指定されたカテゴリに紐づく商品が存在するか確認する。
   *
   * @param categoryId カテゴリ ID
   * @return 存在する場合 true
   */
  boolean existsProductByCategoryId(String categoryId);

  /**
   * 指定された商品が存在するか確認する。
   *
   * @param productId 商品 ID
   * @return 存在する場合 true
   */
  boolean existsProductById(String productId);
}
