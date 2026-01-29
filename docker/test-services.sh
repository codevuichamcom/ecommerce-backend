#!/bin/bash
# Script to test all services health

echo "🔍 Testing E-Commerce Services Health..."
echo "========================================"
echo ""

services=(
  "API Gateway:http://localhost:8080/actuator/health"
  "Auth Service:http://localhost:8086/actuator/health"
  "Product Service:http://localhost:8081/actuator/health"
  "Inventory Service:http://localhost:8082/actuator/health"
  "Order Service:http://localhost:8083/actuator/health"
  "Payment Service:http://localhost:8084/actuator/health"
  "Notification Service:http://localhost:8085/actuator/health"
  "Zipkin:http://localhost:9411/health"
  "Prometheus:http://localhost:9090/-/healthy"
)

passed=0
failed=0

for service in "${services[@]}"; do
  IFS=':' read -r name url <<< "$service"
  
  printf "%-25s " "$name"
  
  if curl -s -f -m 5 "$url" > /dev/null 2>&1; then
    echo "✅ UP"
    ((passed++))
  else
    echo "❌ DOWN"
    ((failed++))
  fi
done

echo ""
echo "========================================"
echo "Results: $passed passed, $failed failed"

if [ $failed -eq 0 ]; then
  echo "🎉 All services are healthy!"
  exit 0
else
  echo "⚠️  Some services are not ready. Check logs with: docker-compose logs"
  exit 1
fi
