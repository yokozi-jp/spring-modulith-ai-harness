package com.example.demo.catalog.presentation.request;

import jakarta.validation.constraints.NotBlank;

/** Product 更新リクエスト。 */
public record UpdateProductRequest(
    @NotBlank(message = "商品名は必須です") String name,
    @NotBlank(message = "商品説明は必須です") String description,
    @NotBlank(message = "カテゴリ ID は必須です") String categoryId,
    int version) {}
