package com.demandworkbench.requirement.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class RequirementSchemaMigrationTest {
    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("demand_workbench")
            .withUsername("demand")
            .withPassword("demand_test");

    @Test
    void shouldCreateRequirementSchemaAndAllowNumberBackfillAfterInsert() throws Exception {
        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .load()
                .migrate();

        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())) {
            long requirementId;
            try (var statement = connection.prepareStatement("""
                    INSERT INTO requirement (title_code, title, stage)
                    VALUES ('KFYTYY', '开发一套预约服务系统', 'draft')
                    """, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                statement.executeUpdate();
                try (var keys = statement.getGeneratedKeys()) {
                    assertThat(keys.next()).isTrue();
                    requirementId = keys.getLong(1);
                }
            }

            var requirementNo = "REQ-KFYTYY-%06d".formatted(requirementId);
            try (var statement = connection.prepareStatement("""
                    UPDATE requirement SET requirement_no = ? WHERE id = ?
                    """)) {
                statement.setString(1, requirementNo);
                statement.setLong(2, requirementId);
                assertThat(statement.executeUpdate()).isOne();
            }

            try (var statement = connection.prepareStatement("""
                    SELECT requirement_no, title_code FROM requirement WHERE id = ?
                    """)) {
                statement.setLong(1, requirementId);
                try (var result = statement.executeQuery()) {
                    assertThat(result.next()).isTrue();
                    assertThat(result.getString("requirement_no")).isEqualTo("REQ-KFYTYY-000001");
                    assertThat(result.getString("title_code")).isEqualTo("KFYTYY");
                }
            }
        }

        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             var statement = connection.createStatement();
             var columns = statement.executeQuery("""
                     SELECT COUNT(*) AS undocumented_columns
                     FROM information_schema.columns
                     WHERE table_schema = DATABASE()
                       AND table_name IN ('requirement', 'requirement_attachment')
                       AND column_comment = ''
                     """)) {
            assertThat(columns.next()).isTrue();
            assertThat(columns.getInt("undocumented_columns")).isZero();
        }
    }
}
