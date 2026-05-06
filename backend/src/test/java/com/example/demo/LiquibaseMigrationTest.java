package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.testconfig.PostgresContainerConfig;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Liquibase マイグレーションテスト。
 *
 * <p>全 changeset が正常に適用されること、および期待するテーブルが存在することを検証する。 Spring Boot 起動時に Liquibase
 * が自動適用するため、コンテキストが正常にロードされれば適用成功。
 */
@Tag("integration")
@Import(PostgresContainerConfig.class)
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class LiquibaseMigrationTest {

  /** データソース。 */
  private final DataSource dataSource;

  /** Liquibase が正常に適用されていること（コンテキスト起動で検証）。 */
  @Test
  void shouldApplyAllMigrations() {
    assertNotNull(dataSource, "dataSource should be injected (all migrations applied)");
  }

  /** event_publication テーブルが存在すること。 */
  @Test
  void shouldHaveEventPublicationTable() {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    final Integer count =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables"
                + " WHERE table_schema = 'demo' AND table_name = 'event_publication'",
            Integer.class);

    assertNotNull(count, "query result should not be null");
    assert count > 0 : "event_publication table should exist";
  }

  /** databasechangelog テーブルに changeset が記録されていること。 */
  @Test
  void shouldHaveChangeLogEntries() {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    final Integer count =
        jdbc.queryForObject("SELECT COUNT(*) FROM demo.databasechangelog", Integer.class);

    assertNotNull(count, "query result should not be null");
    assert count > 0 : "should have at least one changeset applied";
  }
}
