package org.example.service;

import org.example.model.Order;

import java.util.List;

public interface IOrderManagement {
    void addOrder(Order order);
    void removeOrder(String id);
    Order getOrder(String id);
    List<Order> allOrders();
}
