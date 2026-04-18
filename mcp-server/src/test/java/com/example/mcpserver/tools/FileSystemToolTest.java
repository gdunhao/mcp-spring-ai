package com.example.mcpserver.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive unit tests for FileSystemTool.
 */
class FileSystemToolTest {

    private FileSystemTool tool;

    @BeforeEach
    void setUp() {
        tool = new FileSystemTool();
    }

    // ── listFiles ─────────────────────────────────────────────────────────────

    @Test
    void listFiles_workspaceRoot_returnsContents() {
        String result = tool.listFiles(".");
        assertThat(result).isNotNull().doesNotStartWith("Error:");
    }

    @Test
    void listFiles_emptyPath_treatedAsRoot() {
        String result = tool.listFiles("");
        assertThat(result).isNotNull().doesNotStartWith("Error:");
    }

    @Test
    void listFiles_nonExistentPath_returnsError() {
        String result = tool.listFiles("no/such/dir");
        assertThat(result).contains("Error");
    }

    @Test
    void listFiles_fileAsPath_returnsError() {
        tool.writeFile("list-as-dir-test.txt", "content");
        String result = tool.listFiles("list-as-dir-test.txt");
        assertThat(result).contains("Error");
    }

    @Test
    void listFiles_directoryShowsSlashSuffix() {
        tool.writeFile("subdir-test/nested.txt", "hi");
        String result = tool.listFiles(".");
        assertThat(result).contains("subdir-test/");
    }

    // ── readFile ──────────────────────────────────────────────────────────────

    @Test
    void readFile_nonExistentFile_returnsError() {
        String result = tool.readFile("nonexistent.txt");
        assertThat(result).contains("Error");
    }

    @Test
    void readFile_existingFile_returnsContent() {
        tool.writeFile("readable.txt", "hello world");
        assertThat(tool.readFile("readable.txt")).isEqualTo("hello world");
    }

    @Test
    void readFile_directory_returnsError() {
        String result = tool.readFile(".");
        assertThat(result).contains("Error");
    }

    // ── writeFile ─────────────────────────────────────────────────────────────

    @Test
    void writeFile_newFile_succeeds() {
        String result = tool.writeFile("write-test.txt", "data");
        assertThat(result).contains("Successfully").contains("4 characters");
    }

    @Test
    void writeFile_overwritesExistingFile() {
        tool.writeFile("overwrite.txt", "original");
        tool.writeFile("overwrite.txt", "updated");
        assertThat(tool.readFile("overwrite.txt")).isEqualTo("updated");
    }

    @Test
    void writeFile_createsParentDirectories() {
        String result = tool.writeFile("deep/nested/dir/file.txt", "nested content");
        assertThat(result).contains("Successfully");
        assertThat(tool.readFile("deep/nested/dir/file.txt")).isEqualTo("nested content");
    }

    // ── roundtrip ─────────────────────────────────────────────────────────────

    @Test
    void writeAndReadFile_roundTrip() {
        String testContent = "Hello from test!";
        tool.writeFile("roundtrip-test.txt", testContent);
        assertThat(tool.readFile("roundtrip-test.txt")).isEqualTo(testContent);
    }

    // ── searchFiles ───────────────────────────────────────────────────────────

    @Test
    void searchFiles_findsMatchingFiles() {
        tool.writeFile("search-test.md", "test content");
        String result = tool.searchFiles("*.md");
        assertThat(result).containsAnyOf("search-test.md", "Found");
    }

    @Test
    void searchFiles_noMatchingFiles_returnsNotFoundMessage() {
        String result = tool.searchFiles("*.xyz_nonexistent");
        assertThat(result).contains("No files found");
    }

    @Test
    void searchFiles_globRecursive_findsNestedFiles() {
        tool.writeFile("nested/search/deep.txt", "deep");
        String result = tool.searchFiles("**/*.txt");
        assertThat(result).contains("Found");
    }

    // ── fileInfo ──────────────────────────────────────────────────────────────

    @Test
    void fileInfo_existingFile_returnsDetails() {
        tool.writeFile("info-test.txt", "some content");
        String result = tool.fileInfo("info-test.txt");
        assertThat(result)
                .contains("Path: info-test.txt")
                .contains("Type: File")
                .contains("Size:")
                .contains("Modified:")
                .contains("Readable: true");
    }

    @Test
    void fileInfo_directory_showsDirectoryType() {
        tool.writeFile("infodir/marker.txt", "x");
        assertThat(tool.fileInfo("infodir")).contains("Type: Directory");
    }

    @Test
    void fileInfo_nonExistentPath_returnsError() {
        assertThat(tool.fileInfo("does-not-exist.txt")).contains("Error");
    }

    // ── path traversal security ───────────────────────────────────────────────

    @Test
    void readFile_pathTraversal_returnsAccessDenied() {
        String result = tool.readFile("../../etc/passwd");
        assertThat(result).contains("Error").containsAnyOf("Access denied", "traversal");
    }

    @Test
    void writeFile_pathTraversal_returnsAccessDenied() {
        String result = tool.writeFile("../../tmp/evil.txt", "evil");
        assertThat(result).contains("Error").containsAnyOf("Access denied", "traversal");
    }

    @Test
    void listFiles_pathTraversal_returnsError() {
        String result = tool.listFiles("../../etc");
        assertThat(result).contains("Error");
    }
}


