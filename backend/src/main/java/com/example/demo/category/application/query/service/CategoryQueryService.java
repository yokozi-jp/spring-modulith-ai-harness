package com.example.demo.category.application.query.service;

import com.example.demo.category.application.query.dto.CategoryDetailDto;
import com.example.demo.category.application.query.dto.CategorySummaryDto;
import com.example.demo.category.application.query.param.CategoryListParam;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Category クエリサービス。 */
public interface CategoryQueryService {

  /** ルートカテゴリ一覧取得。 */
  Page<CategorySummaryDto> findAll(CategoryListParam param, Pageable pageable);

  /** ID で詳細取得（祖先パス含む）。 */
  Optional<CategoryDetailDto> findById(String id);

  /** 直接の子カテゴリ一覧取得。 */
  List<CategorySummaryDto> findChildrenById(String id);
}
