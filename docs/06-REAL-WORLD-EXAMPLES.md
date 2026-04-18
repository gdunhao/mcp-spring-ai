# 06 — Real-World Examples: Production MCP Patterns

## Table of Contents
- [Overview](#overview)
- [Example 1: Multi-Tool Orchestration](#example-1-multi-tool-orchestration)
- [Example 2: Currency Converter — Fintech Pattern](#example-2-currency-converter--fintech-pattern)
- [Example 3: Notification Service — Enterprise Messaging Pattern](#example-3-notification-service--enterprise-messaging-pattern)
- [Production Patterns & Best Practices](#production-patterns--best-practices)
- [Anti-Patterns to Avoid](#anti-patterns-to-avoid)

---

## Overview

This document covers the **real-world MCP examples** added to the demo project.
Each example demonstrates a production-grade pattern that you can adapt for your own MCP servers.

| Example | Tool | Pattern | Real-World Analog |
|---------|------|---------|-------------------|
| Multi-Tool Orchestration | All tools | **Agentic workflow** | Executive dashboard, incident response |
| Currency Converter | `CurrencyConverterTool` | **External API wrapper** | Fintech apps, international e-commerce |
| Notification Service | `NotificationTool` | **Side-effect tool with audit trail** | Ops alerting, customer comms |

---

## Example 1: Multi-Tool Orchestration

**Demo endpoint:** `GET /demo/multi-tool`

### What It Demonstrates

The most powerful MCP pattern: an LLM autonomously orchestrating **5 different tools**
in a single request to complete a real business workflow.

```
User: "Prepare an executive briefing"
  → LLM plans the workflow
  → Tool 1: DatabaseQueryTool.executeQuery() — get top orders
  → Tool 2: WeatherTool.getCurrentWeather("Tokyo") — office weather
  → Tool 3: WeatherTool.getCurrentWeather("New York")
  → Tool 4: CurrencyConverterTool.convertCurrency() — USD → EUR/JPY
  → Tool 5: FileSystemTool.writeFile() — save briefing
  → Tool 6: NotificationTool.sendNotification() — alert team
  → Final response: unified executive briefing
```

### Why This Matters

In production, this pattern enables:
- **Incident response bots** that query monitoring, check logs, notify on-call, and create tickets
- **Daily standup assistants** that pull JIRA data, Git commits, and calendar events
- **Customer support agents** that look up orders, check inventory, and draft responses

### Key Insight: Composability

The LLM decides the execution order based on data dependencies. MCP servers don't
need to know about each other — the client (with the LLM) handles orchestration.

```
┌─────────────┐     ┌──────────────┐     ┌────────────────┐
│  DB Server   │     │Weather Server│     │Notification Svc│
│  (MCP)       │     │  (MCP)       │     │   (MCP)        │
└──────┬───────┘     └──────┬───────┘     └───────┬────────┘
       │                    │                     │
       └────────────┬───────┘─────────────────────┘
                    │
            ┌───────┴────────┐
            │  MCP Client    │
            │  + LLM         │
            │  (orchestrator)│
            └────────────────┘
```

### Try It

```bash
curl http://localhost:8080/demo/multi-tool | jq

# Or in chat with a custom workflow:
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Check the weather in London, find employees in the Engineering department, convert their average salary to GBP, and notify #hr-team on Slack with a summary"}' | jq
```

---

## Example 2: Currency Converter — Fintech Pattern

**Demo endpoint:** `GET /demo/currency`

### Tools Provided

| Tool | Description | Parameters |
|------|-------------|------------|
| `convertCurrency` | Convert amount between currencies | `amount`, `fromCurrency`, `toCurrency` |
| `getExchangeRates` | Show all rates for a base currency | `baseCurrency` |
| `calculateMultiCurrencyTotal` | Total items in different currencies | `items`, `targetCurrency` |

### Supported Currencies
USD, EUR, GBP, JPY, BRL, CAD, AUD, CHF, CNY, INR, MXN, SGD, AED, KRW

### Pattern: External API Wrapper

This tool uses mock data, but the conversion to a real API is trivial:

```java
// Mock version (this demo)
private static final Map<String, Double> RATES_TO_USD = Map.of("EUR", 0.92, ...);

// Production version — same @Tool interface, different implementation
@Tool(description = "Convert an amount from one currency to another")
public String convertCurrency(double amount, String from, String to) {
    // Call a real API — the MCP contract doesn't change
    var rates = restClient.get()
        .uri("https://api.exchangerate-api.com/v4/latest/{base}", from)
        .retrieve()
        .body(ExchangeRateResponse.class);
    double converted = amount * rates.getRates().get(to);
    return formatResult(amount, from, converted, to);
}
```

### Design Decisions

1. **`calculateMultiCurrencyTotal`** — A higher-level tool that reduces round-trips.
   Instead of the LLM calling `convertCurrency` N times, it calls this once.
   **Best practice:** Provide batch operations for common multi-step workflows.

2. **String return type** — All results are formatted markdown tables.
   The LLM can parse and re-format these naturally.

3. **Input normalization** — Currency codes are uppercased and trimmed internally.
   **Best practice:** Be lenient in what you accept, strict in what you produce.

### Try It

```bash
curl http://localhost:8080/demo/currency | jq

# Custom conversion
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Convert 10000 JPY to BRL and tell me the exchange rate"}' | jq

# Multi-currency invoice
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Calculate the USD total for: 3000 EUR, 2500 GBP, 500000 JPY, 25000 MXN"}' | jq
```

---

## Example 3: Notification Service — Enterprise Messaging Pattern

**Demo endpoint:** `GET /demo/notification`

### Tools Provided

| Tool | Description | Parameters |
|------|-------------|------------|
| `sendNotification` | Send via email/Slack/SMS | `channel`, `recipient`, `subject`, `body` |
| `sendBulkNotification` | Multi-recipient send | `channel`, `recipients`, `subject`, `body` |
| `getNotificationLog` | View delivery audit trail | `limit` |

### Pattern: Side-Effect Tool with Audit Trail

Unlike read-only tools (weather, currency), notifications **change state** (send messages).
This requires additional safety patterns:

```java
// 1. INPUT VALIDATION — reject bad data before acting
private String validateChannel(String channel, String recipient) {
    return switch (channel) {
        case "email" -> !recipient.contains("@") ? "❌ Invalid email" : null;
        case "slack" -> !recipient.startsWith("#") ? "❌ Use #channel format" : null;
        // ...
    };
}

// 2. AUDIT TRAIL — log every action for accountability
private final List<NotificationRecord> sentLog = new CopyOnWriteArrayList<>();

// 3. TRACKING IDS — every action gets a unique reference
String trackingId = UUID.randomUUID().toString().substring(0, 8);
```

### Production Considerations

| Concern | Demo Implementation | Production Implementation |
|---------|-------------------|--------------------------|
| Delivery | Mock (instant) | Async with retry + dead-letter queue |
| Rate limiting | None | Token bucket per channel per minute |
| Authentication | None | OAuth2 for Slack, API keys for SMS/email |
| Audit | In-memory list | Persistent database + event stream |
| Idempotency | None | Dedup by content hash + time window |

### Try It

```bash
curl http://localhost:8080/demo/notification | jq

# Custom notification
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Send a Slack message to #general saying the weekly report is ready, then check the delivery log"}' | jq

# Bulk notification
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Send an email to alice@company.com, bob@company.com, and carol@company.com about the team meeting tomorrow at 3pm"}' | jq
```

---

## Production Patterns & Best Practices

### 1. Tool Granularity: Fine vs Coarse

| Approach | Example | When to Use |
|----------|---------|-------------|
| **Fine-grained** | `convertCurrency(amount, from, to)` | Simple, composable operations |
| **Coarse-grained** | `calculateMultiCurrencyTotal(items, target)` | Common multi-step workflows |

**Rule of thumb:** Start fine-grained. Add coarse-grained tools when you see the LLM
repeatedly chaining the same 3+ tool calls.

### 2. Error Handling: Strings, Not Exceptions

```java
// ✅ Good — LLM can understand and recover
if (data == null) {
    return "Currency not found: " + code + ". Available: USD, EUR, GBP...";
}

// ❌ Bad — LLM gets an opaque error
if (data == null) {
    throw new IllegalArgumentException("Invalid currency code");
}
```

### 3. Tool Descriptions Are Your API Contract

The LLM reads descriptions to decide when and how to use tools. Invest time in them:

```java
// ✅ Good — specific, includes constraints and examples
@Tool(description = "Convert an amount from one currency to another. " +
        "Supported currencies: USD, EUR, GBP, JPY, BRL, CAD, AUD, CHF, CNY, INR, MXN, SGD, AED, KRW.")

// ❌ Bad — vague, LLM won't know when to use it
@Tool(description = "Currency tool")
```

### 4. Stateful Tools Need Thread Safety

The `NotificationTool` maintains an in-memory log. Use thread-safe collections:

```java
// ✅ Thread-safe for concurrent requests
private final List<NotificationRecord> sentLog = new CopyOnWriteArrayList<>();

// ❌ Not safe under concurrent access
private final List<NotificationRecord> sentLog = new ArrayList<>();
```

### 5. Per-Bean Tool Registration

Register each tool as its own `ToolCallbackProvider` bean for modularity:

```java
@Bean
public ToolCallbackProvider currencyToolProvider(CurrencyConverterTool tool) {
    return MethodToolCallbackProvider.builder().toolObjects(tool).build();
}
```

This allows individual tools to be enabled/disabled via Spring profiles or feature flags.

---

## Anti-Patterns to Avoid

### ❌ God Tool
Don't create one tool that does everything. Split responsibilities:
```
Bad:  doEverything(action, param1, param2, param3...)
Good: convertCurrency(), getExchangeRates(), calculateTotal()
```

### ❌ Raw Data Dumps
Don't return huge JSON blobs. Format results for LLM consumption:
```
Bad:  return objectMapper.writeValueAsString(hugeObject);
Good: return formatAsMarkdownTable(topResults);
```

### ❌ Tools Without Validation
Always validate inputs before performing actions, especially for side-effect tools:
```
Bad:  sendEmail(recipient, body)  // no validation
Good: if (!recipient.contains("@")) return "Invalid email...";
```

### ❌ Ignoring Rate Limits
In production, add rate limiting to prevent LLM loops from overwhelming external APIs.

---

## Summary

| Example | Pattern | Key Takeaway |
|---------|---------|--------------|
| Multi-Tool Orchestration | Agentic workflow | MCP tools compose naturally; the LLM is the orchestrator |
| Currency Converter | API wrapper + batch ops | Provide both fine-grained and coarse-grained tools |
| Notification Service | Side-effect + audit | Validate inputs, log actions, use tracking IDs |

These examples demonstrate that **MCP tools are production building blocks** — not just demos.
The same patterns scale from a single-server prototype to a multi-server enterprise deployment.

