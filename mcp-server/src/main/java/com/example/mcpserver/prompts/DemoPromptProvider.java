package com.example.mcpserver.prompts;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP Prompt Provider: Reusable Prompt Templates
 *
 * Demonstrates MCP Prompts — a capability that lets servers define reusable,
 * parameterized prompt templates. These are NOT the same as ad-hoc user messages;
 * they are structured templates that guide the LLM toward specific tasks.
 *
 * MCP Prompts support:
 * - Named, discoverable templates
 * - Required and optional arguments
 * - Multi-message prompt sequences (system + user messages)
 * - Dynamic content generation based on arguments
 *
 * Real-world use cases:
 * - Standardized code review prompts across teams
 * - Consistent report generation templates
 * - Domain-specific analysis prompts (legal, medical, financial)
 * - Multi-step reasoning chains
 */
@Component
public class DemoPromptProvider {

    /**
     * Creates MCP prompt specifications for all demo prompt templates.
     */
    public List<McpServerFeatures.SyncPromptSpecification> getPromptSpecifications() {
        return List.of(
                createSummarizeDocumentPrompt(),
                createSqlQueryHelperPrompt(),
                createCodeReviewPrompt(),
                createExplainConceptPrompt(),
                createCompareAndContrastPrompt()
        );
    }

    /**
     * Prompt: Summarize Document
     * Takes document content and produces a structured summary.
     */
    private McpServerFeatures.SyncPromptSpecification createSummarizeDocumentPrompt() {
        var prompt = new McpSchema.Prompt(
                "summarize-document",
                "Summarize a document with key points, main ideas, and a brief conclusion",
                List.of(
                        new McpSchema.PromptArgument("content", "The document text to summarize", true),
                        new McpSchema.PromptArgument("style", "Summary style: 'brief', 'detailed', or 'bullet-points' (default: brief)", false)
                )
        );

        return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, request) -> {
            String content = arg(request.arguments(), "content", "");
            String style = arg(request.arguments(), "style", "brief");

            String instruction = switch (style) {
                case "detailed" -> "Provide a comprehensive, detailed summary of the following document. " +
                        "Include all key points, supporting arguments, and conclusions.";
                case "bullet-points" -> "Summarize the following document as a list of bullet points. " +
                        "Each bullet should capture one key idea or fact.";
                default -> "Provide a brief, concise summary of the following document in 2-3 paragraphs.";
            };

            return new McpSchema.GetPromptResult(
                    "Summarize a document in " + style + " style",
                    List.of(
                            new McpSchema.PromptMessage(
                                    McpSchema.Role.USER,
                                    new McpSchema.TextContent(instruction + "\n\n---\n\n" + content)
                            )
                    )
            );
        });
    }

    /**
     * Prompt: SQL Query Helper
     * Helps users construct SQL queries from natural language descriptions.
     */
    private McpServerFeatures.SyncPromptSpecification createSqlQueryHelperPrompt() {
        var prompt = new McpSchema.Prompt(
                "sql-query-helper",
                "Generate a SQL query from a natural language description of the data needed",
                List.of(
                        new McpSchema.PromptArgument("question", "Natural language description of the data you want", true),
                        new McpSchema.PromptArgument("tables", "Available table names and their columns (optional)", false)
                )
        );

        return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, request) -> {
            String question = arg(request.arguments(), "question", "");
            String tables = arg(request.arguments(), "tables",
                    "employees (id, name, email, department, salary, hire_date), " +
                            "departments (id, name, budget, manager_name), " +
                            "products (id, name, category, price, stock_quantity), " +
                            "orders (id, product_id, customer_name, quantity, order_date, status)");

            String systemMessage = """
                    You are a SQL expert. Generate a SQL SELECT query based on the user's question.
                    
                    Available tables and columns:
                    %s
                    
                    Rules:
                    - Only generate SELECT queries
                    - Use standard SQL syntax compatible with H2 database
                    - Include helpful comments explaining the query
                    - If the question is ambiguous, provide the most likely interpretation
                    """.formatted(tables);

            return new McpSchema.GetPromptResult(
                    "SQL query generation from natural language",
                    List.of(
                            new McpSchema.PromptMessage(
                                    McpSchema.Role.USER,
                                    new McpSchema.TextContent(systemMessage + "\n\nQuestion: " + question)
                            )
                    )
            );
        });
    }

    /**
     * Prompt: Code Review
     * Provides structured code review feedback.
     */
    private McpServerFeatures.SyncPromptSpecification createCodeReviewPrompt() {
        var prompt = new McpSchema.Prompt(
                "code-review",
                "Perform a structured code review on the provided code",
                List.of(
                        new McpSchema.PromptArgument("code", "The source code to review", true),
                        new McpSchema.PromptArgument("language", "Programming language (e.g., java, python)", false),
                        new McpSchema.PromptArgument("focus", "Review focus: 'security', 'performance', 'readability', or 'all' (default: all)", false)
                )
        );

        return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, request) -> {
            String code = arg(request.arguments(), "code", "");
            String language = arg(request.arguments(), "language", "java");
            String focus = arg(request.arguments(), "focus", "all");

            String reviewInstruction = """
                    You are an experienced %s code reviewer. Review the following code and provide feedback.
                    
                    Focus areas: %s
                    
                    Structure your review as:
                    1. **Overview**: Brief description of what the code does
                    2. **Strengths**: What's done well
                    3. **Issues**: Problems found (categorized by severity: 🔴 Critical, 🟡 Warning, 🔵 Info)
                    4. **Suggestions**: Specific improvement recommendations with code examples
                    5. **Rating**: Overall code quality score (1-10)
                    """.formatted(language, focus);

            return new McpSchema.GetPromptResult(
                    "Code review for " + language + " code",
                    List.of(
                            new McpSchema.PromptMessage(
                                    McpSchema.Role.USER,
                                    new McpSchema.TextContent(reviewInstruction + "\n\n```" + language + "\n" + code + "\n```")
                            )
                    )
            );
        });
    }

    /**
     * Prompt: Explain Concept
     * Explains a technical concept at a specified expertise level.
     */
    private McpServerFeatures.SyncPromptSpecification createExplainConceptPrompt() {
        var prompt = new McpSchema.Prompt(
                "explain-concept",
                "Explain a technical concept at the specified level of expertise",
                List.of(
                        new McpSchema.PromptArgument("concept", "The concept to explain", true),
                        new McpSchema.PromptArgument("level", "Expertise level: 'beginner', 'intermediate', 'advanced' (default: intermediate)", false)
                )
        );

        return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, request) -> {
            String concept = arg(request.arguments(), "concept", "");
            String level = arg(request.arguments(), "level", "intermediate");

            String instruction = switch (level) {
                case "beginner" -> "Explain the following concept as if you're talking to someone who is new to programming. " +
                        "Use simple analogies, avoid jargon, and include a basic example.";
                case "advanced" -> "Provide an in-depth, advanced explanation of the following concept. " +
                        "Include implementation details, edge cases, performance considerations, " +
                        "and comparisons with alternative approaches.";
                default -> "Explain the following concept for someone with intermediate programming experience. " +
                        "Include a clear definition, practical example, and common pitfalls.";
            };

            return new McpSchema.GetPromptResult(
                    "Explain '" + concept + "' at " + level + " level",
                    List.of(
                            new McpSchema.PromptMessage(
                                    McpSchema.Role.USER,
                                    new McpSchema.TextContent(instruction + "\n\nConcept: " + concept)
                            )
                    )
            );
        });
    }

    /**
     * Prompt: Compare and Contrast
     * Compares two technologies, approaches, or concepts.
     */
    private McpServerFeatures.SyncPromptSpecification createCompareAndContrastPrompt() {
        var prompt = new McpSchema.Prompt(
                "compare-and-contrast",
                "Compare two technologies, approaches, or concepts with pros/cons analysis",
                List.of(
                        new McpSchema.PromptArgument("item1", "First item to compare", true),
                        new McpSchema.PromptArgument("item2", "Second item to compare", true),
                        new McpSchema.PromptArgument("context", "Context for the comparison (e.g., 'for a microservices project')", false)
                )
        );

        return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, request) -> {
            String item1 = arg(request.arguments(), "item1", "");
            String item2 = arg(request.arguments(), "item2", "");
            String context = arg(request.arguments(), "context", "general software development");

            String instruction = """
                    Compare and contrast %s vs %s in the context of %s.
                    
                    Structure your comparison as:
                    1. **Brief Overview** of each
                    2. **Comparison Table** with key criteria
                    3. **Pros and Cons** for each
                    4. **When to Use Each** — specific scenarios
                    5. **Recommendation** based on the given context
                    """.formatted(item1, item2, context);

            return new McpSchema.GetPromptResult(
                    "Comparing " + item1 + " vs " + item2,
                    List.of(
                            new McpSchema.PromptMessage(
                                    McpSchema.Role.USER,
                                    new McpSchema.TextContent(instruction)
                            )
                    )
            );
        });
    }

    /**
     * Safely extract a String argument from the prompt request arguments map.
     * The MCP SDK uses Map<String, Object>, so we need to convert to String.
     */
    private static String arg(Map<String, Object> arguments, String key, String defaultValue) {
        if (arguments == null) return defaultValue;
        Object value = arguments.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}



