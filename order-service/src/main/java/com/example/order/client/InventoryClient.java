package com.example.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class InventoryClient {
    private final RestTemplate restTemplate;
    private final String inventoryUrl;

    public InventoryClient(@Value("${services.inventory.url}") String inventoryUrl) {
        this.restTemplate = new RestTemplate();
        this.inventoryUrl = inventoryUrl;
    }

    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000),
            include = {RuntimeException.class}
    )
    public boolean checkStock(String productId, Integer quantity) {
        try {
            String url = inventoryUrl + "/api/inventory/check?productId=" + productId + "&quantity=" + quantity;
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            return response.getBody() != null && response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Inventory service unavailable", e);
        }
    }

    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000),
            include = {RuntimeException.class}
    )
    public void reserveStock(String productId, Integer quantity) {
        try {
            String url = inventoryUrl + "/api/inventory/reserve";
            restTemplate.postForObject(url, Map.of("productId", productId, "quantity", quantity), Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Inventory service unavailable", e);
        }
    }

    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000),
            include = {RuntimeException.class}
    )
    public void releaseStock(String productId, Integer quantity) {
        try {
            String url = inventoryUrl + "/api/inventory/release";
            restTemplate.postForObject(url, Map.of("productId", productId, "quantity", quantity), Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Inventory service unavailable", e);
        }
    }
}