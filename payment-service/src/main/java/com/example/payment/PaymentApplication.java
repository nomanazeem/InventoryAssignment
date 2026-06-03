package com.example.payment;

import com.example.payment.repository.PaymentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(PaymentRepository paymentRepository) {
        return args -> {
            // Sample data will be created dynamically during order processing
            System.out.println("Payment service started!");
        };
    }
}