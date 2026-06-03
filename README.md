Here's a concise **README.md** file with app URLs, names, and instructions for running the test script:

# Order Management System - Microservices

## Service URLs

| Service | Port | Base URL | Health Check |
|---------|------|----------|---------------|
| **Order Service** | 8080 | http://localhost:8080 | http://localhost:8080/actuator/health |
| **Inventory Service** | 8081 | http://localhost:8081 | http://localhost:8081/actuator/health |
| **Customer Service** | 8082 | http://localhost:8082 | http://localhost:8082/actuator/health |
| **Payment Service** | 8083 | http://localhost:8083 | http://localhost:8083/actuator/health |

## Endpoints

| Service | Endpoint | Method | Description |
|---------|----------|--------|-------------|
| Order | `/api/orders` | POST | Place an order |
| Order | `/api/orders/{id}` | GET | Get order by ID |
| Inventory | `/api/inventory/products` | POST | Add a product |
| Inventory | `/api/inventory/products` | GET | Get all products |
| Inventory | `/api/inventory/check` | GET | Check stock |
| Inventory | `/api/inventory/reserve` | POST | Reserve stock |
| Customer | `/api/customers` | POST | Create customer |
| Customer | `/api/customers/{id}/validate` | GET | Validate customer |
| Payment | `/api/payments/process` | POST | Process payment |

## How to Run

### 1. Start all services (in separate terminals)

```bash
# Order Service
./gradlew :order-service:bootRun

# Inventory Service
./gradlew :inventory-service:bootRun

# Customer Service
./gradlew :customer-service:bootRun

# Payment Service
./gradlew :payment-service:bootRun
```

### 2. Run the test script

```bash
# Make the script executable
chmod +x test-order-system.sh

# Run the test
./test-order-system.sh
```

## Test Script Flow

The `test-order-system.sh` script performs:

1. **Customer Service** - Creates a test customer and validates
2. **Inventory Service** - Adds a product, checks stock, reserves stock
3. **Payment Service** - Processes a payment
4. **Order Service** - Places a complete order
5. **Failure Scenarios** - Tests insufficient stock, invalid customer, payment failure

## Sample API Calls

```bash
# Create a customer
curl -X POST http://localhost:8082/api/customers \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","firstName":"Test","lastName":"User"}'

# Add a product
curl -X POST http://localhost:8081/api/inventory/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","quantity":50,"price":999.99}'

# Place an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {"productId": "660e8400-e29b-41d4-a716-446655440001", "quantity": 1}
    ]
  }'
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Connection refused | Ensure all services are running on correct ports |
| 404 Not Found | Check endpoint URLs (use `/api` prefix) |
| Payment fails | Amount must be ≤ $1000 (or update PaymentService) |
| Product not found | Add product first using Inventory Service |

## Requirements

- Java 21
- Gradle 8.x
- curl
- jq (for JSON parsing in test script)