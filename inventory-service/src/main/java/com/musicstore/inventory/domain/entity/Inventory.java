package com.musicstore.inventory.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "inventory")
@EntityListeners(AuditingEntityListener.class)
@Data
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private Integer quantityOnHand = 0;

    @Column(nullable = false)
    private Integer quantityReserved = 0;

    @Column(nullable = false)
    private Integer lowStockThreshold = 10;

    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public boolean canReserve(int quantity) {
        return quantity > 0 && getAvailableQuantity() >= quantity;
    }

    public int reserve(int quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("Insufficient stock to reserve " + quantity + " units for product " + productId);
        }
        this.quantityReserved += quantity;
        return this.quantityReserved;
    }

    public int release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to release must be positive");
        }
        int toRelease = Math.min(quantity, this.quantityReserved);
        this.quantityReserved -= toRelease;
        return this.quantityReserved;
    }

    public int getAvailableQuantity() {
        return this.quantityOnHand - this.quantityReserved;
    }
}
