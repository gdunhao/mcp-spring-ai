package com.example.mcpclient;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class McpClientApplicationTests {

    @Test
    void applicationClassExists() {
        // Basic smoke test — full context loading requires Ollama + MCP server
        assertDoesNotThrow(() -> {
            McpClientApplication.class.getDeclaredConstructor();
        });
    }
}
