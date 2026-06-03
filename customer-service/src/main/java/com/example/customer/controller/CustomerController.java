package com.example.customer.controller;

import com.example.customer.entity.Customer;
import com.example.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/{customerId}/validate")
    public ResponseEntity<Boolean> validateCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(customerService.validateCustomer(customerId));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(customerService.getCustomer(customerId));
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        Customer customer = customerService.createCustomer(email, firstName, lastName);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PatchMapping("/{customerId}/status")
    public ResponseEntity<Customer> updateCustomerStatus(
            @PathVariable String customerId,
            @RequestBody Map<String, Boolean> request) {
        Boolean active = request.get("active");
        Customer customer = customerService.updateCustomerStatus(customerId, active);
        return ResponseEntity.ok(customer);
    }
}