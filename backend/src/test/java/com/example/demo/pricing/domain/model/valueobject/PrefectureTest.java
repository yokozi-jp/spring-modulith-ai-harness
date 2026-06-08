package com.example.demo.pricing.domain.model.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link Prefecture} enum. */
class PrefectureTest {

  @Test
  void shouldHaveFortySevenPrefectures() {
    assertEquals(47, Prefecture.values().length, "should have 47 prefectures");
  }

  @Test
  void shouldReturnCodeForTokyo() {
    assertEquals("13", Prefecture.TOKYO.getCode(), "TOKYO code should be 13");
  }

  @Test
  void shouldReturnRegionForTokyo() {
    assertEquals(Region.KANTO, Prefecture.TOKYO.getRegion(), "TOKYO should belong to KANTO");
  }

  @Test
  void shouldReturnRegionForOkinawa() {
    assertEquals(
        Region.KYUSHU_OKINAWA,
        Prefecture.OKINAWA.getRegion(),
        "OKINAWA should belong to KYUSHU_OKINAWA");
  }

  @Test
  void shouldResolveFromCode() {
    assertEquals(Prefecture.OSAKA, Prefecture.fromCode("27"), "should resolve OSAKA from code 27");
  }

  @Test
  void shouldThrowOnUnknownCode() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Prefecture.fromCode("99"),
        "should throw on unknown code");
  }

  @Test
  void shouldReturnCodeForHokkaido() {
    assertEquals("01", Prefecture.HOKKAIDO.getCode(), "HOKKAIDO code should be 01");
  }

  @Test
  void shouldReturnRegionForHokkaido() {
    assertEquals(
        Region.HOKKAIDO, Prefecture.HOKKAIDO.getRegion(), "HOKKAIDO should belong to HOKKAIDO");
  }
}
