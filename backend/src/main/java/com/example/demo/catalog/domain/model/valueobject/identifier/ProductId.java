package com.example.demo.catalog.domain.model.valueobject.identifier;

import java.util.Objects;
import org.jmolecules.ddd.types.Identifier;

/** Product の識別子。 */
public record ProductId(String value) implements Identifier {

  /** ProductId を生成する。 */
  public ProductId {
    Objects.requireNonNull(value, "ProductId value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("ProductId value must not be blank");
    }
  }
}
