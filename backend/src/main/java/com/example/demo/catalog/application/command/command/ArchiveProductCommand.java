package com.example.demo.catalog.application.command.command;

import org.jmolecules.architecture.cqrs.Command;

/** 商品アーカイブコマンド。 */
@Command
public record ArchiveProductCommand(String id, int version, String operatorId) {}
