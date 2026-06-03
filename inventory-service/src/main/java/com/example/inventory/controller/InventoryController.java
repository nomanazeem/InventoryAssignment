package com.example.inventory.controller;

import com.example.inventory.entity.Product;
import com.example.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    // Add this method to create products
    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.addProduct(product));
    }

    // Or if using query parameters:
    @PostMapping("/products-query")
    public ResponseEntity<Product> addProductWithParams(
            @RequestParam String name,
            @RequestParam Integer quantity,
            @RequestParam Double price) {
        Product product = new Product();
        product.setName(name);
        product.setQuantity(quantity);
        product.setPrice(price);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.addProduct(product));
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(inventoryService.getAllProducts());
    }
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkStock(
            @RequestParam String productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.checkStock(productId, quantity));
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveStock(@RequestBody Map<String, Object> request) {
        String productId = (String) request.get("productId");
        Integer quantity = (Integer) request.get("quantity");
        inventoryService.reserveStock(productId, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseStock(@RequestBody Map<String, Object> request) {
        String productId = (String) request.get("productId");
        Integer quantity = (Integer) request.get("quantity");
        inventoryService.releaseStock(productId, quantity);
        return ResponseEntity.ok().build();
    }
}