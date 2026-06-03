package com.example.inventory.service;

import com.example.inventory.entity.Product;
import com.example.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ProductRepository productRepository;

    // Add this method
    @Transactional
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    // Add this method
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Add this method
    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findByQuantityLessThan(threshold);
    }

    public boolean checkStock(String productId, Integer quantity) {
        return productRepository.findById(productId)
                .map(product -> product.getQuantity() >= quantity)
                .orElse(false);
    }

    @Transactional
    public void reserveStock(String productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
    }

    @Transactional
    public void releaseStock(String productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);
    }
}