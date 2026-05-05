package com.example.demo;

import com.example.demo.testconfig.FullStackContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/** アプリケーションコンテキストの統合テスト */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@Import(FullStackContainerConfig.class)
@SpringBootTest
class DemoApplicationTests {

  @Test
  void contextLoads() {
    // Spring アプリケーションコンテキストが正常にロードされることを確認する
  }
}
