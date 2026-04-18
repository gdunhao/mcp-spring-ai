package com.example.mcpclient.controller;

import com.example.mcpclient.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demo Controller — Pre-built Scenarios
 *
 * Provides ready-to-run demo endpoints that showcase each MCP capability
 * with carefully crafted prompts.
 *
 * Uses Java records for all request/response types.
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    // ── Records ───────────────────────────────────────────────────────────────

    public record DemoResponse(String demo, String description, String response) {}

    public record CustomDemoRequest(String message) {}

    // ─────────────────────────────────────────────────────────────────────────

    private final ChatService chatService;

    public DemoController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * List all available demo scenarios with descriptions and curl commands.
     */
    @GetMapping("/scenarios")
    public Map<String, Object> listScenarios() {
        // LinkedHashMap preserves insertion order for predictable JSON output
        Map<String, Object> scenarios = new LinkedHashMap<>();

        scenarios.put("file-search", Map.of(
                "description", "LLM lists files in the workspace, reads a file, and creates a summary",
                "curl", "curl http://localhost:8080/demo/file-search"
        ));
        scenarios.put("db-query", Map.of(
                "description", "LLM translates natural language to SQL and queries the database",
                "curl", "curl http://localhost:8080/demo/db-query"
        ));
        scenarios.put("weather", Map.of(
                "description", "LLM fetches weather data and provides a travel recommendation",
                "curl", "curl http://localhost:8080/demo/weather"
        ));
        scenarios.put("code-review", Map.of(
                "description", "LLM analyzes source code files and provides review feedback",
                "curl", "curl http://localhost:8080/demo/code-review"
        ));
        scenarios.put("knowledge-qa", Map.of(
                "description", "LLM answers questions using knowledge base documents",
                "curl", "curl http://localhost:8080/demo/knowledge-qa"
        ));
        scenarios.put("custom", Map.of(
                "description", "Send your own message with: POST /chat {\"message\": \"your question\"}",
                "curl", "curl -X POST http://localhost:8080/chat -H 'Content-Type: application/json' -d '{\"message\": \"your question here\"}'"
        ));

        return Map.of("available_demos", scenarios);
    }

    @GetMapping("/file-search")
    public DemoResponse fileSearchDemo() {
        String prompt = """
                Please do the following:
                1. List all files in the workspace root directory
                2. If there are any text or markdown files, read one of them
                3. Write a brief summary of what you found to a new file called "workspace-summary.txt"
                4. Tell me what you did and what you found
                """;
        return new DemoResponse(
                "file-search",
                "File system exploration, reading, and writing via MCP tools",
                chatService.chat(prompt)
        );
    }

    @GetMapping("/db-query")
    public DemoResponse dbQueryDemo() {
        String prompt = """
                I need a business report. Please:
                1. First, list the available tables in the database to understand the schema
                2. Find the top 5 highest-paid employees and their departments
                3. Show me the total order value by product (quantity × price), ordered by value descending
                4. Summarize your findings in a brief executive report
                """;
        return new DemoResponse(
                "db-query",
                "Natural language to SQL — querying employees, products, and orders",
                chatService.chat(prompt)
        );
    }

    @GetMapping("/weather")
    public DemoResponse weatherDemo() {
        String prompt = """
                I'm planning a trip and considering three destinations: Tokyo, Paris, and Sydney.
                Please:
                1. Get the current weather for all three cities
                2. Compare the weather conditions between them
                3. Based on the weather, recommend which city would be best to visit right now
                4. Also show me the 3-day forecast for your recommended city
                """;
        return new DemoResponse(
                "weather",
                "Multi-city weather comparison and travel recommendation",
                chatService.chat(prompt)
        );
    }

    @GetMapping("/code-review")
    public DemoResponse codeReviewDemo() {
        String prompt = """
                Please analyze the MCP server source code:
                1. Scan the directory "mcp-server/src/main/java" for Java files
                2. Pick one of the tool classes and analyze it in detail
                3. Search for any TODO or FIXME comments across the codebase
                4. Provide a brief code quality summary with suggestions
                """;
        return new DemoResponse(
                "code-review",
                "AI-powered code analysis and review via MCP tools",
                chatService.chat(prompt)
        );
    }

    @GetMapping("/knowledge-qa")
    public DemoResponse knowledgeQaDemo() {
        String prompt = """
                I'm new to MCP and Spring AI. Can you help me understand:
                1. What is the Model Context Protocol (MCP) and why was it created?
                2. What are the main differences between MCP tools, resources, and prompts?
                3. How does Spring AI integrate with MCP?
                4. What transport modes does MCP support and when should I use each?
                
                Please provide a comprehensive but beginner-friendly explanation.
                """;
        return new DemoResponse(
                "knowledge-qa",
                "Knowledge base Q&A about MCP and Spring AI concepts",
                chatService.chat(prompt)
        );
    }

    /**
     * Run a custom demo by name with an optional custom prompt.
     */
    @PostMapping("/{scenario}")
    public DemoResponse customDemo(
            @PathVariable String scenario,
            @RequestBody(required = false) CustomDemoRequest request) {

        String customPrompt = request != null && request.message() != null ? request.message() : "";

        if (!customPrompt.isBlank()) {
            return new DemoResponse(scenario, "Custom prompt", chatService.chat(customPrompt));
        }

        return switch (scenario) {
            case "file-search" -> fileSearchDemo();
            case "db-query" -> dbQueryDemo();
            case "weather" -> weatherDemo();
            case "code-review" -> codeReviewDemo();
            case "knowledge-qa" -> knowledgeQaDemo();
            default -> new DemoResponse(scenario, "Error",
                    "Unknown scenario: " + scenario + ". Use GET /demo/scenarios to see available options.");
        };
    }
}

