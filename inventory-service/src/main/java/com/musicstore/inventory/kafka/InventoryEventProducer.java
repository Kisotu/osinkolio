package com.musicstore.inventory.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishInventoryReserved(Long orderId, Long productId, Integer quantity, Boolean reserved) {
        log.info("Publishing inventory.reserved event for order: {}, product: {}", orderId, productId);
        var event = new InventoryReservedEvent(orderId, productId, quantity, reserved, Instant.now());
        kafkaTemplate.send("inventory.reserved", orderId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish inventory.reserved event: {}", ex.getMessage(), ex);
                    } else {
                        log.debug("Published inventory.reserved event to partition: {}",
                                result.getRecordMetadata().partition());
                    }
                });
    }

    public void publishInventoryFailed(Long orderId, Long productId, String reason) {
        log.info("Publishing inventory.failed event for order: {}, product: {}", orderId, productId);
        var event = new InventoryFailedEvent(orderId, productId, reason, Instant.now());
        kafkaTemplate.send("inventory.failed", orderId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish inventory.failed event: {}", ex.getMessage(), ex);
                    } else {
                        log.debug("Published inventory.failed event to partition: {}",
                                result.getRecordMetadata().partition());
                    }
                });
    }

    public record InventoryReservedEvent(Long orderId, Long productId, Integer quantity, Boolean reserved, Instant timestamp) {}
    public record InventoryFailedEvent(Long orderId, Long productId, String reason, Instant timestamp) {}
}
