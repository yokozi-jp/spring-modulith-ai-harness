package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * DB スキーマ規約検証テスト。
 *
 * <p>適用済みスキーマのテーブル名、必須カラム、インデックス名が liquibase-rules.md の規約に従っているかを検証する。
 */
@Tag("integration")
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
@SuppressWarnings("PMD.TooManyMethods")
class DatabaseSchemaConventionTest {

  /** データソース。 */
  private final DataSource dataSource;

  /** 規約対象外のテーブル。 */
  private static final Set<String> EXCLUDED_TABLES =
      Set.of(
          "event_publication",
          "event_publication_archive",
          "databasechangelog",
          "databasechangeloglock");

  /** スネークケースパターン。 */
  private static final Pattern SNAKE_CASE = Pattern.compile("^[a-z][a-z0-9]*(_[a-z0-9]+)*$");

  /** ColumnInfo 用 RowMapper。 */
  private static final RowMapper<ColumnInfo> COLUMN_INFO_ROW_MAPPER =
      (rs, rowNum) -> new ColumnInfo(rs.getString("data_type"), rs.getString("is_nullable"));

  /** スキーマ名。 */
  private static final String SCHEMA = "demo";

  // --- テーブル命名規約 ---

  /** テーブル名が小文字スネークケースであること。 */
  @Test
  void shouldHaveSnakeCaseTableNames() {
    final List<String> tables = getApplicationTables();
    for (final String table : tables) {
      assertTrue(SNAKE_CASE.matcher(table).matches(), "table name should be snake_case: " + table);
    }
  }

  /** テーブル名が複数形であること。 */
  @Test
  void shouldHavePluralTableNames() {
    final List<String> tables = getApplicationTables();
    for (final String table : tables) {
      assertTrue(table.endsWith("s"), "table name should be plural (end with 's'): " + table);
    }
  }

  // --- 必須カラム ---

  /** 全テーブルに created_at カラムが存在し型が正しいこと。 */
  @Test
  void shouldHaveCreatedAtColumn() {
    assertRequiredColumn("created_at", "timestamp with time zone", true);
  }

  /** 全テーブルに updated_at カラムが存在し型が正しいこと。 */
  @Test
  void shouldHaveUpdatedAtColumn() {
    assertRequiredColumn("updated_at", "timestamp with time zone", true);
  }

  /** 全テーブルに created_by カラムが存在し型が正しいこと。 */
  @Test
  void shouldHaveCreatedByColumn() {
    assertRequiredColumn("created_by", "text", true);
  }

  /** 全テーブルに updated_by カラムが存在し型が正しいこと。 */
  @Test
  void shouldHaveUpdatedByColumn() {
    assertRequiredColumn("updated_by", "text", true);
  }

  /** 全テーブルに version カラムが存在し型が正しいこと。 */
  @Test
  void shouldHaveVersionColumn() {
    assertRequiredColumn("version", "integer", true);
  }

  /** 全テーブルに deleted_at カラムが存在し nullable であること。 */
  @Test
  void shouldHaveDeletedAtColumn() {
    assertRequiredColumn("deleted_at", "timestamp with time zone", false);
  }

  // --- インデックス命名規約 ---

  /** インデックス名がテーブル名で始まること。 */
  @Test
  void shouldHaveIndexNamesStartingWithTableName() {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    final List<IndexInfo> indexes = getApplicationIndexes(jdbc);
    for (final IndexInfo idx : indexes) {
      assertTrue(
          idx.indexName().startsWith(idx.tableName() + "_"),
          "index '"
              + idx.indexName()
              + "' should start with table name '"
              + idx.tableName()
              + "_'");
    }
  }

  /** インデックス名が _idx または _key で終わること。 */
  @Test
  void shouldHaveIndexNamesWithProperSuffix() {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    final List<IndexInfo> indexes = getApplicationIndexes(jdbc);
    for (final IndexInfo idx : indexes) {
      assertTrue(
          idx.indexName().endsWith("_idx") || idx.indexName().endsWith("_key"),
          "index '" + idx.indexName() + "' should end with '_idx' or '_key'");
    }
  }

  /** インデックス名が小文字スネークケースであること。 */
  @Test
  void shouldHaveSnakeCaseIndexNames() {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    final List<IndexInfo> indexes = getApplicationIndexes(jdbc);
    for (final IndexInfo idx : indexes) {
      assertTrue(
          SNAKE_CASE.matcher(idx.indexName()).matches(),
          "index name should be snake_case: " + idx.indexName());
    }
  }

  // --- ヘルパーメソッド ---

  private List<String> getApplicationTables() {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    return jdbc
        .queryForList(
            "SELECT table_name FROM information_schema.tables"
                + " WHERE table_schema = ? AND table_type = 'BASE TABLE'",
            String.class,
            SCHEMA)
        .stream()
        .filter(t -> !EXCLUDED_TABLES.contains(t))
        .toList();
  }

  private void assertRequiredColumn(
      final String columnName, final String expectedType, final boolean notNull) {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    final List<String> tables = getApplicationTables();
    for (final String table : tables) {
      final List<ColumnInfo> columns =
          jdbc.query(
              "SELECT data_type, is_nullable FROM information_schema.columns"
                  + " WHERE table_schema = ? AND table_name = ? AND column_name = ?",
              COLUMN_INFO_ROW_MAPPER,
              SCHEMA,
              table,
              columnName);

      assertFalse(
          columns.isEmpty(), "table '" + table + "' should have column '" + columnName + "'");

      final ColumnInfo col = columns.getFirst();
      assertTrue(
          col.dataType().equals(expectedType),
          "column '"
              + columnName
              + "' in table '"
              + table
              + "' should have type '"
              + expectedType
              + "' but was '"
              + col.dataType()
              + "'");

      if (notNull) {
        assertTrue(
            "NO".equals(col.isNullable()),
            "column '" + columnName + "' in table '" + table + "' should be NOT NULL");
      } else {
        assertTrue(
            "YES".equals(col.isNullable()),
            "column '" + columnName + "' in table '" + table + "' should be nullable");
      }
    }
  }

  private List<IndexInfo> getApplicationIndexes(final JdbcTemplate jdbc) {
    return jdbc
        .query(
            "SELECT tablename, indexname FROM pg_indexes"
                + " WHERE schemaname = ?"
                + " AND NOT indexname LIKE '%_pkey'",
            (rs, rowNum) -> new IndexInfo(rs.getString("tablename"), rs.getString("indexname")),
            SCHEMA)
        .stream()
        .filter(idx -> !EXCLUDED_TABLES.contains(idx.tableName()))
        .toList();
  }

  private record ColumnInfo(String dataType, String isNullable) {}

  private record IndexInfo(String tableName, String indexName) {}
}
