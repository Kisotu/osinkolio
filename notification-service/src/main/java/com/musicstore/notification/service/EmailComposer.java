package com.musicstore.notification.service;

import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailComposer {

    private final SpringTemplateEngine templateEngine;

    public EmailContent composeOrderConfirmation(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        Long userId = toLong(event.get("userId"));
        BigDecimal totalAmount = new BigDecimal(event.get("totalAmount").toString());
        String status = (String) event.get("status");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) event.getOrDefault("items", List.of());

        Context ctx = new Context();
        ctx.setVariable("orderId", orderId);
        ctx.setVariable("userId", userId);
        ctx.setVariable("totalAmount", totalAmount);
        ctx.setVariable("status", status);
        ctx.setVariable("items", items);
        ctx.setVariable("itemCount", items.size());

        String html = templateEngine.process("email/order-confirmation", ctx);

        return new EmailContent(
                "Order Confirmed - #" + orderId,
                html
        );
    }

    public EmailContent composePaymentConfirmation(Map<String, Object> event) {
        Long paymentId = toLong(event.get("paymentId"));
        Long orderId = toLong(event.get("orderId"));
        BigDecimal amount = new BigDecimal(event.get("amount").toString());
        String status = (String) event.get("status");
        String paymentMethod = (String) event.get("paymentMethod");
        String transactionId = (String) event.get("transactionId");

        Context ctx = new Context();
        ctx.setVariable("paymentId", paymentId);
        ctx.setVariable("orderId", orderId);
        ctx.setVariable("amount", amount);
        ctx.setVariable("status", status);
        ctx.setVariable("paymentMethod", paymentMethod);
        ctx.setVariable("transactionId", transactionId);

        String html = templateEngine.process("email/payment-confirmation", ctx);

        return new EmailContent(
                "Payment Confirmed - Order #" + orderId,
                html
        );
    }

    public EmailContent composeOrderCancellation(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        String reason = (String) event.getOrDefault("reason", "No reason provided");
        String timestamp = (String) event.getOrDefault("timestamp", "");

        Context ctx = new Context();
        ctx.setVariable("orderId", orderId);
        ctx.setVariable("reason", reason);
        ctx.setVariable("timestamp", timestamp);

        String html = templateEngine.process("email/order-cancelled", ctx);

        return new EmailContent(
                "Order Cancelled - #" + orderId,
                html
        );
    }

    public EmailContent composeShippingNotification(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        String trackingNumber = (String) event.getOrDefault("trackingNumber", "N/A");
        String carrier = (String) event.getOrDefault("carrier", "Standard Shipping");

        Context ctx = new Context();
        ctx.setVariable("orderId", orderId);
        ctx.setVariable("trackingNumber", trackingNumber);
        ctx.setVariable("carrier", carrier);

        String html = templateEngine.process("email/shipping-notification", ctx);

        return new EmailContent(
                "Order Shipped - #" + orderId,
                html
        );
    }

    private Long toLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s) return Long.parseLong(s);
        return null;
    }

    public record EmailContent(String subject, String htmlBody) {}
}
