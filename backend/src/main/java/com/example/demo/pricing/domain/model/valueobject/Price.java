package com.example.demo.pricing.domain.model.valueobject;

import java.math.BigDecimal;
import java.util.Objects;
import org.jmolecules.ddd.types.ValueObject;

/** 金額値オブジェクト。正の値であることを保証する。 */
public record Price(BigDecimal value) implements ValueObject {

  /** Price を生成する。 */
  public Price {
    Objects.requireNonNull(value, "Price value must not be null");
    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
  }
}
