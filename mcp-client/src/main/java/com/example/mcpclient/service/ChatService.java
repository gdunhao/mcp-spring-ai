package com.example.mcpclient.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Chat Service
 *
 * Encapsulates all AI interaction logic. The ChatClient is pre-configured with:
 * - Ollama (qwen3:0.6b) as the LLM
 * - All MCP-discovered tools as available function calls
 * - A system prompt guiding the AI's behavior
 *
 * When a user sends a message:
 * 1. The message goes to Ollama
 * 2. Ollama analyzes the message and may request tool calls
 * 3. Spring AI executes the tool calls via MCP
 * 4. Tool results go back to Ollama
 * 5. Ollama generates the final response
 *
 * This multi-step process is handled transparently by Spring AI's ChatClient.
 */
@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Send a message and get a complete response.
     * Tool calls are executed automatically during this process.
     */
    public String chat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * Send a message with a specific system instruction override.
     * Useful for demo scenarios that need specialized behavior.
     */
    public String chatWithSystem(String systemMessage, String userMessage) {
        return chatClient.prompt()
                .system(systemMessage)
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * Stream a response token-by-token via SSE.
     * Note: Tool calls are still executed in full before streaming begins.
     */
    public Flux<String> chatStream(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }
}

