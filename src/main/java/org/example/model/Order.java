package org.example.model;

public class Order {
    private String id;
    private Product product;
    private int quantity;

    public Order(String id, Product product, int quantity) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
    }
    public String getId(){
        return id;
    }
    public Product getProduct(){
        return product;
    }
    public int getQuantity(){
        return quantity;
    }
}
