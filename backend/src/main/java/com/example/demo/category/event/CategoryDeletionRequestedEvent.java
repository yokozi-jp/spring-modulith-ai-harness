package com.example.demo.category.event;

import org.jmolecules.event.annotation.DomainEvent;

/** カテゴリ削除要求イベント。リスナーが拒否すればトランザクションがロールバックされる。 */
@DomainEvent
public record CategoryDeletionRequestedEvent(String categoryId) {}
