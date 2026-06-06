package com.example.demo.category.presentation.request;

import org.jspecify.annotations.Nullable;

/** Category 移動リクエスト。 */
public record MoveCategoryRequest(@Nullable String newParentCategoryId, int version) {}
