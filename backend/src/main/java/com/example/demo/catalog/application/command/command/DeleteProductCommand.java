package com.example.demo.catalog.application.command.command;

import org.jmolecules.architecture.cqrs.Command;

/** 商品削除コマンド。 */
@Command
public record DeleteProductCommand(String id, int version, String operatorId) {}
