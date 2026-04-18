# Model Context Protocol (MCP)

## What is MCP?

The Model Context Protocol (MCP) is an open standard created by Anthropic that defines
how AI applications communicate with external data sources and tools. Think of it as
"USB for AI" — a universal connector that lets any AI model work with any tool provider.

## Architecture

MCP follows a client-server architecture with clear roles:

```
┌─────────────────────────────────────────────────┐
│                   HOST APPLICATION               │
│  (Your Spring Boot app, IDE, Claude Desktop)     │
│                                                   │
│  ┌─────────────┐  ┌─────────────┐               │
│  │ MCP Client  │  │ MCP Client  │  ...          │
│  │ (Server A)  │  │ (Server B)  │               │
│  └──────┬──────┘  └──────┬──────┘               │
└─────────┼────────────────┼──────────────────────┘
          │                │
    ┌─────▼──────┐  ┌─────▼──────┐
    │ MCP Server │  │ MCP Server │
    │   (Tools,  │  │  (Weather  │
    │  Database) │  │    API)    │
    └────────────┘  └────────────┘
```

### Roles

- **Host**: The application that hosts MCP clients (e.g., Claude Desktop, your app)
- **Client**: Maintains a 1:1 connection with a single MCP server
- **Server**: Exposes capabilities (tools, resources, prompts) via the MCP protocol

## Core Capabilities

### 1. Tools
Tools let servers expose executable functions to the LLM. The LLM decides when to
call a tool based on the user's request.

**Examples**: Execute SQL queries, call APIs, manipulate files, run calculations.

### 2. Resources
Resources provide read-only data access. Unlike tools, resources don't perform
actions — they provide information.

**Examples**: Read documents, access configuration files, browse file systems.

### 3. Prompts
Prompts are reusable, parameterized templates that guide LLM interactions.

**Examples**: Code review templates, summary generators, analysis frameworks.

### 4. Sampling (Experimental)
Allows servers to request LLM completions through the client, enabling
sophisticated AI-powered workflows within the server itself.

### 5. Roots
Roots define the file system boundaries that clients share with servers,
providing a security boundary for file-based operations.

## Transport Modes

### STDIO (Standard I/O)
- Server runs as a subprocess
- Communication via stdin/stdout
- Best for: Local tools, CLI integrations
- Pros: Simple, no network config
- Cons: Same machine only

### SSE (Server-Sent Events)
- Server runs as an HTTP service
- Communication via HTTP + SSE
- Best for: Remote servers, shared services
- Pros: Network-accessible, scalable
- Cons: Requires HTTP server setup

### Streamable HTTP (Newer)
- Evolution of SSE transport
- Supports both streaming and request-response
- Best for: Production deployments

## Protocol Lifecycle

1. **Initialize**: Client sends capabilities, server responds with its capabilities
2. **Negotiate**: Both sides agree on supported features
3. **Operate**: Client invokes tools, reads resources, uses prompts
4. **Notify**: Server sends change notifications (resources updated, tools changed)
5. **Shutdown**: Graceful disconnection

## Why MCP Matters

Before MCP, every AI integration was custom:
- Each tool needed its own API adapter
- No standard for capability discovery
- Inconsistent error handling
- Difficult to compose multiple tools

MCP solves this with a universal protocol that any LLM and any tool provider can implement.

