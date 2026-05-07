package com.musicstore.payment.kafka;

import com.musicstore.payment.domain.entity.Payment;
import com.musicstore.payment.domain.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.payment-completed:payment.completed}")
    private String paymentCompletedTopic;

    @Value("${kafka.topics.payment-failed:payment.failed}")
    private String paymentFailedTopic;

    @Value("${kafka.topics.payment-refunded:payment.refunded}")
    private String paymentRefundedTopic;

    public void publishPaymentCompleted(Payment payment) {
        log.info("Publishing payment.completed event for payment: {}", payment.getId());
        var event = buildPaymentEvent(payment);
        kafkaTemplate.send(paymentCompletedTopic, payment.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish payment.completed event: {}", ex.getMessage(), ex);
                    } else {
                        log.debug("Published payment.completed event to topic: {}", paymentCompletedTopic);
                    }
                });
    }

    public void publishPaymentFailed(Payment payment) {
        log.info("Publishing payment.failed event for payment: {}", payment.getId());
        var event = buildPaymentEvent(payment);
        kafkaTemplate.send(paymentFailedTopic, payment.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish payment.failed event: {}", ex.getMessage(), ex);
                    } else {
                        log.debug("Published payment.failed event to topic: {}", paymentFailedTopic);
                    }
                });
    }

    public void publishPaymentRefunded(Payment payment) {
        log.info("Publishing payment.refunded event for payment: {}", payment.getId());
        var event = buildPaymentEvent(payment);
        kafkaTemplate.send(paymentRefundedTopic, payment.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish payment.refunded event: {}", ex.getMessage(), ex);
                    } else {
                        log.debug("Published payment.refunded event to topic: {}", paymentRefundedTopic);
                    }
                });
    }

    private Map<String, Object> buildPaymentEvent(Payment payment) {
        Map<String, Object> event = new HashMap<>();
        event.put("paymentId", payment.getId());
        event.put("orderId", payment.getOrderId());
        event.put("amount", payment.getAmount());
        event.put("status", payment.getStatus().name());
        event.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        event.put("transactionId", payment.getTransactionId());
        event.put("failureReason", payment.getFailureReason());
        event.put("timestamp", Instant.now().toString());
        return event;
    }
}
