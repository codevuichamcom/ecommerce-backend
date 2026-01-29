#!/bin/bash
# Helper script to run E-Commerce Backend

set -e

echo "🚀 E-Commerce Backend Docker Manager"
echo "===================================="

case "${1:-}" in
  start)
    echo "Starting all services..."
    docker-compose up -d
    echo "✅ Services started! Check status with: ./run.sh status"
    ;;
  
  stop)
    echo "Stopping all services..."
    docker-compose down
    echo "✅ Services stopped!"
    ;;
  
  restart)
    echo "Restarting all services..."
    docker-compose restart
    echo "✅ Services restarted!"
    ;;
  
  logs)
    echo "Showing logs (Ctrl+C to exit)..."
    docker-compose logs -f ${2:-}
    ;;
  
  status)
    echo "Service Status:"
    docker-compose ps
    ;;
  
  build)
    echo "Rebuilding services..."
    docker-compose build --no-cache
    echo "✅ Build complete!"
    ;;
  
  clean)
    echo "⚠️  WARNING: This will remove all data!"
    read -p "Are you sure? (yes/no): " -r
    if [[ $REPLY == "yes" ]]; then
      docker-compose down -v
      echo "✅ All data cleaned!"
    else
      echo "Cancelled."
    fi
    ;;
  
  dev)
    echo "Starting in development mode..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
    echo "✅ Dev mode started!"
    ;;
  
  *)
    echo "Usage: ./run.sh {start|stop|restart|logs|status|build|clean|dev}"
    echo ""
    echo "Commands:"
    echo "  start   - Start all services"
    echo "  stop    - Stop all services"
    echo "  restart - Restart all services"
    echo "  logs    - Show logs (add service name: logs api-gateway)"
    echo "  status  - Show service status"
    echo "  build   - Rebuild all services"
    echo "  clean   - Remove all data (WARNING: destructive!)"
    echo "  dev     - Start in development mode"
    exit 1
    ;;
esac
