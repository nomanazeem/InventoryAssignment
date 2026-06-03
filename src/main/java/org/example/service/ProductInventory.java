package org.example.service;

import org.example.model.Product;

import java.util.List;

public class ProductInventory implements IProductInventory {
    private List<Product> products;

    @Override
    public Product getProduct(String name) {
        return products.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public void addProduct(Product product) {
        products.add(product);
    }

    @Override
    public void removeProduct(String name) {
        products.removeIf(p -> p.getName().equals(name));
    }

    @Override
    public List<Product> allProducts() {
        return products;
    }
}
