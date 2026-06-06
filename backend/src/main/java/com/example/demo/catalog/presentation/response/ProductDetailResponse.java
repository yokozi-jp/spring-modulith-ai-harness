package com.example.demo.catalog.presentation.response;

import com.example.demo.catalog.application.query.dto.ProductDetailDto;

/** Product 詳細レスポンス。 */
public record ProductDetailResponse(String id) {

  /** DTO から変換する。 */
  public static ProductDetailResponse from(final ProductDetailDto dto) {
    return new ProductDetailResponse(dto.id());
  }
}
