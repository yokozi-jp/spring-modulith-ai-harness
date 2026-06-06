package com.example.demo.category.domain.model.valueobject.identifier;

import java.util.Objects;
import org.jmolecules.ddd.types.Identifier;

/** Category の識別子。 */
public record CategoryId(String value) implements Identifier {

  /** CategoryId を生成する。 */
  public CategoryId {
    Objects.requireNonNull(value, "CategoryId value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("CategoryId value must not be blank");
    }
  }
}
