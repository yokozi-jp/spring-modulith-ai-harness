package com.example.demo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** アプリケーションコンテキストの統合テスト */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@Tag("integration")
@SpringBootTest
@Transactional
class DemoApplicationTests {

  @Test
  void contextLoads() {
    // Spring アプリケーションコンテキストが正常にロードされることを確認する
  }
}
