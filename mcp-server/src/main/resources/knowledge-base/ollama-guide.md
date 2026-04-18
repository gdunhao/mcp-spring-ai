# Ollama Guide

## What is Ollama?

Ollama is an open-source tool that makes it easy to run large language models (LLMs)
locally on your machine. It handles model download, optimization, and serving — giving
you a local AI inference engine with a simple API.

## Why Use Ollama?

1. **Privacy**: Your data never leaves your machine
2. **Cost**: No API fees — run as many queries as you want
3. **Offline**: Works without internet after model download
4. **Speed**: No network latency for inference
5. **Flexibility**: Wide selection of open-source models

## Quick Start

### Installation
```bash
# macOS
brew install ollama

# Linux
curl -fsSL https://ollama.ai/install.sh | sh

# Windows
# Download from https://ollama.ai/download
```

### Running a Model
```bash
# Pull and run a model
ollama run qwen3:0.6b

# Pull a model without running
ollama pull qwen3:0.6b

# List downloaded models
ollama list
```

## Recommended Models for Development

| Model | Size | Best For |
|-------|------|----------|
| qwen3:0.6b | ~400MB | Fast prototyping, tool calling demos |
| qwen3:1.7b | ~1GB | Better reasoning, still fast |
| llama3.2:3b | ~2GB | Good balance of quality and speed |
| mistral:7b | ~4GB | High quality, needs more RAM |
| codellama:7b | ~4GB | Code-specific tasks |

## API Compatibility

Ollama exposes an API compatible with OpenAI's format at `http://localhost:11434`.
Spring AI's Ollama integration uses this API natively.

### Key Endpoints
- `POST /api/chat` — Chat completion
- `POST /api/generate` — Text generation
- `POST /api/embeddings` — Text embeddings
- `GET /api/tags` — List models

## Tool Calling Support

Modern Ollama models (Qwen3, Llama 3.2+, Mistral) support function/tool calling,
which is essential for MCP integration. The model can:

1. Analyze the user's request
2. Decide which tool(s) to call
3. Format the tool call with proper arguments
4. Process the tool results
5. Generate a final response

## Tips for MCP + Ollama

- **Use small models** (0.6b-3b) for tool calling demos — they're fast and responsive
- **Enable tool calling** by using models that support it (Qwen3, Llama 3.2)
- **Monitor memory** — each model consumes RAM equal to roughly its size
- **Set temperature low** (0.1-0.3) for tool calling to get more deterministic results

