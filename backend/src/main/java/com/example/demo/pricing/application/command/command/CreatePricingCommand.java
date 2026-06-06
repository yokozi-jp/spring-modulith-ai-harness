package com.example.demo.pricing.application.command.command;

import java.math.BigDecimal;
import java.time.Instant;
import org.jmolecules.architecture.cqrs.Command;
import org.jspecify.annotations.Nullable;

/** Pricing 作成コマンド。 */
@Command
public record CreatePricingCommand(
    String productId,
    String level,
    String areaCode,
    BigDecimal amount,
    Instant validFrom,
    @Nullable Instant validTo,
    String operatorId) {}
