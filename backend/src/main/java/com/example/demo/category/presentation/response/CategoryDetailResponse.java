package com.example.demo.category.presentation.response;

import com.example.demo.category.application.query.dto.CategoryDetailDto;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Category 詳細レスポンス。 */
public record CategoryDetailResponse(
    String id,
    String name,
    int sortOrder,
    @Nullable String parentCategoryId,
    int version,
    List<AncestorResponse> ancestors) {

  /** 祖先カテゴリレスポンス。 */
  public record AncestorResponse(String id, String name) {}

  /** DTO から変換する。 */
  public static CategoryDetailResponse from(final CategoryDetailDto dto) {
    final List<AncestorResponse> ancestors =
        dto.ancestors().stream().map(a -> new AncestorResponse(a.id(), a.name())).toList();
    return new CategoryDetailResponse(
        dto.id(), dto.name(), dto.sortOrder(), dto.parentCategoryId(), dto.version(), ancestors);
  }
}
