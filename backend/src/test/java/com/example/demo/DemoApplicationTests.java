package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/** アプリケーションコンテキストの統合テスト */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DemoApplicationTests {

  @Test
  void contextLoads() {
    // Spring アプリケーションコンテキストが正常にロードされることを確認する
  }
}
