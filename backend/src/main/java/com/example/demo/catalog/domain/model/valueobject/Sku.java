package com.example.demo.catalog.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;
import org.jmolecules.ddd.types.ValueObject;

/** SKU 値オブジェクト。SKU- に続く28桁の数字で構成される。 */
public record Sku(String value) implements ValueObject {

  /** SKU フォーマットパターン: SKU- + 28桁数字。 */
  private static final Pattern SKU_PATTERN = Pattern.compile("^SKU-\\d{28}$");

  /** Sku を生成する。 */
  public Sku {
    Objects.requireNonNull(value, "SKU value must not be null");
    if (!SKU_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "SKU must match format 'SKU-' followed by exactly 28 digits");
    }
  }
}
