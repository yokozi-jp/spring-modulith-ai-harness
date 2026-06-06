package com.example.demo.catalog.domain.model.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.catalog.domain.model.valueobject.Sku;
import com.example.demo.catalog.domain.model.valueobject.identifier.ProductId;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Product} aggregate. */
@SuppressWarnings("PMD.TooManyMethods")
class ProductTest {

  /** テスト用商品 ID。 */
  private static final String ID_VALUE = "prod-001";

  /** テスト用商品名。 */
  private static final String NAME = "テスト商品";

  /** テスト用商品説明。 */
  private static final String DESCRIPTION = "テスト商品の説明";

  /** テスト用カテゴリ ID。 */
  private static final String CATEGORY_ID = "cat-001";

  /** テスト用 SKU 値。 */
  private static final String SKU_VALUE = "SKU-1234567890123456789012345678";

  /** 更新用商品名。 */
  private static final String NEW_NAME = "新商品名";

  /** 更新用商品説明。 */
  private static final String NEW_DESCRIPTION = "新説明";

  /** 更新用カテゴリ ID。 */
  private static final String NEW_CATEGORY_ID = "cat-002";

  @Test
  void shouldCreateWithDraftStatus() {
    final Product product = createDraftProduct();

    assertEquals(ID_VALUE, product.getId().value(), "id should match");
    assertEquals(NAME, product.getName(), "name should match");
    assertEquals(DESCRIPTION, product.getDescription(), "description should match");
    assertEquals(CATEGORY_ID, product.getCategoryId(), "categoryId should match");
    assertEquals(SKU_VALUE, product.getSku().value(), "sku should match");
    assertEquals(ProductStatus.DRAFT, product.getStatus(), "initial status should be DRAFT");
  }

  @Test
  void shouldThrowWhenNameIsBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Product(
                new ProductId(ID_VALUE), "  ", DESCRIPTION, CATEGORY_ID, new Sku(SKU_VALUE)),
        "should throw on blank name");
  }

  @Test
  void shouldThrowWhenNameExceedsMaxLength() {
    final String longName = "a".repeat(Product.MAX_NAME_LENGTH + 1);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Product(
                new ProductId(ID_VALUE), longName, DESCRIPTION, CATEGORY_ID, new Sku(SKU_VALUE)),
        "should throw on name exceeding max length");
  }

  @Test
  void shouldThrowWhenDescriptionIsBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Product(new ProductId(ID_VALUE), NAME, "  ", CATEGORY_ID, new Sku(SKU_VALUE)),
        "should throw on blank description");
  }

  @Test
  void shouldThrowWhenDescriptionExceedsMaxLength() {
    final String longDesc = "a".repeat(Product.MAX_DESCRIPTION_LENGTH + 1);
    assertThrows(
        IllegalArgumentException.class,
        () -> new Product(new ProductId(ID_VALUE), NAME, longDesc, CATEGORY_ID, new Sku(SKU_VALUE)),
        "should throw on description exceeding max length");
  }

  @Test
  void shouldPublishFromDraft() {
    final Product product = createDraftProduct();
    final Product published = product.publish();

    assertEquals(ProductStatus.PUBLISHED, published.getStatus(), "status should be PUBLISHED");
    assertEquals(ID_VALUE, published.getId().value(), "id should remain the same");
  }

  @Test
  void shouldPublishFromPublished() {
    final Product product = createDraftProduct().publish();
    final Product republished = product.publish();

    assertEquals(ProductStatus.PUBLISHED, republished.getStatus(), "status should be PUBLISHED");
  }

  @Test
  void shouldThrowWhenPublishingArchived() {
    final Product product = createDraftProduct().archive();

    assertThrows(
        IllegalStateException.class,
        product::publish,
        "should throw on publishing archived product");
  }

  @Test
  void shouldUnpublishFromPublished() {
    final Product product = createDraftProduct().publish();
    final Product unpublished = product.unpublish();

    assertEquals(ProductStatus.DRAFT, unpublished.getStatus(), "status should be DRAFT");
  }

  @Test
  void shouldThrowWhenUnpublishingDraft() {
    final Product product = createDraftProduct();

    assertThrows(
        IllegalStateException.class,
        product::unpublish,
        "should throw on unpublishing draft product");
  }

  @Test
  void shouldThrowWhenUnpublishingArchived() {
    final Product product = createDraftProduct().archive();

    assertThrows(
        IllegalStateException.class,
        product::unpublish,
        "should throw on unpublishing archived product");
  }

  @Test
  void shouldArchiveFromDraft() {
    final Product product = createDraftProduct();
    final Product archived = product.archive();

    assertEquals(ProductStatus.ARCHIVED, archived.getStatus(), "status should be ARCHIVED");
  }

  @Test
  void shouldArchiveFromPublished() {
    final Product product = createDraftProduct().publish();
    final Product archived = product.archive();

    assertEquals(ProductStatus.ARCHIVED, archived.getStatus(), "status should be ARCHIVED");
  }

  @Test
  void shouldThrowWhenArchivingAlreadyArchived() {
    final Product product = createDraftProduct().archive();

    assertThrows(
        IllegalStateException.class,
        product::archive,
        "should throw on archiving already archived product");
  }

  @Test
  void shouldUpdateNameDescriptionAndCategoryId() {
    final Product product = createDraftProduct();
    final Product updated = product.update(NEW_NAME, NEW_DESCRIPTION, NEW_CATEGORY_ID);

    assertEquals(NEW_NAME, updated.getName(), "name should be updated");
    assertEquals(NEW_DESCRIPTION, updated.getDescription(), "description should be updated");
    assertEquals(NEW_CATEGORY_ID, updated.getCategoryId(), "categoryId should be updated");
    assertEquals(ProductStatus.DRAFT, updated.getStatus(), "status should remain DRAFT");
  }

  @Test
  void shouldUpdatePublishedProduct() {
    final Product product = createDraftProduct().publish();
    final Product updated = product.update(NEW_NAME, NEW_DESCRIPTION, NEW_CATEGORY_ID);

    assertEquals(ProductStatus.PUBLISHED, updated.getStatus(), "status should remain PUBLISHED");
  }

  @Test
  void shouldThrowWhenUpdatingArchived() {
    final Product product = createDraftProduct().archive();

    assertThrows(
        IllegalStateException.class,
        () -> product.update(NEW_NAME, NEW_DESCRIPTION, NEW_CATEGORY_ID),
        "should throw on updating archived product");
  }

  @Test
  void shouldReconstitute() {
    final Product product =
        Product.reconstitute(
            new ProductId(ID_VALUE),
            NAME,
            DESCRIPTION,
            CATEGORY_ID,
            new Sku(SKU_VALUE),
            ProductStatus.PUBLISHED);

    assertEquals(ID_VALUE, product.getId().value(), "id should match");
    assertEquals(NAME, product.getName(), "name should match");
    assertEquals(DESCRIPTION, product.getDescription(), "description should match");
    assertEquals(CATEGORY_ID, product.getCategoryId(), "categoryId should match");
    assertEquals(SKU_VALUE, product.getSku().value(), "sku should match");
    assertEquals(ProductStatus.PUBLISHED, product.getStatus(), "status should match");
  }

  private Product createDraftProduct() {
    return new Product(new ProductId(ID_VALUE), NAME, DESCRIPTION, CATEGORY_ID, new Sku(SKU_VALUE));
  }
}
