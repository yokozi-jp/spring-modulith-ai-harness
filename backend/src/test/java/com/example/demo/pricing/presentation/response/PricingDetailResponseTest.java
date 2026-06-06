package com.example.demo.pricing.presentation.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.pricing.application.query.dto.PricingDetailDto;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PricingDetailResponse}. */
class PricingDetailResponseTest {

  /** テスト用 ID。 */
  private static final String PRICING_ID = "pricing-1";

  /** from() が DTO のフィールドを正しく変換すること。 */
  @Test
  void shouldConvertFromDto() {
    final Instant now = Instant.parse("2025-01-01T00:00:00Z");
    final PricingDetailDto dto =
        new PricingDetailDto(
            PRICING_ID,
            "product-1",
            "REGION",
            "KANTO",
            new BigDecimal("1000.0000"),
            now,
            null,
            1,
            now,
            now);

    final PricingDetailResponse result = PricingDetailResponse.from(dto);

    assertEquals(PRICING_ID, result.id(), "id should match");
    assertEquals("product-1", result.productId(), "productId should match");
    assertEquals("REGION", result.level(), "level should match");
    assertEquals(new BigDecimal("1000.0000"), result.amount(), "amount should match");
    assertEquals(1, result.version(), "version should match");
  }
}
