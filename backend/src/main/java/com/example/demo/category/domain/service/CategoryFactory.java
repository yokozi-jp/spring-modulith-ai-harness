package com.example.demo.category.domain.service;

import com.example.demo.category.domain.model.aggregate.Category;
import com.example.demo.category.domain.model.valueobject.identifier.CategoryId;
import com.example.demo.category.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Factory;
import org.jspecify.annotations.Nullable;

/** Category ファクトリ。 */
@RequiredArgsConstructor
@Factory
public class CategoryFactory {

  /** リポジトリ。 */
  private final CategoryRepository repository;

  /**
   * 新しいカテゴリを生成する。
   *
   * @param name カテゴリ名
   * @param sortOrder 表示順
   * @param parentCategoryId 親カテゴリ ID（ルートの場合は null）
   * @return 生成されたカテゴリ
   */
  public Category create(
      final String name, final int sortOrder, @Nullable final CategoryId parentCategoryId) {
    final CategoryId id = repository.generateId();
    return new Category(id, name, sortOrder, parentCategoryId);
  }
}
