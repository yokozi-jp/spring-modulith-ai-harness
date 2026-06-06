package com.example.demo.catalog.domain.model.aggregate;

/** 商品ステータス。 */
public enum ProductStatus {
  /** 下書き。 */
  DRAFT,
  /** 公開中。 */
  PUBLISHED,
  /** アーカイブ済み。 */
  ARCHIVED
}
