package com.example.demo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** アプリケーションコンテキストの統合テスト */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@Tag("e2e")
@SpringBootTest
class DemoApplicationTests {

  @Test
  void contextLoads() {
    // Spring アプリケーションコンテキストが正常にロードされることを確認する
  }
}
