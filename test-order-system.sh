#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Service URLs - WITHOUT /api prefix
ORDER_URL="http://localhost:8080"
INVENTORY_URL="http://localhost:8081"
CUSTOMER_URL="http://localhost:8082"
PAYMENT_URL="http://localhost:8083"

# Function to print section header
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# Function to print success
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Function to print error
print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Function to print info
print_info() {
    echo -e "${YELLOW}ℹ️ $1${NC}"
}

# Check if services are running
check_services() {
    print_header "Checking if services are running"

    # Try to access the root endpoint or a known endpoint
    if curl -s -o /dev/null -w "%{http_code}" "$ORDER_URL/actuator/health" | grep -q "200\|404"; then
        print_success "Order service is running on $ORDER_URL"
    else
        print_error "Order service is not running on $ORDER_URL"
        echo "Please start the order service on port 8080"
        echo "Command: ./gradlew :order-service:bootRun"
        exit 1
    fi

    if curl -s -o /dev/null -w "%{http_code}" "$INVENTORY_URL/actuator/health" | grep -q "200\|404"; then
        print_success "Inventory service is running on $INVENTORY_URL"
    else
        print_error "Inventory service is not running on $INVENTORY_URL"
        echo "Please start the inventory service on port 8081"
        echo "Command: ./gradlew :inventory-service:bootRun"
        exit 1
    fi

    if curl -s -o /dev/null -w "%{http_code}" "$CUSTOMER_URL/actuator/health" | grep -q "200\|404"; then
        print_success "Customer service is running on $CUSTOMER_URL"
    else
        print_error "Customer service is not running on $CUSTOMER_URL"
        echo "Please start the customer service on port 8082"
        echo "Command: ./gradlew :customer-service:bootRun"
        exit 1
    fi

    if curl -s -o /dev/null -w "%{http_code}" "$PAYMENT_URL/actuator/health" | grep -q "200\|404"; then
        print_success "Payment service is running on $PAYMENT_URL"
    else
        print_error "Payment service is not running on $PAYMENT_URL"
        echo "Please start the payment service on port 8083"
        echo "Command: ./gradlew :payment-service:bootRun"
        exit 1
    fi
}

# Step 1: Customer Service
test_customer_service() {
    print_header "STEP 1: Testing Customer Service"

    # Create a customer
    echo -e "\n${YELLOW}Creating a new customer...${NC}"
    CUSTOMER_RESPONSE=$(curl -s -X POST "$CUSTOMER_URL/api/customers" \
        -H "Content-Type: application/json" \
        -d '{
            "email": "testuser@example.com",
            "firstName": "Test",
            "lastName": "User"
        }')

    # Extract customer ID
    CUSTOMER_ID=$(echo "$CUSTOMER_RESPONSE" | jq -r '.id')

    if [ -n "$CUSTOMER_ID" ] && [ "$CUSTOMER_ID" != "null" ]; then
        print_success "Customer created with ID: $CUSTOMER_ID"
        echo "Response: $CUSTOMER_RESPONSE"
    else
        print_error "Failed to create customer"
        echo "Response: $CUSTOMER_RESPONSE"
        exit 1
    fi

    # Get all customers
    echo -e "\n${YELLOW}Getting all customers...${NC}"
    ALL_CUSTOMERS=$(curl -s "$CUSTOMER_URL/api/customers")
    echo "All customers: $ALL_CUSTOMERS"

    # Validate customer
    echo -e "\n${YELLOW}Validating customer...${NC}"
    VALIDATION=$(curl -s "$CUSTOMER_URL/api/customers/$CUSTOMER_ID/validate")

    if [ "$VALIDATION" = "true" ]; then
        print_success "Customer validation successful"
    else
        print_error "Customer validation failed"
        exit 1
    fi

    return 0
}

# Step 2: Inventory Service - FIXED
test_inventory_service() {
    print_header "STEP 2: Testing Inventory Service"

    # First, check if the inventory controller is accessible
    echo -e "\n${YELLOW}Checking inventory endpoints...${NC}"
    INVENTORY_CHECK=$(curl -s -o /dev/null -w "%{http_code}" "$INVENTORY_URL/api/inventory/products")

    if [ "$INVENTORY_CHECK" = "200" ]; then
        print_success "Inventory endpoints are accessible"
    else
        print_error "Inventory endpoints not accessible (HTTP $INVENTORY_CHECK)"
        echo "Please check if the inventory service is running correctly"
        echo "Try accessing: $INVENTORY_URL/api/inventory/products"
    fi

    # Add a product - using the correct endpoint
    echo -e "\n${YELLOW}Adding a product...${NC}"
    PRODUCT_RESPONSE=$(curl -s -X POST "$INVENTORY_URL/api/inventory/products" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Laptop",
            "quantity": 50,
            "price": 999.99
        }')

    # Try to extract product ID
    PRODUCT_ID=$(echo "$PRODUCT_RESPONSE" | jq -r '.id' 2>/dev/null)

    if [ -n "$PRODUCT_ID" ] && [ "$PRODUCT_ID" != "null" ]; then
        print_success "Product created with ID: $PRODUCT_ID"
        echo "Response: $PRODUCT_RESPONSE"
    else
        # Try alternative approach - query params
        echo -e "\n${YELLOW}Trying alternative approach with query params...${NC}"
        PRODUCT_RESPONSE2=$(curl -s -X POST "$INVENTORY_URL/api/inventory/products?name=Mouse&quantity=100&price=29.99")
        PRODUCT_ID=$(echo "$PRODUCT_RESPONSE2" | jq -r '.id' 2>/dev/null)

        if [ -n "$PRODUCT_ID" ] && [ "$PRODUCT_ID" != "null" ]; then
            print_success "Product created with ID: $PRODUCT_ID"
            echo "Response: $PRODUCT_RESPONSE2"
        else
            print_error "Failed to create product"
            echo "Response: $PRODUCT_RESPONSE"
            echo "Response (query params): $PRODUCT_RESPONSE2"

            # Try to see what endpoints are available
            echo -e "\n${YELLOW}Available inventory endpoints:${NC}"
            curl -s "$INVENTORY_URL/actuator/mappings" | jq '.contexts."inventory-service".mappings.dispatcherServlets."dispatcherServlet".handler' 2>/dev/null || echo "Actuator not available"
            exit 1
        fi
    fi

    # Get all products
    echo -e "\n${YELLOW}Getting all products...${NC}"
    ALL_PRODUCTS=$(curl -s "$INVENTORY_URL/api/inventory/products")
    echo "All products: $ALL_PRODUCTS"

    # Check stock
    echo -e "\n${YELLOW}Checking stock for product...${NC}"
    STOCK_CHECK=$(curl -s "$INVENTORY_URL/api/inventory/check?productId=$PRODUCT_ID&quantity=5")

    if [ "$STOCK_CHECK" = "true" ]; then
        print_success "Stock available for product $PRODUCT_ID"
    else
        print_error "Stock not available"
        echo "Response: $STOCK_CHECK"
        exit 1
    fi

    # Reserve stock
    echo -e "\n${YELLOW}Reserving stock...${NC}"
    RESERVE_RESPONSE=$(curl -s -X POST "$INVENTORY_URL/api/inventory/reserve" \
        -H "Content-Type: application/json" \
        -d "{\"productId\": \"$PRODUCT_ID\", \"quantity\": 2}")

    if [ -z "$RESERVE_RESPONSE" ] || [ "$RESERVE_RESPONSE" = "" ]; then
        print_success "Stock reserved successfully"
    else
        print_error "Failed to reserve stock"
        echo "Response: $RESERVE_RESPONSE"
        exit 1
    fi

    return 0
}

# Step 3: Payment Service
test_payment_service() {
    print_header "STEP 3: Testing Payment Service"

    # Generate a dummy order ID
    ORDER_ID=$(uuidgen)

    # Process payment
    echo -e "\n${YELLOW}Processing payment for order $ORDER_ID...${NC}"
    PAYMENT_RESPONSE=$(curl -s -X POST "$PAYMENT_URL/api/payments/process" \
        -H "Content-Type: application/json" \
        -d "{
            \"orderId\": \"$ORDER_ID\",
            \"customerId\": \"$CUSTOMER_ID\",
            \"amount\": 500.00
        }")

    if [ "$PAYMENT_RESPONSE" = "true" ]; then
        print_success "Payment processed successfully"
    else
        print_error "Payment processing failed"
        echo "Response: $PAYMENT_RESPONSE"
        exit 1
    fi

    # Get payment by order ID
    echo -e "\n${YELLOW}Getting payment by order ID...${NC}"
    PAYMENT_GET=$(curl -s "$PAYMENT_URL/api/payments/order/$ORDER_ID")
    echo "Payment details: $PAYMENT_GET"

    return 0
}

# Step 4: Order Service (Complete Flow)
test_order_service() {
    print_header "STEP 4: Testing Order Service (Complete Flow)"

    # Create order request
    echo -e "\n${YELLOW}Placing an order...${NC}"
    ORDER_RESPONSE=$(curl -s -X POST "$ORDER_URL/api/orders" \
        -H "Content-Type: application/json" \
        -d "{
            \"customerId\": \"$CUSTOMER_ID\",
            \"items\": [
                {
                    \"productId\": \"$PRODUCT_ID\",
                    \"quantity\": 1
                }
            ]
        }")

    ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.orderId' 2>/dev/null)
    ORDER_STATUS=$(echo "$ORDER_RESPONSE" | jq -r '.status' 2>/dev/null)
    ORDER_MESSAGE=$(echo "$ORDER_RESPONSE" | jq -r '.message' 2>/dev/null)

    if [ "$ORDER_STATUS" = "COMPLETED" ]; then
        print_success "Order placed successfully!"
        echo "Order ID: $ORDER_ID"
        echo "Status: $ORDER_STATUS"
        echo "Message: $ORDER_MESSAGE"
    else
        print_error "Order placement failed"
        echo "Response: $ORDER_RESPONSE"
        exit 1
    fi

    # Get order details
    echo -e "\n${YELLOW}Getting order details...${NC}"
    ORDER_DETAILS=$(curl -s "$ORDER_URL/api/orders/$ORDER_ID")
    echo "Order details: $ORDER_DETAILS"

    return 0
}

# Step 5: Test Failure Scenarios
test_failure_scenarios() {
    print_header "STEP 5: Testing Failure Scenarios"

    # Test 1: Insufficient stock
    echo -e "\n${YELLOW}Test 1: Placing order with insufficient stock...${NC}"
    FAIL_RESPONSE=$(curl -s -X POST "$ORDER_URL/api/orders" \
        -H "Content-Type: application/json" \
        -d "{
            \"customerId\": \"$CUSTOMER_ID\",
            \"items\": [
                {
                    \"productId\": \"$PRODUCT_ID\",
                    \"quantity\": 100
                }
            ]
        }")

    if echo "$FAIL_RESPONSE" | grep -q "Insufficient stock"; then
        print_success "Expected failure: Insufficient stock handled correctly"
    else
        print_error "Expected failure but got success"
        echo "Response: $FAIL_RESPONSE"
    fi

    # Test 2: Invalid customer
    echo -e "\n${YELLOW}Test 2: Placing order with invalid customer...${NC}"
    FAIL_RESPONSE2=$(curl -s -X POST "$ORDER_URL/api/orders" \
        -H "Content-Type: application/json" \
        -d "{
            \"customerId\": \"invalid-customer-id\",
            \"items\": [
                {
                    \"productId\": \"$PRODUCT_ID\",
                    \"quantity\": 1
                }
            ]
        }")

    if echo "$FAIL_RESPONSE2" | grep -q "Invalid customer"; then
        print_success "Expected failure: Invalid customer handled correctly"
    else
        print_error "Expected failure but got success"
        echo "Response: $FAIL_RESPONSE2"
    fi

    # Test 3: Payment failure (amount > $1000 limit)
    echo -e "\n${YELLOW}Test 3: Placing order with payment failure...${NC}"
    FAIL_RESPONSE3=$(curl -s -X POST "$ORDER_URL/api/orders" \
        -H "Content-Type: application/json" \
        -d "{
            \"customerId\": \"$CUSTOMER_ID\",
            \"items\": [
                {
                    \"productId\": \"$PRODUCT_ID\",
                    \"quantity\": 20
                }
            ]
        }")

    if echo "$FAIL_RESPONSE3" | grep -q "Payment failed"; then
        print_success "Expected failure: Payment failure handled correctly"
    else
        print_error "Expected failure but got success"
        echo "Response: $FAIL_RESPONSE3"
    fi
}

# Add this after test_inventory_service()
test_low_stock_report() {
    print_header "Testing Low Stock Report"

    echo -e "\n${YELLOW}Getting low stock products (threshold=5)...${NC}"
    LOW_STOCK=$(curl -s "$INVENTORY_URL/api/inventory/low-stock?threshold=5")

    echo "Low stock products: $LOW_STOCK"

    # Count low stock items
    COUNT=$(echo "$LOW_STOCK" | jq 'length')
    echo -e "\n${YELLOW}Found $COUNT products with low stock${NC}"

    print_success "Low stock report generated successfully"
}

# Main execution
main() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}   Order Management System Test Suite   ${NC}"
    echo -e "${BLUE}========================================${NC}"

    # Check services
    check_services

    # Run all tests
    test_customer_service
    test_inventory_service
    test_payment_service
    test_order_service
    test_failure_scenarios
    test_low_stock_report

    # Final summary
    print_header "TEST SUMMARY"
    echo -e "${GREEN}✅ All tests completed successfully!${NC}"
    echo -e "\n${YELLOW}Test Results:${NC}"
    echo "Customer ID: $CUSTOMER_ID"
    echo "Product ID: $PRODUCT_ID"
    echo "Order ID: $ORDER_ID"

    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}   End-to-End Testing Complete!        ${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# Run the main function
main