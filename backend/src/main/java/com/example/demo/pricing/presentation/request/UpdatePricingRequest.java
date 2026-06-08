package com.example.demo.pricing.presentation.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/** Pricing 更新リクエスト。 */
public record UpdatePricingRequest(
    @NotNull(message = "金額は必須です") @Positive(message = "金額は正の値である必要があります") BigDecimal amount,
    @NotNull(message = "有効開始日時は必須です") Instant validFrom,
    @Nullable Instant validTo,
    int version) {}
