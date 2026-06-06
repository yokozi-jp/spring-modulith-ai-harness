package com.example.demo.category.application.command.handler;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.category.application.command.command.DeleteCategoryCommand;
import com.example.demo.category.domain.model.aggregate.Category;
import com.example.demo.category.domain.model.valueobject.identifier.CategoryId;
import com.example.demo.category.domain.repository.CategoryRepository;
import com.example.demo.category.event.CategoryDeletionRequestedEvent;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/** Unit tests for {@link CategoryCommandHandler}. */
@ExtendWith(MockitoExtension.class)
class CategoryCommandHandlerTest {

  /** カテゴリ ID 定数。 */
  private static final String CATEGORY_ID = "cat-1";

  /** オペレータ ID 定数。 */
  private static final String OPERATOR_ID = "operator-1";

  /** リポジトリモック。 */
  @Mock private CategoryRepository repository;

  /** イベント発行モック。 */
  @Mock private ApplicationEventPublisher eventPublisher;

  /** カテゴリモック。 */
  @Mock private Category category;

  /** テスト対象。 */
  @InjectMocks private CategoryCommandHandler sut;

  /** 子カテゴリが存在する場合に削除で例外を投げること。 */
  @Test
  void shouldThrowWhenChildCategoriesExistOnDelete() {
    final DeleteCategoryCommand command = new DeleteCategoryCommand(CATEGORY_ID, 0, OPERATOR_ID);
    final CategoryId id = new CategoryId(CATEGORY_ID);
    when(repository.findById(id)).thenReturn(Optional.of(category));
    when(repository.getVersion(id)).thenReturn(0);
    when(repository.existsChildCategories(id)).thenReturn(true);

    assertThrows(
        IllegalStateException.class, () -> sut.handle(command), "should throw on child exists");
  }

  /** 削除時に CategoryDeletionRequestedEvent を発行すること。 */
  @Test
  void shouldPublishDeletionRequestedEventOnDelete() {
    final DeleteCategoryCommand command = new DeleteCategoryCommand(CATEGORY_ID, 0, OPERATOR_ID);
    final CategoryId id = new CategoryId(CATEGORY_ID);
    when(repository.findById(id)).thenReturn(Optional.of(category));
    when(repository.getVersion(id)).thenReturn(0);
    when(repository.existsChildCategories(id)).thenReturn(false);

    sut.handle(command);

    verify(eventPublisher).publishEvent(new CategoryDeletionRequestedEvent(CATEGORY_ID));
    verify(repository).delete(id, 0, OPERATOR_ID);
  }
}
