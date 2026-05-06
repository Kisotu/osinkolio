package com.musicstore.order.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for order-related events.
 * Listens to events from other services that affect order state.
 */
@Slf4j
@Component
public class OrderEventListener {

    @KafkaListener(topics = "payment.completed", groupId = "order-service")
    public void handlePaymentCompleted(String event) {
        log.info("Received payment.completed event: {}", event);
        // Update order status to CONFIRMED
        // This will be fully implemented in Sprint 6 (Kafka & Saga Pattern)
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-service")
    public void handlePaymentFailed(String event) {
        log.info("Received payment.failed event: {}", event);
        // Update order status to CANCELLED or trigger compensating transaction
        // This will be fully implemented in Sprint 6 (Kafka & Saga Pattern)
    }

    @KafkaListener(topics = "inventory.reserved", groupId = "order-service")
    public void handleInventoryReserved(String event) {
        log.info("Received inventory.reserved event: {}", event);
        // Proceed to payment processing
        // This will be fully implemented in Sprint 6 (Kafka & Saga Pattern)
    }

    @KafkaListener(topics = "inventory.failed", groupId = "order-service")
    public void handleInventoryFailed(String event) {
        log.info("Received inventory.failed event: {}", event);
        // Update order status to CANCELLED - stock unavailable
        // This will be fully implemented in Sprint 6 (Kafka & Saga Pattern)
    }
}
