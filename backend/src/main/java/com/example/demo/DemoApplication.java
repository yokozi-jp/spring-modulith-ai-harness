package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot アプリケーションのエントリーポイント */
@SuppressWarnings({"PMD.UseUtilityClass", "PMD.MethodArgumentCouldBeFinal"})
@SpringBootApplication
public class DemoApplication {

  /** アプリケーションを起動する */
  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }
}
