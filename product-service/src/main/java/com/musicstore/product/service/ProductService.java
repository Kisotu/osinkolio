package com.musicstore.product.service;

import com.musicstore.product.api.v1.dto.InventoryResponse;
import com.musicstore.product.api.v1.dto.ProductRequest;
import com.musicstore.product.api.v1.dto.ProductResponse;
import com.musicstore.product.domain.entity.Category;
import com.musicstore.product.domain.entity.Inventory;
import com.musicstore.product.domain.entity.Product;
import com.musicstore.product.domain.repository.CategoryRepository;
import com.musicstore.product.domain.repository.InventoryRepository;
import com.musicstore.product.domain.repository.ProductRepository;
import com.musicstore.product.search.service.ProductIndexService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductIndexService indexService;
    private final ProductMapper mapper;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable)
            .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return mapper.toResponse(productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id)));
    }

    @Transactional(readOnly = true)
    public ProductResponse getBySku(String sku) {
        return mapper.toResponse(productRepository.findBySku(sku)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + sku)));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("Product with SKU " + request.sku() + " already exists");
        }

        Product product = Product.builder()
            .sku(request.sku())
            .name(request.name())
            .description(request.description())
            .brand(request.brand())
            .price(request.price())
            .weight(request.weight())
            .images(request.images() != null ? request.images() : "[]")
            .isActive(request.isActive() != null ? request.isActive() : true)
            .build();

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.categoryId()));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);

        Inventory inventory = Inventory.builder()
            .product(saved)
            .productId(saved.getId())
            .quantityReservable(0)
            .quantityOnHand(0)
            .lowStockThreshold(10)
            .build();
        inventoryRepository.save(inventory);

        indexService.indexProduct(saved);

        return mapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        if (!product.getSku().equals(request.sku()) && productRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("Product with SKU " + request.sku() + " already exists");
        }

        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setBrand(request.brand());
        product.setPrice(request.price());
        product.setWeight(request.weight());
        product.setImages(request.images() != null ? request.images() : "[]");
        product.setIsActive(request.isActive() != null ? request.isActive() : product.getIsActive());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.categoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        Product saved = productRepository.save(product);
        indexService.indexProduct(saved);

        return mapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
        indexService.removeFromIndex(id);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String search, Long categoryId,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         Boolean isActive, Pageable pageable) {
        return productRepository.searchProducts(search, categoryId, minPrice, maxPrice, isActive, pageable)
            .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long productId) {
        Inventory inventory = inventoryRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + productId));
        return mapper.toResponse(inventory);
    }
}