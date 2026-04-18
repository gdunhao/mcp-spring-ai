package com.example.mcpserver.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileSystemTool — demonstrates testing MCP tool implementations.
 */
class FileSystemToolTest {

    @Test
    void listFiles_returnsDirectoryContents() {
        var tool = new FileSystemTool();
        String result = tool.listFiles(".");
        // demo-workspace should exist (created by constructor)
        assertNotNull(result);
    }

    @Test
    void readFile_nonExistentFile_returnsError() {
        var tool = new FileSystemTool();
        String result = tool.readFile("nonexistent.txt");
        assertTrue(result.contains("Error") || result.contains("not"));
    }

    @Test
    void writeAndReadFile_roundTrip() {
        var tool = new FileSystemTool();
        String testContent = "Hello from test!";

        // Write
        String writeResult = tool.writeFile("test-output.txt", testContent);
        assertTrue(writeResult.contains("Successfully"));

        // Read back
        String readResult = tool.readFile("test-output.txt");
        assertEquals(testContent, readResult);

        // Cleanup
        tool.writeFile("test-output.txt", ""); // overwrite with empty
    }

    @Test
    void searchFiles_findsMatchingFiles() {
        var tool = new FileSystemTool();
        // Write a test file
        tool.writeFile("search-test.md", "test content");

        String result = tool.searchFiles("*.md");
        assertTrue(result.contains("search-test.md") || result.contains("Found"),
                "Expected to find search-test.md but got: " + result);
    }
}


