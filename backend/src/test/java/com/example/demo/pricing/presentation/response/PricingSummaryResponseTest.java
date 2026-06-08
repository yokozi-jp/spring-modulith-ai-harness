package com.example.demo.pricing.presentation.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.pricing.application.query.dto.PricingSummaryDto;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PricingSummaryResponse}. */
class PricingSummaryResponseTest {

  /** from() が DTO のフィールドを正しく変換すること。 */
  @Test
  void shouldConvertFromDto() {
    final PricingSummaryDto dto =
        new PricingSummaryDto(
            "pricing-1",
            "product-1",
            "REGION",
            "KANTO",
            new BigDecimal("1000.0000"),
            Instant.parse("2025-01-01T00:00:00Z"),
            null);

    final PricingSummaryResponse result = PricingSummaryResponse.from(dto);

    assertEquals("pricing-1", result.id(), "id should match");
    assertEquals("product-1", result.productId(), "productId should match");
    assertEquals("REGION", result.level(), "level should match");
    assertEquals("KANTO", result.areaCode(), "areaCode should match");
    assertEquals(new BigDecimal("1000.0000"), result.amount(), "amount should match");
  }
}
