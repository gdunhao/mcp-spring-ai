package com.example.mcpclient.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Client Configuration
 *
 * This configuration sets up the ChatClient with MCP tool integration.
 *
 * How it works:
 * 1. The spring-ai-starter-mcp-client auto-configures MCP client connections
 *    based on application.yaml settings (SSE URL or STDIO command).
 *
 * 2. The MCP client connects to the server and discovers all available tools.
 *    These tools are converted into ToolCallbackProvider instances.
 *
 * 3. We inject all ToolCallbackProvider beans (including MCP-discovered ones)
 *    into the ChatClient, making them available for the LLM to invoke.
 *
 * 4. When the LLM decides to call a tool, Spring AI:
 *    a. Serializes the tool call into MCP protocol format
 *    b. Sends it to the MCP server
 *    c. Receives the result
 *    d. Feeds it back to the LLM for final response generation
 *
 * The ChatClient.Builder is auto-configured by spring-ai-starter-model-ollama
 * with the Ollama ChatModel already wired in.
 */
@Configuration
public class McpClientConfig {

    private static final Logger log = LoggerFactory.getLogger(McpClientConfig.class);

    /**
     * Creates a ChatClient with all discovered MCP tools registered as default tools.
     *
     * The ToolCallbackProvider array is injected by Spring — it includes:
     * - Tools discovered from MCP server connections
     * - Any locally defined tool beans
     *
     * Using defaultTools() means these tools are available for EVERY chat interaction.
     * The LLM will see the tool descriptions and decide when to use them.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ToolCallbackProvider[] toolCallbackProviders) {
        log.info("Initializing ChatClient with {} ToolCallbackProvider(s)", toolCallbackProviders.length);
        ChatClient client = builder
                .defaultToolCallbacks(toolCallbackProviders)
                .defaultSystem("""
                        You are a helpful AI assistant with access to various tools.
                        You can:
                        - Read and write files in the workspace
                        - Query a database with SQL
                        - Check weather for cities worldwide
                        - Analyze Java source code
                        
                        When a user asks something that requires using a tool, use the appropriate
                        tool to get the information. Always explain what you're doing and present
                        the results in a clear, formatted way.
                        
                        If a tool returns an error, explain the error and suggest alternatives.
                        """)
                .build();
        log.info("ChatClient initialized successfully");
        return client;
    }
}

