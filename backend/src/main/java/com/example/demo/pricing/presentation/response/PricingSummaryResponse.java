package com.example.demo.pricing.presentation.response;

import com.example.demo.pricing.application.query.dto.PricingSummaryDto;
import java.math.BigDecimal;

/** Pricing 一覧レスポンス。 */
public record PricingSummaryResponse(
    String id, String productId, String level, String areaCode, BigDecimal amount) {

  /** DTO から変換する。 */
  public static PricingSummaryResponse from(final PricingSummaryDto dto) {
    return new PricingSummaryResponse(
        dto.id(), dto.productId(), dto.level(), dto.areaCode(), dto.amount());
  }
}
