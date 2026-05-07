package com.musicstore.payment.kafka;

import com.musicstore.payment.domain.entity.Payment;
import com.musicstore.payment.domain.entity.PaymentMethod;
import com.musicstore.payment.domain.entity.PaymentStatus;
import com.musicstore.payment.domain.repository.PaymentRepository;
import com.musicstore.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final PaymentService paymentService;

    @KafkaListener(topics = "${kafka.topics.order-created:order.created}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleOrderCreated(Map<String, Object> event) {
        log.info("Received order.created event: {}", event);

        try {
            Long orderId = ((Number) event.get("orderId")).longValue();
            log.info("Processing order.created for orderId: {}", orderId);

            // Payment processing is typically initiated via the REST API, 
            // but we listen for order events to track order status
            log.debug("Event processed successfully for orderId: {}", orderId);
        } catch (Exception e) {
            log.error("Error processing order.created event: {}", event, e);
        }
    }

    @KafkaListener(topics = "${kafka.topics.inventory-reserved:inventory.reserved}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleInventoryReserved(Map<String, Object> event) {
        log.info("Received inventory.reserved event: {}", event);

        try {
            Long orderId = ((Number) event.get("orderId")).longValue();
            log.info("Inventory reserved for orderId: {}", orderId);

            // Once inventory is reserved, we can proceed to process payment
            // This would typically trigger the payment processing flow
            // In a real scenario, the payment is initiated from the order service
            // after inventory is confirmed. For now, we log the event.
        } catch (Exception e) {
            log.error("Error processing inventory.reserved event: {}", event, e);
        }
    }
}
