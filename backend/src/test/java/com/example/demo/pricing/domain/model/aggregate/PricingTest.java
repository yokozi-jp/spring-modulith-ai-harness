package com.example.demo.pricing.domain.model.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.pricing.domain.model.valueobject.Price;
import com.example.demo.pricing.domain.model.valueobject.PricingLevel;
import com.example.demo.pricing.domain.model.valueobject.identifier.PricingId;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Pricing} aggregate. */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class PricingTest {

  /** テスト用価格 ID。 */
  private static final String ID_VALUE = "pricing-001";

  /** テスト用商品 ID。 */
  private static final String PRODUCT_ID = "prod-001";

  /** テスト用エリアコード。 */
  private static final String AREA_CODE = "kanto";

  /** テスト用有効開始日時。 */
  private static final Instant VALID_FROM = Instant.parse("2025-01-01T00:00:00Z");

  /** テスト用有効終了日時。 */
  private static final Instant VALID_TO = Instant.parse("2025-12-31T23:59:59Z");

  @Test
  void shouldCreateWithAllFields() {
    final Pricing pricing = createPricing();

    assertEquals(ID_VALUE, pricing.getId().value(), "id should match");
    assertEquals(PRODUCT_ID, pricing.getProductId(), "productId should match");
    assertEquals(PricingLevel.REGION, pricing.getLevel(), "level should match");
    assertEquals(AREA_CODE, pricing.getAreaCode(), "areaCode should match");
    assertEquals(new BigDecimal("1000.0000"), pricing.getAmount().value(), "amount should match");
    assertEquals(VALID_FROM, pricing.getValidFrom(), "validFrom should match");
    assertEquals(VALID_TO, pricing.getValidTo(), "validTo should match");
  }

  @Test
  void shouldCreateWithNullValidTo() {
    final Pricing pricing =
        new Pricing(
            new PricingId(ID_VALUE),
            PRODUCT_ID,
            PricingLevel.REGION,
            AREA_CODE,
            new Price(new BigDecimal("1000.0000")),
            VALID_FROM,
            null);

    assertNull(pricing.getValidTo(), "validTo should be null");
  }

  @Test
  void shouldThrowWhenValidToIsBeforeValidFrom() {
    final Instant invalidValidTo = Instant.parse("2024-01-01T00:00:00Z");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Pricing(
                new PricingId(ID_VALUE),
                PRODUCT_ID,
                PricingLevel.REGION,
                AREA_CODE,
                new Price(new BigDecimal("1000.0000")),
                VALID_FROM,
                invalidValidTo),
        "should throw when validTo is before validFrom");
  }

  @Test
  void shouldThrowWhenValidToEqualsValidFrom() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Pricing(
                new PricingId(ID_VALUE),
                PRODUCT_ID,
                PricingLevel.REGION,
                AREA_CODE,
                new Price(new BigDecimal("1000.0000")),
                VALID_FROM,
                VALID_FROM),
        "should throw when validTo equals validFrom");
  }

  @Test
  void shouldThrowWhenIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            new Pricing(
                null,
                PRODUCT_ID,
                PricingLevel.REGION,
                AREA_CODE,
                new Price(new BigDecimal("1000.0000")),
                VALID_FROM,
                VALID_TO),
        "should throw when id is null");
  }

  @Test
  void shouldThrowWhenProductIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            new Pricing(
                new PricingId(ID_VALUE),
                null,
                PricingLevel.REGION,
                AREA_CODE,
                new Price(new BigDecimal("1000.0000")),
                VALID_FROM,
                VALID_TO),
        "should throw when productId is null");
  }

  @Test
  void shouldUpdateAmountAndPeriod() {
    final Pricing pricing = createPricing();
    final Price newAmount = new Price(new BigDecimal("2000.0000"));
    final Instant newValidFrom = Instant.parse("2025-04-01T00:00:00Z");
    final Instant newValidTo = Instant.parse("2025-09-30T23:59:59Z");

    final Pricing updated = pricing.update(newAmount, newValidFrom, newValidTo);

    assertEquals(ID_VALUE, updated.getId().value(), "id should remain the same");
    assertEquals(PRODUCT_ID, updated.getProductId(), "productId should remain the same");
    assertEquals(PricingLevel.REGION, updated.getLevel(), "level should remain the same");
    assertEquals(AREA_CODE, updated.getAreaCode(), "areaCode should remain the same");
    assertEquals(new BigDecimal("2000.0000"), updated.getAmount().value(), "amount should update");
    assertEquals(newValidFrom, updated.getValidFrom(), "validFrom should update");
    assertEquals(newValidTo, updated.getValidTo(), "validTo should update");
  }

  @Test
  void shouldUpdateWithNullValidTo() {
    final Pricing pricing = createPricing();
    final Price newAmount = new Price(new BigDecimal("2000.0000"));
    final Instant newValidFrom = Instant.parse("2025-04-01T00:00:00Z");

    final Pricing updated = pricing.update(newAmount, newValidFrom, null);

    assertNull(updated.getValidTo(), "validTo should be null after update");
  }

  @Test
  void shouldReconstitute() {
    final Pricing pricing =
        Pricing.reconstitute(
            new PricingId(ID_VALUE),
            PRODUCT_ID,
            PricingLevel.PREFECTURE,
            "13",
            new Price(new BigDecimal("500.0000")),
            VALID_FROM,
            VALID_TO);

    assertEquals(ID_VALUE, pricing.getId().value(), "id should match");
    assertEquals(PricingLevel.PREFECTURE, pricing.getLevel(), "level should match");
  }

  private Pricing createPricing() {
    return new Pricing(
        new PricingId(ID_VALUE),
        PRODUCT_ID,
        PricingLevel.REGION,
        AREA_CODE,
        new Price(new BigDecimal("1000.0000")),
        VALID_FROM,
        VALID_TO);
  }
}
