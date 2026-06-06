package com.example.demo.catalog.event;

import org.jmolecules.event.annotation.DomainEvent;

/**
 * 商品が削除された際に発行されるドメインイベント。
 *
 * @param productId 削除された商品の ID
 */
@DomainEvent
public record ProductDeletedEvent(String productId) {}
