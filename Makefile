# ============================================================
# Makefile — MCP Spring AI Demo
# ============================================================
# Convenience targets for running the project locally.
#
# Prerequisites: Java 25+, Docker (for Ollama)
# ============================================================

.DEFAULT_GOAL := help

# ── Colours ──────────────────────────────────────────────────
GREEN  := \033[0;32m
YELLOW := \033[1;33m
CYAN   := \033[0;36m
RESET  := \033[0m

# ── Config ───────────────────────────────────────────────────
MAVEN  := ./mvnw
SERVER_PORT := 3001
CLIENT_PORT := 8080

.PHONY: help setup infra-up infra-down infra-logs build test clean server client server-stdio demo frontend

help: ## Show this help message
	@echo ""
	@echo "  $(CYAN)MCP Spring AI Demo$(RESET) — local developer commands"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' Makefile | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-18s$(RESET) %s\n", $$1, $$2}'
	@echo ""

# ── Setup ────────────────────────────────────────────────────

setup: infra-up build ## Pull Ollama image + LLM model, then build all modules
	@echo ""
	@echo "  $(GREEN)✅ Setup complete!$(RESET)"
	@echo ""
	@echo "  Start the server:  make server"
	@echo "  Start the client:  make client  (in a second terminal)"
	@echo "  Run demos:         make demo"
	@echo ""

infra-up: ## Start Ollama (Docker) in the background
	@echo "$(YELLOW)▶ Starting Ollama via Docker Compose…$(RESET)"
	@cp -n .env.example .env 2>/dev/null || true
	docker compose up -d --wait
	@echo "$(GREEN)✅ Ollama is running on http://localhost:11434$(RESET)"

infra-down: ## Stop Ollama (Docker)
	@echo "$(YELLOW)▶ Stopping Ollama…$(RESET)"
	docker compose down

infra-logs: ## Tail Ollama container logs
	docker compose logs -f ollama

# ── Build ─────────────────────────────────────────────────────

build: ## Compile all modules (skip tests)
	@echo "$(YELLOW)▶ Building all modules…$(RESET)"
	$(MAVEN) -q package -DskipTests
	@echo "$(GREEN)✅ Build successful$(RESET)"

test: ## Run all unit tests
	@echo "$(YELLOW)▶ Running tests…$(RESET)"
	$(MAVEN) test

clean: ## Remove build artefacts
	@echo "$(YELLOW)▶ Cleaning build artefacts…$(RESET)"
	$(MAVEN) -q clean

# ── Run ───────────────────────────────────────────────────────

server: ## Start the MCP Server on port 3001
	@echo "$(CYAN)▶ Starting MCP Server on port $(SERVER_PORT)…$(RESET)"
	$(MAVEN) spring-boot:run -pl mcp-server

server-stdio: ## Start the MCP Server in STDIO mode
	@echo "$(CYAN)▶ Starting MCP Server in STDIO mode…$(RESET)"
	$(MAVEN) spring-boot:run -pl mcp-server -Dspring-boot.run.profiles=stdio

client: ## Start the MCP Client on port 8080 (server must be running)
	@echo "$(CYAN)▶ Starting MCP Client on port $(CLIENT_PORT)…$(RESET)"
	@echo "$(YELLOW)  Make sure the MCP Server (make server) is already running!$(RESET)"
	$(MAVEN) spring-boot:run -pl mcp-client

# ── Demo ──────────────────────────────────────────────────────

frontend: ## Start the React frontend dev server on port 5173 (client must be running)
	@echo "$(CYAN)▶ Starting frontend dev server on http://localhost:5173…$(RESET)"
	@echo "$(YELLOW)  Make sure the MCP Client (make client) is already running!$(RESET)"
	cd frontend && npm run dev

demo: ## Smoke-test all demo endpoints (client must be running)
	@echo ""
	@echo "$(CYAN)══ Demo Smoke Tests (http://localhost:$(CLIENT_PORT)) ══$(RESET)"
	@echo ""
	@echo "$(YELLOW)[1/4] List available scenarios$(RESET)"
	@curl -sf http://localhost:$(CLIENT_PORT)/demo/scenarios | python3 -m json.tool 2>/dev/null || echo "  ⚠  Server not reachable"
	@echo ""
	@echo "$(YELLOW)[2/4] Weather demo$(RESET)"
	@curl -sf http://localhost:$(CLIENT_PORT)/demo/weather | python3 -m json.tool 2>/dev/null || echo "  ⚠  Server not reachable"
	@echo ""
	@echo "$(YELLOW)[3/4] Database query demo$(RESET)"
	@curl -sf http://localhost:$(CLIENT_PORT)/demo/db-query | python3 -m json.tool 2>/dev/null || echo "  ⚠  Server not reachable"
	@echo ""
	@echo "$(YELLOW)[4/4] Chat — 'What files are in the workspace?'$(RESET)"
	@curl -sf -X POST http://localhost:$(CLIENT_PORT)/chat \
	  -H "Content-Type: application/json" \
	  -d '{"message":"What files are in the workspace?"}' | python3 -m json.tool 2>/dev/null || echo "  ⚠  Server not reachable"
	@echo ""

