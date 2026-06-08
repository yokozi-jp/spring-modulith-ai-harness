package com.example.demo.pricing.infrastructure.db.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.jooq.tables.Categories;
import com.example.demo.jooq.tables.CategoryClosures;
import com.example.demo.jooq.tables.Products;
import com.example.demo.pricing.domain.model.aggregate.Pricing;
import com.example.demo.pricing.domain.model.valueobject.Price;
import com.example.demo.pricing.domain.model.valueobject.PricingLevel;
import com.example.demo.pricing.domain.model.valueobject.identifier.PricingId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Integration tests for {@link PricingRepositoryImpl}. */
@Tag("integration")
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class PricingRepositoryImplTest {

  /** テスト対象リポジトリ。 */
  private final PricingRepositoryImpl sut;

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

  /** テスト用エリアコード。 */
  private static final String AREA_CODE = "JP-13";

  /** テスト用有効開始日時。 */
  private static final Instant VALID_FROM_APRIL = Instant.parse("2025-04-01T00:00:00Z");

  /** テスト前にカテゴリと商品を挿入する。 */
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
        .set(Products.PRODUCTS.SKU, "SKU-TEST-" + UUID.randomUUID())
        .set(Products.PRODUCTS.STATUS, "ACTIVE")
        .set(Products.PRODUCTS.CREATED_AT, NOW)
        .set(Products.PRODUCTS.UPDATED_AT, NOW)
        .set(Products.PRODUCTS.CREATED_BY, OPERATOR_ID)
        .set(Products.PRODUCTS.UPDATED_BY, OPERATOR_ID)
        .set(Products.PRODUCTS.VERSION, 1)
        .execute();
  }

  /** ID が生成されること。 */
  @Test
  void shouldGenerateId() {
    final PricingId id = sut.generateId();
    assertNotNull(id, "generated id should not be null");
    assertNotNull(id.value(), "generated id value should not be null");
  }

  /** 保存後に findById で取得できること。 */
  @Test
  void shouldSaveAndFindById() {
    final PricingId id = sut.generateId();
    final Instant validFrom = VALID_FROM_APRIL;
    final Instant validTo = Instant.parse("2025-09-30T23:59:59Z");
    final Pricing pricing =
        new Pricing(
            id,
            PRODUCT_ID.toString(),
            PricingLevel.REGION,
            AREA_CODE,
            new Price(new BigDecimal("1500")),
            validFrom,
            validTo);

    sut.save(pricing, 0, OPERATOR_ID);

    final Optional<Pricing> found = sut.findById(id);
    assertTrue(found.isPresent(), "saved pricing should be found");
    assertEquals(id, found.get().getId(), "id should match");
    assertEquals(PRODUCT_ID.toString(), found.get().getProductId(), "productId should match");
    assertEquals(PricingLevel.REGION, found.get().getLevel(), "level should match");
    assertEquals(AREA_CODE, found.get().getAreaCode(), "areaCode should match");
  }

  /** 更新時にバージョンがインクリメントされること。 */
  @Test
  void shouldUpdateWithVersionIncrement() {
    final PricingId id = sut.generateId();
    final Pricing pricing =
        new Pricing(
            id,
            PRODUCT_ID.toString(),
            PricingLevel.PREFECTURE,
            AREA_CODE,
            new Price(new BigDecimal("2000")),
            VALID_FROM_APRIL,
            null);

    sut.save(pricing, 0, OPERATOR_ID);
    assertEquals(1, sut.getVersion(id), "version after insert should be 1");

    final Pricing updated =
        pricing.update(
            new Price(new BigDecimal("2500")), Instant.parse("2025-05-01T00:00:00Z"), null);
    sut.save(updated, 1, OPERATOR_ID);
    assertEquals(2, sut.getVersion(id), "version after update should be 2");
  }

  /** 論理削除できること。 */
  @Test
  void shouldSoftDelete() {
    final PricingId id = sut.generateId();
    final Pricing pricing =
        new Pricing(
            id,
            PRODUCT_ID.toString(),
            PricingLevel.REGION,
            AREA_CODE,
            new Price(new BigDecimal("1000")),
            VALID_FROM_APRIL,
            null);

    sut.save(pricing, 0, OPERATOR_ID);
    assertTrue(sut.findById(id).isPresent(), "should exist before delete");

    sut.delete(id, 1, OPERATOR_ID);
    assertFalse(sut.findById(id).isPresent(), "should not exist after soft delete");
  }

  /** 重複期間を検出できること。 */
  @Test
  void shouldDetectOverlappingPeriod() {
    final PricingId id1 = sut.generateId();
    final Pricing existing =
        new Pricing(
            id1,
            PRODUCT_ID.toString(),
            PricingLevel.REGION,
            AREA_CODE,
            new Price(new BigDecimal("1000")),
            VALID_FROM_APRIL,
            Instant.parse("2025-06-30T23:59:59Z"));
    sut.save(existing, 0, OPERATOR_ID);

    final PricingId id2 = sut.generateId();
    final Pricing overlapping =
        new Pricing(
            id2,
            PRODUCT_ID.toString(),
            PricingLevel.REGION,
            AREA_CODE,
            new Price(new BigDecimal("1200")),
            Instant.parse("2025-05-01T00:00:00Z"),
            Instant.parse("2025-08-31T23:59:59Z"));

    assertTrue(sut.existsOverlapping(overlapping), "should detect overlapping period");
  }

  /** 商品 ID で全件論理削除できること。 */
  @Test
  void shouldDeleteAllByProductId() {
    final PricingId id1 = sut.generateId();
    final Pricing pricing1 =
        new Pricing(
            id1,
            PRODUCT_ID.toString(),
            PricingLevel.REGION,
            AREA_CODE,
            new Price(new BigDecimal("1000")),
            VALID_FROM_APRIL,
            Instant.parse("2025-06-30T23:59:59Z"));
    sut.save(pricing1, 0, OPERATOR_ID);

    final PricingId id2 = sut.generateId();
    final Pricing pricing2 =
        new Pricing(
            id2,
            PRODUCT_ID.toString(),
            PricingLevel.PREFECTURE,
            "JP-27",
            new Price(new BigDecimal("1500")),
            Instant.parse("2025-07-01T00:00:00Z"),
            null);
    sut.save(pricing2, 0, OPERATOR_ID);

    sut.deleteAllByProductId(PRODUCT_ID.toString(), OPERATOR_ID);

    assertFalse(sut.findById(id1).isPresent(), "first pricing should be soft-deleted");
    assertFalse(sut.findById(id2).isPresent(), "second pricing should be soft-deleted");
  }
}
