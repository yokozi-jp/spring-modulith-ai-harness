package com.example.demo.catalog.application.command.command;

import org.jmolecules.architecture.cqrs.Command;

/** Product 作成コマンド。 */
@Command
public record CreateProductCommand(
    String name, String description, String categoryId, String sku, String operatorId) {}
