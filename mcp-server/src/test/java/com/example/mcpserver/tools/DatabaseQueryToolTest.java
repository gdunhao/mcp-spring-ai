package com.example.mcpserver.tools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for DatabaseQueryTool using an in-memory H2 database.
 * Uses @JdbcTest to load only JDBC-related infrastructure (fast slice test).
 */
@JdbcTest
@Import(DatabaseQueryTool.class)
@Sql(scripts = {"/schema.sql", "/data.sql"})
class DatabaseQueryToolTest {

    @Autowired
    private DatabaseQueryTool tool;

    // ── executeQuery ──────────────────────────────────────────────────────────

    @Test
    void executeQuery_validSelect_returnsFormattedTable() {
        String result = tool.executeQuery("SELECT * FROM employees");
        assertThat(result).contains("NAME");
        assertThat(result).contains("row(s) returned");
    }

    @Test
    void executeQuery_nonSelectStatement_returnsError() {
        String result = tool.executeQuery("DELETE FROM employees WHERE id = 1");
        assertThat(result).contains("Error").contains("SELECT");
    }

    @Test
    void executeQuery_dropStatement_isBlocked() {
        String result = tool.executeQuery("SELECT 1; DROP TABLE employees");
        assertThat(result).contains("Error").contains("prohibited");
    }

    @Test
    void executeQuery_insertKeyword_isBlocked() {
        String result = tool.executeQuery("SELECT 'INSERT' FROM employees");
        assertThat(result).contains("Error").contains("prohibited");
    }

    @Test
    void executeQuery_emptyResultSet_returnsZeroRows() {
        String result = tool.executeQuery("SELECT * FROM employees WHERE id = -9999");
        assertThat(result).contains("0 rows");
    }

    @Test
    void executeQuery_invalidSql_returnsError() {
        String result = tool.executeQuery("SELECT * FROM nonexistent_table_xyz");
        assertThat(result).contains("Error");
    }

    @Test
    void executeQuery_aggregation_works() {
        String result = tool.executeQuery("SELECT COUNT(*) AS total FROM employees");
        assertThat(result).contains("TOTAL");
        assertThat(result).contains("row(s) returned");
    }

    // ── listTables ────────────────────────────────────────────────────────────

    @Test
    void listTables_returnsKnownTables() {
        String result = tool.listTables();
        assertThat(result).containsIgnoringCase("EMPLOYEES");
        assertThat(result).containsIgnoringCase("DEPARTMENTS");
        assertThat(result).containsIgnoringCase("PRODUCTS");
        assertThat(result).containsIgnoringCase("ORDERS");
    }

    @Test
    void listTables_returnsColumnInfo() {
        String result = tool.listTables();
        assertThat(result).containsIgnoringCase("NAME");
        assertThat(result).containsIgnoringCase("nullable");
    }

    // ── tableSummary ──────────────────────────────────────────────────────────

    @Test
    void tableSummary_validTable_returnsRowCountAndSample() {
        String result = tool.tableSummary("employees");
        assertThat(result).contains("Table: employees");
        assertThat(result).contains("Total rows:");
    }

    @Test
    void tableSummary_invalidTableName_returnsError() {
        String result = tool.tableSummary("employees; DROP TABLE employees");
        assertThat(result).contains("Error").contains("Invalid table name");
    }

    @Test
    void tableSummary_nonExistentTable_returnsError() {
        String result = tool.tableSummary("ghost_table");
        assertThat(result).contains("Error");
    }

    @Test
    void tableSummary_productsTable_returnsData() {
        String result = tool.tableSummary("products");
        assertThat(result).contains("Table: products");
        assertThat(result).contains("Total rows:");
    }
}

