package com.example.mcpclient.controller;

import com.example.mcpclient.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

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
        scenarios.put("multi-tool", Map.of(
                "description", "LLM orchestrates multiple tools (DB + Weather + Currency + Notifications) in a single workflow",
                "curl", "curl http://localhost:8080/demo/multi-tool"
        ));
        scenarios.put("currency", Map.of(
                "description", "LLM converts currencies, calculates multi-currency totals, and shows exchange rates",
                "curl", "curl http://localhost:8080/demo/currency"
        ));
        scenarios.put("notification", Map.of(
                "description", "LLM drafts and sends notifications via email, Slack, and SMS channels",
                "curl", "curl http://localhost:8080/demo/notification"
        ));
        scenarios.put("custom", Map.of(
                "description", "Send your own message with: POST /chat {\"message\": \"your question\"}",
                "curl", "curl -X POST http://localhost:8080/chat -H 'Content-Type: application/json' -d '{\"message\": \"your question here\"}'"
        ));

        return Map.of("available_demos", scenarios);
    }

    @GetMapping("/file-search")
    public DemoResponse fileSearchDemo() {
        log.info("GET /demo/file-search — running file system exploration demo");
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
        log.info("GET /demo/db-query — running database query demo");
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
        log.info("GET /demo/weather — running multi-city weather demo");
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
        log.info("GET /demo/code-review — running AI code review demo");
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
        log.info("GET /demo/knowledge-qa — running knowledge base Q&A demo");
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

    @GetMapping("/multi-tool")
    public DemoResponse multiToolDemo() {
        log.info("GET /demo/multi-tool — running multi-tool orchestration demo");
        String prompt = """
                You are an executive assistant preparing a briefing. Please complete this multi-step workflow:
                1. Query the database to find the top 3 highest-value orders (join orders with products to get price × quantity)
                2. Get the current weather for Tokyo and New York (our two main office locations)
                3. Convert the total order value you found from USD to EUR and JPY
                4. Write a brief executive summary to a file called "daily-briefing.txt" in the workspace
                5. Send a Slack notification to #executive-team with a one-line summary of the briefing
                
                Present a unified executive briefing with all findings.
                """;
        return new DemoResponse(
                "multi-tool",
                "Multi-tool orchestration: DB → Weather → Currency → File → Notification in one workflow",
                chatService.chat(prompt)
        );
    }

    @GetMapping("/currency")
    public DemoResponse currencyDemo() {
        log.info("GET /demo/currency — running currency conversion demo");
        String prompt = """
                I'm managing expenses for an international team. Please help me:
                1. Show the current exchange rates for USD against all available currencies
                2. Convert 5000 USD to EUR, GBP, and JPY
                3. Calculate the total of these international invoices in USD:
                   2500 EUR, 1800 GBP, 350000 JPY, 15000 BRL, 8500 INR
                4. Provide a summary with the total cost and a breakdown
                """;
        return new DemoResponse(
                "currency",
                "Currency conversion, exchange rates, and multi-currency invoice calculation",
                chatService.chat(prompt)
        );
    }

    @GetMapping("/notification")
    public DemoResponse notificationDemo() {
        log.info("GET /demo/notification — running multi-channel notification demo");
        String prompt = """
                We have a critical deployment happening. Please help me coordinate notifications:
                1. Send an email to ops-team@company.com with subject "Deployment v2.5 Starting" and a brief body about the deployment window
                2. Send a Slack message to #deployments with a shorter heads-up
                3. Send an SMS to +1555000123 for the on-call engineer with a brief alert
                4. Check the notification delivery log to confirm all messages were sent
                5. Summarize what notifications were sent and their delivery status
                """;
        return new DemoResponse(
                "notification",
                "Multi-channel notification orchestration: email, Slack, and SMS",
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
            log.info("POST /demo/{} — running with custom prompt, length: {}", scenario, customPrompt.length());
            return new DemoResponse(scenario, "Custom prompt", chatService.chat(customPrompt));
        }

        log.info("POST /demo/{} — delegating to built-in scenario", scenario);
        return switch (scenario) {
            case "file-search" -> fileSearchDemo();
            case "db-query" -> dbQueryDemo();
            case "weather" -> weatherDemo();
            case "code-review" -> codeReviewDemo();
            case "knowledge-qa" -> knowledgeQaDemo();
            case "multi-tool" -> multiToolDemo();
            case "currency" -> currencyDemo();
            case "notification" -> notificationDemo();
            default -> {
                log.warn("POST /demo/{} — unknown scenario requested", scenario);
                yield new DemoResponse(scenario, "Error",
                        "Unknown scenario: " + scenario + ". Use GET /demo/scenarios to see available options.");
            }
        };
    }
}

