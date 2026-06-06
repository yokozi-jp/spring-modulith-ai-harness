package com.example.demo.category.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Category 更新リクエスト。 */
public record UpdateCategoryRequest(
    @NotBlank(message = "カテゴリ名は必須です") @Size(max = 50, message = "カテゴリ名は50文字以内です") String name,
    int sortOrder,
    int version) {}
