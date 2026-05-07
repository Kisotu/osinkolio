package com.musicstore.product.kafka;

import com.musicstore.product.domain.entity.Product;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.product-created:product.created}")
    private String productCreatedTopic;

    public void publishProductCreated(Product product) {
        log.info("Publishing product.created event for product: {}", product.getId());

        var event = new ProductCreatedEvent(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getPrice(),
            product.getCategory() != null ? product.getCategory().getName() : null,
            product.getBrand()
        );

        kafkaTemplate.send(productCreatedTopic, product.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish product.created event: {}", ex.getMessage(), ex);
                    } else {
                        log.debug("Published product.created event to topic: {}", productCreatedTopic);
                    }
                });
    }

    // Event DTO for product created
    public record ProductCreatedEvent(
            Long productId,
            String sku,
            String name,
            BigDecimal price,
            String category,
            String brand,
            Instant timestamp
    ) {
        public ProductCreatedEvent(Long productId, String sku, String name, BigDecimal price, String category, String brand) {
            this(productId, sku, name, price, category, brand, Instant.now());
        }
    }
}
