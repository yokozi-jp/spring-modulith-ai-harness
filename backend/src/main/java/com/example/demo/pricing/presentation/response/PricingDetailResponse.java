package com.example.demo.pricing.presentation.response;

import com.example.demo.pricing.application.query.dto.PricingDetailDto;
import java.math.BigDecimal;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/** Pricing 詳細レスポンス。 */
public record PricingDetailResponse(
    String id,
    String productId,
    String level,
    String areaCode,
    BigDecimal amount,
    Instant validFrom,
    @Nullable Instant validTo,
    int version) {

  /** DTO から変換する。 */
  public static PricingDetailResponse from(final PricingDetailDto dto) {
    return new PricingDetailResponse(
        dto.id(),
        dto.productId(),
        dto.level(),
        dto.areaCode(),
        dto.amount(),
        dto.validFrom(),
        dto.validTo(),
        dto.version());
  }
}
