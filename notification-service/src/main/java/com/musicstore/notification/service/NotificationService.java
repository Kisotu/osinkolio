package com.musicstore.notification.service;

import com.musicstore.notification.domain.entity.Notification;
import com.musicstore.notification.domain.entity.NotificationStatus;
import com.musicstore.notification.domain.entity.NotificationType;
import com.musicstore.notification.domain.repository.NotificationRepository;
import com.musicstore.notification.service.EmailComposer.EmailContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final EmailComposer emailComposer;

    @Transactional
    public void sendOrderConfirmation(String recipientEmail, Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        log.info("Sending order confirmation email for order: {} to: {}", orderId, recipientEmail);

        EmailContent content = emailComposer.composeOrderConfirmation(event);
        sendNotification(recipientEmail, NotificationType.ORDER_CONFIRMATION, content, orderId);
    }

    @Transactional
    public void sendPaymentConfirmation(String recipientEmail, Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        log.info("Sending payment confirmation email for order: {} to: {}", orderId, recipientEmail);

        EmailContent content = emailComposer.composePaymentConfirmation(event);
        sendNotification(recipientEmail, NotificationType.PAYMENT_CONFIRMATION, content, orderId);
    }

    @Transactional
    public void sendOrderCancellation(String recipientEmail, Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        log.info("Sending order cancellation email for order: {} to: {}", orderId, recipientEmail);

        EmailContent content = emailComposer.composeOrderCancellation(event);
        sendNotification(recipientEmail, NotificationType.ORDER_CANCELLATION, content, orderId);
    }

    @Transactional
    public void sendShippingNotification(String recipientEmail, Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        log.info("Sending shipping notification for order: {} to: {}", orderId, recipientEmail);

        EmailContent content = emailComposer.composeShippingNotification(event);
        sendNotification(recipientEmail, NotificationType.SHIPPING_UPDATE, content, orderId);
    }

    private void sendNotification(String recipientEmail, NotificationType type, EmailContent content, Long orderId) {
        Notification notification = Notification.builder()
                .type(type)
                .recipientEmail(recipientEmail)
                .subject(content.subject())
                .body(content.htmlBody())
                .orderId(orderId)
                .build();

        boolean sent = emailService.sendHtmlEmail(recipientEmail, content.subject(), content.htmlBody());

        if (sent) {
            notification.markSent();
            log.debug("Notification sent: type={}, order={}, recipient={}", type, orderId, recipientEmail);
        } else {
            notification.markFailed("SMTP delivery failed");
            log.warn("Notification failed: type={}, order={}, recipient={}", type, orderId, recipientEmail);
        }

        notificationRepository.save(notification);
    }

    // --- Query methods ---

    public Page<Notification> getNotificationsByOrderId(Long orderId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByOrderId(orderId, pageable);
    }

    public Page<Notification> getNotificationsByType(NotificationType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByType(type, pageable);
    }

    public Page<Notification> getAllNotifications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findAll(pageable);
    }

    public List<Notification> getNotificationsByEmail(String email) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    private Long toLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s) return Long.parseLong(s);
        return null;
    }
}
