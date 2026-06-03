package org.example.service;

import org.example.model.Product;

import java.util.List;

public interface IProductInventory {
    Product getProduct(String name);
    void addProduct(Product product);
    void removeProduct(String name);
    List<Product> allProducts();
}
