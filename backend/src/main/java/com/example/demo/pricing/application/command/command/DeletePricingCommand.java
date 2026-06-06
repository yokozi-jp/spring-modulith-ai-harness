package com.example.demo.pricing.application.command.command;

import org.jmolecules.architecture.cqrs.Command;

/** Pricing 削除コマンド。 */
@Command
public record DeletePricingCommand(String id, int version, String operatorId) {}
