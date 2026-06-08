package com.example.demo.pricing.domain.model.valueobject.identifier;

import java.util.Objects;
import org.jmolecules.ddd.types.Identifier;

/** Pricing の識別子。 */
public record PricingId(String value) implements Identifier {

  /** PricingId を生成する。 */
  public PricingId {
    Objects.requireNonNull(value, "PricingId value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("PricingId value must not be blank");
    }
  }
}
