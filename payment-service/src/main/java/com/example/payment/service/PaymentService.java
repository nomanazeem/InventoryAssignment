package com.example.payment.service;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Transactional
    public boolean processPayment(String orderId, String customerId, Double amount) {
        try {
            // Create payment record
            Payment payment = new Payment(orderId, customerId, amount);
            payment.setStatus(PaymentStatus.PROCESSING);
            payment = paymentRepository.save(payment);

            // Simulate payment processing
            boolean success = amount <= 10000; // Allow up to $10,000
            payment.setTransactionId(UUID.randomUUID().toString());

            if (success) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                return true;
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage("Amount exceeds limit");
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                return false;
            }

        } catch (Exception e) {
            Payment payment = new Payment(orderId, customerId, amount);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("Payment processing failed: " + e.getMessage());
            paymentRepository.save(payment);
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }

    public Payment getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    public List<Payment> getPaymentsByCustomerId(String customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }

    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Transactional
    public Payment refundPayment(String orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
}