package com.demandworkbench.decomposition.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class InitialSchemaMigrationTest {
    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("demand_workbench")
            .withUsername("demand")
            .withPassword("demand_test");

    @Test
    void shouldCreateDocumentedSchemaWithoutBusinessValueOrForeignKeyConstraints() throws Exception {
        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .load()
                .migrate();

        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO decomposition_record
                    (id, source_content, status, stage, prompt_version, error_code, created_at)
                    VALUES ('DEC-1', 'test', 'failed', 'failed', 'v1', 'S048101004', CURRENT_TIMESTAMP(3))
                    """);
            statement.executeUpdate("""
                    INSERT INTO work_item
                    (id, decomposition_id, type, name, status, priority, sort_order, created_at, updated_at)
                    VALUES ('WORK-1', 'DEC-NOT-EXISTS', 'work', 'test', '未开始', 'high', 1,
                            CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
                    """);
        }

        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT version FROM flyway_schema_history WHERE success = 1")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getString("version")).isEqualTo("1");
            assertThat(result.next()).isTrue();
            assertThat(result.getString("version")).isEqualTo("2");
            assertThat(result.next()).isFalse();
        }

        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             var statement = connection.createStatement();
             var constraints = statement.executeQuery("""
                     SELECT COUNT(*) AS constraint_count
                     FROM information_schema.table_constraints
                     WHERE constraint_schema = DATABASE()
                       AND constraint_type IN ('FOREIGN KEY', 'CHECK')
                     """)) {
            assertThat(constraints.next()).isTrue();
            assertThat(constraints.getInt("constraint_count")).isZero();
        }

        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             var statement = connection.createStatement();
             var columns = statement.executeQuery("""
                     SELECT COUNT(*) AS documented_columns
                     FROM information_schema.columns
                     WHERE table_schema = DATABASE()
                       AND table_name IN ('decomposition_record', 'work_item')
                       AND column_comment <> ''
                     """)) {
            assertThat(columns.next()).isTrue();
            assertThat(columns.getInt("documented_columns")).isEqualTo(29);
        }
    }
}
