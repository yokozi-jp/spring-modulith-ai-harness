package com.example.demo.pricing.domain.model.valueobject;

import org.jmolecules.ddd.types.ValueObject;

/** 価格レベル。地方単位か都道府県単位かを表す。 */
public enum PricingLevel implements ValueObject {

  /** 地方単位。 */
  REGION,

  /** 都道府県単位。 */
  PREFECTURE;
}
