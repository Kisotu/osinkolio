package com.musicstore.inventory.domain.repository;

import com.musicstore.inventory.domain.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);
    Optional<Inventory> findBySku(String sku);
    List<Inventory> findByQuantityOnHandLessThanEqual(Integer threshold);
    boolean existsByProductId(Long productId);
}
