package com.example.demo.catalog.application.query.service;

import com.example.demo.catalog.application.query.dto.ProductDetailDto;
import com.example.demo.catalog.application.query.dto.ProductSummaryDto;
import com.example.demo.catalog.application.query.param.ProductListParam;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Product クエリサービス。 */
public interface ProductQueryService {

  /** 一覧取得。 */
  Page<ProductSummaryDto> findAll(ProductListParam param, Pageable pageable);

  /** ID で取得。 */
  Optional<ProductDetailDto> findById(String id);
}
