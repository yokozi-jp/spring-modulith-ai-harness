package com.example.demo.catalog.application.command.command;

import org.jmolecules.architecture.cqrs.Command;

/** 商品公開コマンド。 */
@Command
public record PublishProductCommand(String id, int version, String operatorId) {}
