package com.musicstore.payment.api.v1.controller;

import com.musicstore.payment.api.v1.dto.*;
import com.musicstore.payment.domain.entity.PaymentStatus;
import com.musicstore.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Endpoints for payment processing and management")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create a new payment", description = "Processes a new payment for an order")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment created successfully"),
            @ApiResponse(responseCode = "402", description = "Payment failed"),
            @ApiResponse(responseCode = "400", description = "Invalid payment data")
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Payment request for order: {}", request.orderId());
        PaymentResponse payment = paymentService.createPayment(request);
        HttpStatus status = payment.status() == PaymentStatus.FAILED ? HttpStatus.PAYMENT_REQUIRED : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(payment);
    }

    @Operation(summary = "Get payment by ID", description = "Retrieves a payment by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @Operation(summary = "Get payment by order ID", description = "Retrieves the payment associated with an order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @Operation(summary = "Get payment by transaction ID", description = "Retrieves a payment by its transaction ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentResponse> getPaymentByTransactionId(
            @Parameter(description = "Transaction ID") @PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getPaymentByTransactionId(transactionId));
    }

    @Operation(summary = "Get all payments", description = "Retrieves a paginated list of all payments")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of payments returned")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<PaymentResponse>> getAllPayments(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(paymentService.getAllPayments(page, size));
    }

    @Operation(summary = "Get payments by status", description = "Retrieves payments filtered by status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments filtered by status")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @Parameter(description = "Payment status (e.g., PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED)")
            @PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @Operation(summary = "Get payments by order ID", description = "Retrieves all payment attempts for an order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments found")
    })
    @GetMapping("/order/{orderId}/all")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
    }

    @Operation(summary = "Update payment status", description = "Manually updates the status of a payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId,
            @Valid @RequestBody PaymentStatusUpdateRequest request) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, request));
    }

    @Operation(summary = "Process a refund", description = "Processes a refund for a completed payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund processed"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "409", description = "Refund cannot be processed")
    })
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> processRefund(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId,
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.processRefund(paymentId, request.reason()));
    }

    @Operation(summary = "Retry a failed payment", description = "Retries processing for a previously failed payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment retried"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "409", description = "Payment cannot be retried")
    })
    @PostMapping("/{paymentId}/retry")
    public ResponseEntity<PaymentResponse> retryPayment(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.retryFailedPayment(paymentId));
    }
}
