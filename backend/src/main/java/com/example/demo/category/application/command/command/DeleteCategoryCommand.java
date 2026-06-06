package com.example.demo.category.application.command.command;

import org.jmolecules.architecture.cqrs.Command;

/** Category 削除コマンド。 */
@Command
public record DeleteCategoryCommand(String id, int version, String operatorId) {}
