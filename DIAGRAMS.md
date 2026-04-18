# 📊 MCP Spring AI — Diagrams

> All diagrams are written in [Mermaid](https://mermaid.js.org/) and optimised for **dark appearance** using the `%%{init: ...}%%` directive.

---

## 1. High-Level System Architecture

```mermaid
%%{init: { "theme": "dark", "themeVariables": { "primaryColor": "#1e293b", "primaryTextColor": "#e2e8f0", "primaryBorderColor": "#475569", "lineColor": "#94a3b8", "secondaryColor": "#0f172a", "tertiaryColor": "#1e293b", "edgeLabelBackground": "#1e293b", "clusterBkg": "#0f172a", "titleColor": "#e2e8f0" } } }%%
graph TB
    subgraph UI["🖥️  User Interfaces"]
        A[React UI<br/>:5173]
        B[curl / HTTP<br/>REST Client]
    end

    subgraph CLIENT["☁️  MCP Client  :8080"]
        C[ChatController<br/>/chat]
        D[DemoController<br/>/demo/*]
        E[ChatService]
        F[Spring AI<br/>ChatClient]
        G[Ollama LLM<br/>qwen3:0.6b<br/>:11434]
    end

    subgraph SERVER["🔧  MCP Server  :3001"]
        H[McpServerConfig]

        subgraph TOOLS["Tools"]
            T1[📁 FileSystemTool]
            T2[🗄️ DatabaseQueryTool]
            T3[🌤️ WeatherTool]
            T4[🔍 CodeAnalysisTool]
        end

        subgraph RESOURCES["Resources"]
            R1[📚 kb://mcp-protocol]
            R2[📚 kb://ollama-guide]
            R3[📚 kb://spring-ai-overview]
        end

        subgraph PROMPTS["Prompts"]
            P1[💬 summarize-document]
            P2[💬 sql-query-helper]
            P3[💬 code-review]
            P4[💬 explain-concept]
            P5[💬 compare-contrast]
        end

        DB[(H2 In-Memory<br/>Database)]
        WS[📂 demo-workspace]
    end

    A -->|HTTP/SSE| C
    B -->|HTTP| C
    A -->|HTTP| D
    B -->|HTTP| D
    C --> E
    D --> E
    E --> F
    F -->|ollama API| G
    F <-->|SSE Transport| H
    H --> TOOLS
    H --> RESOURCES
    H --> PROMPTS
    T2 --> DB
    T1 --> WS
```

---

## 2. Tool Call Sequence (SSE Transport)

```mermaid
%%{init: { "theme": "dark", "themeVariables": { "primaryColor": "#1e293b", "primaryTextColor": "#e2e8f0", "primaryBorderColor": "#475569", "lineColor": "#94a3b8", "secondaryColor": "#0f172a", "actorBkg": "#1e293b", "actorTextColor": "#e2e8f0", "actorBorderColor": "#475569", "activationBkgColor": "#334155", "activationBorderColor": "#64748b", "noteBkgColor": "#0f172a", "noteTextColor": "#94a3b8", "signalColor": "#94a3b8" } } }%%
sequenceDiagram
    actor User
    participant Client as MCP Client<br/>:8080
    participant Ollama as Ollama LLM<br/>:11434
    participant Server as MCP Server<br/>:3001
    participant Tool as Tool<br/>(e.g. WeatherTool)

    User->>+Client: POST /chat<br/>{"message": "Weather in Tokyo?"}
    Client->>+Ollama: chat(messages, tools=[...])
    Note over Ollama: Decides a tool call<br/>is needed
    Ollama-->>-Client: tool_call: getCurrentWeather("Tokyo")
    Client->>+Server: MCP tool/call<br/>getCurrentWeather("Tokyo")
    Server->>+Tool: invoke getCurrentWeather("Tokyo")
    Tool-->>-Server: "☀️ 28°C, Sunny..."
    Server-->>-Client: MCP result
    Client->>+Ollama: chat(messages + tool_result)
    Note over Ollama: Formulates natural<br/>language answer
    Ollama-->>-Client: "Tokyo is sunny at 28°C..."
    Client-->>-User: {"response": "Tokyo is sunny at 28°C..."}
```

---

## 3. Streaming Chat Sequence

```mermaid
%%{init: { "theme": "dark", "themeVariables": { "primaryColor": "#1e293b", "primaryTextColor": "#e2e8f0", "primaryBorderColor": "#475569", "lineColor": "#94a3b8", "secondaryColor": "#0f172a", "actorBkg": "#1e293b", "actorTextColor": "#e2e8f0", "actorBorderColor": "#475569", "activationBkgColor": "#334155", "activationBorderColor": "#64748b", "noteBkgColor": "#0f172a", "noteTextColor": "#94a3b8" } } }%%
sequenceDiagram
    actor User
    participant UI as React UI<br/>:5173
    participant Client as MCP Client<br/>:8080
    participant Ollama as Ollama LLM<br/>:11434
    participant Server as MCP Server<br/>:3001

    User->>UI: types message + clicks Send (Stream)
    UI->>+Client: POST /chat/stream (SSE)
    Client->>+Server: MCP tool discovery (on startup)
    Server-->>-Client: available tools list
    Client->>+Ollama: stream chat(message, tools)
    Note over Client,Server: Tool calls execute<br/>synchronously before streaming
    Ollama-->>Client: token₁
    Client-->>UI: data: token₁
    Ollama-->>Client: token₂
    Client-->>UI: data: token₂
    Ollama-->>-Client: [DONE]
    Client-->>-UI: stream closed
    UI-->>User: full message rendered
```

---

## 4. MCP Server — Component Overview

```mermaid
%%{init: { "theme": "dark", "themeVariables": { "primaryColor": "#1e293b", "primaryTextColor": "#e2e8f0", "primaryBorderColor": "#475569", "lineColor": "#94a3b8", "secondaryColor": "#0f172a", "tertiaryColor": "#1e293b", "clusterBkg": "#0f172a" } } }%%
graph LR
    subgraph ENTRY["Entry Point"]
        APP[McpServerApplication]
        CFG[McpServerConfig]
    end

    subgraph TOOLS_GRP["🔧 MCP Tools (@Tool)"]
        FS[FileSystemTool<br/>listFiles · readFile<br/>writeFile · searchFiles]
        DB[DatabaseQueryTool<br/>executeQuery · listTables<br/>tableSummary]
        WX[WeatherTool<br/>getCurrentWeather<br/>getWeatherForecast<br/>compareWeather]
        CA[CodeAnalysisTool<br/>analyzeJavaFile<br/>findPattern<br/>scanDirectory]
        CC[CurrencyConverterTool<br/>convertCurrency<br/>getExchangeRates<br/>calculateMultiCurrencyTotal]
        NT[NotificationTool<br/>sendNotification<br/>sendBulkNotification<br/>getNotificationLog]
    end

    subgraph RES_GRP["📚 MCP Resources"]
        KB[KnowledgeBaseResourceProvider<br/>kb://mcp-protocol<br/>kb://ollama-guide<br/>kb://spring-ai-overview]
    end

    subgraph PROMPT_GRP["💬 MCP Prompts"]
        PP[DemoPromptProvider<br/>summarize-document<br/>sql-query-helper<br/>code-review<br/>explain-concept<br/>compare-contrast]
    end

    subgraph INFRA["Infrastructure"]
        H2[(H2 Database<br/>employees · departments<br/>products · orders)]
        WS2[demo-workspace/]
    end

    APP --> CFG
    CFG --> TOOLS_GRP
    CFG --> RES_GRP
    CFG --> PROMPT_GRP
    DB --> H2
    FS --> WS2
```

---

## 5. Transport Modes Comparison

```mermaid
%%{init: { "theme": "dark", "themeVariables": { "primaryColor": "#1e293b", "primaryTextColor": "#e2e8f0", "primaryBorderColor": "#475569", "lineColor": "#94a3b8", "secondaryColor": "#0f172a", "tertiaryColor": "#1e293b", "clusterBkg": "#0f172a" } } }%%
graph LR
    subgraph SSE["SSE Transport (default)"]
        SC[MCP Client<br/>:8080]
        SS[MCP Server<br/>:3001]
        SC <-->|HTTP + Server-Sent Events| SS
    end

    subgraph STDIO_GRP["STDIO Transport"]
        PC[Parent Process<br/>MCP Client]
        CP[Child Process<br/>MCP Server]
        PC <-->|stdin / stdout| CP
    end

    NOTE1["✅ Remote deployments<br/>✅ Multiple clients<br/>✅ Network separation"]
    NOTE2["✅ Local / embedded<br/>✅ Zero network overhead<br/>✅ Simple process model"]

    SSE --- NOTE1
    STDIO_GRP --- NOTE2
```

---

## 6. Database Schema (H2 In-Memory)

```mermaid
%%{init: { "theme": "dark", "themeVariables": { "primaryColor": "#1e293b", "primaryTextColor": "#e2e8f0", "primaryBorderColor": "#475569", "lineColor": "#94a3b8", "secondaryColor": "#0f172a" } } }%%
erDiagram
    DEPARTMENTS {
        int id PK
        string name
        decimal budget
        string manager_name
    }

    EMPLOYEES {
        int id PK
        string name
        string email
        string department
        decimal salary
        date hire_date
    }

    PRODUCTS {
        int id PK
        string name
        string category
        decimal price
        int stock_quantity
    }

    ORDERS {
        int id PK
        int product_id FK
        string customer_name
        int quantity
        date order_date
        string status
    }

    PRODUCTS ||--o{ ORDERS : "referenced by"
```

---

## 7. Maven Module Dependency Graph

```mermaid
%%{init: { "theme": "dark", "themeVariables": { "primaryColor": "#1e293b", "primaryTextColor": "#e2e8f0", "primaryBorderColor": "#475569", "lineColor": "#94a3b8", "secondaryColor": "#0f172a", "tertiaryColor": "#1e293b", "clusterBkg": "#0f172a" } } }%%
graph TD
    PARENT["📦 mcp-spring-ai<br/><i>parent pom</i>"]

    subgraph MODS["Modules"]
        SERVER_MOD["📦 mcp-server<br/>spring-ai-starter-mcp-server-webmvc<br/>spring-boot-starter-web<br/>spring-boot-starter-jdbc<br/>h2"]
        CLIENT_MOD["📦 mcp-client<br/>spring-ai-starter-mcp-client-webmvc<br/>spring-ai-ollama<br/>spring-boot-starter-web"]
    end

    subgraph EXTERNAL["External"]
        OLLAMA_EXT["🦙 Ollama<br/>qwen3:0.6b"]
        DOCKER["🐳 Docker<br/>docker-compose.yml"]
    end

    PARENT --> SERVER_MOD
    PARENT --> CLIENT_MOD
    CLIENT_MOD -->|SSE| SERVER_MOD
    CLIENT_MOD -->|ollama API| OLLAMA_EXT
    OLLAMA_EXT -.->|optional| DOCKER
```

