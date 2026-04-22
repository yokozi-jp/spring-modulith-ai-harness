package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/** Spring Boot アプリケーションのエントリーポイント */
@SuppressWarnings("PMD.UseUtilityClass")
@Modulithic(systemName = "Demo AI Harness")
@SpringBootApplication
public class DemoApplication {

  /** アプリケーションを起動する */
  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }
}
