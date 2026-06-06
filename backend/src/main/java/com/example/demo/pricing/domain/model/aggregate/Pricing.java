package com.example.demo.pricing.domain.model.aggregate;

import com.example.demo.pricing.domain.model.valueobject.Price;
import com.example.demo.pricing.domain.model.valueobject.PricingLevel;
import com.example.demo.pricing.domain.model.valueobject.identifier.PricingId;
import java.time.Instant;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jspecify.annotations.Nullable;

/** Pricing 集約ルート。 */
@Getter
@EqualsAndHashCode(of = "id")
public class Pricing implements AggregateRoot<Pricing, PricingId> {

  /** 識別子。 */
  private final PricingId id;

  /** 商品 ID。 */
  private final String productId;

  /** 価格レベル。 */
  private final PricingLevel level;

  /** エリアコード。 */
  private final String areaCode;

  /** 金額。 */
  private final Price amount;

  /** 有効開始日時。 */
  private final Instant validFrom;

  /** 有効終了日時（null の場合は無期限）。 */
  @Nullable private final Instant validTo;

  /**
   * 新規作成用コンストラクタ（Factory から呼び出す）。
   *
   * @param id 識別子
   * @param productId 商品 ID
   * @param level 価格レベル
   * @param areaCode エリアコード
   * @param amount 金額
   * @param validFrom 有効開始日時
   * @param validTo 有効終了日時
   */
  public Pricing(
      final PricingId id,
      final String productId,
      final PricingLevel level,
      final String areaCode,
      final Price amount,
      final Instant validFrom,
      @Nullable final Instant validTo) {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(productId, "productId must not be null");
    Objects.requireNonNull(level, "level must not be null");
    Objects.requireNonNull(areaCode, "areaCode must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(validFrom, "validFrom must not be null");
    validatePeriod(validFrom, validTo);
    this.id = id;
    this.productId = productId;
    this.level = level;
    this.areaCode = areaCode;
    this.amount = amount;
    this.validFrom = validFrom;
    this.validTo = validTo;
  }

  /**
   * 永続化データから集約を再構築する。
   *
   * @param id 識別子
   * @param productId 商品 ID
   * @param level 価格レベル
   * @param areaCode エリアコード
   * @param amount 金額
   * @param validFrom 有効開始日時
   * @param validTo 有効終了日時
   * @return 再構築された Pricing
   */
  public static Pricing reconstitute(
      final PricingId id,
      final String productId,
      final PricingLevel level,
      final String areaCode,
      final Price amount,
      final Instant validFrom,
      @Nullable final Instant validTo) {
    return new Pricing(id, productId, level, areaCode, amount, validFrom, validTo);
  }

  /**
   * 価格情報を更新する。
   *
   * @param newAmount 新しい金額
   * @param newValidFrom 新しい有効開始日時
   * @param newValidTo 新しい有効終了日時
   * @return 更新された新しいインスタンス
   */
  public Pricing update(
      final Price newAmount, final Instant newValidFrom, @Nullable final Instant newValidTo) {
    Objects.requireNonNull(newAmount, "amount must not be null");
    Objects.requireNonNull(newValidFrom, "validFrom must not be null");
    validatePeriod(newValidFrom, newValidTo);
    return new Pricing(
        this.id, this.productId, this.level, this.areaCode, newAmount, newValidFrom, newValidTo);
  }

  @Override
  public PricingId getId() {
    return id;
  }

  private static void validatePeriod(final Instant validFrom, @Nullable final Instant validTo) {
    if (validTo != null && !validTo.isAfter(validFrom)) {
      throw new IllegalArgumentException("validTo must be after validFrom");
    }
  }
}
