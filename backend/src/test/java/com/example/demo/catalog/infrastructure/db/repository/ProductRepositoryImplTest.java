package com.example.demo.catalog.infrastructure.db.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.catalog.domain.model.aggregate.Product;
import com.example.demo.catalog.domain.model.aggregate.ProductStatus;
import com.example.demo.catalog.domain.model.valueobject.Sku;
import com.example.demo.catalog.domain.model.valueobject.identifier.ProductId;
import com.example.demo.jooq.tables.Categories;
import com.example.demo.jooq.tables.CategoryClosures;
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
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Integration tests for {@link ProductRepositoryImpl}. */
@Tag("integration")
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ProductRepositoryImplTest {

  /** テスト対象リポジトリ。 */
  private final ProductRepositoryImpl sut;

  /** jOOQ DSL コンテキスト。 */
  private final DSLContext dsl;

  /** テスト用カテゴリ ID。 */
  private static final UUID CATEGORY_ID = UUID.randomUUID();

  /** テスト用 SKU 値。 */
  private static final String SKU_VALUE = "SKU-1234567890123456789012345678";

  /** テスト用操作者 ID。 */
  private static final String OPERATOR_ID = "test-operator";

  /** テスト用固定時刻。 */
  private static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC);

  /** 各テスト前にテスト用カテゴリを挿入する。 */
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
  }

  /** ID 生成が null でないこと。 */
  @Test
  void shouldGenerateId() {
    final ProductId id = sut.generateId();
    assertNotNull(id, "generated id should not be null");
  }

  /** 保存後に findById で取得できること。 */
  @Test
  void shouldSaveAndFindById() {
    final ProductId id = sut.generateId();
    final Product product =
        new Product(id, "テスト商品", "テスト説明文です", CATEGORY_ID.toString(), new Sku(SKU_VALUE));

    sut.save(product, 0, OPERATOR_ID);

    final Optional<Product> found = sut.findById(id);
    assertTrue(found.isPresent(), "saved product should be found");
    assertEquals(id, found.get().getId(), "id should match");
    assertEquals("テスト商品", found.get().getName(), "name should match");
    assertEquals(ProductStatus.DRAFT, found.get().getStatus(), "status should be DRAFT");
  }

  /** 更新時にバージョンがインクリメントされること。 */
  @Test
  void shouldUpdateWithVersionIncrement() {
    final ProductId id = sut.generateId();
    final Product product =
        new Product(id, "テスト商品", "テスト説明文です", CATEGORY_ID.toString(), new Sku(SKU_VALUE));

    sut.save(product, 0, OPERATOR_ID);

    final Product updated = product.publish();
    sut.save(updated, 1, OPERATOR_ID);

    final int version = sut.getVersion(id);
    assertEquals(2, version, "version should be incremented after update");
  }

  /** 論理削除後に findById で取得できないこと。 */
  @Test
  void shouldSoftDelete() {
    final ProductId id = sut.generateId();
    final Product product =
        new Product(id, "テスト商品", "テスト説明文です", CATEGORY_ID.toString(), new Sku(SKU_VALUE));

    sut.save(product, 0, OPERATOR_ID);
    sut.delete(id, 1, OPERATOR_ID);

    final Optional<Product> found = sut.findById(id);
    assertTrue(found.isEmpty(), "soft-deleted product should not be found");
  }
}
