package com.example.demo.category.presentation.response;

import com.example.demo.category.application.query.dto.CategorySummaryDto;

/** Category 一覧レスポンス。 */
public record CategorySummaryResponse(String id, String name, int sortOrder) {

  /** DTO から変換する。 */
  public static CategorySummaryResponse from(final CategorySummaryDto dto) {
    return new CategorySummaryResponse(dto.id(), dto.name(), dto.sortOrder());
  }
}
