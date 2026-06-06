package com.example.demo.catalog.application.command.command;

import org.jmolecules.architecture.cqrs.Command;

/** 商品更新コマンド。 */
@Command
public record UpdateProductCommand(
    String id,
    String name,
    String description,
    String categoryId,
    int version,
    String operatorId) {}
