package com.musicstore.order.kafka;

import com.musicstore.order.domain.entity.Order;
import com.musicstore.order.domain.entity.OrderStatus;
import com.musicstore.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Kafka listener for order-related events.
 * Listens to events from other services that affect order state.
 * Implements the Saga pattern for distributed order processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    @KafkaListener(topics = "payment.completed", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handlePaymentCompleted(Map<String, Object> event) {
        log.info("Received payment.completed event: {}", event);

        try {
            Long orderId = ((Number) event.get("orderId")).longValue();

            Order order = orderRepository.findById(orderId)
                    .orElse(null);

            if (order == null) {
                log.warn("Order not found for payment.completed event: {}", orderId);
                return;
            }

            if (order.getStatus() == OrderStatus.PENDING) {
                log.info("Updating order {} status from PENDING to CONFIRMED", orderId);
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);

                orderEventProducer.publishOrderStatusChanged(orderId, "PENDING", "CONFIRMED");
                log.info("Order {} confirmed after successful payment", orderId);
            } else {
                log.info("Order {} is in status {}, skipping CONFIRMED update", orderId, order.getStatus());
            }
        } catch (Exception e) {
            log.error("Error processing payment.completed event: {}", event, e);
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handlePaymentFailed(Map<String, Object> event) {
        log.info("Received payment.failed event: {}", event);

        try {
            Long orderId = ((Number) event.get("orderId")).longValue();
            String reason = (String) event.get("reason");

            Order order = orderRepository.findById(orderId)
                    .orElse(null);

            if (order == null) {
                log.warn("Order not found for payment.failed event: {}", orderId);
                return;
            }

            if (order.getStatus() == OrderStatus.PENDING) {
                log.info("Cancelling order {} due to payment failure: {}", orderId, reason);
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                // Trigger compensating transaction for inventory release
                orderEventProducer.publishOrderCancelled(orderId, "Payment failed: " + reason);
                log.info("Order {} cancelled due to payment failure, compensating transaction triggered", orderId);
            }
        } catch (Exception e) {
            log.error("Error processing payment.failed event: {}", event, e);
        }
    }

    @KafkaListener(topics = "inventory.reserved", groupId = "${spring.kafka.consumer.group-id}")
    public void handleInventoryReserved(Map<String, Object> event) {
        log.info("Received inventory.reserved event: {}", event);
        // Inventory has been reserved, payment processing is handled by Payment Service
        // listening to the inventory.reserved topic
    }

    @KafkaListener(topics = "inventory.failed", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleInventoryFailed(Map<String, Object> event) {
        log.info("Received inventory.failed event: {}", event);

        try {
            Long orderId = ((Number) event.get("orderId")).longValue();
            String reason = (String) event.get("reason");

            Order order = orderRepository.findById(orderId)
                    .orElse(null);

            if (order == null) {
                log.warn("Order not found for inventory.failed event: {}", orderId);
                return;
            }

            if (order.getStatus() == OrderStatus.PENDING) {
                log.info("Cancelling order {} due to inventory failure: {}", orderId, reason);
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                orderEventProducer.publishOrderCancelled(orderId, "Inventory unavailable: " + reason);
                log.info("Order {} cancelled due to inventory unavailability", orderId);
            }
        } catch (Exception e) {
            log.error("Error processing inventory.failed event: {}", event, e);
        }
    }
}
