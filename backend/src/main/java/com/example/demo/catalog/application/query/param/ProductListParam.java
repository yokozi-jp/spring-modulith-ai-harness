package com.example.demo.catalog.application.query.param;

import com.example.demo.annotation.QueryParam;
import org.jspecify.annotations.Nullable;

/** Product 一覧検索パラメータ。 */
@QueryParam
public record ProductListParam(@Nullable String categoryId, @Nullable String status) {}
