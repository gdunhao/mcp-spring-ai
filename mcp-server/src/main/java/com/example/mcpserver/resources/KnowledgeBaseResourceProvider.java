package com.example.mcpserver.resources;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * MCP Resource Provider: Knowledge Base
 *
 * Demonstrates MCP Resources — a core MCP capability that lets servers expose
 * data and content that clients (and LLMs) can read. Unlike tools (which perform
 * actions), resources provide READ-ONLY access to information.
 *
 * This provider exposes markdown documents from the classpath as MCP resources
 * using the "kb://" URI scheme. Each document becomes a browsable, readable
 * resource that the LLM can access on demand.
 *
 * MCP Resources support:
 * - URI-based addressing (custom schemes allowed)
 * - MIME type declarations
 * - Text and binary content
 * - Resource templates (parameterized URIs)
 *
 * Real-world use cases:
 * - Company knowledge bases for AI assistants
 * - API documentation servers
 * - Configuration file browsers
 * - Log file viewers
 */
@Component
public class KnowledgeBaseResourceProvider {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseResourceProvider.class);

    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    /**
     * Creates MCP resource specifications for all markdown files in the knowledge-base directory.
     * Each file becomes an MCP resource accessible via kb://filename URI.
     */
    public List<McpServerFeatures.SyncResourceSpecification> getResourceSpecifications() {
        List<McpServerFeatures.SyncResourceSpecification> specifications = new ArrayList<>();

        try {
            Resource[] resources = resolver.getResources("classpath:knowledge-base/*.md");
            log.info("KnowledgeBaseResourceProvider — found {} knowledge base document(s)", resources.length);

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null) continue;

                String uri = "kb://" + filename.replace(".md", "");
                String name = filename.replace(".md", "").replace("-", " ");
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

                log.debug("KnowledgeBaseResourceProvider — registering resource: uri='{}', name='{}'", uri, name);

                // Create the MCP resource descriptor
                var mcpResource = new McpSchema.Resource(
                        uri,
                        name,
                        "Knowledge base document: " + name,
                        "text/markdown",
                        null  // no annotations
                );

                // Create the specification with a handler that reads the file content
                String resourcePath = "classpath:knowledge-base/" + filename;
                final String resourceName = name;
                specifications.add(new McpServerFeatures.SyncResourceSpecification(
                        mcpResource,
                        (exchange, request) -> {
                            log.debug("KnowledgeBaseResourceProvider — reading resource: '{}'", request.uri());
                            try {
                                Resource res = resolver.getResource(resourcePath);
                                String content = res.getContentAsString(StandardCharsets.UTF_8);
                                log.info("KnowledgeBaseResourceProvider — served resource '{}' ({} chars)",
                                        resourceName, content.length());
                                return new McpSchema.ReadResourceResult(List.of(
                                        new McpSchema.TextResourceContents(
                                                request.uri(),
                                                "text/markdown",
                                                content
                                        )
                                ));
                            } catch (IOException e) {
                                log.error("KnowledgeBaseResourceProvider — failed to read resource '{}': {}",
                                        resourcePath, e.getMessage(), e);
                                throw new RuntimeException("Failed to read resource: " + resourcePath, e);
                            }
                        }
                ));
            }
            log.info("KnowledgeBaseResourceProvider — registered {} MCP resource(s)", specifications.size());
        } catch (IOException e) {
            log.error("KnowledgeBaseResourceProvider — failed to scan knowledge base: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to scan knowledge base resources", e);
        }

        return specifications;
    }
}
