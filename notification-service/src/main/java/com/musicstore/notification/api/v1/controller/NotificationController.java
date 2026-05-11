package com.musicstore.notification.api.v1.controller;

import com.musicstore.notification.domain.entity.Notification;
import com.musicstore.notification.domain.entity.NotificationType;
import com.musicstore.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "Endpoints for viewing notification history")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications", description = "Retrieves a paginated list of all sent notifications")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of notifications")
    })
    @GetMapping
    public ResponseEntity<Page<Notification>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getAllNotifications(page, size));
    }

    @Operation(summary = "Get notifications by order", description = "Retrieves notifications for a specific order")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Page<Notification>> getNotificationsByOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getNotificationsByOrderId(orderId, page, size));
    }

    @Operation(summary = "Get notifications by type", description = "Retrieves notifications filtered by type")
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<Notification>> getNotificationsByType(
            @Parameter(description = "Notification type") @PathVariable NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getNotificationsByType(type, page, size));
    }

    @Operation(summary = "Get notifications by email", description = "Retrieves notifications sent to a specific email")
    @GetMapping("/email/{email}")
    public ResponseEntity<List<Notification>> getNotificationsByEmail(
            @Parameter(description = "Recipient email") @PathVariable String email) {
        return ResponseEntity.ok(notificationService.getNotificationsByEmail(email));
    }
}
