#!/bin/bash

# Phase 4.1.6: Resilience4j Metrics Verification Script
# This script verifies that Resilience4j metrics are properly exposed

echo "🔍 Phase 4.1.6: Verifying Resilience4j Metrics Exposure"
echo "========================================================="
echo ""

# Check if order-service is running
if ! curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
    echo "❌ order-service is not running on port 8083"
    echo "   Please start it with: ./gradlew :order-service:bootRun"
    exit 1
fi

echo "✅ order-service is running"
echo ""

# Check actuator endpoints
echo "📊 Checking Actuator Endpoints..."
ACTUATOR_RESPONSE=$(curl -s http://localhost:8083/actuator)
if echo "$ACTUATOR_RESPONSE" | grep -q "prometheus"; then
    echo "✅ Prometheus endpoint is exposed"
else
    echo "❌ Prometheus endpoint is NOT exposed"
    exit 1
fi

if echo "$ACTUATOR_RESPONSE" | grep -q "health"; then
    echo "✅ Health endpoint is exposed"
else
    echo "❌ Health endpoint is NOT exposed"
    exit 1
fi

echo ""

# Check Resilience4j metrics
echo "🛡️  Checking Resilience4j Metrics..."
METRICS_RESPONSE=$(curl -s http://localhost:8083/actuator/prometheus)

# Check CircuitBreaker metrics
if echo "$METRICS_RESPONSE" | grep -q "resilience4j_circuitbreaker"; then
    echo "✅ CircuitBreaker metrics found"
    echo "   Metrics available:"
    echo "$METRICS_RESPONSE" | grep "resilience4j_circuitbreaker" | grep -E "product-service|inventory-service" | head -5
else
    echo "⚠️  CircuitBreaker metrics not found (may appear after first call)"
fi

echo ""

# Check Retry metrics
if echo "$METRICS_RESPONSE" | grep -q "resilience4j_retry"; then
    echo "✅ Retry metrics found"
else
    echo "⚠️  Retry metrics not found (may appear after first retry)"
fi

echo ""

# Check Bulkhead metrics
if echo "$METRICS_RESPONSE" | grep -q "resilience4j_bulkhead"; then
    echo "✅ Bulkhead metrics found"
else
    echo "⚠️  Bulkhead metrics not found (may appear after first call)"
fi

echo ""

# Check TimeLimiter metrics
if echo "$METRICS_RESPONSE" | grep -q "resilience4j_timelimiter"; then
    echo "✅ TimeLimiter metrics found"
else
    echo "⚠️  TimeLimiter metrics not found (may appear after first call)"
fi

echo ""

# Check health indicators
echo "🏥 Checking Health Indicators..."
HEALTH_RESPONSE=$(curl -s http://localhost:8083/actuator/health)
if echo "$HEALTH_RESPONSE" | grep -q "circuitBreakers"; then
    echo "✅ CircuitBreaker health indicator is registered"
    echo "$HEALTH_RESPONSE" | grep -A 10 "circuitBreakers" | head -15
else
    echo "⚠️  CircuitBreaker health indicator not found"
fi

echo ""
echo "========================================================="
echo "✅ Verification Complete!"
echo ""
echo "📝 Next Steps:"
echo "   1. Make some API calls to trigger metrics"
echo "   2. Check Prometheus at http://localhost:9090"
echo "   3. Check Grafana at http://localhost:3000"
echo ""
