package com.example.demo.pricing.application.query.service;

import com.example.demo.pricing.application.query.dto.PricingDetailDto;
import com.example.demo.pricing.application.query.dto.PricingSummaryDto;
import com.example.demo.pricing.application.query.param.PricingListParam;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Pricing クエリサービス。 */
public interface PricingQueryService {

  /**
   * 一覧取得。
   *
   * @param param 検索パラメータ
   * @param pageable ページネーション
   * @return 価格一覧
   */
  Page<PricingSummaryDto> findAll(PricingListParam param, Pageable pageable);

  /**
   * ID で取得。
   *
   * @param id 価格 ID
   * @return 価格詳細（存在しない場合は empty）
   */
  Optional<PricingDetailDto> findById(String id);
}
