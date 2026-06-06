package com.example.demo.category.application.command.handler;

import com.example.demo.OptimisticLockException;
import com.example.demo.category.application.command.command.CreateCategoryCommand;
import com.example.demo.category.application.command.command.DeleteCategoryCommand;
import com.example.demo.category.application.command.command.MoveCategoryCommand;
import com.example.demo.category.application.command.command.UpdateCategoryCommand;
import com.example.demo.category.application.command.dto.CreatedCategoryDto;
import com.example.demo.category.domain.model.aggregate.Category;
import com.example.demo.category.domain.model.valueobject.identifier.CategoryId;
import com.example.demo.category.domain.repository.CategoryRepository;
import com.example.demo.category.domain.service.CategoryFactory;
import com.example.demo.category.event.CategoryDeletionRequestedEvent;
import com.example.demo.category.exception.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Category コマンドハンドラ。 */
@RequiredArgsConstructor
@Component
public class CategoryCommandHandler {

  /** ファクトリ。 */
  private final CategoryFactory factory;

  /** リポジトリ。 */
  private final CategoryRepository repository;

  /** イベント発行。 */
  private final ApplicationEventPublisher eventPublisher;

  /** カテゴリを作成する。 */
  @Transactional
  @CommandHandler
  public CreatedCategoryDto handle(final CreateCategoryCommand command) {
    @Nullable final CategoryId parentId = resolveParentId(command.parentCategoryId());
    if (parentId != null) {
      validateParentExists(parentId);
    }
    final Category category = factory.create(command.name(), command.sortOrder(), parentId);
    repository.save(category, 0, command.operatorId());
    return new CreatedCategoryDto(category.getId().value());
  }

  /** カテゴリを更新する。 */
  @Transactional
  @CommandHandler
  public void handle(final UpdateCategoryCommand command) {
    final CategoryId id = new CategoryId(command.id());
    final Category category =
        repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(command.id()));
    verifyVersion(id, command.version());
    final Category updated = category.update(command.name(), command.sortOrder());
    repository.save(updated, command.version(), command.operatorId());
  }

  /** カテゴリを移動する。 */
  @Transactional
  @CommandHandler
  public void handle(final MoveCategoryCommand command) {
    final CategoryId id = new CategoryId(command.id());
    final Category category =
        repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(command.id()));
    verifyVersion(id, command.version());

    @Nullable final CategoryId newParentId = resolveParentId(command.newParentCategoryId());
    if (newParentId != null) {
      validateParentExists(newParentId);
      validateNotCircular(id, newParentId);
    }

    final Category moved = category.move(newParentId);
    repository.move(moved, command.version(), command.operatorId());
  }

  /** カテゴリを削除する。 */
  @Transactional
  @CommandHandler
  public void handle(final DeleteCategoryCommand command) {
    final CategoryId id = new CategoryId(command.id());
    if (repository.findById(id).isEmpty()) {
      throw new CategoryNotFoundException(command.id());
    }
    verifyVersion(id, command.version());
    if (repository.existsChildCategories(id)) {
      throw new IllegalStateException(
          "Cannot delete category " + command.id() + ": child categories exist");
    }
    eventPublisher.publishEvent(new CategoryDeletionRequestedEvent(command.id()));
    repository.delete(id, command.version(), command.operatorId());
  }

  @Nullable private static CategoryId resolveParentId(@Nullable final String parentCategoryId) {
    if (parentCategoryId == null || parentCategoryId.isBlank()) {
      return null;
    }
    return new CategoryId(parentCategoryId);
  }

  private void validateParentExists(final CategoryId parentId) {
    if (repository.findById(parentId).isEmpty()) {
      throw new IllegalArgumentException("Parent category not found: " + parentId.value());
    }
  }

  private void validateNotCircular(final CategoryId categoryId, final CategoryId newParentId) {
    if (categoryId.equals(newParentId)) {
      throw new IllegalArgumentException("Cannot move category to itself");
    }
    if (repository.isDescendant(categoryId, newParentId)) {
      throw new IllegalArgumentException(
          "Cannot move category to its own descendant: " + newParentId.value());
    }
  }

  private void verifyVersion(final CategoryId id, final int expectedVersion) {
    final int currentVersion = repository.getVersion(id);
    if (currentVersion != expectedVersion) {
      throw new OptimisticLockException("Category", id.value());
    }
  }
}
