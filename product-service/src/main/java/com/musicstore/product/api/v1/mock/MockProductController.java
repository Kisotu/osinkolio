package com.musicstore.product.api.v1.mock;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Profile("mock")
@RestController
@RequestMapping("/api/v1/products")
public class MockProductController {

    private static final List<Map<String, Object>> MOCK_PRODUCTS = List.of(
        Map.of(
            "id", 1L,
            "sku", "GTR-001",
            "name", "Fender Stratocaster",
            "description", "Classic electric guitar with alder body and maple neck",
            "category", "Electric Guitars",
            "brand", "Fender",
            "price", new BigDecimal("1299.99"),
            "weight", 3.5
        ),
        Map.of(
            "id", 2L,
            "sku", "PNO-001",
            "name", "Yamaha YDP-145",
            "description", "Digital piano with weighted keys and premium sound engine",
            "category", "Digital Pianos",
            "brand", "Yamaha",
            "price", new BigDecimal("899.00"),
            "weight", 25.0
        ),
        Map.of(
            "id", 3L,
            "sku", "AMP-001",
            "name", "Marshall DSL40CR",
            "description", "40W all-valve combo amplifier with dual channels",
            "category", "Amplifiers",
            "brand", "Marshall",
            "price", new BigDecimal("749.99"),
            "weight", 22.0
        )
    );

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts() {
        return ResponseEntity.ok(Map.of(
            "content", MOCK_PRODUCTS,
            "totalElements", MOCK_PRODUCTS.size(),
            "totalPages", 1,
            "number", 0,
            "size", 20,
            "first", true,
            "last", true,
            "empty", false
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        return MOCK_PRODUCTS.stream()
            .filter(p -> p.get("id").equals(id))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}