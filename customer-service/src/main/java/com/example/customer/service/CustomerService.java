package com.example.customer.service;

import com.example.customer.entity.Customer;
import com.example.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public boolean validateCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .map(Customer::getActive)
                .orElse(false);
    }

    public Customer getCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional
    public Customer createCustomer(String email, String firstName, String lastName) {
        if (customerRepository.existsByEmail(email)) {
            throw new RuntimeException("Customer with email " + email + " already exists");
        }
        Customer customer = new Customer(email, firstName, lastName);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateCustomerStatus(String customerId, Boolean active) {
        Customer customer = getCustomer(customerId);
        customer.setActive(active);
        return customerRepository.save(customer);
    }
}