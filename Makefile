# ============================================================================
# Makefile for E-Commerce Backend Docker Management
# ============================================================================
# Provides convenient commands for managing Docker services
# ============================================================================

.PHONY: help up down restart logs build clean ps health backup restore update dev prod

# Default target
.DEFAULT_GOAL := help

# Colors
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[1;33m
NC := \033[0m # No Color

# Configuration
DOCKER_DIR := docker
COMPOSE_FILE := $(DOCKER_DIR)/docker-compose.yml
COMPOSE_DEV := $(DOCKER_DIR)/docker-compose.dev.yml
COMPOSE_PROD := $(DOCKER_DIR)/docker-compose.prod.yml
ENV_FILE := $(DOCKER_DIR)/.env

# Docker Compose command
COMPOSE := docker-compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE)
COMPOSE_DEV_CMD := $(COMPOSE) -f $(COMPOSE_DEV)
COMPOSE_PROD_CMD := $(COMPOSE) -f $(COMPOSE_PROD)

##@ General

help: ## Display this help message
	@echo "$(GREEN)E-Commerce Backend - Docker Management$(NC)"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "$(YELLOW)Usage:$(NC)\n  make $(BLUE)<target>$(NC)\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  $(BLUE)%-15s$(NC) %s\n", $$1, $$2 } /^##@/ { printf "\n$(YELLOW)%s$(NC)\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Development

dev: ## Start services in development mode
	@echo "$(GREEN)Starting services in development mode...$(NC)"
	@$(COMPOSE_DEV_CMD) up -d
	@echo "$(GREEN)✓ Services started successfully!$(NC)"
	@echo "$(BLUE)Run 'make logs' to view logs$(NC)"

dev-build: ## Build and start services in development mode
	@echo "$(GREEN)Building and starting services in development mode...$(NC)"
	@$(COMPOSE_DEV_CMD) up -d --build
	@echo "$(GREEN)✓ Services started successfully!$(NC)"

##@ Production

prod: ## Start services in production mode
	@echo "$(GREEN)Starting services in production mode...$(NC)"
	@$(COMPOSE_PROD_CMD) up -d
	@echo "$(GREEN)✓ Services started successfully!$(NC)"

prod-build: ## Build and start services in production mode
	@echo "$(GREEN)Building and starting services in production mode...$(NC)"
	@$(COMPOSE_PROD_CMD) up -d --build
	@echo "$(GREEN)✓ Services started successfully!$(NC)"

##@ Service Management

up: dev ## Alias for 'make dev'

down: ## Stop all services
	@echo "$(YELLOW)Stopping services...$(NC)"
	@$(COMPOSE) down
	@echo "$(GREEN)✓ Services stopped successfully!$(NC)"

restart: ## Restart all services
	@echo "$(YELLOW)Restarting services...$(NC)"
	@$(COMPOSE) restart
	@echo "$(GREEN)✓ Services restarted successfully!$(NC)"

ps: ## List running containers
	@$(COMPOSE) ps

logs: ## View logs (usage: make logs [service=service-name])
	@$(COMPOSE) logs -f $(service)

##@ Build & Deploy

build: ## Build all images
	@echo "$(GREEN)Building all images...$(NC)"
	@$(COMPOSE) build --no-cache
	@echo "$(GREEN)✓ Build completed!$(NC)"

build-service: ## Build specific service (usage: make build-service service=api-gateway)
	@echo "$(GREEN)Building $(service)...$(NC)"
	@$(COMPOSE) build --no-cache $(service)
	@echo "$(GREEN)✓ Build completed!$(NC)"

pull: ## Pull latest images
	@echo "$(GREEN)Pulling latest images...$(NC)"
	@$(COMPOSE) pull
	@echo "$(GREEN)✓ Pull completed!$(NC)"

push: ## Push images to registry
	@echo "$(GREEN)Pushing images to registry...$(NC)"
	@$(COMPOSE) push
	@echo "$(GREEN)✓ Push completed!$(NC)"

##@ Database

db-shell: ## Open PostgreSQL shell
	@$(COMPOSE) exec postgres psql -U postgres

db-backup: ## Backup PostgreSQL databases
	@echo "$(GREEN)Backing up databases...$(NC)"
	@mkdir -p $(DOCKER_DIR)/backups
	@$(COMPOSE) exec -T postgres pg_dumpall -U postgres > $(DOCKER_DIR)/backups/backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "$(GREEN)✓ Backup completed!$(NC)"

db-restore: ## Restore PostgreSQL databases (usage: make db-restore file=backup.sql)
	@echo "$(YELLOW)Restoring database from $(file)...$(NC)"
	@$(COMPOSE) exec -T postgres psql -U postgres < $(file)
	@echo "$(GREEN)✓ Restore completed!$(NC)"

##@ Cache & Messaging

redis-cli: ## Open Redis CLI
	@$(COMPOSE) exec redis redis-cli -a $$(grep REDIS_PASSWORD $(ENV_FILE) | cut -d '=' -f2)

kafka-topics: ## List Kafka topics
	@$(COMPOSE) exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

kafka-create-topic: ## Create Kafka topic (usage: make kafka-create-topic topic=my-topic)
	@$(COMPOSE) exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --topic $(topic) --partitions 3 --replication-factor 1

##@ Monitoring

health: ## Check health status of all services
	@echo "$(GREEN)Checking service health...$(NC)"
	@$(COMPOSE) ps

metrics: ## Open Prometheus
	@echo "$(BLUE)Opening Prometheus at http://localhost:9090$(NC)"
	@open http://localhost:9090 || xdg-open http://localhost:9090 || start http://localhost:9090

dashboards: ## Open Grafana
	@echo "$(BLUE)Opening Grafana at http://localhost:3000$(NC)"
	@open http://localhost:3000 || xdg-open http://localhost:3000 || start http://localhost:3000

traces: ## Open Zipkin
	@echo "$(BLUE)Opening Zipkin at http://localhost:9411$(NC)"
	@open http://localhost:9411 || xdg-open http://localhost:9411 || start http://localhost:9411

##@ Maintenance

clean: ## Remove all containers and volumes
	@echo "$(YELLOW)⚠ This will remove all containers, images, and volumes!$(NC)"
	@read -p "Are you sure? (yes/no): " confirm && [ "$$confirm" = "yes" ]
	@echo "$(YELLOW)Cleaning Docker resources...$(NC)"
	@$(COMPOSE) down -v --rmi all
	@echo "$(GREEN)✓ Cleanup completed!$(NC)"

clean-volumes: ## Remove only volumes
	@echo "$(YELLOW)⚠ This will remove all volumes!$(NC)"
	@read -p "Are you sure? (yes/no): " confirm && [ "$$confirm" = "yes" ]
	@$(COMPOSE) down -v
	@echo "$(GREEN)✓ Volumes removed!$(NC)"

prune: ## Prune unused Docker resources
	@echo "$(YELLOW)Pruning unused Docker resources...$(NC)"
	@docker system prune -f
	@echo "$(GREEN)✓ Prune completed!$(NC)"

##@ Utilities

exec: ## Execute command in service (usage: make exec service=postgres cmd="psql -U postgres")
	@$(COMPOSE) exec $(service) $(cmd)

shell: ## Open shell in service (usage: make shell service=api-gateway)
	@$(COMPOSE) exec $(service) sh

stats: ## Show container resource usage
	@docker stats --no-stream

update: ## Pull latest images and restart services
	@echo "$(GREEN)Updating services...$(NC)"
	@$(COMPOSE) pull
	@$(COMPOSE) up -d
	@echo "$(GREEN)✓ Update completed!$(NC)"

validate: ## Validate docker-compose configuration
	@echo "$(GREEN)Validating configuration...$(NC)"
	@$(COMPOSE) config > /dev/null
	@echo "$(GREEN)✓ Configuration is valid!$(NC)"

env-check: ## Check if .env file exists
	@if [ ! -f $(ENV_FILE) ]; then \
		echo "$(YELLOW)⚠ .env file not found. Creating from .env.example...$(NC)"; \
		cp $(DOCKER_DIR)/.env.example $(ENV_FILE); \
		echo "$(YELLOW)⚠ Please update $(ENV_FILE) with your configuration!$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)✓ .env file exists$(NC)"

##@ Testing

test-integration: ## Run integration tests
	@echo "$(GREEN)Running integration tests...$(NC)"
	@cd .. && ./gradlew test --tests "*IntegrationTest"

test-e2e: ## Run end-to-end tests
	@echo "$(GREEN)Running end-to-end tests...$(NC)"
	@cd .. && ./gradlew test --tests "*E2ETest"
