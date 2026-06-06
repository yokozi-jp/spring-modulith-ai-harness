package com.example.demo.category.application.command.command;

import org.jmolecules.architecture.cqrs.Command;
import org.jspecify.annotations.Nullable;

/** Category 作成コマンド。 */
@Command
public record CreateCategoryCommand(
    String name, int sortOrder, @Nullable String parentCategoryId, String operatorId) {}
