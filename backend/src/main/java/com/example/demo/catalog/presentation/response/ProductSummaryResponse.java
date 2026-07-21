package com.example.demo.catalog.presentation.response;

import com.example.demo.catalog.application.query.dto.ProductSummaryDto;

/** Product 一覧レスポンス。 */
public record ProductSummaryResponse(String id, String name, String status, String categoryId) {

  /** DTO から変換する。 */
  public static ProductSummaryResponse from(final ProductSummaryDto dto) {
    return new ProductSummaryResponse(dto.id(), dto.name(), dto.status(), dto.categoryId());
  }
}
