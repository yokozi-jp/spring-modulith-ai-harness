package com.example.demo.catalog.presentation.response;

import com.example.demo.catalog.application.query.dto.ProductDetailDto;

/** Product 詳細レスポンス。 */
public record ProductDetailResponse(
    String id,
    String name,
    String description,
    String categoryId,
    String sku,
    String status,
    int version) {

  /** DTO から変換する。 */
  public static ProductDetailResponse from(final ProductDetailDto dto) {
    return new ProductDetailResponse(
        dto.id(),
        dto.name(),
        dto.description(),
        dto.categoryId(),
        dto.sku(),
        dto.status(),
        dto.version());
  }
}
