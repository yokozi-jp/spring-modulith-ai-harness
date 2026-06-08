package com.example.demo.category.domain.model.aggregate;

import com.example.demo.category.domain.model.valueobject.identifier.CategoryId;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jspecify.annotations.Nullable;

/** Category 集約ルート。 */
@Getter
@EqualsAndHashCode(of = "id")
public class Category implements AggregateRoot<Category, CategoryId> {

  /** カテゴリ名の最大文字数。 */
  public static final int MAX_NAME_LENGTH = 50;

  /** 識別子。 */
  private final CategoryId id;

  /** カテゴリ名。 */
  private final String name;

  /** 同階層内の表示順。 */
  private final int sortOrder;

  /** 親カテゴリ ID（ルートカテゴリの場合は null）。 */
  @Nullable private final CategoryId parentCategoryId;

  /**
   * 新規作成用コンストラクタ（Factory から呼び出す）。
   *
   * @param id 識別子
   * @param name カテゴリ名
   * @param sortOrder 表示順
   * @param parentCategoryId 親カテゴリ ID（ルートの場合は null）
   */
  public Category(
      final CategoryId id,
      final String name,
      final int sortOrder,
      @Nullable final CategoryId parentCategoryId) {
    Objects.requireNonNull(id, "id must not be null");
    validateName(name);
    this.id = id;
    this.name = name;
    this.sortOrder = sortOrder;
    this.parentCategoryId = parentCategoryId;
  }

  /**
   * 永続化データから集約を再構築する。
   *
   * @param id 識別子
   * @param name カテゴリ名
   * @param sortOrder 表示順
   * @param parentCategoryId 親カテゴリ ID
   * @return 再構築された Category
   */
  public static Category reconstitute(
      final CategoryId id,
      final String name,
      final int sortOrder,
      @Nullable final CategoryId parentCategoryId) {
    return new Category(id, name, sortOrder, parentCategoryId);
  }

  /**
   * カテゴリ情報を更新する。
   *
   * @param newName 新しい名前
   * @param newSortOrder 新しい表示順
   * @return 更新された新しいインスタンス
   */
  public Category update(final String newName, final int newSortOrder) {
    validateName(newName);
    return new Category(this.id, newName, newSortOrder, this.parentCategoryId);
  }

  /**
   * 親カテゴリを変更する（ツリー内移動）。
   *
   * @param newParentCategoryId 新しい親カテゴリ ID（ルートに昇格する場合は null）
   * @return 移動後の新しいインスタンス
   */
  public Category move(@Nullable final CategoryId newParentCategoryId) {
    return new Category(this.id, this.name, this.sortOrder, newParentCategoryId);
  }

  @Override
  public CategoryId getId() {
    return id;
  }

  private static void validateName(final String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Category name must not be blank");
    }
    if (name.length() > MAX_NAME_LENGTH) {
      throw new IllegalArgumentException(
          "Category name must not exceed " + MAX_NAME_LENGTH + " characters");
    }
  }
}
