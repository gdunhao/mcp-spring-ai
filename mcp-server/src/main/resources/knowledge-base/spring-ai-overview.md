# Spring AI Overview

## What is Spring AI?

Spring AI is the Spring ecosystem's framework for building AI-powered applications.
It provides a consistent, Spring-friendly API for integrating with various AI models,
vector stores, and tools — following the same principles that make Spring Boot productive.

## Core Concepts

### Chat Models
Spring AI abstracts different LLM providers (OpenAI, Ollama, Anthropic, etc.) behind
a unified `ChatModel` interface. This means you can switch providers without changing
your application code.

### Tool Calling (Function Calling)
Tools allow LLMs to interact with external systems. When an LLM determines it needs
information or wants to perform an action, it can "call" a registered tool. Spring AI
handles the serialization, invocation, and response routing automatically.

### Prompt Engineering
Spring AI provides `PromptTemplate` for building dynamic prompts with variable
substitution, similar to how Spring MVC handles view templates.

### Vector Stores
For RAG (Retrieval-Augmented Generation) applications, Spring AI integrates with
vector databases like Chroma, Pinecone, Milvus, and PGVector.

### MCP (Model Context Protocol)
Spring AI includes first-class support for MCP, enabling standardized communication
between AI applications and external tool/data providers.

## Key Benefits

1. **Portable**: Switch between AI providers with minimal code changes
2. **Spring Native**: Uses familiar Spring patterns (DI, auto-configuration, properties)
3. **Production Ready**: Built-in observability, retry policies, and error handling
4. **Extensible**: Easy to add custom models, tools, and integrations

## Supported Providers

| Provider | Chat | Embedding | Image |
|----------|------|-----------|-------|
| OpenAI   | ✅   | ✅        | ✅    |
| Ollama   | ✅   | ✅        | ❌    |
| Anthropic| ✅   | ❌        | ❌    |
| Azure AI | ✅   | ✅        | ✅    |
| Mistral  | ✅   | ✅        | ❌    |
| Google   | ✅   | ✅        | ✅    |

