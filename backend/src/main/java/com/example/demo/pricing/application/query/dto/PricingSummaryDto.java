package com.example.demo.pricing.application.query.dto;

import java.math.BigDecimal;
import java.time.Instant;
import org.jmolecules.architecture.cqrs.QueryModel;
import org.jspecify.annotations.Nullable;

/** Pricing 一覧用クエリモデル。 */
@QueryModel
public record PricingSummaryDto(
    String id,
    String productId,
    String level,
    String areaCode,
    BigDecimal amount,
    Instant validFrom,
    @Nullable Instant validTo) {}
