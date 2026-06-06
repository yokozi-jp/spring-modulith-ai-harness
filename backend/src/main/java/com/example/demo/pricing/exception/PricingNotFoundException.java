package com.example.demo.pricing.exception;

/** Pricing が見つからない場合の例外。 */
public class PricingNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** ID を指定して例外を生成する。 */
  public PricingNotFoundException(final String id) {
    super("Pricing not found: " + id);
  }
}
