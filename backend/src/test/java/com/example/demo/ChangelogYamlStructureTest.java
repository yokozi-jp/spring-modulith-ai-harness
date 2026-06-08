package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Changelog YAML 構造検証テスト。
 *
 * <p>changeset YAML ファイルが liquibase-rules.md の規約に沿った構造で記述されているかを検証する。 DB 不要の unit test。
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
class ChangelogYamlStructureTest {

  /** マイグレーションディレクトリのパス。 */
  private static final Path MIGRATIONS_DIR =
      Paths.get("src/main/resources/db/changelog/migrations");

  /** ファイル名パターン: NNN-<説明>.yaml */
  private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^\\d{3}-.+\\.yaml$");

  /** 許可されたカラム型。 */
  private static final Set<String> ALLOWED_TYPES =
      Set.of("uuid", "text", "int", "bigint", "timestamptz", "boolean", "numeric(12,4)");

  /** マイグレーションファイルが存在すること。 */
  @Test
  void shouldHaveMigrationFiles() throws IOException {
    try (Stream<Path> files = Files.list(MIGRATIONS_DIR)) {
      final long count = files.filter(p -> p.toString().endsWith(".yaml")).count();
      assertTrue(count > 0, "at least one migration file should exist");
    }
  }

  /** ファイル名が NNN-<説明>.yaml 形式であること。 */
  @Test
  void shouldHaveValidFileNames() throws IOException {
    for (final Path file : listMigrationFiles()) {
      final String fileName = Objects.requireNonNull(file.getFileName(), "fileName").toString();
      assertTrue(
          FILE_NAME_PATTERN.matcher(fileName).matches(),
          "file name should match NNN-<description>.yaml: " + fileName);
    }
  }

  /** changeset ID がファイル名（拡張子なし）と一致すること。 */
  @Test
  void shouldHaveIdMatchingFileName() throws IOException {
    for (final Path file : listMigrationFiles()) {
      final String expectedId =
          Objects.requireNonNull(file.getFileName(), "fileName").toString().replace(".yaml", "");
      final Map<String, Object> changeSet = loadChangeSet(file);
      assertEquals(
          expectedId,
          changeSet.get("id"),
          "changeset id should match file name for: " + file.getFileName());
    }
  }

  /** author が system であること。 */
  @Test
  void shouldHaveSystemAuthor() throws IOException {
    for (final Path file : listMigrationFiles()) {
      final Map<String, Object> changeSet = loadChangeSet(file);
      assertEquals(
          "system",
          changeSet.get("author"),
          "author should be 'system' for: " + file.getFileName());
    }
  }

  /** 1 ファイル 1 changeset であること。 */
  @Test
  void shouldHaveSingleChangeSetPerFile() throws IOException {
    for (final Path file : listMigrationFiles()) {
      final List<Map<String, Object>> entries = loadChangeLog(file);
      final long changeSetCount =
          entries.stream().filter(entry -> entry.containsKey("changeSet")).count();
      assertEquals(
          1, changeSetCount, "should have exactly 1 changeSet per file: " + file.getFileName());
    }
  }

  /** sql change type 使用時に rollback が存在すること。 */
  @Test
  void shouldHaveRollbackWhenUsingSql() throws IOException {
    for (final Path file : listMigrationFiles()) {
      final Map<String, Object> changeSet = loadChangeSet(file);
      @SuppressWarnings("unchecked")
      final List<Map<String, Object>> changes =
          (List<Map<String, Object>>) changeSet.get("changes");
      if (changes == null) {
        continue;
      }
      final boolean hasSql = changes.stream().anyMatch(c -> c.containsKey("sql"));
      if (hasSql) {
        assertNotNull(
            changeSet.get("rollback"),
            "rollback is required when using sql change type: " + file.getFileName());
      }
    }
  }

  /** カラム型が許可リストに含まれること。 */
  @Test
  void shouldUseAllowedColumnTypes() throws IOException {
    final List<Path> files = listMigrationFiles();
    assertFalse(files.isEmpty(), "migration files should exist");
    for (final Path file : files) {
      final Map<String, Object> changeSet = loadChangeSet(file);
      @SuppressWarnings("unchecked")
      final List<Map<String, Object>> changes =
          (List<Map<String, Object>>) changeSet.get("changes");
      if (changes == null) {
        continue;
      }
      for (final Map<String, Object> change : changes) {
        if (change.containsKey("createTable")) {
          @SuppressWarnings("unchecked")
          final Map<String, Object> createTable = (Map<String, Object>) change.get("createTable");
          validateColumnTypes(createTable, file);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void validateColumnTypes(final Map<String, Object> createTable, final Path file) {
    final List<Map<String, Object>> columns =
        (List<Map<String, Object>>) createTable.get("columns");
    if (columns == null) {
      return;
    }
    for (final Map<String, Object> columnWrapper : columns) {
      final Map<String, Object> column = (Map<String, Object>) columnWrapper.get("column");
      if (column == null) {
        continue;
      }
      final String type = (String) column.get("type");
      if (type != null) {
        assertFalse(type.isBlank(), "column type should not be blank in: " + file.getFileName());
        assertTrue(
            ALLOWED_TYPES.contains(type.toLowerCase(Locale.ROOT)),
            "column type '"
                + type
                + "' is not in allowed list "
                + ALLOWED_TYPES
                + " in: "
                + file.getFileName());
      }
    }
  }

  private List<Path> listMigrationFiles() throws IOException {
    try (Stream<Path> files = Files.list(MIGRATIONS_DIR)) {
      return files.filter(p -> p.toString().endsWith(".yaml")).sorted().toList();
    }
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> loadChangeLog(final Path file) throws IOException {
    try (InputStream inputStream = Files.newInputStream(file)) {
      final Yaml yaml = new Yaml();
      final Map<String, Object> root = yaml.load(inputStream);
      return (List<Map<String, Object>>) root.get("databaseChangeLog");
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> loadChangeSet(final Path file) throws IOException {
    final List<Map<String, Object>> entries = loadChangeLog(file);
    for (final Map<String, Object> entry : entries) {
      if (entry.containsKey("changeSet")) {
        return (Map<String, Object>) entry.get("changeSet");
      }
    }
    throw new IllegalStateException("No changeSet found in: " + file.getFileName());
  }
}
