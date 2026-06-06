package com.example.demo.catalog.domain.repository;

import com.example.demo.catalog.domain.model.aggregate.Product;
import com.example.demo.catalog.domain.model.valueobject.identifier.ProductId;
import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

/** Product リポジトリ。 */
public interface ProductRepository extends Repository<Product, ProductId> {

  /** ID を生成する。 */
  ProductId generateId();

  /**
   * 商品を永続化する（新規作成・更新共通）。
   *
   * @param product 保存対象の商品
   * @param version 楽観ロック用バージョン（新規作成時は 0）
   * @param operatorId 操作者 ID
   */
  void save(Product product, int version, String operatorId);

  /**
   * ID で商品を取得する。
   *
   * @param id 商品 ID
   * @return 商品（存在しない場合は empty）
   */
  Optional<Product> findById(ProductId id);

  /**
   * 商品のバージョンを取得する。
   *
   * @param id 商品 ID
   * @return バージョン番号
   */
  int getVersion(ProductId id);

  /**
   * 商品を論理削除する。
   *
   * @param id 商品 ID
   * @param version 楽観ロック用バージョン
   * @param operatorId 操作者 ID
   */
  void delete(ProductId id, int version, String operatorId);
}
