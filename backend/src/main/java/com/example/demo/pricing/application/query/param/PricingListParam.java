package com.example.demo.pricing.application.query.param;

import com.example.demo.annotation.QueryParam;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/** Pricing 一覧検索パラメータ。 */
@QueryParam
public record PricingListParam(
    @Nullable String productId,
    @Nullable String level,
    @Nullable String areaCode,
    @Nullable Instant asOf) {}
