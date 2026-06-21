package com.example.demo.pricing.domain.service;

import com.example.demo.catalog.CatalogApi;
import com.example.demo.pricing.domain.model.aggregate.Pricing;
import com.example.demo.pricing.domain.model.valueobject.Price;
import com.example.demo.pricing.domain.model.valueobject.PricingLevel;
import com.example.demo.pricing.domain.model.valueobject.identifier.PricingId;
import com.example.demo.pricing.domain.repository.PricingRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Factory;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Pricing ファクトリ。 */
@RequiredArgsConstructor
@Factory
@Component
public class PricingFactory {

  /** リポジトリ。 */
  private final PricingRepository repository;

  /** カタログ API。 */
  private final CatalogApi catalogApi;

  /**
   * 新しい価格を生成する。
   *
   * @param productId 商品 ID
   * @param level 価格レベル
   * @param areaCode エリアコード
   * @param amount 金額
   * @param validFrom 有効開始日時
   * @param validTo 有効終了日時
   * @return 生成された価格
   */
  public Pricing create(
      final String productId,
      final PricingLevel level,
      final String areaCode,
      final Price amount,
      final Instant validFrom,
      @Nullable final Instant validTo) {
    if (!catalogApi.existsProductById(productId)) {
      throw new IllegalArgumentException("Product does not exist: " + productId);
    }
    final PricingId id = repository.generateId();
    return new Pricing(id, productId, level, areaCode, amount, validFrom, validTo);
  }
}
