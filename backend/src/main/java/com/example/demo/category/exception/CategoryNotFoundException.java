package com.example.demo.category.exception;

/** Category が見つからない場合の例外。 */
public class CategoryNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** ID を指定して例外を生成する。 */
  public CategoryNotFoundException(final String id) {
    super("Category not found: " + id);
  }
}
