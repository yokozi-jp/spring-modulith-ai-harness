package com.example.demo.category.application.command.dto;

import com.example.demo.annotation.CommandResult;

/** Category 作成結果。 */
@CommandResult
public record CreatedCategoryDto(String id) {}
