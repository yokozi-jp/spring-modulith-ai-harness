package com.example.demo.category.application.query.dto;

import java.util.List;
import org.jmolecules.architecture.cqrs.QueryModel;
import org.jspecify.annotations.Nullable;

/** Category 詳細クエリモデル。 */
@QueryModel
public record CategoryDetailDto(
    String id,
    String name,
    int sortOrder,
    @Nullable String parentCategoryId,
    int version,
    List<AncestorDto> ancestors) {

  /** 祖先カテゴリ情報。 */
  public record AncestorDto(String id, String name) {}
}
