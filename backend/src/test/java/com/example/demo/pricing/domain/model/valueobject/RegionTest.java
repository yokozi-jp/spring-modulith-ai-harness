package com.example.demo.pricing.domain.model.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link Region} enum. */
class RegionTest {

  @Test
  void shouldHaveEightRegions() {
    assertEquals(8, Region.values().length, "should have 8 regions");
  }

  @Test
  void shouldReturnCodeForHokkaido() {
    assertEquals("hokkaido", Region.HOKKAIDO.getCode(), "HOKKAIDO code should match");
  }

  @Test
  void shouldReturnCodeForKyushuOkinawa() {
    assertEquals(
        "kyushu_okinawa", Region.KYUSHU_OKINAWA.getCode(), "KYUSHU_OKINAWA code should match");
  }

  @Test
  void shouldResolveFromCode() {
    assertEquals(Region.KANTO, Region.fromCode("kanto"), "should resolve KANTO from code");
  }

  @Test
  void shouldThrowOnUnknownCode() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Region.fromCode("unknown"),
        "should throw on unknown code");
  }
}
