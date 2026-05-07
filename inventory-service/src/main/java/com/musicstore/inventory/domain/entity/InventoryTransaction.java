package com.musicstore.inventory.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "inventory_transaction")
@EntityListeners(AuditingEntityListener.class)
@Data
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    private Long orderId;

    @Column(nullable = false)
    private String transactionType;

    @Column(nullable = false)
    private Integer quantityChange;

    @Column(nullable = false)
    private Integer quantityBefore;

    @Column(nullable = false)
    private Integer quantityAfter;

    private String reason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
