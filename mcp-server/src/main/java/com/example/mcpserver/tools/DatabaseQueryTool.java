package com.example.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP Tool: Database Query Operations
 *
 * Demonstrates how MCP tools allow an LLM to interact with a relational database.
 * The tool executes read-only SQL queries against an H2 in-memory database
 * pre-loaded with sample data (employees, departments, products, orders).
 *
 * Security: Only SELECT statements are allowed to prevent data modification.
 *
 * Real-world use cases:
 * - Natural language to SQL chatbots
 * - Business intelligence assistants
 * - Database exploration and documentation tools
 * - Automated report generation
 */
@Component
public class DatabaseQueryTool {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseQueryTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = "Execute a read-only SQL SELECT query against the database. " +
            "Available tables: employees (id, name, email, department, salary, hire_date), " +
            "departments (id, name, budget, manager_name), " +
            "products (id, name, category, price, stock_quantity), " +
            "orders (id, product_id, customer_name, quantity, order_date, status). " +
            "Only SELECT statements are permitted.")
    public String executeQuery(
            @ToolParam(description = "SQL SELECT query to execute") String sql) {
        // Security: Only allow SELECT statements
        String trimmed = sql.trim().toUpperCase();
        if (!trimmed.startsWith("SELECT")) {
            return "Error: Only SELECT queries are allowed for safety. Got: " +
                    trimmed.substring(0, Math.min(trimmed.length(), 20)) + "...";
        }

        // Block dangerous keywords
        if (trimmed.contains("DROP") || trimmed.contains("DELETE") ||
                trimmed.contains("INSERT") || trimmed.contains("UPDATE") ||
                trimmed.contains("ALTER") || trimmed.contains("CREATE") ||
                trimmed.contains("TRUNCATE")) {
            return "Error: Query contains prohibited keywords.";
        }

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            if (results.isEmpty()) {
                return "Query returned 0 rows.";
            }

            // Format results as a readable table
            return formatResults(results);
        } catch (Exception e) {
            return "Error executing query: " + e.getMessage();
        }
    }

    @Tool(description = "List all available tables in the database with their column information.")
    public String listTables() {
        try {
            StringBuilder sb = new StringBuilder("Available Tables:\n\n");

            // Query H2 metadata for tables in the PUBLIC schema
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                    "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'");

            for (Map<String, Object> table : tables) {
                String tableName = (String) table.get("TABLE_NAME");
                sb.append("📋 ").append(tableName).append("\n");

                List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                        "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE " +
                                "FROM INFORMATION_SCHEMA.COLUMNS " +
                                "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = ? " +
                                "ORDER BY ORDINAL_POSITION", tableName);

                for (Map<String, Object> col : columns) {
                    sb.append("   - ").append(col.get("COLUMN_NAME"))
                            .append(" (").append(col.get("DATA_TYPE"))
                            .append(", nullable=").append(col.get("IS_NULLABLE")).append(")\n");
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error listing tables: " + e.getMessage();
        }
    }

    @Tool(description = "Get a summary of data in a specific table, including row count and sample rows.")
    public String tableSummary(
            @ToolParam(description = "Name of the table to summarize") String tableName) {
        // Validate table name (alphanumeric only to prevent injection)
        if (!tableName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return "Error: Invalid table name.";
        }

        try {
            // Row count — queryForObject can return null; default to 0 defensively
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + tableName, Integer.class);
            int rowCount = count != null ? count : 0;

            // Sample rows (first 5)
            List<Map<String, Object>> sample = jdbcTemplate.queryForList(
                    "SELECT * FROM " + tableName + " LIMIT 5");

            StringBuilder sb = new StringBuilder();
            sb.append("Table: ").append(tableName).append("\n");
            sb.append("Total rows: ").append(rowCount).append("\n\n");
            sb.append("Sample data (first 5 rows):\n");
            sb.append(formatResults(sample));
            return sb.toString();
        } catch (Exception e) {
            return "Error summarizing table '" + tableName + "': " + e.getMessage();
        }
    }

    private String formatResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) return "(no data)";

        // Get column names
        List<String> columns = results.getFirst().keySet().stream().toList();

        // Calculate column widths
        Map<String, Integer> widths = columns.stream()
                .collect(Collectors.toMap(
                        col -> col,
                        col -> Math.max(col.length(),
                                results.stream()
                                        .mapToInt(row -> String.valueOf(row.get(col)).length())
                                        .max().orElse(0))
                ));

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("| ");
        for (String col : columns) {
            sb.append(String.format("%-" + widths.get(col) + "s | ", col));
        }
        sb.append("\n|");
        for (String col : columns) {
            sb.append("-".repeat(widths.get(col) + 2)).append("|");
        }
        sb.append("\n");

        // Rows
        for (Map<String, Object> row : results) {
            sb.append("| ");
            for (String col : columns) {
                sb.append(String.format("%-" + widths.get(col) + "s | ", String.valueOf(row.get(col))));
            }
            sb.append("\n");
        }

        sb.append("\n(").append(results.size()).append(" row(s) returned)");
        return sb.toString();
    }
}

