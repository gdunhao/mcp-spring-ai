package com.example.mcpserver.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MCP Tool: File System Operations
 *
 * Demonstrates how MCP tools enable an LLM to interact with the local file system
 * in a controlled, sandboxed manner. All operations are restricted to the configured
 * workspace directory for security.
 *
 * Real-world use cases:
 * - AI-powered file managers
 * - Automated documentation generators
 * - Code scaffolding assistants
 * - Log file analyzers
 */
@Component
public class FileSystemTool {

    private static final Logger log = LoggerFactory.getLogger(FileSystemTool.class);

    private final Path workspaceRoot;

    public FileSystemTool() {
        // Sandbox all operations to the demo-workspace directory
        this.workspaceRoot = Path.of(System.getProperty("user.dir"), "demo-workspace").toAbsolutePath();
        try {
            Files.createDirectories(workspaceRoot);
            log.info("FileSystemTool workspace initialized at: {}", workspaceRoot);
        } catch (IOException e) {
            log.error("Failed to create workspace directory: {}", workspaceRoot, e);
            throw new RuntimeException("Failed to create workspace directory: " + workspaceRoot, e);
        }
    }

    @Tool(description = "List files and directories in the specified path within the workspace. " +
            "Returns names with '/' suffix for directories. Use '.' or '' for the workspace root.")
    public String listFiles(
            @ToolParam(description = "Relative path within the workspace to list. Use '.' for root.") String relativePath) {
        log.debug("listFiles() — path: '{}'", relativePath);
        try {
            Path target = resolveAndValidate(relativePath);
            if (!Files.isDirectory(target)) {
                log.warn("listFiles() — not a directory: '{}'", relativePath);
                return "Error: Path is not a directory: " + relativePath;
            }

            try (Stream<Path> entries = Files.list(target)) {
                String listing = entries
                        .map(p -> {
                            String name = p.getFileName().toString();
                            return Files.isDirectory(p) ? name + "/" : name;
                        })
                        .sorted()
                        .collect(Collectors.joining("\n"));
                String result = listing.isEmpty() ? "(empty directory)" : listing;
                log.info("listFiles() — listed '{}': {} entries", relativePath, listing.isEmpty() ? 0 : listing.split("\n").length);
                return result;
            }
        } catch (SecurityException e) {
            log.warn("listFiles() — path traversal attempt blocked: '{}'", relativePath);
            return "Error: Access denied - " + e.getMessage();
        } catch (IOException e) {
            log.error("listFiles() — I/O error for path '{}': {}", relativePath, e.getMessage(), e);
            return "Error listing files: " + e.getMessage();
        }
    }

    @Tool(description = "Read the contents of a text file within the workspace. " +
            "Returns the full file content as a string.")
    public String readFile(
            @ToolParam(description = "Relative path to the file within the workspace") String relativePath) {
        log.debug("readFile() — path: '{}'", relativePath);
        try {
            Path target = resolveAndValidate(relativePath);
            if (!Files.isRegularFile(target)) {
                log.warn("readFile() — not a regular file: '{}'", relativePath);
                return "Error: Not a regular file: " + relativePath;
            }
            long size = Files.size(target);
            if (size > 100_000) {
                log.warn("readFile() — file too large ({} bytes): '{}'", size, relativePath);
                return "Error: File too large (" + size + " bytes). Maximum is 100KB.";
            }
            String content = Files.readString(target);
            log.info("readFile() — read '{}' ({} bytes)", relativePath, size);
            return content;
        } catch (SecurityException e) {
            log.warn("readFile() — path traversal attempt blocked: '{}'", relativePath);
            return "Error: Access denied - " + e.getMessage();
        } catch (IOException e) {
            log.error("readFile() — I/O error for path '{}': {}", relativePath, e.getMessage(), e);
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file within the workspace. " +
            "Creates the file if it doesn't exist, or overwrites it if it does. " +
            "Parent directories are created automatically.")
    public String writeFile(
            @ToolParam(description = "Relative path for the file within the workspace") String relativePath,
            @ToolParam(description = "Content to write to the file") String content) {
        log.debug("writeFile() — path: '{}', content length: {}", relativePath, content.length());
        try {
            Path target = resolveAndValidate(relativePath);
            Files.createDirectories(target.getParent());
            Files.writeString(target, content);
            log.info("writeFile() — wrote {} chars to '{}'", content.length(), relativePath);
            return "Successfully wrote " + content.length() + " characters to " + relativePath;
        } catch (SecurityException e) {
            log.warn("writeFile() — path traversal attempt blocked: '{}'", relativePath);
            return "Error: Access denied - " + e.getMessage();
        } catch (IOException e) {
            log.error("writeFile() — I/O error for path '{}': {}", relativePath, e.getMessage(), e);
            return "Error writing file: " + e.getMessage();
        }
    }

    @Tool(description = "Search for files matching a pattern within the workspace. " +
            "Uses glob patterns (e.g., '*.java', '**/*.md'). Returns matching file paths.")
    public String searchFiles(
            @ToolParam(description = "Glob pattern to match files (e.g., '*.java', '**/*.txt')") String pattern) {
        log.debug("searchFiles() — pattern: '{}'", pattern);
        try {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            List<String> matches = new ArrayList<>();

            Files.walkFileTree(workspaceRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    Path relative = workspaceRoot.relativize(file);
                    if (matcher.matches(relative)) {
                        matches.add(relative.toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            if (matches.isEmpty()) {
                log.debug("searchFiles() — no matches for pattern '{}'", pattern);
                return "No files found matching pattern: " + pattern;
            }
            log.info("searchFiles() — found {} file(s) matching '{}'", matches.size(), pattern);
            return "Found " + matches.size() + " file(s):\n" + String.join("\n", matches);
        } catch (IOException e) {
            log.error("searchFiles() — error searching for pattern '{}': {}", pattern, e.getMessage(), e);
            return "Error searching files: " + e.getMessage();
        }
    }

    @Tool(description = "Get detailed information about a file or directory within the workspace, " +
            "including size, type, and last modified time.")
    public String fileInfo(
            @ToolParam(description = "Relative path to the file or directory") String relativePath) {
        log.debug("fileInfo() — path: '{}'", relativePath);
        try {
            Path target = resolveAndValidate(relativePath);
            if (!Files.exists(target)) {
                log.debug("fileInfo() — path does not exist: '{}'", relativePath);
                return "Error: Path does not exist: " + relativePath;
            }

            BasicFileAttributes attrs = Files.readAttributes(target, BasicFileAttributes.class);
            log.info("fileInfo() — retrieved info for '{}' ({}, {} bytes)",
                    relativePath, attrs.isDirectory() ? "directory" : "file", attrs.size());
            StringBuilder info = new StringBuilder();
            info.append("Path: ").append(relativePath).append("\n");
            info.append("Type: ").append(attrs.isDirectory() ? "Directory" : "File").append("\n");
            info.append("Size: ").append(attrs.size()).append(" bytes\n");
            info.append("Created: ").append(attrs.creationTime()).append("\n");
            info.append("Modified: ").append(attrs.lastModifiedTime()).append("\n");
            info.append("Readable: ").append(Files.isReadable(target)).append("\n");
            info.append("Writable: ").append(Files.isWritable(target));
            return info.toString();
        } catch (SecurityException e) {
            log.warn("fileInfo() — path traversal attempt blocked: '{}'", relativePath);
            return "Error: Access denied - " + e.getMessage();
        } catch (IOException e) {
            log.error("fileInfo() — I/O error for path '{}': {}", relativePath, e.getMessage(), e);
            return "Error getting file info: " + e.getMessage();
        }
    }

    /**
     * Resolves a relative path against the workspace root and validates it
     * stays within the sandbox boundary (prevents path traversal attacks).
     */
    private Path resolveAndValidate(String relativePath) {
        if (relativePath == null || relativePath.isBlank() || relativePath.equals(".")) {
            return workspaceRoot;
        }
        Path resolved = workspaceRoot.resolve(relativePath).normalize().toAbsolutePath();
        if (!resolved.startsWith(workspaceRoot)) {
            log.warn("resolveAndValidate() — path traversal detected: '{}'", relativePath);
            throw new SecurityException("Path traversal detected: " + relativePath);
        }
        return resolved;
    }
}

