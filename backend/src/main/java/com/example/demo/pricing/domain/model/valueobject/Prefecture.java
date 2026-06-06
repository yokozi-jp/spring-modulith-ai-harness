package com.example.demo.pricing.domain.model.valueobject;

import lombok.Getter;
import org.jmolecules.ddd.types.ValueObject;

/** 都道府県（47都道府県）。 */
@SuppressWarnings("PMD.ExcessivePublicCount")
@Getter
public enum Prefecture implements ValueObject {

  /** 北海道。 */
  HOKKAIDO("01", Region.HOKKAIDO),

  /** 青森県。 */
  AOMORI("02", Region.TOHOKU),

  /** 岩手県。 */
  IWATE("03", Region.TOHOKU),

  /** 宮城県。 */
  MIYAGI("04", Region.TOHOKU),

  /** 秋田県。 */
  AKITA("05", Region.TOHOKU),

  /** 山形県。 */
  YAMAGATA("06", Region.TOHOKU),

  /** 福島県。 */
  FUKUSHIMA("07", Region.TOHOKU),

  /** 茨城県。 */
  IBARAKI("08", Region.KANTO),

  /** 栃木県。 */
  TOCHIGI("09", Region.KANTO),

  /** 群馬県。 */
  GUNMA("10", Region.KANTO),

  /** 埼玉県。 */
  SAITAMA("11", Region.KANTO),

  /** 千葉県。 */
  CHIBA("12", Region.KANTO),

  /** 東京都。 */
  TOKYO("13", Region.KANTO),

  /** 神奈川県。 */
  KANAGAWA("14", Region.KANTO),

  /** 新潟県。 */
  NIIGATA("15", Region.CHUBU),

  /** 富山県。 */
  TOYAMA("16", Region.CHUBU),

  /** 石川県。 */
  ISHIKAWA("17", Region.CHUBU),

  /** 福井県。 */
  FUKUI("18", Region.CHUBU),

  /** 山梨県。 */
  YAMANASHI("19", Region.CHUBU),

  /** 長野県。 */
  NAGANO("20", Region.CHUBU),

  /** 岐阜県。 */
  GIFU("21", Region.CHUBU),

  /** 静岡県。 */
  SHIZUOKA("22", Region.CHUBU),

  /** 愛知県。 */
  AICHI("23", Region.CHUBU),

  /** 三重県。 */
  MIE("24", Region.KINKI),

  /** 滋賀県。 */
  SHIGA("25", Region.KINKI),

  /** 京都府。 */
  KYOTO("26", Region.KINKI),

  /** 大阪府。 */
  OSAKA("27", Region.KINKI),

  /** 兵庫県。 */
  HYOGO("28", Region.KINKI),

  /** 奈良県。 */
  NARA("29", Region.KINKI),

  /** 和歌山県。 */
  WAKAYAMA("30", Region.KINKI),

  /** 鳥取県。 */
  TOTTORI("31", Region.CHUGOKU),

  /** 島根県。 */
  SHIMANE("32", Region.CHUGOKU),

  /** 岡山県。 */
  OKAYAMA("33", Region.CHUGOKU),

  /** 広島県。 */
  HIROSHIMA("34", Region.CHUGOKU),

  /** 山口県。 */
  YAMAGUCHI("35", Region.CHUGOKU),

  /** 徳島県。 */
  TOKUSHIMA("36", Region.SHIKOKU),

  /** 香川県。 */
  KAGAWA("37", Region.SHIKOKU),

  /** 愛媛県。 */
  EHIME("38", Region.SHIKOKU),

  /** 高知県。 */
  KOCHI("39", Region.SHIKOKU),

  /** 福岡県。 */
  FUKUOKA("40", Region.KYUSHU_OKINAWA),

  /** 佐賀県。 */
  SAGA("41", Region.KYUSHU_OKINAWA),

  /** 長崎県。 */
  NAGASAKI("42", Region.KYUSHU_OKINAWA),

  /** 熊本県。 */
  KUMAMOTO("43", Region.KYUSHU_OKINAWA),

  /** 大分県。 */
  OITA("44", Region.KYUSHU_OKINAWA),

  /** 宮崎県。 */
  MIYAZAKI("45", Region.KYUSHU_OKINAWA),

  /** 鹿児島県。 */
  KAGOSHIMA("46", Region.KYUSHU_OKINAWA),

  /** 沖縄県。 */
  OKINAWA("47", Region.KYUSHU_OKINAWA);

  /** 都道府県コード。 */
  private final String code;

  /** 所属する地方。 */
  private final Region region;

  Prefecture(final String code, final Region region) {
    this.code = code;
    this.region = region;
  }

  /**
   * コードから Prefecture を取得する。
   *
   * @param code 都道府県コード
   * @return 対応する Prefecture
   */
  public static Prefecture fromCode(final String code) {
    for (final Prefecture prefecture : values()) {
      if (prefecture.code.equals(code)) {
        return prefecture;
      }
    }
    throw new IllegalArgumentException("Unknown prefecture code: " + code);
  }
}
