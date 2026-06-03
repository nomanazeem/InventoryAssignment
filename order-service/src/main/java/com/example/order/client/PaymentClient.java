package com.example.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class PaymentClient {
    private final RestTemplate restTemplate;
    private final String paymentUrl;

    public PaymentClient(@Value("${services.payment.url}") String paymentUrl) {
        this.restTemplate = new RestTemplate();
        this.paymentUrl = paymentUrl;
    }

    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000),
            include = {RuntimeException.class}
    )
    public boolean processPayment(String orderId, String customerId, Double amount) {
        try {
            String url = paymentUrl + "/api/payments/process";
            Map<String, Object> request = Map.of(
                    "orderId", orderId,
                    "customerId", customerId,
                    "amount", amount
            );
            ResponseEntity<Boolean> response = restTemplate.postForEntity(url, request, Boolean.class);
            return response.getBody() != null && response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Payment service unavailable", e);
        }
    }
}