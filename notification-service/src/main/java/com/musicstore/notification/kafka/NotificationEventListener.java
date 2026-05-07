package com.musicstore.notification.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    @KafkaListener(topics = "${kafka.topics.order-created:order.created}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(Map<String, Object> event) {
        log.info("Received order.created event: {}", event);
        // Future: send order confirmation email
    }

    @KafkaListener(topics = "${kafka.topics.order-cancelled:order.cancelled}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCancelled(Map<String, Object> event) {
        log.info("Received order.cancelled event: {}", event);
        // Future: send order cancellation email
    }

    @KafkaListener(topics = "${kafka.topics.payment-completed:payment.completed}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentCompleted(Map<String, Object> event) {
        log.info("Received payment.completed event: {}", event);
        // Future: send payment confirmation email
    }

    @KafkaListener(topics = "${kafka.topics.order-shipped:order.shipped}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderShipped(Map<String, Object> event) {
        log.info("Received order.shipped event: {}", event);
        // Future: send shipping notification email
    }
}
