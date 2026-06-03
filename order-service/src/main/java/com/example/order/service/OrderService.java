package com.example.order.service;

import com.example.common.dto.OrderRequest;
import com.example.common.dto.OrderResponse;
import com.example.order.client.CustomerClient;
import com.example.order.client.InventoryClient;
import com.example.order.client.PaymentClient;
import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final CustomerClient customerClient;
    private final PaymentClient paymentClient;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        String orderId = null;

        try {
            // Step 1: Create order (PENDING) - Need orderId before payment
            Order order = new Order();
            order.setCustomerId(request.getCustomerId());
            order.setStatus("PENDING");
            order = orderRepository.save(order);
            orderId = order.getId();

            // Step 2: Validate customer
            if (!customerClient.validateCustomer(request.getCustomerId())) {
                throw new RuntimeException("Invalid customer");
            }

            // Step 3: Check inventory for all items
            for (var item : request.getItems()) {
                if (!inventoryClient.checkStock(item.getProductId(), item.getQuantity())) {
                    throw new RuntimeException("Insufficient stock for product: " + item.getProductId());
                }
            }

            // Step 4: Reserve inventory
            for (var item : request.getItems()) {
                inventoryClient.reserveStock(item.getProductId(), item.getQuantity());
            }

            // Step 5: Process payment - FIXED: Pass orderId as first parameter
            Double totalAmount = calculateTotal(request);
            if (!paymentClient.processPayment(orderId, request.getCustomerId(), totalAmount)) {
                throw new RuntimeException("Payment failed");
            }

            // Step 6: Update order to COMPLETED
            order.setStatus("COMPLETED");
            orderRepository.save(order);

            return new OrderResponse(order.getId(), "COMPLETED", "Order placed successfully");

        } catch (Exception e) {
            // Automatic rollback due to @Transactional
            // Release reserved stock if any
            if (orderId != null) {
                for (var item : request.getItems()) {
                    try {
                        inventoryClient.releaseStock(item.getProductId(), item.getQuantity());
                    } catch (Exception ex) {
                        // Log but don't throw - already rolling back
                    }
                }
            }
            throw new RuntimeException("Order failed: " + e.getMessage());
        }
    }

    private Double calculateTotal(OrderRequest request) {
        // Simplified - in real app, fetch prices from inventory service
        return request.getItems().stream()
                .mapToDouble(item -> 100.0 * item.getQuantity()) // Assuming $100 per item
                .sum();
    }
}