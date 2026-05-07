package com.musicstore.payment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    @KafkaListener(topics = "${kafka.topics.order-created:order.created}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(Map<String, Object> event) {
        log.info("Received order.created event: {}", event);

        try {
            Long orderId = ((Number) event.get("orderId")).longValue();
            log.info("Processing order.created for orderId: {}", orderId);

            // Future: auto-initiate payment for COD orders or pre-authorized payments
            log.debug("Event processed successfully for orderId: {}", orderId);
        } catch (Exception e) {
            log.error("Error processing order.created event: {}", event, e);
        }
    }

    @KafkaListener(topics = "${kafka.topics.inventory-reserved:inventory.reserved}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleInventoryReserved(Map<String, Object> event) {
        log.info("Received inventory.reserved event: {}", event);

        try {
            Long orderId = ((Number) event.get("orderId")).longValue();
            log.info("Inventory reserved for orderId: {}", orderId);

            // Future: trigger payment capture when inventory is reserved
        } catch (Exception e) {
            log.error("Error processing inventory.reserved event: {}", event, e);
        }
    }
}
