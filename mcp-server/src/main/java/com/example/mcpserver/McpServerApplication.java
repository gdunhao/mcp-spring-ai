package com.example.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP Server Application.
 *
 * This application exposes MCP (Model Context Protocol) capabilities including:
 * - Tools: File system operations, database queries, weather data, code analysis
 * - Resources: Knowledge base documents accessible via MCP resource URIs
 * - Prompts: Reusable prompt templates for common AI tasks
 *
 * Supports two transport modes:
 * - SSE (Server-Sent Events): Default mode, runs on port 3001
 * - STDIO: For subprocess-based communication (activate with --spring.profiles.active=stdio)
 */
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}

