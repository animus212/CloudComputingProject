#!/bin/bash



set -e

COMMAND=$1
SERVICE=$2

NAMESPACE="event-management"
MONITORING_NS="monitoring"

IMAGES=(
    "user-service"
    "event-service"
    "registration-service"
    "notification-service"
    "nginx"
)

# ── Helpers ───────────────────────────────────────────────────────────────────
check_minikube() {
    if ! minikube status | grep -q "Running"; then
        echo "⚠️  Minikube is not running. Starting it..."
        minikube start --driver=docker
    else
        echo "✅ Minikube is running."
    fi
}

build_images() {
    echo ""
    echo "🔨 Building all images with Docker Compose (prod profile)..."
    # Must NOT have eval \$(minikube docker-env) active — uses host Docker for internet access
    docker-compose -f docker-compose.prod.yml --env-file .env.prod build
    echo "✅ All images built."
}

load_images() {
    echo ""
    echo "📦 Loading images into Minikube..."
    for img in "${IMAGES[@]}"; do
        echo "   Loading eventmgmt/${img}:latest ..."
        minikube image load "eventmgmt/${img}:latest"
    done
    echo "✅ All images loaded into Minikube."
}

apply_manifests() {
    echo ""
    echo "📋 Applying Kubernetes manifests..."

    kubectl apply -f k8s/namespace.yaml
    kubectl apply -f monitoring/namespace.yaml
    kubectl apply -f k8s/secrets.yaml
    kubectl apply -f k8s/configmap-nginx.yaml
    kubectl apply -f k8s/databases/
    kubectl apply -f k8s/infrastructure/

    

    kubectl apply -f k8s/services/
    kubectl apply -f k8s/networking/

    echo ""
    echo "📊 Applying monitoring stack..."
    kubectl apply -f monitoring/prometheus/
    kubectl apply -f monitoring/grafana/
    

    echo "✅ All manifests applied."
}

print_urls() {
    NODE_IP=$(minikube ip)
    echo ""
    echo "═══════════════════════════════════════════════"
    echo "  🌐  Application  : http://${NODE_IP}:30080"
    echo "  📈  Prometheus   : http://${NODE_IP}:30090"
    echo "  📊  Grafana      : http://${NODE_IP}:30030  (admin/admin)"
    echo "═══════════════════════════════════════════════"
    echo ""
}

# ── Commands ──────────────────────────────────────────────────────────────────
case "$COMMAND" in

    up)
        echo "🚀 Deploying Event Management System to Kubernetes..."
        check_minikube
        build_images
        load_images
        apply_manifests
        echo ""
        echo "⏳ Waiting for application services to start..."
        sleep 10
        kubectl get pods -n "$NAMESPACE"
        print_urls
        echo "✅ Deployment complete!"
        ;;

    down)
        echo "🗑️  Tearing down Kubernetes deployment..."
        kubectl delete -f k8s/services/      --ignore-not-found
        kubectl delete -f k8s/networking/    --ignore-not-found
        kubectl delete -f k8s/infrastructure/ --ignore-not-found
        kubectl delete -f k8s/databases/     --ignore-not-found
        kubectl delete -f k8s/secrets.yaml   --ignore-not-found
        kubectl delete -f k8s/configmap-nginx.yaml --ignore-not-found
        kubectl delete -f monitoring/grafana/      --ignore-not-found
        kubectl delete -f monitoring/prometheus/   --ignore-not-found
        kubectl delete namespace "$NAMESPACE"      --ignore-not-found
        kubectl delete namespace "$MONITORING_NS"  --ignore-not-found
        echo "✅ All resources deleted. Minikube is still running."
        ;;

    status)
        echo "📋 Application pods:"
        kubectl get pods -n "$NAMESPACE" -o wide
        echo ""
        echo "📋 Monitoring pods:"
        kubectl get pods -n "$MONITORING_NS" -o wide
        echo ""
        echo "📋 Services:"
        kubectl get services -n "$NAMESPACE"
        print_urls
        ;;

    logs)
        if [[ -z "$SERVICE" ]]; then
            echo "❌ Please specify a service. Example: $0 logs user-service"
            echo "   Available: user-service, event-service, registration-service,"
            echo "              notification-service, nginx, rabbitmq"
            exit 1
        fi
        echo "📜 Streaming logs for: $SERVICE"
        kubectl logs -n "$NAMESPACE" deployment/"$SERVICE" -f
        ;;

    restart)
        if [[ -z "$SERVICE" ]]; then
            echo "❌ Please specify a service. Example: $0 restart user-service"
            exit 1
        fi

        # Map service name to build context
        case "$SERVICE" in
            user-service)         BUILD_CONTEXT="./UserService" ;;
            event-service)        BUILD_CONTEXT="./EventService" ;;
            registration-service) BUILD_CONTEXT="./RegistrationService" ;;
            notification-service) BUILD_CONTEXT="./NotificationService" ;;
            nginx)                BUILD_CONTEXT="./nginx" ;;
            *)
                echo "❌ Unknown service: $SERVICE"
                echo "   Available: user-service, event-service, registration-service,"
                echo "              notification-service, nginx"
                exit 1
                ;;
        esac

        echo "🔄 Rebuilding and restarting: $SERVICE"
        echo ""

        echo "🔨 Building image..."
        if [[ "$SERVICE" == "nginx" ]]; then
            docker build -t "eventmgmt/${SERVICE}:latest" \
                --build-arg NGINX_CONF=nginx.prod.conf "$BUILD_CONTEXT"
        else
            docker build -t "eventmgmt/${SERVICE}:latest" "$BUILD_CONTEXT"
        fi

        echo "📦 Loading into Minikube..."
        minikube image load "eventmgmt/${SERVICE}:latest"

        echo "🔁 Restarting deployment..."
        kubectl rollout restart deployment/"$SERVICE" -n "$NAMESPACE"

        echo "⏳ Waiting for rollout..."
        kubectl rollout status deployment/"$SERVICE" -n "$NAMESPACE"

        echo "✅ $SERVICE restarted successfully."
        ;;

    *)
        echo "Usage: $0 [up|down|status|logs <service>|restart <service>]"
        echo ""
        echo "  up                  Build, load, and deploy everything"
        echo "  down                Delete all K8s resources"
        echo "  status              Show pods, services, and URLs"
        echo "  logs <service>      Stream logs (e.g. logs user-service)"
        echo "  restart <service>   Rebuild and redeploy one service"
        exit 1
        ;;
esac
