package com.example.demo.category.application.command.command;

import org.jmolecules.architecture.cqrs.Command;

/** Category 更新コマンド。 */
@Command
public record UpdateCategoryCommand(
    String id, String name, int sortOrder, int version, String operatorId) {}
