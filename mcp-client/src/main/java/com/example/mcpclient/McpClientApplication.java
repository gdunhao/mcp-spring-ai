package com.example.mcpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP Client Application.
 *
 * This application acts as an MCP Host + Client that:
 * 1. Connects to the MCP Server (via SSE or STDIO) to discover tools, resources, and prompts
 * 2. Uses Ollama (qwen3:0.6b) as the LLM backend
 * 3. Exposes REST endpoints for interacting with the AI + MCP capabilities
 *
 * The ChatClient is configured with all MCP-discovered tools, so the LLM can
 * automatically invoke server-side tools when needed to answer user questions.
 *
 * Run with: ./mvnw spring-boot:run -pl mcp-client
 * (Make sure the MCP server is running first, and Ollama is serving qwen3:0.6b)
 */
@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
    }
}

