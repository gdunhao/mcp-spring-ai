# 03 — Setup Guide: Getting Everything Running

## Table of Contents
- [Prerequisites](#prerequisites)
- [Step 1: Install Java 25](#step-1-install-java-25)
- [Step 2: Install Ollama](#step-2-install-ollama)
- [Step 3: Pull the LLM Model](#step-3-pull-the-llm-model)
- [Step 4: Build the Project](#step-4-build-the-project)
- [Step 5: Start the MCP Server](#step-5-start-the-mcp-server)
- [Step 6: Start the MCP Client](#step-6-start-the-mcp-client)
- [Step 7: Verify Everything Works](#step-7-verify-everything-works)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

| Requirement | Version | Purpose |
|-------------|---------|---------|
| Java JDK | 25+ | Compile and run the project |
| Maven | 3.9+ (included via `mvnw`) | Build tool |
| Ollama | Latest | Local LLM inference engine |
| qwen3:0.6b | Latest | LLM model (supports tool calling) |
| ~1 GB disk | — | For model + project |
| ~2 GB RAM | — | For Ollama + 2 Spring Boot apps |

---

## Step 1: Install Java 25

### macOS (Homebrew)
```bash
brew install openjdk@25

# Verify
java -version
# Expected: openjdk version "25.x.x"
```

### macOS (SDKMAN)
```bash
curl -s "https://get.sdkman.io" | bash
sdk install java 25-open
sdk use java 25-open

java -version
```

### Linux (Ubuntu/Debian)
```bash
sudo apt install openjdk-25-jdk

java -version
```

### Windows
Download from [Adoptium](https://adoptium.net/) or use:
```powershell
winget install Microsoft.OpenJDK.25
```

---

## Step 2: Install Ollama

### macOS
```bash
brew install ollama
```

### Linux
```bash
curl -fsSL https://ollama.ai/install.sh | sh
```

### Windows
Download from [ollama.ai/download](https://ollama.ai/download)

### Verify Installation
```bash
ollama --version
# Expected: ollama version x.x.x
```

### Start Ollama Service
```bash
# Start in the background (if not already running)
ollama serve

# Or check if it's already running
curl http://localhost:11434/api/tags
# Expected: {"models": [...]}
```

---

## Step 3: Pull the LLM Model

```bash
# Pull the default model used by this demo
ollama pull qwen3:0.6b

# Verify the model is available
ollama list
# Expected output includes: qwen3:0.6b
```

### Alternative Models

If you want better quality (at the cost of speed/memory):
```bash
# Better reasoning, still fast (~1GB)
ollama pull qwen3:1.7b

# Good all-around model (~2GB)
ollama pull llama3.2:3b

# High quality, needs more RAM (~4GB)
ollama pull mistral:7b
```

> **Note:** After pulling an alternative model, update `mcp-client/src/main/resources/application.yaml`:
> ```yaml
> spring.ai.ollama.chat.model: qwen3:1.7b  # change to your model
> ```

### Quick Test
```bash
# Test that the model works
ollama run qwen3:0.6b "What is 2+2?"
# Expected: A response containing "4"
# Press Ctrl+D to exit
```

---

## Step 4: Build the Project

```bash
# Navigate to project root
cd mcp-spring-ai

# Build all modules (skip tests for faster initial build)
./mvnw clean package -DskipTests

# Expected output:
# [INFO] mcp-spring-ai .......................... SUCCESS
# [INFO] mcp-server ............................. SUCCESS
# [INFO] mcp-client ............................. SUCCESS
# [INFO] BUILD SUCCESS
```

### If Build Fails

```bash
# Check Java version
java -version  # Must be 25+

# Check Maven wrapper is executable
chmod +x mvnw

# Try with verbose output
./mvnw clean package -DskipTests -X 2>&1 | tail -50
```

---

## Step 5: Start the MCP Server

Open **Terminal 1**:

```bash
./mvnw spring-boot:run -pl mcp-server
```

**Expected output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::             (v3.5.13)

...
INFO  --- MCP Server started on port 3001
INFO  --- H2 Console available at /h2-console
```

### Verify Server

```bash
# Check if SSE endpoint is responding
curl -N http://localhost:3001/sse
# Expected: SSE connection established (will hang — that's normal, Ctrl+C to stop)

# Check H2 Console (open in browser)
open http://localhost:3001/h2-console
# JDBC URL: jdbc:h2:mem:demodb
# Username: sa
# Password: (empty)
```

---

## Step 6: Start the MCP Client

Open **Terminal 2** (keep the server running in Terminal 1):

```bash
./mvnw spring-boot:run -pl mcp-client
```

**Expected output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
...

INFO  --- Connected to MCP server: mcp-demo-server
INFO  --- Discovered 12 tools from MCP server
INFO  --- MCP Client started on port 8080
```

> **Important:** The client must start AFTER the server, since it connects to the server during initialization.

---

## Step 7: Verify Everything Works

```bash
# 1. List available demos
curl http://localhost:8080/demo/scenarios | jq

# 2. Try a simple chat
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello! What tools do you have access to?"}' | jq

# 3. Run the weather demo
curl http://localhost:8080/demo/weather | jq

# 4. Run the database demo
curl http://localhost:8080/demo/db-query | jq

# 5. Run the file system demo
curl http://localhost:8080/demo/file-search | jq
```

---

## Troubleshooting

### "Connection refused" on port 3001
**Cause:** MCP server is not running.
**Fix:** Start the server first: `./mvnw spring-boot:run -pl mcp-server`

### "Connection refused" on port 11434
**Cause:** Ollama is not running.
**Fix:** Start Ollama: `ollama serve`

### "Model not found: qwen3:0.6b"
**Cause:** Model not downloaded.
**Fix:** Pull it: `ollama pull qwen3:0.6b`

### Build fails with "release version 25 not supported"
**Cause:** Wrong Java version.
**Fix:** Install Java 25+ and ensure it's on your PATH:
```bash
java -version
# If wrong version, set JAVA_HOME:
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
```

### Slow responses from Ollama
**Cause:** Model is large or system has limited RAM.
**Fix:**
- Use a smaller model: `qwen3:0.6b` (default)
- Close other memory-intensive applications
- Check RAM usage: `ollama ps`

### "Port 8080 already in use"
**Fix:** Either stop the other application on port 8080, or change the client port:
```bash
./mvnw spring-boot:run -pl mcp-client -Dspring-boot.run.arguments="--server.port=9090"
```

### Client starts but no tools are discovered
**Cause:** Server not reachable or wrong URL.
**Fix:** Verify server is running and check client config:
```yaml
# In mcp-client/src/main/resources/application.yaml
spring.ai.mcp.client.sse.connections.mcp-demo-server.url: http://localhost:3001
```

---

## Next Steps

- **[04-TRANSPORT-MODES.md](04-TRANSPORT-MODES.md)** — Learn about SSE vs STDIO transports
- **[05-USE-CASES.md](05-USE-CASES.md)** — Walk through each demo scenario

