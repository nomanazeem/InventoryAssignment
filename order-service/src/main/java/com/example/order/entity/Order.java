package com.example.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders") // Changed from "order" to "orders"
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String customerId;
    private String status;
    private LocalDateTime createdAt;

    public Order() {
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }
}