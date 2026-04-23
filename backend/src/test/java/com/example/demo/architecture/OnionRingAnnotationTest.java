package com.example.demo.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * 各モジュールのサブパッケージに正しい jMolecules Onion Architecture アノテーションが付与されていることを検証する。
 *
 * <p>パッケージ名と期待するアノテーションの対応:
 *
 * <ul>
 *   <li>{@code event/} → {@code @DomainModelRing}
 *   <li>{@code domain/} → {@code @DomainModelRing}
 *   <li>{@code domain/model/} → {@code @DomainModelRing}
 *   <li>{@code domain/model/aggregate/} → {@code @DomainModelRing}
 *   <li>{@code domain/model/entity/} → {@code @DomainModelRing}
 *   <li>{@code domain/model/valueobject/} → {@code @DomainModelRing}
 *   <li>{@code domain/model/valueobject/identifier/} → {@code @DomainModelRing}
 *   <li>{@code domain/repository/} → {@code @DomainModelRing}
 *   <li>{@code domain/service/} → {@code @DomainServiceRing}
 *   <li>{@code application/} → {@code @ApplicationServiceRing}
 *   <li>{@code application/command/} → {@code @ApplicationServiceRing}
 *   <li>{@code application/command/dto/} → {@code @ApplicationServiceRing}
 *   <li>{@code application/command/handler/} → {@code @ApplicationServiceRing}
 *   <li>{@code application/query/} → {@code @ApplicationServiceRing}
 *   <li>{@code application/query/dto/} → {@code @ApplicationServiceRing}
 *   <li>{@code application/query/service/} → {@code @ApplicationServiceRing}
 *   <li>{@code presentation/} → {@code @InfrastructureRing}
 *   <li>{@code presentation/controller/} → {@code @InfrastructureRing}
 *   <li>{@code presentation/request/} → {@code @InfrastructureRing}
 *   <li>{@code presentation/response/} → {@code @InfrastructureRing}
 *   <li>{@code infrastructure/} → {@code @InfrastructureRing}
 *   <li>{@code infrastructure/db/} → {@code @InfrastructureRing}
 *   <li>{@code infrastructure/db/repository/} → {@code @InfrastructureRing}
 *   <li>{@code infrastructure/db/query/} → {@code @InfrastructureRing}
 * </ul>
 */
class OnionRingAnnotationTest {

  /** ソースルートパス。 */
  private static final Path SRC_ROOT = Path.of("src/main/java/com/example/demo");

  /** Onion アノテーション名の定数。 */
  private static final String DOMAIN_MODEL = "@DomainModelRing";

  /** Onion アノテーション名の定数。 */
  private static final String DOMAIN_SVC = "@DomainServiceRing";

  /** Onion アノテーション名の定数。 */
  private static final String APP_SVC = "@ApplicationServiceRing";

  /** Onion アノテーション名の定数。 */
  private static final String INFRA = "@InfrastructureRing";

  /** パッケージ末尾名と期待する Onion アノテーションの対応。順序は具体的なパスから先にマッチさせる。 */
  private static final List<Map.Entry<String, String>> LAYER_RULES =
      List.of(
          Map.entry("event", DOMAIN_MODEL),
          Map.entry("domain/model/aggregate", DOMAIN_MODEL),
          Map.entry("domain/model/entity", DOMAIN_MODEL),
          Map.entry("domain/model/valueobject/identifier", DOMAIN_MODEL),
          Map.entry("domain/model/valueobject", DOMAIN_MODEL),
          Map.entry("domain/model", DOMAIN_MODEL),
          Map.entry("domain/repository", DOMAIN_MODEL),
          Map.entry("domain/service", DOMAIN_SVC),
          Map.entry("domain", DOMAIN_MODEL),
          Map.entry("application/command/dto", APP_SVC),
          Map.entry("application/command/handler", APP_SVC),
          Map.entry("application/command", APP_SVC),
          Map.entry("application/query/dto", APP_SVC),
          Map.entry("application/query/service", APP_SVC),
          Map.entry("application/query", APP_SVC),
          Map.entry("application", APP_SVC),
          Map.entry("presentation/controller", INFRA),
          Map.entry("presentation/request", INFRA),
          Map.entry("presentation/response", INFRA),
          Map.entry("presentation", INFRA),
          Map.entry("infrastructure/db/repository", INFRA),
          Map.entry("infrastructure/db/query", INFRA),
          Map.entry("infrastructure/db", INFRA),
          Map.entry("infrastructure", INFRA));

  @Test
  void allLayerPackagesShouldHaveCorrectOnionAnnotation() throws IOException {
    final List<String> violations = new ArrayList<>();

    try (Stream<Path> dirs = Files.walk(SRC_ROOT)) {
      dirs.filter(Files::isDirectory)
          .forEach(
              dir -> {
                final String relativePath = SRC_ROOT.relativize(dir).toString().replace('\\', '/');
                for (final Map.Entry<String, String> rule : LAYER_RULES) {
                  if (relativePath.endsWith(rule.getKey())) {
                    checkAnnotation(dir, rule.getValue(), violations);
                    break;
                  }
                }
              });
    }

    assertThat(violations)
        .as("Onion Architecture annotation violations in package-info.java")
        .isEmpty();
  }

  private void checkAnnotation(
      final Path dir, final String expected, final List<String> violations) {
    final Path packageInfo = dir.resolve("package-info.java");
    if (!Files.exists(packageInfo)) {
      violations.add(dir + " — package-info.java が存在しない（期待: " + expected + "）");
      return;
    }
    try {
      final String content = Files.readString(packageInfo);
      if (!content.contains(expected)) {
        violations.add(dir + " — " + expected + " が見つからない");
      }
    } catch (IOException exception) {
      violations.add(dir + " — package-info.java の読み取りに失敗");
    }
  }
}
