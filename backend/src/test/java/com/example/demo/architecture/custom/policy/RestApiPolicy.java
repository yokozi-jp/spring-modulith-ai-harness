package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * REST API レスポンス規約ポリシー。
 *
 * <p>HTTP メソッドと戻り値型の整合性を検証する。
 */
public final class RestApiPolicy {

  /** {@code @PostMapping} メソッドは {@code ResponseEntity} を返すべき。 */
  @ArchTest
  /* default */ static final ArchRule POST_RETURNS_RESPONSE_ENTITY =
      methods()
          .that()
          .areAnnotatedWith(PostMapping.class)
          .should()
          .haveRawReturnType(ResponseEntity.class)
          .as("@PostMapping メソッドは ResponseEntity を返すべき（201 + Location ヘッダー）")
          .because(
              "REST 規約: POST は ResponseEntity<Void> で 201 Created + Location ヘッダーを返してください")
          .allowEmptyShould(true);

  /** {@code @DeleteMapping} メソッドは {@code ResponseEntity} を返すべき。 */
  @ArchTest
  /* default */ static final ArchRule DELETE_RETURNS_RESPONSE_ENTITY =
      methods()
          .that()
          .areAnnotatedWith(DeleteMapping.class)
          .should()
          .haveRawReturnType(ResponseEntity.class)
          .as("@DeleteMapping メソッドは ResponseEntity を返すべき（204 No Content）")
          .because(
              "REST 規約: DELETE は ResponseEntity<Void> で 204 No Content を返してください")
          .allowEmptyShould(true);

  private RestApiPolicy() {}
}
