package com.example.demo.pricing.infrastructure.db.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.jooq.tables.Categories;
import com.example.demo.jooq.tables.CategoryClosures;
import com.example.demo.jooq.tables.Pricings;
import com.example.demo.jooq.tables.Products;
import com.example.demo.pricing.application.query.dto.PricingDetailDto;
import com.example.demo.pricing.application.query.dto.PricingSummaryDto;
import com.example.demo.pricing.application.query.param.PricingListParam;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Integration tests for {@link PricingQueryServiceImpl}. */
@Tag("integration")
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class PricingQueryServiceImplTest {

  /** テスト対象クエリサービス。 */
  private final PricingQueryServiceImpl sut;

  /** jOOQ DSL コンテキスト。 */
  private final DSLContext dsl;

  /** テスト用固定時刻。 */
  private static final OffsetDateTime NOW = OffsetDateTime.parse("2025-01-01T00:00:00Z");

  /** テスト用カテゴリ ID。 */
  private static final UUID CATEGORY_ID = UUID.randomUUID();

  /** テスト用商品 ID。 */
  private static final UUID PRODUCT_ID = UUID.randomUUID();

  /** テスト用オペレータ ID。 */
  private static final String OPERATOR_ID = "test-operator";

  /** テスト用 Pricing ID 1。 */
  private static final UUID PRICING_ID_1 = UUID.randomUUID();

  /** テスト用 Pricing ID 2。 */
  private static final UUID PRICING_ID_2 = UUID.randomUUID();

  /** テスト前にカテゴリ・商品・価格データを挿入する。 */
  @BeforeEach
  void setUp() {
    dsl.insertInto(Categories.CATEGORIES)
        .set(Categories.CATEGORIES.ID, CATEGORY_ID)
        .set(Categories.CATEGORIES.NAME, "テストカテゴリ")
        .set(Categories.CATEGORIES.SORT_ORDER, 1)
        .set(Categories.CATEGORIES.CREATED_AT, NOW)
        .set(Categories.CATEGORIES.UPDATED_AT, NOW)
        .set(Categories.CATEGORIES.CREATED_BY, OPERATOR_ID)
        .set(Categories.CATEGORIES.UPDATED_BY, OPERATOR_ID)
        .set(Categories.CATEGORIES.VERSION, 1)
        .execute();

    dsl.insertInto(CategoryClosures.CATEGORY_CLOSURES)
        .set(CategoryClosures.CATEGORY_CLOSURES.ANCESTOR_ID, CATEGORY_ID)
        .set(CategoryClosures.CATEGORY_CLOSURES.DESCENDANT_ID, CATEGORY_ID)
        .set(CategoryClosures.CATEGORY_CLOSURES.DEPTH, 0)
        .set(CategoryClosures.CATEGORY_CLOSURES.CREATED_AT, NOW)
        .set(CategoryClosures.CATEGORY_CLOSURES.UPDATED_AT, NOW)
        .set(CategoryClosures.CATEGORY_CLOSURES.CREATED_BY, OPERATOR_ID)
        .set(CategoryClosures.CATEGORY_CLOSURES.UPDATED_BY, OPERATOR_ID)
        .set(CategoryClosures.CATEGORY_CLOSURES.VERSION, 1)
        .execute();

    dsl.insertInto(Products.PRODUCTS)
        .set(Products.PRODUCTS.ID, PRODUCT_ID)
        .set(Products.PRODUCTS.NAME, "テスト商品")
        .set(Products.PRODUCTS.DESCRIPTION, "説明")
        .set(Products.PRODUCTS.CATEGORY_ID, CATEGORY_ID)
        .set(Products.PRODUCTS.SKU, "SKU-QUERY-" + UUID.randomUUID())
        .set(Products.PRODUCTS.STATUS, "ACTIVE")
        .set(Products.PRODUCTS.CREATED_AT, NOW)
        .set(Products.PRODUCTS.UPDATED_AT, NOW)
        .set(Products.PRODUCTS.CREATED_BY, OPERATOR_ID)
        .set(Products.PRODUCTS.UPDATED_BY, OPERATOR_ID)
        .set(Products.PRODUCTS.VERSION, 1)
        .execute();

    final OffsetDateTime validFrom1 =
        Instant.parse("2025-04-01T00:00:00Z").atOffset(ZoneOffset.UTC);
    final OffsetDateTime validTo1 = Instant.parse("2025-06-30T23:59:59Z").atOffset(ZoneOffset.UTC);
    dsl.insertInto(Pricings.PRICINGS)
        .set(Pricings.PRICINGS.ID, PRICING_ID_1)
        .set(Pricings.PRICINGS.PRODUCT_ID, PRODUCT_ID)
        .set(Pricings.PRICINGS.LEVEL, "REGION")
        .set(Pricings.PRICINGS.AREA_CODE, "JP-13")
        .set(Pricings.PRICINGS.AMOUNT, new BigDecimal("1000"))
        .set(Pricings.PRICINGS.VALID_FROM, validFrom1)
        .set(Pricings.PRICINGS.VALID_TO, validTo1)
        .set(Pricings.PRICINGS.CREATED_AT, NOW)
        .set(Pricings.PRICINGS.UPDATED_AT, NOW)
        .set(Pricings.PRICINGS.CREATED_BY, OPERATOR_ID)
        .set(Pricings.PRICINGS.UPDATED_BY, OPERATOR_ID)
        .set(Pricings.PRICINGS.VERSION, 1)
        .execute();

    final OffsetDateTime validFrom2 =
        Instant.parse("2025-07-01T00:00:00Z").atOffset(ZoneOffset.UTC);
    dsl.insertInto(Pricings.PRICINGS)
        .set(Pricings.PRICINGS.ID, PRICING_ID_2)
        .set(Pricings.PRICINGS.PRODUCT_ID, PRODUCT_ID)
        .set(Pricings.PRICINGS.LEVEL, "PREFECTURE")
        .set(Pricings.PRICINGS.AREA_CODE, "JP-27")
        .set(Pricings.PRICINGS.AMOUNT, new BigDecimal("1500"))
        .set(Pricings.PRICINGS.VALID_FROM, validFrom2)
        .set(Pricings.PRICINGS.CREATED_AT, NOW)
        .set(Pricings.PRICINGS.UPDATED_AT, NOW)
        .set(Pricings.PRICINGS.CREATED_BY, OPERATOR_ID)
        .set(Pricings.PRICINGS.UPDATED_BY, OPERATOR_ID)
        .set(Pricings.PRICINGS.VERSION, 1)
        .execute();
  }

  /** 商品 ID で一覧取得できること。 */
  @Test
  void shouldFindAllByProductId() {
    final PricingListParam param = new PricingListParam(PRODUCT_ID.toString(), null, null, null);
    final Page<PricingSummaryDto> result = sut.findAll(param, PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements(), "should find 2 pricings for the product");
    assertNotNull(result.getContent().get(0).id(), "first pricing id should not be null");
  }

  /** asOf で有効期間内のレコードのみ取得できること。 */
  @Test
  void shouldFilterByAsOf() {
    final Instant asOf = Instant.parse("2025-05-15T00:00:00Z");
    final PricingListParam param = new PricingListParam(PRODUCT_ID.toString(), null, null, asOf);
    final Page<PricingSummaryDto> result = sut.findAll(param, PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements(), "only one pricing should be valid at asOf");
    assertEquals(
        PRICING_ID_1.toString(), result.getContent().get(0).id(), "valid pricing id should match");
  }

  /** ID で詳細取得できること。 */
  @Test
  void shouldFindById() {
    final Optional<PricingDetailDto> result = sut.findById(PRICING_ID_1.toString());

    assertTrue(result.isPresent(), "pricing should be found by id");
    final PricingDetailDto dto = result.get();
    assertEquals(PRICING_ID_1.toString(), dto.id(), "id should match");
    assertEquals(PRODUCT_ID.toString(), dto.productId(), "productId should match");
    assertEquals("REGION", dto.level(), "level should match");
    assertEquals("JP-13", dto.areaCode(), "areaCode should match");
    assertEquals(0, new BigDecimal("1000").compareTo(dto.amount()), "amount should match");
  }
}
