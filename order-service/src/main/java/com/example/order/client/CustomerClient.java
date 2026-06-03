package com.example.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomerClient {
    private final RestTemplate restTemplate;
    private final String customerUrl;

    public CustomerClient(@Value("${services.customer.url}") String customerUrl) {
        this.restTemplate = new RestTemplate();
        this.customerUrl = customerUrl;
    }

    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000),
            include = {RuntimeException.class}
    )
    public boolean validateCustomer(String customerId) {
        try {
            String url = customerUrl + "/api/customers/" + customerId + "/validate";
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            return response.getBody() != null && response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Customer service unavailable", e);
        }
    }
}