/**
 * Performance Testing Script using k6
 * 
 * k6 is a modern load testing tool for developers
 * Install: https://k6.io/docs/getting-started/installation/
 * Run: k6 run performance/k6-load-test.js
 * 
 * Test Scenarios:
 * 1. Login API Load Test
 * 2. Product CRUD Load Test
 * 3. Stress Test
 * 4. Spike Test
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const loginDuration = new Trend('login_duration');
const productListDuration = new Trend('product_list_duration');
const productCreateDuration = new Trend('product_create_duration');

// Configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';

// Test options
export const options = {
  scenarios: {
    // Scenario 1: Smoke Test (Basic functionality check)
    smoke_test: {
      executor: 'constant-vus',
      vus: 1,
      duration: '30s',
      tags: { test_type: 'smoke' },
      startTime: '0s',
    },
    
    // Scenario 2: Load Test (Normal expected load)
    load_test: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 10 },   // Ramp up to 10 users
        { duration: '3m', target: 10 },   // Stay at 10 users
        { duration: '1m', target: 20 },   // Ramp up to 20 users
        { duration: '3m', target: 20 },   // Stay at 20 users
        { duration: '1m', target: 0 },    // Ramp down to 0
      ],
      tags: { test_type: 'load' },
      startTime: '30s',
    },
    
    // Scenario 3: Stress Test (Beyond normal capacity)
    stress_test: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: 50 },   // Ramp up to 50 users
        { duration: '5m', target: 50 },   // Stay at 50 users
        { duration: '2m', target: 100 },  // Ramp up to 100 users
        { duration: '5m', target: 100 },  // Stay at 100 users
        { duration: '2m', target: 0 },    // Ramp down
      ],
      tags: { test_type: 'stress' },
      startTime: '10m',
      exec: 'stressTest',
    },
    
    // Scenario 4: Spike Test (Sudden traffic spike)
    spike_test: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 100 }, // Spike to 100 users
        { duration: '1m', target: 100 },  // Stay at 100 users
        { duration: '10s', target: 0 },   // Drop to 0
      ],
      tags: { test_type: 'spike' },
      startTime: '25m',
      exec: 'spikeTest',
    },
  },
  
  thresholds: {
    // Response time thresholds
    http_req_duration: ['p(95)<2000', 'p(99)<5000'], // 95% under 2s, 99% under 5s
    
    // Error rate threshold
    errors: ['rate<0.1'], // Error rate under 10%
    
    // Custom metric thresholds
    login_duration: ['p(95)<1000'],         // Login under 1s for 95%
    product_list_duration: ['p(95)<500'],   // Product list under 500ms for 95%
    product_create_duration: ['p(95)<1500'], // Create under 1.5s for 95%
  },
};

// Test Data
const testUsers = [
  { username: 'testuser1', password: 'Test123' },
  { username: 'testuser2', password: 'Test456' },
  { username: 'loadtest', password: 'Load123' },
];

const testProducts = [
  { name: 'Test Product 1', description: 'Load test product', price: 99.99, quantity: 100 },
  { name: 'Test Product 2', description: 'Stress test product', price: 149.99, quantity: 50 },
];

// Utility functions
function getRandomUser() {
  return testUsers[Math.floor(Math.random() * testUsers.length)];
}

function getRandomProduct() {
  const product = testProducts[Math.floor(Math.random() * testProducts.length)];
  return {
    ...product,
    name: `${product.name} - ${Date.now()}`, // Make unique
  };
}

// Main test function (default scenario)
export default function() {
  group('Login API Test', function() {
    const user = getRandomUser();
    const loginPayload = JSON.stringify({
      username: user.username,
      password: user.password,
    });
    
    const loginParams = {
      headers: { 'Content-Type': 'application/json' },
    };
    
    const startTime = Date.now();
    const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, loginParams);
    loginDuration.add(Date.now() - startTime);
    
    const loginSuccess = check(loginRes, {
      'login status is 200': (r) => r.status === 200,
      'login response has token': (r) => r.json('token') !== undefined,
    });
    
    errorRate.add(!loginSuccess);
    
    if (loginSuccess) {
      const token = loginRes.json('token');
      
      // Test authenticated endpoints
      group('Product API Test', function() {
        const authHeaders = {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        };
        
        // Get all products
        const listStart = Date.now();
        const listRes = http.get(`${BASE_URL}/products`, { headers: authHeaders });
        productListDuration.add(Date.now() - listStart);
        
        check(listRes, {
          'product list status is 200': (r) => r.status === 200,
          'product list is array': (r) => Array.isArray(r.json()),
        });
        
        // Create a product
        const newProduct = getRandomProduct();
        const createStart = Date.now();
        const createRes = http.post(
          `${BASE_URL}/products`,
          JSON.stringify(newProduct),
          { headers: authHeaders }
        );
        productCreateDuration.add(Date.now() - createStart);
        
        const createSuccess = check(createRes, {
          'create product status is 201': (r) => r.status === 201,
          'created product has id': (r) => r.json('id') !== undefined,
        });
        
        errorRate.add(!createSuccess);
        
        // Cleanup: Delete created product
        if (createSuccess) {
          const productId = createRes.json('id');
          http.del(`${BASE_URL}/products/${productId}`, null, { headers: authHeaders });
        }
      });
    }
    
    sleep(1); // Think time between iterations
  });
}

// Stress test function
export function stressTest() {
  const user = getRandomUser();
  const loginPayload = JSON.stringify({
    username: user.username,
    password: user.password,
  });
  
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    loginPayload,
    { headers: { 'Content-Type': 'application/json' } }
  );
  
  const success = check(loginRes, {
    'stress test login status 200 or 429': (r) => r.status === 200 || r.status === 429,
  });
  
  errorRate.add(!success && loginRes.status !== 429);
  
  sleep(0.5);
}

// Spike test function
export function spikeTest() {
  const user = getRandomUser();
  const loginPayload = JSON.stringify({
    username: user.username,
    password: user.password,
  });
  
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    loginPayload,
    { headers: { 'Content-Type': 'application/json' } }
  );
  
  const success = check(loginRes, {
    'spike test handles request': (r) => r.status < 500,
  });
  
  errorRate.add(!success);
  
  sleep(0.1);
}

// Summary handler
export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: '  ', enableColors: true }),
    'performance/k6-results.json': JSON.stringify(data, null, 2),
    'performance/k6-results.html': htmlReport(data),
  };
}

// Helper function for text summary (simplified)
function textSummary(data, options) {
  const metrics = data.metrics;
  let summary = '\n========== K6 PERFORMANCE TEST RESULTS ==========\n\n';
  
  summary += `Total Requests: ${metrics.http_reqs?.values?.count || 0}\n`;
  summary += `Failed Requests: ${metrics.http_req_failed?.values?.rate || 0}\n`;
  summary += `Average Response Time: ${Math.round(metrics.http_req_duration?.values?.avg || 0)}ms\n`;
  summary += `P95 Response Time: ${Math.round(metrics.http_req_duration?.values?.p95 || 0)}ms\n`;
  summary += `P99 Response Time: ${Math.round(metrics.http_req_duration?.values?.p99 || 0)}ms\n\n`;
  
  summary += '========== THRESHOLDS ==========\n';
  for (const [name, threshold] of Object.entries(data.thresholds || {})) {
    const status = threshold.ok ? '✅ PASS' : '❌ FAIL';
    summary += `${name}: ${status}\n`;
  }
  
  return summary;
}

// Helper function for HTML report (simplified)
function htmlReport(data) {
  return `<!DOCTYPE html>
<html>
<head>
  <title>K6 Performance Test Results</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; }
    .metric { margin: 10px 0; padding: 10px; background: #f5f5f5; }
    .pass { color: green; }
    .fail { color: red; }
    h1 { color: #333; }
    table { border-collapse: collapse; width: 100%; }
    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
    th { background: #4CAF50; color: white; }
  </style>
</head>
<body>
  <h1>K6 Performance Test Results</h1>
  <h2>Summary</h2>
  <table>
    <tr><th>Metric</th><th>Value</th></tr>
    <tr><td>Total Requests</td><td>${data.metrics?.http_reqs?.values?.count || 0}</td></tr>
    <tr><td>Request Rate</td><td>${Math.round(data.metrics?.http_reqs?.values?.rate || 0)}/s</td></tr>
    <tr><td>Average Response Time</td><td>${Math.round(data.metrics?.http_req_duration?.values?.avg || 0)}ms</td></tr>
    <tr><td>P95 Response Time</td><td>${Math.round(data.metrics?.http_req_duration?.values?.['p(95)'] || 0)}ms</td></tr>
    <tr><td>P99 Response Time</td><td>${Math.round(data.metrics?.http_req_duration?.values?.['p(99)'] || 0)}ms</td></tr>
    <tr><td>Error Rate</td><td>${(data.metrics?.errors?.values?.rate * 100 || 0).toFixed(2)}%</td></tr>
  </table>
  
  <h2>Threshold Results</h2>
  <table>
    <tr><th>Threshold</th><th>Status</th></tr>
    ${Object.entries(data.thresholds || {}).map(([name, t]) => 
      `<tr><td>${name}</td><td class="${t.ok ? 'pass' : 'fail'}">${t.ok ? 'PASS' : 'FAIL'}</td></tr>`
    ).join('')}
  </table>
</body>
</html>`;
}
