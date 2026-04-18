package com.example.mcpserver.prompts;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DemoPromptProvider.
 */
class DemoPromptProviderTest {

    private final DemoPromptProvider provider = new DemoPromptProvider();

    @Test
    void getPromptSpecifications_returnsAllFivePrompts() {
        List<McpServerFeatures.SyncPromptSpecification> specs = provider.getPromptSpecifications();
        assertThat(specs).hasSize(5);
    }

    @Test
    void getPromptSpecifications_hasExpectedNames() {
        List<String> names = provider.getPromptSpecifications().stream()
                .map(s -> s.prompt().name())
                .toList();
        assertThat(names).containsExactlyInAnyOrder(
                "summarize-document",
                "sql-query-helper",
                "code-review",
                "explain-concept",
                "compare-and-contrast"
        );
    }

    // ── summarize-document ────────────────────────────────────────────────────

    @Test
    void summarizeDocument_defaultStyle_returnsBriefPrompt() {
        McpSchema.GetPromptResult result = invokePrompt("summarize-document",
                Map.of("content", "Some document text"));
        String text = extractMessageText(result);
        assertThat(text).containsIgnoringCase("summary").contains("Some document text");
    }

    @Test
    void summarizeDocument_bulletPointsStyle_returnsBulletPrompt() {
        McpSchema.GetPromptResult result = invokePrompt("summarize-document",
                Map.of("content", "Content here", "style", "bullet-points"));
        assertThat(extractMessageText(result)).containsIgnoringCase("bullet");
    }

    @Test
    void summarizeDocument_detailedStyle_returnsDetailedPrompt() {
        McpSchema.GetPromptResult result = invokePrompt("summarize-document",
                Map.of("content", "Content here", "style", "detailed"));
        assertThat(extractMessageText(result)).containsIgnoringCase("comprehensive");
    }

    @Test
    void summarizeDocument_nullArguments_usesDefaults() {
        McpSchema.GetPromptResult result = invokePrompt("summarize-document", null);
        assertThat(result.messages()).isNotEmpty();
    }

    // ── sql-query-helper ──────────────────────────────────────────────────────

    @Test
    void sqlQueryHelper_withQuestion_returnsQueryPrompt() {
        McpSchema.GetPromptResult result = invokePrompt("sql-query-helper",
                Map.of("question", "Show me all employees with salary > 100000"));
        String text = extractMessageText(result);
        assertThat(text).containsIgnoringCase("SQL").contains("Show me all employees");
    }

    @Test
    void sqlQueryHelper_includesDefaultTableSchema() {
        McpSchema.GetPromptResult result = invokePrompt("sql-query-helper",
                Map.of("question", "How many products?"));
        assertThat(extractMessageText(result)).containsIgnoringCase("employees");
    }

    @Test
    void sqlQueryHelper_customTables_overridesDefault() {
        McpSchema.GetPromptResult result = invokePrompt("sql-query-helper",
                Map.of("question", "get data", "tables", "custom_table (id, value)"));
        assertThat(extractMessageText(result)).contains("custom_table");
    }

    // ── code-review ───────────────────────────────────────────────────────────

    @Test
    void codeReview_withCode_returnsReviewPrompt() {
        McpSchema.GetPromptResult result = invokePrompt("code-review",
                Map.of("code", "public class Foo { }"));
        String text = extractMessageText(result);
        assertThat(text).containsIgnoringCase("review").contains("public class Foo");
    }

    @Test
    void codeReview_withLanguageAndFocus_includesInPrompt() {
        McpSchema.GetPromptResult result = invokePrompt("code-review",
                Map.of("code", "def foo(): pass", "language", "python", "focus", "security"));
        String text = extractMessageText(result);
        assertThat(text).containsIgnoringCase("python").containsIgnoringCase("security");
    }

    @Test
    void codeReview_descriptionContainsLanguage() {
        McpSchema.GetPromptResult result = invokePrompt("code-review",
                Map.of("code", "...", "language", "kotlin"));
        assertThat(result.description()).containsIgnoringCase("kotlin");
    }

    // ── explain-concept ───────────────────────────────────────────────────────

    @Test
    void explainConcept_beginnerLevel_usesSimpleLanguage() {
        McpSchema.GetPromptResult result = invokePrompt("explain-concept",
                Map.of("concept", "dependency injection", "level", "beginner"));
        assertThat(extractMessageText(result)).containsIgnoringCase("analogies")
                    .contains("dependency injection");
    }

    @Test
    void explainConcept_advancedLevel_includesEdgeCases() {
        McpSchema.GetPromptResult result = invokePrompt("explain-concept",
                Map.of("concept", "JVM garbage collection", "level", "advanced"));
        assertThat(extractMessageText(result)).containsIgnoringCase("edge cases")
                .containsIgnoringCase("performance");
    }

    @Test
    void explainConcept_defaultLevel_intermediateLanguage() {
        McpSchema.GetPromptResult result = invokePrompt("explain-concept",
                Map.of("concept", "REST API"));
        assertThat(extractMessageText(result)).containsIgnoringCase("intermediate")
                .contains("REST API");
    }

    // ── compare-and-contrast ──────────────────────────────────────────────────

    @Test
    void compareAndContrast_twoItems_returnsComparisonPrompt() {
        McpSchema.GetPromptResult result = invokePrompt("compare-and-contrast",
                Map.of("item1", "Spring Boot", "item2", "Quarkus"));
        String text = extractMessageText(result);
        assertThat(text).contains("Spring Boot").contains("Quarkus");
    }

    @Test
    void compareAndContrast_withContext_includesContext() {
        McpSchema.GetPromptResult result = invokePrompt("compare-and-contrast",
                Map.of("item1", "REST", "item2", "GraphQL", "context", "for a mobile app"));
        assertThat(extractMessageText(result)).contains("for a mobile app");
    }

    @Test
    void compareAndContrast_description_containsBothItems() {
        McpSchema.GetPromptResult result = invokePrompt("compare-and-contrast",
                Map.of("item1", "Kafka", "item2", "RabbitMQ"));
        assertThat(result.description()).contains("Kafka").contains("RabbitMQ");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Finds a prompt spec by name and invokes it with the given arguments.
     */
    private McpSchema.GetPromptResult invokePrompt(String name, Map<String, Object> args) {
        McpServerFeatures.SyncPromptSpecification spec = provider.getPromptSpecifications().stream()
                .filter(s -> s.prompt().name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Prompt not found: " + name));

        McpSchema.GetPromptRequest request = new McpSchema.GetPromptRequest(name, args);
        return spec.promptHandler().apply(null, request);
    }

    private String extractMessageText(McpSchema.GetPromptResult result) {
        assertThat(result.messages()).isNotEmpty();
        McpSchema.PromptMessage first = result.messages().getFirst();
        assertThat(first.content()).isInstanceOf(McpSchema.TextContent.class);
        return ((McpSchema.TextContent) first.content()).text();
    }
}


