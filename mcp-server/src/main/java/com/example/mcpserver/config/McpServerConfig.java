package com.example.mcpserver.config;

import com.example.mcpserver.prompts.DemoPromptProvider;
import com.example.mcpserver.resources.KnowledgeBaseResourceProvider;
import com.example.mcpserver.tools.CodeAnalysisTool;
import com.example.mcpserver.tools.CurrencyConverterTool;
import com.example.mcpserver.tools.DatabaseQueryTool;
import com.example.mcpserver.tools.FileSystemTool;
import com.example.mcpserver.tools.NotificationTool;
import com.example.mcpserver.tools.WeatherTool;
import io.modelcontextprotocol.server.McpServerFeatures;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * MCP Server Configuration
 *
 * This configuration class wires together all MCP capabilities:
 *
 * 1. TOOLS — Each tool is registered as its own ToolCallbackProvider bean,
 *    following Spring AI 1.0.0's recommended per-component registration pattern.
 *    The MCP server auto-configuration discovers all ToolCallbackProvider beans
 *    in the context and merges them automatically.
 *
 * 2. RESOURCES — Registered as List<McpServerFeatures.SyncResourceSpecification> beans.
 *
 * 3. PROMPTS — Registered as List<McpServerFeatures.SyncPromptSpecification> beans.
 */
@Configuration
public class McpServerConfig {

    /**
     * File system tool — exposed as its own ToolCallbackProvider.
     * Per-bean registration improves modularity and enables individual bean lifecycle control.
     */
    @Bean
    public ToolCallbackProvider fileSystemToolProvider(FileSystemTool fileSystemTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(fileSystemTool)
                .build();
    }

    /**
     * Database query tool — exposed as its own ToolCallbackProvider.
     */
    @Bean
    public ToolCallbackProvider databaseQueryToolProvider(DatabaseQueryTool databaseQueryTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(databaseQueryTool)
                .build();
    }

    /**
     * Weather tool — exposed as its own ToolCallbackProvider.
     */
    @Bean
    public ToolCallbackProvider weatherToolProvider(WeatherTool weatherTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherTool)
                .build();
    }

    /**
     * Code analysis tool — exposed as its own ToolCallbackProvider.
     */
    @Bean
    public ToolCallbackProvider codeAnalysisToolProvider(CodeAnalysisTool codeAnalysisTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(codeAnalysisTool)
                .build();
    }

    /**
     * Currency converter tool — exposed as its own ToolCallbackProvider.
     */
    @Bean
    public ToolCallbackProvider currencyConverterToolProvider(CurrencyConverterTool currencyConverterTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(currencyConverterTool)
                .build();
    }

    /**
     * Notification tool — exposed as its own ToolCallbackProvider.
     */
    @Bean
    public ToolCallbackProvider notificationToolProvider(NotificationTool notificationTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(notificationTool)
                .build();
    }

    /**
     * Register MCP Resources from the knowledge base.
     */
    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> resourceSpecifications(
            KnowledgeBaseResourceProvider knowledgeBaseProvider) {
        return knowledgeBaseProvider.getResourceSpecifications();
    }

    /**
     * Register MCP Prompt templates.
     */
    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> promptSpecifications(
            DemoPromptProvider promptProvider) {
        return promptProvider.getPromptSpecifications();
    }
}
