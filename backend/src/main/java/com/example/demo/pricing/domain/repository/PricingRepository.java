package com.example.demo.pricing.domain.repository;

import com.example.demo.pricing.domain.model.aggregate.Pricing;
import com.example.demo.pricing.domain.model.valueobject.identifier.PricingId;
import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

/** Pricing リポジトリ。 */
public interface PricingRepository extends Repository<Pricing, PricingId> {

  /** ID を生成する。 */
  PricingId generateId();

  /**
   * 価格を永続化する（新規作成・更新共通）。
   *
   * @param pricing 保存対象の価格
   * @param version 楽観ロック用バージョン（新規作成時は 0）
   * @param operatorId 操作者 ID
   */
  void save(Pricing pricing, int version, String operatorId);

  /**
   * ID で価格を取得する。
   *
   * @param id 価格 ID
   * @return 価格（存在しない場合は empty）
   */
  Optional<Pricing> findById(PricingId id);

  /**
   * 価格のバージョンを取得する。
   *
   * @param id 価格 ID
   * @return バージョン番号
   */
  int getVersion(PricingId id);

  /**
   * 価格を論理削除する。
   *
   * @param id 価格 ID
   * @param version 楽観ロック用バージョン
   * @param operatorId 操作者 ID
   */
  void delete(PricingId id, int version, String operatorId);

  /**
   * 指定条件で期間が重複する価格が存在するか確認する。
   *
   * @param pricing 確認対象の価格（自身を除外するために ID を参照）
   * @return 重複が存在する場合 true
   */
  boolean existsOverlapping(Pricing pricing);

  /**
   * 指定商品の全価格を論理削除する。
   *
   * @param productId 商品 ID
   * @param operatorId 操作者 ID
   */
  void deleteAllByProductId(String productId, String operatorId);
}
