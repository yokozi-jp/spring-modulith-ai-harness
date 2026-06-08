package com.example.demo.category.domain.repository;

import com.example.demo.category.domain.model.aggregate.Category;
import com.example.demo.category.domain.model.valueobject.identifier.CategoryId;
import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

/** Category リポジトリ。 */
public interface CategoryRepository extends Repository<Category, CategoryId> {

  /** ID を生成する。 */
  CategoryId generateId();

  /**
   * カテゴリを永続化する（新規作成・更新共通）。
   *
   * @param category 保存対象のカテゴリ
   * @param version 楽観ロック用バージョン（新規作成時は 0）
   * @param operatorId 操作者 ID
   */
  void save(Category category, int version, String operatorId);

  /**
   * ID でカテゴリを取得する。
   *
   * @param id カテゴリ ID
   * @return カテゴリ（存在しない場合は empty）
   */
  Optional<Category> findById(CategoryId id);

  /**
   * カテゴリのバージョンを取得する。
   *
   * @param id カテゴリ ID
   * @return バージョン番号
   */
  int getVersion(CategoryId id);

  /**
   * カテゴリを論理削除する。
   *
   * @param id カテゴリ ID
   * @param version 楽観ロック用バージョン
   * @param operatorId 操作者 ID
   */
  void delete(CategoryId id, int version, String operatorId);

  /**
   * 指定カテゴリに直接の子カテゴリが存在するか確認する。
   *
   * @param id カテゴリ ID
   * @return 子カテゴリが存在する場合 true
   */
  boolean existsChildCategories(CategoryId id);

  /**
   * カテゴリをツリー内で移動する（Closure Table を再構築する）。
   *
   * @param category 移動後のカテゴリ（新しい parentCategoryId を持つ）
   * @param version 楽観ロック用バージョン
   * @param operatorId 操作者 ID
   */
  void move(Category category, int version, String operatorId);

  /**
   * 指定カテゴリが指定カテゴリの子孫であるか確認する（循環防止用）。
   *
   * @param ancestorId 祖先候補の ID
   * @param descendantId 子孫候補の ID
   * @return descendantId が ancestorId の子孫である場合 true
   */
  boolean isDescendant(CategoryId ancestorId, CategoryId descendantId);
}
