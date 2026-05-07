package com.musicstore.inventory.service;

import com.musicstore.inventory.domain.entity.Inventory;
import com.musicstore.inventory.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public Inventory createOrUpdateInventory(Long productId, String sku, Integer initialQuantity) {
        log.info("Creating/updating inventory for product: {}, quantity: {}", productId, initialQuantity);

        return inventoryRepository.findByProductId(productId)
                .map(existing -> {
                    existing.setQuantityOnHand(initialQuantity);
                    return inventoryRepository.save(existing);
                })
                .orElseGet(() -> {
                    Inventory inventory = new Inventory();
                    inventory.setProductId(productId);
                    inventory.setSku(sku);
                    inventory.setQuantityOnHand(initialQuantity != null ? initialQuantity : 0);
                    inventory.setQuantityReserved(0);
                    inventory.setLowStockThreshold(10);
                    return inventoryRepository.save(inventory);
                });
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(Long productId, Integer quantity) {
        return inventoryRepository.findByProductId(productId)
                .map(inventory -> inventory.canReserve(quantity))
                .orElse(false);
    }
}
