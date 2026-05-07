package com.musicstore.payment.service;

import com.musicstore.payment.api.v1.dto.*;
import com.musicstore.payment.domain.entity.Payment;
import com.musicstore.payment.domain.entity.PaymentMethod;
import com.musicstore.payment.domain.entity.PaymentStatus;
import com.musicstore.payment.domain.repository.PaymentRepository;
import com.musicstore.payment.kafka.PaymentEventProducer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PspSimulator pspSimulator;
    private final PaymentEventProducer eventProducer;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            return paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                    .map(this::mapToPaymentResponse)
                    .orElseGet(() -> processNewPayment(request));
        }
        return processNewPayment(request);
    }

    private PaymentResponse processNewPayment(PaymentRequest request) {
        log.info("Processing new payment for order: {}", request.orderId());

        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .amount(request.amount())
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.paymentMethod())
                .idempotencyKey(request.idempotencyKey())
                .build();

        payment.markProcessing();
        Payment savedPayment = paymentRepository.save(payment);

        var result = pspSimulator.charge(savedPayment);

        if (result.success()) {
            savedPayment.markCompleted(result.transactionId());
            log.info("Payment completed for order {}, transaction: {}", request.orderId(), result.transactionId());
        } else {
            savedPayment.markFailed(result.errorMessage());
            log.warn("Payment failed for order {}, reason: {}", request.orderId(), result.errorMessage());
        }

        Payment finalPayment = paymentRepository.save(savedPayment);

        // Publish payment event based on result
        if (finalPayment.getStatus() == PaymentStatus.COMPLETED) {
            eventProducer.publishPaymentCompleted(finalPayment);
        } else if (finalPayment.getStatus() == PaymentStatus.FAILED) {
            eventProducer.publishPaymentFailed(finalPayment);
        }

        return mapToPaymentResponse(finalPayment);
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));
        return mapToPaymentResponse(payment);
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for order: " + orderId));
        return mapToPaymentResponse(payment);
    }

    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with transactionId: " + transactionId));
        return mapToPaymentResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    public PagedResponse<PaymentResponse> getAllPayments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Payment> paymentPage = paymentRepository.findAll(pageable);
        return mapToPagedResponse(paymentPage);
    }

    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findAllByOrderId(orderId).stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long id, PaymentStatusUpdateRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));

        log.info("Updating payment {} status from {} to {}", id, payment.getStatus(), request.status());
        payment.setStatus(request.status());
        Payment savedPayment = paymentRepository.save(payment);
        return mapToPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse processRefund(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }

        log.info("Processing refund for payment: {}, reason: {}", paymentId, reason);

        var result = pspSimulator.refund(payment);

        if (result.success()) {
            payment.markRefunded();
            log.info("Refund processed for transaction {}", payment.getTransactionId());
        } else {
            payment.markFailed("Refund failed: " + result.errorMessage());
            log.warn("Refund failed for transaction {}, reason: {}", payment.getTransactionId(), result.errorMessage());
        }

        Payment savedPayment = paymentRepository.save(payment);

        if (savedPayment.getStatus() == PaymentStatus.COMPLETED) {
            eventProducer.publishPaymentCompleted(savedPayment);
        } else if (savedPayment.getStatus() == PaymentStatus.FAILED) {
            eventProducer.publishPaymentFailed(savedPayment);
        }

        return mapToPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse retryFailedPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));

        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new IllegalStateException("Only failed payments can be retried");
        }

        log.info("Retrying payment: {}", id);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setFailureReason(null);
        payment.setTransactionId(null);
        payment.markProcessing();
        paymentRepository.save(payment);

        var result = pspSimulator.charge(payment);

        if (result.success()) {
            payment.markCompleted(result.transactionId());
        } else {
            payment.markFailed(result.errorMessage());
        }

        Payment savedPayment = paymentRepository.save(payment);

        if (savedPayment.getStatus() == PaymentStatus.COMPLETED) {
            eventProducer.publishPaymentCompleted(savedPayment);
        } else if (savedPayment.getStatus() == PaymentStatus.FAILED) {
            eventProducer.publishPaymentFailed(savedPayment);
        }

        return mapToPaymentResponse(savedPayment);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getIdempotencyKey(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getVersion()
        );
    }

    private PagedResponse<PaymentResponse> mapToPagedResponse(Page<Payment> paymentPage) {
        List<PaymentResponse> content = paymentPage.getContent()
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.isFirst(),
                paymentPage.isLast()
        );
    }
}
