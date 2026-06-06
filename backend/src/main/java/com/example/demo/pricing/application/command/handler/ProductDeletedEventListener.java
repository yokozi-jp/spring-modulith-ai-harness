package com.example.demo.pricing.application.command.handler;

import com.example.demo.catalog.event.ProductDeletedEvent;
import com.example.demo.pricing.domain.repository.PricingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** 商品削除イベントを処理するリスナー。 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ProductDeletedEventListener {

  /** 価格リポジトリ。 */
  private final PricingRepository pricingRepository;

  /**
   * 商品削除イベントを処理し、該当商品の全価格を論理削除する。
   *
   * @param event 商品削除イベント
   */
  @ApplicationModuleListener
  /* default */ void handle(final ProductDeletedEvent event) {
    log.info("Received ProductDeletedEvent for productId: {}", event.productId());
    pricingRepository.deleteAllByProductId(event.productId(), "system");
  }
}
