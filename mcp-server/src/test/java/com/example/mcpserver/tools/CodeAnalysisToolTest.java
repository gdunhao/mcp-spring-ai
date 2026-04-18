package com.example.mcpserver.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CodeAnalysisTool.
 */
class CodeAnalysisToolTest {

    private final CodeAnalysisTool tool = new CodeAnalysisTool();

    // ── analyzeJavaFile ───────────────────────────────────────────────────────

    @Test
    void analyzeJavaFile_nonJavaExtension_returnsError() {
        String result = tool.analyzeJavaFile("some/file.txt");
        assertThat(result).contains("Error");
    }

    @Test
    void analyzeJavaFile_nonExistentFile_returnsError() {
        String result = tool.analyzeJavaFile("nonexistent/Path.java");
        assertThat(result).contains("Error");
    }

    @Test
    void analyzeJavaFile_validJavaFile_returnsMetrics(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Sample.java");
        Files.writeString(javaFile, """
                package com.example;

                import java.util.List;
                import java.util.Map;

                // TODO: add logging
                public class Sample {
                    private String name;

                    public String getName() {
                        return name;
                    }

                    /* FIXME: implement this */
                    public void process() {
                    }
                }
                """);

        String result = tool.analyzeJavaFile(javaFile.toString());

        assertThat(result).contains("Sample.java");
        assertThat(result).contains("Lines of Code");
        assertThat(result).contains("Structure");
        assertThat(result).contains("TODO");
        assertThat(result).contains("FIXME");
        assertThat(result).contains("java.util.List");
    }

    @Test
    void analyzeJavaFile_noTodos_doesNotShowTodosSection(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Clean.java");
        Files.writeString(javaFile, """
                package com.example;

                public class Clean {
                    public void doSomething() {}
                }
                """);

        String result = tool.analyzeJavaFile(javaFile.toString());
        assertThat(result).doesNotContain("TODOs/FIXMEs");
    }

    // ── scanDirectory ─────────────────────────────────────────────────────────

    @Test
    void scanDirectory_nonExistentDirectory_returnsError() {
        String result = tool.scanDirectory("/nonexistent/directory");
        assertThat(result).contains("Error");
    }

    @Test
    void scanDirectory_directoryWithJavaFiles_returnsStats(@TempDir Path tempDir) throws IOException {
        // Create sub-package directory
        Path pkg = tempDir.resolve("com/example");
        Files.createDirectories(pkg);

        Files.writeString(pkg.resolve("Alpha.java"), """
                package com.example;
                // TODO: fix this
                public class Alpha {}
                """);
        Files.writeString(pkg.resolve("Beta.java"), """
                package com.example;
                public class Beta {}
                """);

        String result = tool.scanDirectory(tempDir.toString());

        assertThat(result).contains("Java Files: 2");
        assertThat(result).contains("com.example");
        assertThat(result).contains("TODOs/FIXMEs: 1");
    }

    @Test
    void scanDirectory_emptyDirectory_returnsZeroFiles(@TempDir Path tempDir) {
        String result = tool.scanDirectory(tempDir.toString());
        assertThat(result).contains("Java Files: 0");
    }

    @Test
    void scanDirectory_largeFile_showsLargeFileWarning(@TempDir Path tempDir) throws IOException {
        StringBuilder sb = new StringBuilder("package com.example;\npublic class Big {\n");
        for (int i = 0; i < 210; i++) {
            sb.append("    public void method").append(i).append("() {}\n");
        }
        sb.append("}");
        Files.writeString(tempDir.resolve("Big.java"), sb.toString());

        String result = tool.scanDirectory(tempDir.toString());
        assertThat(result).contains("Large Files");
    }

    // ── findPattern ───────────────────────────────────────────────────────────

    @Test
    void findPattern_matchingFiles_returnsMatches(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("Foo.java"), """
                package com.example;
                public class Foo {
                    @Deprecated
                    public void oldMethod() {}
                }
                """);

        String result = tool.findPattern(tempDir.toString(), "@Deprecated");
        assertThat(result).contains("Found");
        assertThat(result).contains("Foo.java");
    }

    @Test
    void findPattern_noMatches_returnsNoMatchesMessage(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("Bar.java"), "package com.example;\npublic class Bar {}");

        String result = tool.findPattern(tempDir.toString(), "nonExistentPatternXYZ");
        assertThat(result).contains("No matches found");
    }

    @Test
    void findPattern_invalidRegex_returnsError(@TempDir Path tempDir) {
        String result = tool.findPattern(tempDir.toString(), "[invalid regex");
        assertThat(result).contains("Error");
    }

    @Test
    void findPattern_regexGroup_returnsMultipleMatches(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("A.java"), "public class A { void foo() {} void bar() {} }");
        Files.writeString(tempDir.resolve("B.java"), "public class B { void foo() {} }");

        String result = tool.findPattern(tempDir.toString(), "void \\w+\\(");
        assertThat(result).contains("Found");
    }
}

