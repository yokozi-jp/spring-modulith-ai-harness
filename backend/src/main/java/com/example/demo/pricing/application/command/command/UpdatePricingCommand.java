package com.example.demo.pricing.application.command.command;

import java.math.BigDecimal;
import java.time.Instant;
import org.jmolecules.architecture.cqrs.Command;
import org.jspecify.annotations.Nullable;

/** Pricing 更新コマンド。 */
@Command
public record UpdatePricingCommand(
    String id,
    BigDecimal amount,
    Instant validFrom,
    @Nullable Instant validTo,
    int version,
    String operatorId) {}
