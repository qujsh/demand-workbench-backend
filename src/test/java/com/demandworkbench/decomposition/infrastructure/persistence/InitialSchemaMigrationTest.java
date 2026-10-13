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

        long decompositionId;
        long parentId;
        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO decomposition_record
                    (decomposition_no, source_content, status, stage, prompt_version, error_code, created_at)
                    VALUES ('DEC-1', 'test', 'failed', 'failed', 'v1', 'S048101004', CURRENT_TIMESTAMP(3))
                    """);
            try (var result = statement.executeQuery(
                    "SELECT id FROM decomposition_record WHERE decomposition_no = 'DEC-1'")) {
                assertThat(result.next()).isTrue();
                decompositionId = result.getLong("id");
            }
            statement.executeUpdate("""
                    INSERT INTO work_item
                    (work_item_no, decomposition_id, type, name, status, priority, sort_order, created_at, updated_at)
                    VALUES ('WORK-1', %d, 'work', 'test', '未开始', 'high', 1,
                            CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
                    """.formatted(decompositionId));
            try (var result = statement.executeQuery(
                    "SELECT id FROM work_item WHERE work_item_no = 'WORK-1'")) {
                assertThat(result.next()).isTrue();
                parentId = result.getLong("id");
            }
            statement.executeUpdate("""
                    INSERT INTO work_item
                    (work_item_no, decomposition_id, parent_id, type, name, status, priority, sort_order, created_at, updated_at)
                    VALUES ('SUB-1', %d, %d, 'subtask', 'child', '未开始', 'high', 2,
                            CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
                    """.formatted(decompositionId, parentId));
            statement.executeUpdate("""
                    INSERT INTO requirement
                    (requirement_no, title_code, title, stage, current_decomposition_id)
                    VALUES ('REQ-CS-000001', 'CS', '测试需求', 'analyzing', %d)
                    """.formatted(decompositionId));
        }

        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT version FROM flyway_schema_history WHERE success = 1")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getString("version")).isEqualTo("202610120100");
            assertThat(result.next()).isTrue();
            assertThat(result.getString("version")).isEqualTo("202610120110");
            assertThat(result.next()).isTrue();
            assertThat(result.getString("version")).isEqualTo("202610131020");
            assertThat(result.next()).isTrue();
            assertThat(result.getString("version")).isEqualTo("202610131500");
            assertThat(result.next()).isFalse();
        }

        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             var statement = connection.createStatement();
             var records = statement.executeQuery("""
                     SELECT d.id AS decomposition_pk, d.decomposition_no,
                            w.id AS work_item_pk, w.work_item_no, w.parent_id,
                            p.work_item_no AS parent_work_item_no,
                            r.requirement_no
                     FROM decomposition_record d
                     JOIN work_item w ON w.decomposition_id = d.id
                     LEFT JOIN work_item p ON p.id = w.parent_id
                     JOIN requirement r ON r.current_decomposition_id = d.id
                     WHERE w.work_item_no = 'SUB-1'
                     """)) {
            assertThat(records.next()).isTrue();
            assertThat(records.getLong("decomposition_pk")).isPositive();
            assertThat(records.getString("decomposition_no")).isEqualTo("DEC-1");
            assertThat(records.getLong("work_item_pk")).isPositive();
            assertThat(records.getString("work_item_no")).isEqualTo("SUB-1");
            assertThat(records.getString("parent_work_item_no")).isEqualTo("WORK-1");
            assertThat(records.getString("requirement_no")).isEqualTo("REQ-CS-000001");
        }

        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    WITH RECURSIVE descendants AS (
                        SELECT id FROM work_item WHERE deleted = 0 AND work_item_no = 'WORK-1'
                        UNION ALL
                        SELECT child.id
                        FROM work_item child
                        JOIN descendants parent ON child.parent_id = parent.id
                        WHERE child.deleted = 0
                    )
                    UPDATE work_item
                    SET deleted = 1, updated_at = CURRENT_TIMESTAMP(3)
                    WHERE id IN (SELECT id FROM descendants)
                    """);
            try (var result = statement.executeQuery("""
                    SELECT COUNT(*) AS visible_count
                    FROM work_item
                    WHERE decomposition_id = %d AND deleted = 0
                    """.formatted(decompositionId))) {
                assertThat(result.next()).isTrue();
                assertThat(result.getInt("visible_count")).isZero();
            }
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
                       AND table_name IN ('decomposition_record', 'work_item', 'requirement', 'requirement_attachment')
                       AND column_comment <> ''
                     """)) {
            assertThat(columns.next()).isTrue();
            assertThat(columns.getInt("documented_columns")).isEqualTo(59);
        }

        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             var statement = connection.createStatement();
             var commonFields = statement.executeQuery("""
                     SELECT COUNT(*) AS common_field_count
                     FROM information_schema.columns
                     WHERE table_schema = DATABASE()
                       AND table_name IN ('decomposition_record', 'work_item', 'requirement', 'requirement_attachment')
                       AND column_name IN ('deleted', 'created_at', 'updated_at')
                     """)) {
            assertThat(commonFields.next()).isTrue();
            assertThat(commonFields.getInt("common_field_count")).isEqualTo(12);
        }
    }
}
