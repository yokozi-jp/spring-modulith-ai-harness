package com.example.demo.catalog.application.command.dto;

import com.example.demo.annotation.CommandResult;

/** Product 作成結果。 */
@CommandResult
public record CreatedProductDto(String id) {}
