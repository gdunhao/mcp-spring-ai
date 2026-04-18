package com.example.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * MCP Tool: Code Analysis
 *
 * Demonstrates how MCP tools can enable an LLM to perform static code analysis.
 * The tool scans Java source files and provides metrics, finds patterns, and
 * helps with code review tasks.
 *
 * Real-world use cases:
 * - AI-assisted code review
 * - Technical debt assessment
 * - Code quality monitoring
 * - Automated documentation generation
 * - Onboarding assistants for new developers
 */
@Component
public class CodeAnalysisTool {

    @Tool(description = "Analyze a Java source file and return metrics including: " +
            "line count, class/method/field counts, TODO/FIXME comments, and import analysis. " +
            "The path should be relative to the project root.")
    public String analyzeJavaFile(
            @ToolParam(description = "Relative path to the Java source file") String filePath) {
        try {
            Path path = Path.of(filePath).toAbsolutePath();
            if (!Files.isRegularFile(path) || !filePath.endsWith(".java")) {
                return "Error: Not a valid Java file: " + filePath;
            }

            String content = Files.readString(path);
            String[] lines = content.split("\n");

            int totalLines = lines.length;
            int blankLines = 0;
            int commentLines = 0;
            int codeLines = 0;
            List<String> todos = new ArrayList<>();
            List<String> imports = new ArrayList<>();
            int classCount = 0;
            int methodCount = 0;
            boolean inBlockComment = false;

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                if (line.isEmpty()) {
                    blankLines++;
                    continue;
                }

                // Track block comments
                if (line.startsWith("/*")) inBlockComment = true;
                if (inBlockComment || line.startsWith("//")) {
                    commentLines++;
                    if (line.contains("*/")) inBlockComment = false;

                    // Find TODOs and FIXMEs
                    if (line.contains("TODO") || line.contains("FIXME")) {
                        todos.add("  Line " + (i + 1) + ": " + line);
                    }
                    continue;
                }
                if (line.contains("*/")) {
                    inBlockComment = false;
                    commentLines++;
                    continue;
                }

                codeLines++;

                if (line.startsWith("import ")) {
                    imports.add(line.replace("import ", "").replace(";", ""));
                }
                if (line.matches(".*\\b(class|interface|enum|record)\\s+\\w+.*")) {
                    classCount++;
                }
                if (line.matches(".*(public|private|protected|static|\\s)\\s+\\w+\\s+\\w+\\s*\\(.*\\).*\\{?")) {
                    methodCount++;
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📊 Code Analysis: ").append(Path.of(filePath).getFileName()).append("\n\n");
            sb.append("📏 Lines of Code:\n");
            sb.append("   Total: ").append(totalLines).append("\n");
            sb.append("   Code: ").append(codeLines).append("\n");
            sb.append("   Comments: ").append(commentLines).append("\n");
            sb.append("   Blank: ").append(blankLines).append("\n\n");
            sb.append("🏗️ Structure:\n");
            sb.append("   Classes/Interfaces/Records: ").append(classCount).append("\n");
            sb.append("   Methods (approx): ").append(methodCount).append("\n");
            sb.append("   Imports: ").append(imports.size()).append("\n\n");

            if (!todos.isEmpty()) {
                sb.append("⚠️ TODOs/FIXMEs (").append(todos.size()).append("):\n");
                todos.forEach(t -> sb.append(t).append("\n"));
                sb.append("\n");
            }

            if (!imports.isEmpty()) {
                sb.append("📦 Dependencies:\n");
                imports.stream().limit(15).forEach(imp -> sb.append("   ").append(imp).append("\n"));
                if (imports.size() > 15) sb.append("   ... and ").append(imports.size() - 15).append(" more\n");
            }

            return sb.toString();
        } catch (IOException e) {
            return "Error analyzing file: " + e.getMessage();
        }
    }

    @Tool(description = "Scan a directory for Java source files and provide a project-level summary " +
            "including total files, lines of code, package structure, and common patterns.")
    public String scanDirectory(
            @ToolParam(description = "Path to the directory to scan for Java files") String directoryPath) {
        try {
            Path dir = Path.of(directoryPath).toAbsolutePath();
            if (!Files.isDirectory(dir)) {
                return "Error: Not a directory: " + directoryPath;
            }

            AtomicInteger fileCount = new AtomicInteger();
            AtomicInteger totalLines = new AtomicInteger();
            AtomicInteger todoCount = new AtomicInteger();
            List<String> packages = new ArrayList<>();
            List<String> largeFiles = new ArrayList<>();

            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".java")) {
                        fileCount.incrementAndGet();
                        List<String> lines = Files.readAllLines(file);
                        totalLines.addAndGet(lines.size());

                        if (lines.size() > 200) {
                            largeFiles.add(dir.relativize(file) + " (" + lines.size() + " lines)");
                        }

                        for (String line : lines) {
                            if (line.trim().startsWith("package ")) {
                                String pkg = line.trim().replace("package ", "").replace(";", "");
                                if (!packages.contains(pkg)) packages.add(pkg);
                            }
                            if (line.contains("TODO") || line.contains("FIXME")) {
                                todoCount.incrementAndGet();
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            StringBuilder sb = new StringBuilder();
            sb.append("📊 Directory Scan: ").append(directoryPath).append("\n\n");
            sb.append("📁 Java Files: ").append(fileCount.get()).append("\n");
            sb.append("📏 Total Lines: ").append(totalLines.get()).append("\n");
            sb.append("📦 Packages: ").append(packages.size()).append("\n");
            sb.append("⚠️ TODOs/FIXMEs: ").append(todoCount.get()).append("\n\n");

            if (!packages.isEmpty()) {
                sb.append("📦 Package Structure:\n");
                packages.stream().sorted().forEach(p -> sb.append("   ").append(p).append("\n"));
                sb.append("\n");
            }

            if (!largeFiles.isEmpty()) {
                sb.append("📏 Large Files (>200 lines):\n");
                largeFiles.forEach(f -> sb.append("   ").append(f).append("\n"));
            }

            return sb.toString();
        } catch (IOException e) {
            return "Error scanning directory: " + e.getMessage();
        }
    }

    @Tool(description = "Find all occurrences of a pattern (regex) in Java files within a directory. " +
            "Useful for finding specific coding patterns, anti-patterns, or usages.")
    public String findPattern(
            @ToolParam(description = "Directory path to search in") String directoryPath,
            @ToolParam(description = "Regex pattern to search for in Java files") String regex) {
        try {
            Path dir = Path.of(directoryPath).toAbsolutePath();
            Pattern pattern = Pattern.compile(regex);
            List<String> matches = new ArrayList<>();

            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".java")) {
                        List<String> lines = Files.readAllLines(file);
                        for (int i = 0; i < lines.size(); i++) {
                            Matcher matcher = pattern.matcher(lines.get(i));
                            if (matcher.find()) {
                                matches.add(dir.relativize(file) + ":" + (i + 1) + " → " + lines.get(i).trim());
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            if (matches.isEmpty()) {
                return "No matches found for pattern: " + regex;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🔍 Pattern Search: ").append(regex).append("\n");
            sb.append("Found ").append(matches.size()).append(" match(es):\n\n");
            matches.stream().limit(50).forEach(m -> sb.append("  ").append(m).append("\n"));
            if (matches.size() > 50) {
                sb.append("  ... and ").append(matches.size() - 50).append(" more matches\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error searching pattern: " + e.getMessage();
        }
    }
}

