package com.example.demo.catalog.application.command.handler;

import com.example.demo.catalog.CatalogApi;
import com.example.demo.category.event.CategoryDeletionRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** カテゴリ削除要求を受信し、商品が存在する場合は拒否する。 */
@RequiredArgsConstructor
@Component
public class CategoryDeletionRequestedListener {

  /** カタログ API。 */
  private final CatalogApi catalogApi;

  /** カテゴリ削除前に商品存在をチェックし、存在すれば例外でロールバックさせる。 */
  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handleCategoryDeletionRequested(final CategoryDeletionRequestedEvent event) {
    if (catalogApi.existsProductByCategoryId(event.categoryId())) {
      throw new IllegalStateException(
          "Cannot delete category " + event.categoryId() + ": products exist");
    }
  }
}
