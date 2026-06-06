package com.example.demo.category.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/** Category 作成リクエスト。 */
public record CreateCategoryRequest(
    @NotBlank(message = "カテゴリ名は必須です") @Size(max = 50, message = "カテゴリ名は50文字以内です") String name,
    int sortOrder,
    @Nullable String parentCategoryId) {}
