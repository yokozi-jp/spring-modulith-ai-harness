package com.example.demo.catalog.application.command.command;

import org.jmolecules.architecture.cqrs.Command;

/** 商品非公開コマンド。 */
@Command
public record UnpublishProductCommand(String id, int version, String operatorId) {}
