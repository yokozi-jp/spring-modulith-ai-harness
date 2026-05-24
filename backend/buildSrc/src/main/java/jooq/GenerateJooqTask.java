package jooq;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;

/**
 * jOOQ コード生成用 Gradle タスク。
 *
 * <p>
 * 開発用 PostgreSQL（smah-postgres）に一時スキーマを作成し、
 * Liquibase マイグレーション → jOOQ codegen → スキーマ DROP の手順で
 * 型安全なデータベースアクセスコードを自動生成する。
 *
 * <p>
 * 使い方: {@code make jooq}（開発コンテナ起動中に実行）
 */
public abstract class GenerateJooqTask extends DefaultTask {

    /** codegen 専用の一時スキーマ名。 */
    private static final String CODEGEN_SCHEMA = "jooq_codegen";

    /** コンストラクタ。プロジェクト規約に基づくデフォルト値を設定する。 */
    @javax.inject.Inject
    public GenerateJooqTask() {
        getJdbcUrl().convention("jdbc:postgresql://postgres:5432/demo");
        getUsername().convention("demo");
        getPassword().convention("demo");
        getLiquibaseSearchPath().convention("src/main/resources");
        getLiquibaseChangelog().convention("db/changelog/db.changelog-master.yaml");
        getPackageName().convention("com.example.demo.jooq");
        getInputSchema().convention("demo");
    }

    // ========================================================================
    // 入力プロパティ
    // ========================================================================

    /** PostgreSQL の JDBC URL。 */
    @Input
    public abstract Property<String> getJdbcUrl();

    /** PostgreSQL の接続ユーザー名。 */
    @Input
    public abstract Property<String> getUsername();

    /** PostgreSQL の接続パスワード。 */
    @Input
    public abstract Property<String> getPassword();

    /** Liquibase の searchPath（changelog からの相対パス解決に使用）。 */
    @Input
    public abstract Property<String> getLiquibaseSearchPath();

    /** Liquibase のマスター changelog ファイルパス（searchPath からの相対）。 */
    @Input
    public abstract Property<String> getLiquibaseChangelog();

    /** 生成する jOOQ クラスのパッケージ名（例: {@code com.example.demo.jooq}）。 */
    @Input
    public abstract Property<String> getPackageName();

    /** jOOQ codegen が生成するコードのスキーマ名（生成コード内の参照名）。 */
    @Input
    public abstract Property<String> getInputSchema();

    // ========================================================================
    // 出力プロパティ
    // ========================================================================

    /** jOOQ 生成コードの出力先ディレクトリ。 */
    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    // ========================================================================
    // タスク実行
    // ========================================================================

    /**
     * タスクのメインエントリポイント。
     *
     * <p>
     * 一時スキーマを作成→マイグレーション→codegen→DROP する。
     * 失敗時も確実にスキーマを DROP する。
     */
    @TaskAction
    public void generate() throws Exception {
        String jdbcUrl = getJdbcUrl().get();
        String user = getUsername().get();
        String pass = getPassword().get();

        try {
            createSchema(jdbcUrl, user, pass);
            runLiquibase(jdbcUrl, user, pass);
            runJooq(jdbcUrl, user, pass);
        } finally {
            dropSchema(jdbcUrl, user, pass);
        }
    }

    private void createSchema(String jdbcUrl, String username, String password) throws Exception {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + CODEGEN_SCHEMA + " CASCADE");
            stmt.execute("CREATE SCHEMA " + CODEGEN_SCHEMA);
        }
    }

    private void dropSchema(String jdbcUrl, String username, String password) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + CODEGEN_SCHEMA + " CASCADE");
        } catch (Exception e) {
            getLogger().warn("Failed to drop codegen schema: " + e.getMessage());
        }
    }

    private void runLiquibase(String jdbcUrl, String username, String password) throws Exception {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultSchemaName(CODEGEN_SCHEMA);
            database.setLiquibaseSchemaName(CODEGEN_SCHEMA);

            Path searchPath = getProject().file(getLiquibaseSearchPath().get()).toPath();

            try (Liquibase liquibase = new Liquibase(
                    getLiquibaseChangelog().get(),
                    new DirectoryResourceAccessor(searchPath),
                    database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }
    }

    private void runJooq(String jdbcUrl, String username, String password) throws Exception {
        File targetDir = getOutputDirectory().get().getAsFile();

        deleteDirectory(targetDir);
        targetDir.mkdirs();

        // codegen は jooq_codegen スキーマを読むが、生成コード内のスキーマ参照は
        // inputSchema（demo）にする。outputSchemaToDefault で実行時のデフォルトスキーマを使う。
        GenerationTool.generate(
                new Configuration()
                        .withJdbc(new Jdbc()
                                .withDriver("org.postgresql.Driver")
                                .withUrl(jdbcUrl)
                                .withUser(username)
                                .withPassword(password))
                        .withGenerator(new Generator()
                                .withName("org.jooq.codegen.JavaGenerator")
                                .withDatabase(new org.jooq.meta.jaxb.Database()
                                        .withName("org.jooq.meta.postgres.PostgresDatabase")
                                        .withInputSchema(CODEGEN_SCHEMA)
                                        .withOutputSchemaToDefault(true)
                                        .withExcludes("databasechangelog|databasechangeloglock"))
                                .withGenerate(new Generate()
                                        .withDeprecated(false)
                                        .withRecords(true)
                                        .withPojos(false)
                                        .withFluentSetters(false)
                                        .withJavaTimeTypes(true))
                                .withTarget(new Target()
                                        .withPackageName(getPackageName().get())
                                        .withDirectory(targetDir.getAbsolutePath()))));
    }

    private void deleteDirectory(File directory) throws Exception {
        if (!directory.exists()) {
            return;
        }

        java.nio.file.Files.walk(directory.toPath())
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        java.nio.file.Files.delete(path);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
