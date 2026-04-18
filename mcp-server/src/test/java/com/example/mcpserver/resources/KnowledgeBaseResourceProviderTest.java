package com.example.mcpserver.resources;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for KnowledgeBaseResourceProvider.
 */
class KnowledgeBaseResourceProviderTest {

    private final KnowledgeBaseResourceProvider provider = new KnowledgeBaseResourceProvider();

    @Test
    void getResourceSpecifications_returnsNonEmptyList() {
        List<McpServerFeatures.SyncResourceSpecification> specs = provider.getResourceSpecifications();
        assertThat(specs).isNotEmpty();
    }

    @Test
    void getResourceSpecifications_allHaveKbUriScheme() {
        provider.getResourceSpecifications().forEach(spec -> {
            assertThat(spec.resource().uri()).startsWith("kb://");
        });
    }

    @Test
    void getResourceSpecifications_allHaveMarkdownMimeType() {
        provider.getResourceSpecifications().forEach(spec -> {
            assertThat(spec.resource().mimeType()).isEqualTo("text/markdown");
        });
    }

    @Test
    void getResourceSpecifications_expectedKnowledgeBaseDocumentsPresent() {
        List<String> uris = provider.getResourceSpecifications().stream()
                .map(s -> s.resource().uri())
                .toList();
        // The three known KB documents
        assertThat(uris).containsAnyOf("kb://mcp-protocol", "kb://spring-ai-overview", "kb://ollama-guide");
    }

    @Test
    void readResource_returnsNonEmptyContent() {
        List<McpServerFeatures.SyncResourceSpecification> specs = provider.getResourceSpecifications();
        assertThat(specs).isNotEmpty();

        McpServerFeatures.SyncResourceSpecification spec = specs.getFirst();
        McpSchema.ReadResourceRequest request = new McpSchema.ReadResourceRequest(spec.resource().uri());
        McpSchema.ReadResourceResult result = spec.readHandler().apply(null, request);

        assertThat(result.contents()).isNotEmpty();
        McpSchema.ResourceContents contents = result.contents().getFirst();
        assertThat(contents).isInstanceOf(McpSchema.TextResourceContents.class);
        assertThat(((McpSchema.TextResourceContents) contents).text()).isNotBlank();
    }

    @Test
    void readResource_allDocuments_readSuccessfully() {
        for (McpServerFeatures.SyncResourceSpecification spec : provider.getResourceSpecifications()) {
            McpSchema.ReadResourceRequest request = new McpSchema.ReadResourceRequest(spec.resource().uri());
            McpSchema.ReadResourceResult result = spec.readHandler().apply(null, request);
            assertThat(result.contents()).as("Resource %s should have content", spec.resource().uri())
                    .isNotEmpty();
        }
    }

    @Test
    void resourceNames_areCapitalizedAndHumanReadable() {
        provider.getResourceSpecifications().forEach(spec -> {
            String name = spec.resource().name();
            assertThat(name).isNotBlank();
            // First letter should be uppercase
            assertThat(Character.isUpperCase(name.charAt(0))).isTrue();
            // Should not contain file extension
            assertThat(name).doesNotEndWith(".md");
        });
    }
}



