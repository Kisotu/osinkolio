package com.musicstore.product.service;

import com.musicstore.product.api.v1.dto.*;
import com.musicstore.product.domain.entity.Category;
import com.musicstore.product.domain.entity.Inventory;
import com.musicstore.product.domain.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            product.getCategory() != null ? product.getCategory().getId() : null,
            product.getCategory() != null ? product.getCategory().getName() : null,
            product.getBrand(),
            product.getPrice(),
            product.getWeight(),
            product.getImages(),
            product.getIsActive(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getParent() != null ? category.getParent().getId() : null,
            category.getParent() != null ? category.getParent().getName() : null,
            null,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }

    public InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
            inventory.getProductId(),
            inventory.getQuantityReservable(),
            inventory.getQuantityOnHand(),
            inventory.getLowStockThreshold(),
            inventory.isLowStock(),
            inventory.getUpdatedAt()
        );
    }
}