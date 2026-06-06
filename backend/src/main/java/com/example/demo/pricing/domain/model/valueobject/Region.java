package com.example.demo.pricing.domain.model.valueobject;

import lombok.Getter;
import org.jmolecules.ddd.types.ValueObject;

/** 地方区分（8地方）。 */
@Getter
public enum Region implements ValueObject {

  /** 北海道。 */
  HOKKAIDO("hokkaido"),

  /** 東北。 */
  TOHOKU("tohoku"),

  /** 関東。 */
  KANTO("kanto"),

  /** 中部。 */
  CHUBU("chubu"),

  /** 近畿。 */
  KINKI("kinki"),

  /** 中国。 */
  CHUGOKU("chugoku"),

  /** 四国。 */
  SHIKOKU("shikoku"),

  /** 九州・沖縄。 */
  KYUSHU_OKINAWA("kyushu_okinawa");

  /** エリアコード。 */
  private final String code;

  Region(final String code) {
    this.code = code;
  }

  /**
   * コードから Region を取得する。
   *
   * @param code エリアコード
   * @return 対応する Region
   */
  public static Region fromCode(final String code) {
    for (final Region region : values()) {
      if (region.code.equals(code)) {
        return region;
      }
    }
    throw new IllegalArgumentException("Unknown region code: " + code);
  }
}
