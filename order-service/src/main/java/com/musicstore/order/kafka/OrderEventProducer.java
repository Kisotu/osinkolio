package com.musicstore.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicstore.order.api.v1.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer for order-related events.
 * Emits events when orders are created, updated, or cancelled.
 * These events are consumed by other services (inventory, payment, notification).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public static final String ORDER_CREATED_TOPIC = "order.created";
    public static final String ORDER_CANCELLED_TOPIC = "order.cancelled";
    public static final String ORDER_STATUS_CHANGED_TOPIC = "order.status-changed";

    public void publishOrderCreated(OrderResponse order) {
        log.info("Publishing order.created event for order: {}", order.id());
        kafkaTemplate.send(ORDER_CREATED_TOPIC, String.valueOf(order.id()), order)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order.created event: {}", ex.getMessage());
                    } else {
                        log.debug("Published order.created event to partition: {}", result.getRecordMetadata().partition());
                    }
                });
    }

    public void publishOrderCancelled(Long orderId, String reason) {
        log.info("Publishing order.cancelled event for order: {}", orderId);
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, reason);
        kafkaTemplate.send(ORDER_CANCELLED_TOPIC, String.valueOf(orderId), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order.cancelled event: {}", ex.getMessage());
                    } else {
                        log.debug("Published order.cancelled event to partition: {}", result.getRecordMetadata().partition());
                    }
                });
    }

    public void publishOrderStatusChanged(Long orderId, String oldStatus, String newStatus) {
        log.info("Publishing order.status-changed event for order: {} from {} to {}", orderId, oldStatus, newStatus);
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(orderId, oldStatus, newStatus);
        kafkaTemplate.send(ORDER_STATUS_CHANGED_TOPIC, String.valueOf(orderId), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order.status-changed event: {}", ex.getMessage());
                    } else {
                        log.debug("Published order.status-changed event to partition: {}", result.getRecordMetadata().partition());
                    }
                });
    }

    public record OrderCancelledEvent(Long orderId, String reason, java.time.Instant timestamp) {
        public OrderCancelledEvent(Long orderId, String reason) {
            this(orderId, reason, java.time.Instant.now());
        }
    }

    public record OrderStatusChangedEvent(Long orderId, String oldStatus, String newStatus, java.time.Instant timestamp) {
        public OrderStatusChangedEvent(Long orderId, String oldStatus, String newStatus) {
            this(orderId, oldStatus, newStatus, java.time.Instant.now());
        }
    }
}
