package com.musicstore.product.search.service;

import com.musicstore.product.domain.entity.Product;
import com.musicstore.product.domain.repository.ProductRepository;
import com.musicstore.product.search.document.ProductDocument;
import com.musicstore.product.search.repository.ProductSearchRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexService {

    private final ProductSearchRepository searchRepository;
    private final ProductRepository productRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void reindexAll() {
        log.info("Starting full product reindex to Elasticsearch...");
        int page = 0;
        int totalIndexed = 0;
        Page<Product> productPage;

        do {
            productPage = productRepository.findAll(PageRequest.of(page, 100));
            productPage.getContent().forEach(this::indexProduct);
            totalIndexed += productPage.getNumberOfElements();
            page++;
        } while (productPage.hasNext());

        log.info("Full reindex complete. Indexed {} products.", totalIndexed);
    }

    @Transactional(readOnly = true)
    public void indexProduct(Product product) {
        ProductDocument document = ProductDocument.builder()
            .id(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .description(product.getDescription())
            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
            .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
            .brand(product.getBrand())
            .price(product.getPrice())
            .isActive(product.getIsActive())
            .build();

        searchRepository.save(document);
        log.debug("Indexed product {}: {}", product.getId(), product.getSku());
    }

    public void removeFromIndex(Long productId) {
        searchRepository.deleteById(productId);
        log.debug("Removed product {} from index", productId);
    }
}