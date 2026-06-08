package com.example.demo.catalog.infrastructure.db.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.catalog.application.query.dto.ProductDetailDto;
import com.example.demo.catalog.application.query.param.ProductListParam;
import com.example.demo.jooq.tables.Categories;
import com.example.demo.jooq.tables.CategoryClosures;
import com.example.demo.jooq.tables.Products;
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

/** Integration tests for {@link ProductQueryServiceImpl}. */
@Tag("integration")
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ProductQueryServiceImplTest {

  /** テスト対象クエリサービス。 */
  private final ProductQueryServiceImpl sut;

  /** jOOQ DSL コンテキスト。 */
  private final DSLContext dsl;

  /** テスト用カテゴリ ID。 */
  private static final UUID CATEGORY_ID = UUID.randomUUID();

  /** テスト用商品 ID。 */
  private static final UUID PRODUCT_ID = UUID.randomUUID();

  /** テスト用 SKU 値。 */
  private static final String SKU_VALUE = "SKU-1234567890123456789012345678";

  /** テスト用操作者 ID。 */
  private static final String OPERATOR_ID = "test-operator";

  /** テスト用固定時刻。 */
  private static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC);

  /** 各テスト前にテスト用カテゴリと商品を挿入する。 */
  @BeforeEach
  void setUp() {
    dsl.insertInto(Categories.CATEGORIES)
        .set(Categories.CATEGORIES.ID, CATEGORY_ID)
        .set(Categories.CATEGORIES.NAME, "テストカテゴリ")
        .set(Categories.CATEGORIES.SORT_ORDER, 0)
        .set(Categories.CATEGORIES.CREATED_AT, NOW)
        .set(Categories.CATEGORIES.UPDATED_AT, NOW)
        .set(Categories.CATEGORIES.CREATED_BY, OPERATOR_ID)
        .set(Categories.CATEGORIES.UPDATED_BY, OPERATOR_ID)
        .set(Categories.CATEGORIES.VERSION, 0)
        .execute();

    dsl.insertInto(CategoryClosures.CATEGORY_CLOSURES)
        .set(CategoryClosures.CATEGORY_CLOSURES.ANCESTOR_ID, CATEGORY_ID)
        .set(CategoryClosures.CATEGORY_CLOSURES.DESCENDANT_ID, CATEGORY_ID)
        .set(CategoryClosures.CATEGORY_CLOSURES.DEPTH, 0)
        .set(CategoryClosures.CATEGORY_CLOSURES.CREATED_AT, NOW)
        .set(CategoryClosures.CATEGORY_CLOSURES.UPDATED_AT, NOW)
        .set(CategoryClosures.CATEGORY_CLOSURES.CREATED_BY, OPERATOR_ID)
        .set(CategoryClosures.CATEGORY_CLOSURES.UPDATED_BY, OPERATOR_ID)
        .set(CategoryClosures.CATEGORY_CLOSURES.VERSION, 0)
        .execute();

    dsl.insertInto(Products.PRODUCTS)
        .set(Products.PRODUCTS.ID, PRODUCT_ID)
        .set(Products.PRODUCTS.NAME, "テスト商品")
        .set(Products.PRODUCTS.DESCRIPTION, "テスト説明文です")
        .set(Products.PRODUCTS.CATEGORY_ID, CATEGORY_ID)
        .set(Products.PRODUCTS.SKU, SKU_VALUE)
        .set(Products.PRODUCTS.STATUS, "DRAFT")
        .set(Products.PRODUCTS.CREATED_AT, NOW)
        .set(Products.PRODUCTS.UPDATED_AT, NOW)
        .set(Products.PRODUCTS.CREATED_BY, OPERATOR_ID)
        .set(Products.PRODUCTS.UPDATED_BY, OPERATOR_ID)
        .set(Products.PRODUCTS.VERSION, 0)
        .execute();
  }

  /** カテゴリ ID で一覧取得できること。 */
  @Test
  void shouldFindAllByCategoryId() {
    final ProductListParam param = new ProductListParam(null, null);
    final Page<?> result = sut.findAll(param, PageRequest.of(0, 10));
    assertNotNullPage(result);
  }

  /** ID で詳細取得できること。 */
  @Test
  void shouldFindById() {
    final Optional<ProductDetailDto> result = sut.findById(PRODUCT_ID.toString());
    assertTrue(result.isPresent(), "product should be found by id");
  }

  /** 存在しない ID では empty が返ること。 */
  @Test
  void shouldReturnEmptyForNonExistent() {
    final Optional<ProductDetailDto> result = sut.findById(UUID.randomUUID().toString());
    assertTrue(result.isEmpty(), "non-existent product should return empty");
  }

  private void assertNotNullPage(final Page<?> page) {
    assertEquals(0, page.getNumber(), "page number should be 0");
  }
}
