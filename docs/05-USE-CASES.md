# 05 — Use Cases: Real-World MCP Applications

## Table of Contents
- [Demo Walkthroughs](#demo-walkthroughs)
  - [Demo 1: File System Operations](#demo-1-file-system-operations)
  - [Demo 2: Database Querying](#demo-2-database-querying)
  - [Demo 3: Weather API Integration](#demo-3-weather-api-integration)
  - [Demo 4: Code Analysis](#demo-4-code-analysis)
  - [Demo 5: Knowledge Base Q&A](#demo-5-knowledge-base-qa)
- [Real-World MCP Applications](#real-world-mcp-applications)
- [Building Your Own MCP Tools](#building-your-own-mcp-tools)

---

## Demo Walkthroughs

### Demo 1: File System Operations

**MCP Capability:** Tools (`FileSystemTool`)
**Real-world analog:** AI-powered file managers, documentation generators

#### Run It
```bash
curl http://localhost:8080/demo/file-search | jq
```

#### What Happens

1. The LLM receives a prompt asking it to explore the workspace
2. It calls `listFiles(".")` to see what's in the workspace root
3. It finds `sample-report.md` and calls `readFile("sample-report.md")` to read it
4. It processes the content and calls `writeFile("workspace-summary.txt", "...")` to save a summary
5. It responds with a description of what it found and did

#### Behind the Scenes

```
User Prompt → Ollama LLM → "I should list files first"
                          → Tool Call: listFiles(".")
                          → MCP Server executes FileSystemTool.listFiles(".")
                          → Returns: "README.md\ndata/\nnotes.txt\nsample-report.md"
                          → "I see a report, let me read it"
                          → Tool Call: readFile("sample-report.md")
                          → MCP Server returns file content
                          → "Now I'll write a summary"
                          → Tool Call: writeFile("workspace-summary.txt", "...")
                          → MCP Server creates the file
                          → Final response to user
```

#### Tools Used

| Tool | Description | Parameters |
|------|-------------|------------|
| `listFiles` | List directory contents | `relativePath` |
| `readFile` | Read file content | `relativePath` |
| `writeFile` | Write content to file | `relativePath`, `content` |
| `searchFiles` | Glob pattern search | `pattern` |
| `fileInfo` | Get file metadata | `relativePath` |

#### Security Features
- All paths are **sandboxed** to `demo-workspace/`
- **Path traversal** attacks are blocked (e.g., `../../etc/passwd` → rejected)
- File size limit: **100KB** per read
- Operations are logged for audit

#### Try It Yourself
```bash
# Custom file operation
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Search for all .md files in the workspace and list them with their sizes"}' | jq

# Create a new file
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Create a file called hello.txt in the workspace with the content: Hello from MCP!"}' | jq
```

---

### Demo 2: Database Querying

**MCP Capability:** Tools (`DatabaseQueryTool`)
**Real-world analog:** Natural language BI tools, database chatbots

#### Run It
```bash
curl http://localhost:8080/demo/db-query | jq
```

#### What Happens

1. The LLM first calls `listTables()` to understand the database schema
2. It translates "top 5 highest-paid employees" into SQL
3. It calls `executeQuery("SELECT name, salary, department FROM employees ORDER BY salary DESC LIMIT 5")`
4. It then constructs a join query for order values
5. It formats the results into an executive report

#### Database Schema

```sql
employees (id, name, email, department, salary, hire_date)
departments (id, name, budget, manager_name)
products (id, name, category, price, stock_quantity)
orders (id, product_id, customer_name, quantity, order_date, status)
```

**Sample data:** 15 employees, 6 departments, 10 products, 12 orders

#### Tools Used

| Tool | Description | Parameters |
|------|-------------|------------|
| `executeQuery` | Run SELECT SQL | `sql` |
| `listTables` | Show all tables + columns | (none) |
| `tableSummary` | Row count + sample data | `tableName` |

#### Security Features
- **Only SELECT** statements are allowed
- Dangerous keywords are blocked: DROP, DELETE, INSERT, UPDATE, ALTER, etc.
- Formatted results prevent SQL injection in output

#### Try It Yourself
```bash
# Natural language query
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Which department has the highest total salary cost?"}' | jq

# Complex query
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Show me all pending orders with product names and total cost"}' | jq

# Explore the database
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Give me a summary of the products table"}' | jq
```

#### H2 Console
You can also browse the database directly:
1. Open http://localhost:3001/h2-console
2. JDBC URL: `jdbc:h2:mem:demodb`
3. Username: `sa`, Password: (empty)

---

### Demo 3: Weather API Integration

**MCP Capability:** Tools (`WeatherTool`)
**Real-world analog:** Travel planning assistants, smart home systems

#### Run It
```bash
curl http://localhost:8080/demo/weather | jq
```

#### What Happens

1. The LLM calls `getCurrentWeather("Tokyo")`, `getCurrentWeather("Paris")`, `getCurrentWeather("Sydney")`
2. It processes the weather data for all three cities
3. It calls `compareWeather("Tokyo", "Paris")` for a detailed comparison
4. Based on the weather conditions, it recommends the best city to visit
5. It calls `getWeatherForecast(city)` for the recommended city

#### Tools Used

| Tool | Description | Parameters |
|------|-------------|------------|
| `getCurrentWeather` | Current conditions | `city` |
| `getWeatherForecast` | 3-day forecast | `city` |
| `compareWeather` | Side-by-side comparison | `city1`, `city2` |

#### Supported Cities
New York, London, Tokyo, Paris, Sydney, São Paulo, Berlin, Mumbai, Dubai, Toronto, Mexico City, Singapore

#### Pattern: Wrapping External APIs
This demo uses mock data, but the pattern is identical for real APIs:

```java
// Mock version (this demo)
@Tool(description = "Get weather for a city")
public String getCurrentWeather(String city) {
    return MOCK_DATA.get(city);  // Instant response
}

// Real version (production)
@Tool(description = "Get weather for a city")
public String getCurrentWeather(String city) {
    return restClient.get()
        .uri("https://api.weatherapi.com/v1/current.json?q={city}", city)
        .retrieve()
        .body(String.class);  // Real API call
}
```

**The MCP protocol doesn't change** — only the tool implementation differs.

#### Try It Yourself
```bash
# Simple weather check
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What is the weather like in Mumbai right now?"}' | jq

# Travel planning
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "I want to visit somewhere warm but not too humid. Compare Dubai and Singapore weather for me."}' | jq
```

---

### Demo 4: Code Analysis

**MCP Capability:** Tools (`CodeAnalysisTool`)
**Real-world analog:** AI code reviewers, tech debt scanners

#### Run It
```bash
curl http://localhost:8080/demo/code-review | jq
```

#### What Happens

1. The LLM calls `scanDirectory("mcp-server/src/main/java")` to get a project overview
2. It picks a tool class and calls `analyzeJavaFile("mcp-server/src/.../WeatherTool.java")`
3. It calls `findPattern("mcp-server/src/main/java", "TODO|FIXME")` to find issues
4. It compiles the findings into a code review summary

#### Tools Used

| Tool | Description | Parameters |
|------|-------------|------------|
| `analyzeJavaFile` | Metrics for a single file | `filePath` |
| `scanDirectory` | Project-level overview | `directoryPath` |
| `findPattern` | Regex search across files | `directoryPath`, `regex` |

#### Metrics Provided
- Lines of code (total, code, comments, blank)
- Class/interface/record count
- Method count (approximate)
- Import analysis
- TODO/FIXME tracking
- Large file detection (>200 lines)
- Package structure

#### Try It Yourself
```bash
# Analyze a specific file
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Analyze the FileSystemTool.java file and tell me about its code quality"}' | jq

# Find patterns
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Find all @Tool annotations in the server source code"}' | jq
```

---

### Demo 5: Knowledge Base Q&A

**MCP Capability:** Resources (`KnowledgeBaseResourceProvider`) + implicit knowledge
**Real-world analog:** Company knowledge bots, documentation assistants

#### Run It
```bash
curl http://localhost:8080/demo/knowledge-qa | jq
```

#### What Happens

1. The LLM is asked about MCP and Spring AI concepts
2. It leverages its training data and any available MCP resources to answer
3. The knowledge base documents (`kb://spring-ai`, `kb://mcp-protocol`, `kb://ollama-guide`)
   are available as MCP resources that can be read

#### Available Resources

| URI | Name | Content |
|-----|------|---------|
| `kb://spring-ai-overview` | Spring AI Overview | Spring AI framework overview |
| `kb://mcp-protocol` | MCP Protocol | MCP concepts and architecture |
| `kb://ollama-guide` | Ollama Guide | Local LLM setup and tips |

#### MCP Resources vs Tools

Resources are **passive data** (like files), while tools are **active functions**:

```
Resource: "Here's the content of document X"  → READ-ONLY
Tool:     "Execute this SQL query"             → PERFORMS ACTION
```

Resources are ideal for:
- Static content (documents, configuration)
- Data that doesn't change during a session
- Content that the LLM should reference but not modify

---

## Real-World MCP Applications

### 1. 🏢 Enterprise Knowledge Assistant

**Scenario:** A company wants an AI assistant that can answer questions about
internal processes, policies, and documentation.

```
MCP Server: Internal Knowledge Base
├── Tools: Search documents, query employee directory
├── Resources: Policy documents, org charts, process guides
└── Prompts: "Write a policy summary", "Onboarding checklist"
```

**Why MCP?** The knowledge base can be exposed as an MCP server that any
AI application in the company can connect to — no custom integration needed.

### 2. 🛒 E-Commerce Operations Bot

**Scenario:** Customer service agents need AI help with orders, inventory, and returns.

```
MCP Server: E-Commerce Backend
├── Tools: Look up order, check inventory, process return, calculate shipping
├── Resources: Product catalog, pricing rules, return policies
└── Prompts: "Draft return email", "Generate order summary"
```

**Why MCP?** The same MCP server works with different AI frontends — web chat,
Slack bot, internal tools — without rebuilding integrations.

### 3. 🏥 Healthcare Data Assistant

**Scenario:** Researchers need to query patient databases and generate reports
while maintaining data privacy.

```
MCP Server: Clinical Data (HIPAA-compliant)
├── Tools: Query anonymized data, generate statistics, create visualizations
├── Resources: Study protocols, drug interaction databases
└── Prompts: "Analyze clinical trial results", "Generate adverse event report"
```

**Why MCP?** Read-only tools with strict input validation ensure data safety.
The MCP server controls all data access — the LLM never sees raw patient data.

### 4. 🔧 DevOps Automation

**Scenario:** Engineers want to use natural language to manage infrastructure.

```
MCP Server A: Kubernetes Cluster
├── Tools: Get pod status, scale deployment, view logs, describe service
├── Resources: Cluster config, namespace list, resource quotas
└── Prompts: "Diagnose pod failure", "Generate deployment manifest"

MCP Server B: CI/CD Pipeline
├── Tools: Trigger build, check pipeline status, rollback deployment
├── Resources: Build history, test reports, deployment configs
└── Prompts: "Analyze build failure", "Create release notes"
```

**Why MCP?** Multiple MCP servers compose naturally. The AI assistant connects
to both servers simultaneously and uses tools from either as needed.

### 5. 📊 Financial Analysis Platform

**Scenario:** Analysts need AI help with market data, portfolio analysis, and reporting.

```
MCP Server: Financial Data
├── Tools: Query market data, calculate returns, run risk models
├── Resources: Regulatory filings, earnings reports, market indices
└── Prompts: "Generate quarterly report", "Risk assessment template"
```

**Why MCP?** Prompts ensure consistent report formats across the team.
Tools enforce read-only access to sensitive financial data.

### 6. 🎓 Educational Platform

**Scenario:** Students use an AI tutor that can access course materials and assignments.

```
MCP Server: Course Management
├── Tools: Search syllabus, check grades, submit assignment, get hints
├── Resources: Lecture notes, textbook excerpts, example solutions
└── Prompts: "Explain concept at beginner level", "Practice problem generator"
```

**Why MCP?** The `explain-concept` prompt template ensures consistent, level-appropriate
explanations. Resource access gives the AI context about the specific course.

---

## Building Your Own MCP Tools

### Step-by-Step: Create a New Tool

1. **Create the tool class** in `mcp-server/src/main/java/com/example/mcpserver/tools/`:

```java
@Component
public class MyNewTool {

    @Tool(description = "Clear description of what this tool does. " +
            "Include what parameters mean and what the tool returns.")
    public String myToolMethod(
            @ToolParam(description = "Description of this parameter") String param1,
            @ToolParam(description = "Optional parameter info") int param2) {
        // Your implementation here
        return "Result string that the LLM will process";
    }
}
```

2. **Register it** in `McpServerConfig.java`:

```java
@Bean
public ToolCallbackProvider toolCallbackProvider(
        FileSystemTool fileSystemTool,
        DatabaseQueryTool databaseQueryTool,
        WeatherTool weatherTool,
        CodeAnalysisTool codeAnalysisTool,
        MyNewTool myNewTool) {  // Add your new tool

    return MethodToolCallbackProvider.builder()
        .toolObjects(fileSystemTool, databaseQueryTool, weatherTool,
                     codeAnalysisTool, myNewTool)  // Include it
        .build();
}
```

3. **Restart the server** — the client will discover the new tool automatically!

### Best Practices for Tool Design

1. **Clear descriptions** — The LLM reads these to decide when to use the tool
2. **String return types** — Return human-readable strings the LLM can process
3. **Error handling** — Return error messages as strings, don't throw exceptions
4. **Input validation** — Validate and sanitize all parameters
5. **Idempotent when possible** — Safe to call multiple times
6. **Scoped access** — Limit what the tool can access (sandbox, read-only, etc.)
7. **Reasonable output size** — Don't return megabytes of data

---

## Summary

| Demo | MCP Feature | Key Takeaway |
|------|-------------|--------------|
| File System | Tools | LLM can safely interact with the file system via sandboxed operations |
| Database | Tools | Natural language to SQL enables non-technical users to query data |
| Weather | Tools | MCP tools are perfect wrappers for external APIs |
| Code Analysis | Tools | AI-assisted code review with configurable analysis depth |
| Knowledge Base | Resources | Static content served via URI-based resource access |

Each demo shows a different facet of MCP. Together, they demonstrate that **MCP provides
a universal, composable protocol** for connecting AI models to any external capability.

