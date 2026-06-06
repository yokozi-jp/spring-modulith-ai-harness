package com.example.demo.category;

/**
 * カテゴリモジュールの公開 API。
 *
 * <p>他モジュールからカテゴリ情報を問い合わせるために使用する。
 */
@FunctionalInterface
public interface CategoryApi {

  /**
   * 指定されたカテゴリが存在するか確認する。
   *
   * @param categoryId カテゴリ ID
   * @return 存在する場合 true
   */
  boolean existsById(String categoryId);
}
