package com.example.demo.pricing.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/** Pricing 作成リクエスト。 */
public record CreatePricingRequest(
    @NotBlank(message = "商品 ID は必須です") String productId,
    @NotBlank(message = "レベルは必須です") String level,
    @NotBlank(message = "エリアコードは必須です") String areaCode,
    @NotNull(message = "金額は必須です") @Positive(message = "金額は正の値である必要があります") BigDecimal amount,
    @NotNull(message = "有効開始日時は必須です") Instant validFrom,
    @Nullable Instant validTo) {}
