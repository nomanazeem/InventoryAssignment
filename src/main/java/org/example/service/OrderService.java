package org.example.service;

import org.example.model.Order;
import org.example.model.Product;

import java.util.List;

public class OrderService implements IOrderManagement {
    private List<Order> orders;

    @Override
    public void addOrder(Order order) {
        //Check Inventory if exists
        ProductInventory inventory = new ProductInventory();
        Product product = inventory.getProduct(order.getProduct().getName());
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        orders.add(order);
    }

    @Override
    public void removeOrder(String id) {
        orders.removeIf(o -> o.getId().equals(id));
    }

    @Override
    public Order getOrder(String id) {
        return orders.stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Order> allOrders() {
        return orders;
    }
}
