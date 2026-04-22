package com.example.demo;

import org.springframework.boot.SpringApplication;

/** Testcontainers を使用してローカル開発用にアプリケーションを起動する */
@SuppressWarnings({"PMD.UseUtilityClass", "PMD.TestClassWithoutTestCases"})
public class TestDemoApplication {

  /** テストコンテナ付きでアプリケーションを起動する */
  public static void main(String[] args) {
    SpringApplication.from(DemoApplication::main).with(TestcontainersConfiguration.class).run(args);
  }
}
