#!/bin/bash

# ─────────────────────────────────────────────────────────────────────────────
# deploy-compose.sh
# Usage: ./deploy-compose.sh [dev|test|prod]
#
# Rules:
#   - If the SAME profile is already running → stop it, rebuild, restart it
#   - If a DIFFERENT profile is running → leave it alone, start the new one
#   - Multiple different profiles can run concurrently
# ─────────────────────────────────────────────────────────────────────────────

set -e

# ── Validate argument ─────────────────────────────────────────────────────────
PROFILE=$1

if [[ -z "$PROFILE" ]]; then
    echo "Usage: $0 [dev|test|prod]"
    exit 1
fi

if [[ "$PROFILE" != "dev" && "$PROFILE" != "test" && "$PROFILE" != "prod" ]]; then
    echo "❌ Invalid profile '$PROFILE'. Choose: dev, test, prod"
    exit 1
fi

# ── Ensure host Docker is used, not Minikube's ───────────────────────────────
if [[ -n "$MINIKUBE_ACTIVE_DOCKERD" ]]; then
    echo "⚠️  Minikube Docker env detected. Unsetting it..."
    eval $(minikube docker-env --unset)
fi

# ── Use docker-compose or docker compose depending on what's available ────────
if command -v docker-compose &>/dev/null; then
    COMPOSE_CMD="docker-compose"
elif docker compose version &>/dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
else
    echo "❌ Neither 'docker-compose' nor 'docker compose' found."
    exit 1
fi

echo "ℹ️  Using compose command: $COMPOSE_CMD"

# ── Resolve file names ────────────────────────────────────────────────────────
COMPOSE_FILE="docker-compose.${PROFILE}.yml"
ENV_FILE=".env.${PROFILE}"
PROJECT_NAME="eventmgmt-${PROFILE}"

if [[ ! -f "$COMPOSE_FILE" ]]; then
    echo "❌ Compose file not found: $COMPOSE_FILE"
    exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
    echo "❌ Env file not found: $ENV_FILE"
    exit 1
fi

# ── Check if same profile is already running ──────────────────────────────────
RUNNING=$($COMPOSE_CMD -f "$COMPOSE_FILE" --env-file "$ENV_FILE" \
    -p "$PROJECT_NAME" ps --services --filter "status=running" 2>/dev/null | wc -l)

if [[ "$RUNNING" -gt 0 ]]; then
    echo "⚠️  Profile '$PROFILE' is already running. Stopping it first..."
    $COMPOSE_CMD -f "$COMPOSE_FILE" --env-file "$ENV_FILE" \
        -p "$PROJECT_NAME" down
    echo "✅ Stopped existing '$PROFILE' deployment."
fi

# ── Build and start ───────────────────────────────────────────────────────────
echo ""
echo "🚀 Building and starting profile: $PROFILE"
echo "   Compose file : $COMPOSE_FILE"
echo "   Env file     : $ENV_FILE"
echo "   Project name : $PROJECT_NAME"
echo ""

$COMPOSE_CMD -f "$COMPOSE_FILE" --env-file "$ENV_FILE" \
    -p "$PROJECT_NAME" up --build -d

echo ""
echo "✅ Profile '$PROFILE' is up!"
echo ""

# ── Show running containers ───────────────────────────────────────────────────
echo "📦 Running containers for '$PROFILE':"
$COMPOSE_CMD -f "$COMPOSE_FILE" --env-file "$ENV_FILE" \
    -p "$PROJECT_NAME" ps

echo ""

# ── Print access URLs ─────────────────────────────────────────────────────────
case "$PROFILE" in
    dev)
        echo "🌐 Application : http://localhost:8080"
        echo "🐇 RabbitMQ UI : http://localhost:15672  (guest/guest)"
        ;;
    test)
        echo "🌐 Application : http://localhost:9080"
        echo "🐇 RabbitMQ UI : http://localhost:19672  (guest/guest)"
        ;;
    prod)
        echo "🌐 Application : http://localhost:80"
        ;;
esac

echo ""
echo "💡 To stop this profile run:"
echo "   $COMPOSE_CMD -f $COMPOSE_FILE --env-file $ENV_FILE -p $PROJECT_NAME down"
