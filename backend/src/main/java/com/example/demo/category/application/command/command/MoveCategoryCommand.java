package com.example.demo.category.application.command.command;

import org.jmolecules.architecture.cqrs.Command;
import org.jspecify.annotations.Nullable;

/** Category 移動コマンド。 */
@Command
public record MoveCategoryCommand(
    String id, @Nullable String newParentCategoryId, int version, String operatorId) {}
