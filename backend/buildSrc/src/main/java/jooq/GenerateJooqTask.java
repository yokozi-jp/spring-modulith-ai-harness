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
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * jOOQ コード生成用 Gradle タスク。
 *
 * <p>
 * 以下の手順で型安全なデータベースアクセスコードを自動生成する:
 *
 * <ol>
 * <li>Testcontainers で一時的な PostgreSQL コンテナを起動
 * <li>Liquibase で最新のマイグレーションを適用し、スキーマを構築
 * <li>jOOQ codegen が実際のスキーマを読み取り、Java ソースを生成
 * <li>コンテナを破棄（使い捨て）
 * </ol>
 *
 * <p>
 * この方式により、開発者のローカル DB や CI 環境に依存せず、
 * 常にマイグレーション定義と一致した jOOQ コードが生成される。
 *
 * <p>
 * 使い方: {@code ./gradlew generateJooq}（{@code compileJava} から自動実行される）
 */
public abstract class GenerateJooqTask extends DefaultTask {

    /** コンストラクタ。プロジェクト規約に基づくデフォルト値を設定する。 */
    @javax.inject.Inject
    public GenerateJooqTask() {
        getPostgresImage().convention("postgres:18.3");
        getDatabaseName().convention("jooq-codegen");
        getUsername().convention("jooq-codegen");
        getPassword().convention("jooq-codegen");
        getLiquibaseSearchPath().convention("src/main/resources");
        getLiquibaseChangelog().convention("db/changelog/db.changelog-master.yaml");
        getPackageName().convention("com.example.demo.jooq");
        getInputSchema().convention("demo");
    }

    // ========================================================================
    // 入力プロパティ — convention デフォルト値を持つ。build.gradle でオーバーライド可能
    // Gradle はこれらの値をキャッシュキーに使い、変更時のみタスクを再実行する
    // ========================================================================

    /** PostgreSQL の Docker イメージ名（例: {@code postgres:18.3}）。 */
    @Input
    public abstract Property<String> getPostgresImage();

    /** コンテナ内に作成するデータベース名。 */
    @Input
    public abstract Property<String> getDatabaseName();

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

    /** jOOQ codegen が読み取る PostgreSQL スキーマ名。 */
    @Input
    public abstract Property<String> getInputSchema();

    // ========================================================================
    // 出力プロパティ — Gradle の UP-TO-DATE 判定に使用される
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
     * PostgreSQL コンテナのライフサイクルは try-with-resources で管理し、
     * タスク完了後（成功・失敗問わず）に確実にコンテナを停止・削除する。
     */
    @TaskAction
    @SuppressWarnings("resource")
    public void generate() throws Exception {
        // Docker Desktop 4.71+ は API バージョン 1.40 未満を拒否する。
        // docker-java 3.4.x のデフォルトは 1.25 のため、/info で 400 エラーになる。
        // このシステムプロパティで docker-java に対応バージョンを強制する。
        System.setProperty("api.version", "1.43");

        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(getPostgresImage().get())
                .withDatabaseName(getDatabaseName().get())
                .withUsername(getUsername().get())
                .withPassword(getPassword().get())) {

            postgres.start();

            String jdbcUrl = postgres.getJdbcUrl();
            String user = postgres.getUsername();
            String pass = postgres.getPassword();

            // 1. アプリケーション用スキーマを作成（public スキーマとは分離）
            createSchema(jdbcUrl, user, pass);
            // 2. Liquibase マイグレーションを実行してテーブル等を構築
            runLiquibase(jdbcUrl, user, pass);
            // 3. 構築済みスキーマから jOOQ コードを生成
            runJooq(jdbcUrl, user, pass);
        }
    }

    /**
     * アプリケーション用スキーマを作成する。
     *
     * <p>
     * PostgreSQL のデフォルト {@code public} スキーマとは分離し、
     * jOOQ codegen が不要なシステムテーブルを拾わないようにする。
     */
    private void createSchema(String jdbcUrl, String username, String password) throws Exception {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                Statement stmt = connection.createStatement()) {
            String schemaName = getInputSchema().get();
            if (!schemaName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                throw new IllegalArgumentException("Invalid schema name: " + schemaName);
            }
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        }
    }

    /**
     * Liquibase マイグレーションを実行する。
     *
     * <p>
     * {@code defaultSchemaName} と {@code liquibaseSchemaName} の両方を設定し、
     * アプリケーションテーブルと Liquibase 管理テーブル（databasechangelog 等）を
     * 同一スキーマに配置する。
     */
    private void runLiquibase(String jdbcUrl, String username, String password) throws Exception {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultSchemaName(getInputSchema().get());
            database.setLiquibaseSchemaName(getInputSchema().get());

            Path searchPath = getProject().file(getLiquibaseSearchPath().get()).toPath();

            try (Liquibase liquibase = new Liquibase(
                    getLiquibaseChangelog().get(),
                    new DirectoryResourceAccessor(searchPath),
                    database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }
    }

    /**
     * jOOQ codegen を実行し、型安全なテーブル・レコードクラスを生成する。
     *
     * <p>
     * 生成前に出力ディレクトリを削除し、古いコードが残らないようにする。
     * Liquibase 管理テーブル（databasechangelog, databasechangeloglock）は
     * excludes パターンで除外する。
     */
    private void runJooq(String jdbcUrl, String username, String password) throws Exception {
        File targetDir = getOutputDirectory().get().getAsFile();

        // 前回の生成コードを削除（テーブル削除時に古いクラスが残るのを防止）
        deleteDirectory(targetDir);
        targetDir.mkdirs();

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
                                        .withInputSchema(getInputSchema().get())
                                        // Liquibase 管理テーブルは生成対象から除外
                                        .withExcludes("databasechangelog|databasechangeloglock"))
                                .withGenerate(new Generate()
                                        .withDeprecated(false) // 非推奨 API を生成しない
                                        .withRecords(true) // Record クラスを生成
                                        .withPojos(false) // POJO は不要（ドメインモデルを使用）
                                        .withFluentSetters(false) // fluent setter は不要
                                        .withJavaTimeTypes(true)) // java.time 型を使用
                                .withTarget(new Target()
                                        .withPackageName(getPackageName().get())
                                        .withDirectory(targetDir.getAbsolutePath()))));
    }

    /**
     * ディレクトリを再帰的に削除する。
     *
     * <p>
     * ファイルツリーを逆順（深い方から）にソートして削除することで、
     * 空でないディレクトリの削除エラーを回避する。
     */
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
