package com.example.demo.pricing.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.demo.catalog.CatalogApi;
import com.example.demo.pricing.domain.model.aggregate.Pricing;
import com.example.demo.pricing.domain.model.valueobject.Price;
import com.example.demo.pricing.domain.model.valueobject.PricingLevel;
import com.example.demo.pricing.domain.model.valueobject.identifier.PricingId;
import com.example.demo.pricing.domain.repository.PricingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link PricingFactory}. */
@ExtendWith(MockitoExtension.class)
class PricingFactoryTest {

  /** テスト用生成 ID。 */
  private static final String GENERATED_ID = "gen-pricing-001";

  /** テスト用商品 ID。 */
  private static final String PRODUCT_ID = "prod-001";

  /** テスト用エリアコード。 */
  private static final String AREA_CODE = "kanto";

  /** テスト用有効開始日時。 */
  private static final Instant VALID_FROM = Instant.parse("2025-01-01T00:00:00Z");

  /** Mock リポジトリ。 */
  @Mock private PricingRepository repository;

  /** Mock カタログ API。 */
  @Mock private CatalogApi catalogApi;

  /** テスト対象。 */
  @InjectMocks private PricingFactory sut;

  @Test
  void shouldCreatePricingWithGeneratedId() {
    when(repository.generateId()).thenReturn(new PricingId(GENERATED_ID));
    when(catalogApi.existsProductById(PRODUCT_ID)).thenReturn(true);

    final Pricing result =
        sut.create(
            PRODUCT_ID,
            PricingLevel.REGION,
            AREA_CODE,
            new Price(new BigDecimal("1000.0000")),
            VALID_FROM,
            null);

    assertEquals(GENERATED_ID, result.getId().value(), "id should match generated id");
    assertEquals(PRODUCT_ID, result.getProductId(), "productId should match");
    assertEquals(PricingLevel.REGION, result.getLevel(), "level should match");
    assertEquals(AREA_CODE, result.getAreaCode(), "areaCode should match");
    assertNull(result.getValidTo(), "validTo should be null");
  }

  @Test
  void shouldThrowWhenProductDoesNotExist() {
    when(catalogApi.existsProductById(PRODUCT_ID)).thenReturn(false);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            sut.create(
                PRODUCT_ID,
                PricingLevel.REGION,
                AREA_CODE,
                new Price(new BigDecimal("1000.0000")),
                VALID_FROM,
                null),
        "should throw when product does not exist");
  }
}
