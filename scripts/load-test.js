import http from 'k6/http';
import { check, sleep } from 'k6';

// k6 load test configuration
export const options = {
    stages: [
        { duration: '30s', target: 20 },  // Ramp up to 20 users
        { duration: '1m', target: 20 },   // Stay at 20 users
        { duration: '30s', target: 0 },   // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
        http_req_failed: ['rate<0.01'],   // Error rate should be less than 1%
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    // 1. Test Public API: Get Products (Expected to be cached in product-service)
    const productsRes = http.get(`${BASE_URL}/api/products`);
    check(productsRes, {
        'get products status is 200': (r) => r.status === 200,
    });

    // 2. Test Rate Limiting (Spamming requests)
    // Note: Unauthenticated rate limit is 10 req/min in RateLimitConfig
    for (let i = 0; i < 5; i++) {
        http.get(`${BASE_URL}/api/products`);
    }

    sleep(1);
}
