package com.example.demo.pricing.application.command.dto;

import com.example.demo.annotation.CommandResult;

/** Pricing 作成結果。 */
@CommandResult
public record CreatedPricingDto(String id) {}
