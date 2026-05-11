package com.musicstore.notification.kafka;

import com.musicstore.notification.service.NotificationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * Handles order.created events.
     * Sends an order confirmation email to the customer.
     * Event shape: { orderId, userId, status, items, totalAmount, timestamp }
     */
    @KafkaListener(
        topics = "${kafka.topics.order-created:order.created}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(Map<String, Object> event) {
        log.info(
            "Received order.created event: orderId={}",
            event.get("orderId")
        );
        try {
            String recipientEmail = resolveRecipientEmail(event);
            notificationService.sendOrderConfirmation(recipientEmail, event);
        } catch (Exception e) {
            log.error("Failed to process order.created event: {}", event, e);
        }
    }

    /**
     * Handles order.cancelled events.
     * Sends an order cancellation email to the customer.
     * Event shape: { orderId, reason, timestamp }
     */
    @KafkaListener(
        topics = "${kafka.topics.order-cancelled:order.cancelled}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCancelled(Map<String, Object> event) {
        log.info(
            "Received order.cancelled event: orderId={}",
            event.get("orderId")
        );
        try {
            String recipientEmail = resolveRecipientEmail(event);
            notificationService.sendOrderCancellation(recipientEmail, event);
        } catch (Exception e) {
            log.error("Failed to process order.cancelled event: {}", event, e);
        }
    }

    /**
     * Handles payment.completed events.
     * Sends a payment confirmation email to the customer.
     * Event shape: { paymentId, orderId, amount, status, paymentMethod, transactionId, timestamp }
     */
    @KafkaListener(
        topics = "${kafka.topics.payment-completed:payment.completed}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(Map<String, Object> event) {
        log.info(
            "Received payment.completed event: orderId={}",
            event.get("orderId")
        );
        try {
            String recipientEmail = resolveRecipientEmail(event);
            notificationService.sendPaymentConfirmation(recipientEmail, event);
        } catch (Exception e) {
            log.error(
                "Failed to process payment.completed event: {}",
                event,
                e
            );
        }
    }

    /**
     * Handles order.shipped events.
     * Sends a shipping notification email to the customer.
     * Event shape: { orderId, trackingNumber, carrier, timestamp }
     */
    @KafkaListener(
        topics = "${kafka.topics.order-shipped:order.shipped}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderShipped(Map<String, Object> event) {
        log.info(
            "Received order.shipped event: orderId={}",
            event.get("orderId")
        );
        try {
            String recipientEmail = resolveRecipientEmail(event);
            notificationService.sendShippingNotification(recipientEmail, event);
        } catch (Exception e) {
            log.error("Failed to process order.shipped event: {}", event, e);
        }
    }

    /**
     * Resolves the recipient email from the event.
     * Events may carry the email directly or a userId that needs resolving.
     * Falls back to a placeholder if neither is available.
     */
    private String resolveRecipientEmail(Map<String, Object> event) {
        // Events can carry recipientEmail directly
        if (event.containsKey("recipientEmail")) {
            return (String) event.get("recipientEmail");
        }
        // Fallback: use a placeholder that works with Mailpit
        return "customer@musicstore.com";
    }
}
