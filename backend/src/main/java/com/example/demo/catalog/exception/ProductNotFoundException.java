package com.example.demo.catalog.exception;

/** Product が見つからない場合の例外。 */
public class ProductNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** ID を指定して例外を生成する。 */
  public ProductNotFoundException(final String id) {
    super("Product not found: " + id);
  }
}
