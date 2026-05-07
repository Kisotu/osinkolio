package com.musicstore.payment.service;

import com.musicstore.payment.domain.entity.Payment;
import com.musicstore.payment.domain.entity.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.UUID;

@Slf4j
@Component
public class PspSimulator {

    private final SecureRandom random = new SecureRandom();

    public PspResult charge(Payment payment) {
        log.info("PSP: Processing charge of {} for order {}", payment.getAmount(), payment.getOrderId());

        // Simulate PSP processing delay
        try {
            Thread.sleep(200 + random.nextInt(300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PspResult.failed("PSP processing interrupted");
        }

        // Simulate rejection cases
        if (shouldReject(payment)) {
            String reason = generateRejectionReason(payment);
            log.warn("PSP: Payment rejected - {}", reason);
            return PspResult.failed(reason);
        }

        // Simulate success
        String transactionId = generateTransactionId();
        log.info("PSP: Payment approved, transactionId={}", transactionId);
        return PspResult.success(transactionId);
    }

    public PspResult refund(Payment payment) {
        log.info("PSP: Processing refund for transaction {}", payment.getTransactionId());

        try {
            Thread.sleep(100 + random.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PspResult.failed("Refund processing interrupted");
        }

        if (random.nextDouble() < 0.02) {
            return PspResult.failed("PSP refund rejected");
        }

        String refundTransactionId = "REF-" + payment.getTransactionId();
        log.info("PSP: Refund processed, refundTransactionId={}", refundTransactionId);
        return PspResult.success(refundTransactionId);
    }

    private boolean shouldReject(Payment payment) {
        BigDecimal amount = payment.getAmount();
        PaymentMethod method = payment.getPaymentMethod();

        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            return random.nextDouble() < 0.3;
        }

        if (method == PaymentMethod.CASH_ON_DELIVERY) {
            return false; // COD always succeeds
        }

        if (method == PaymentMethod.CREDIT_CARD || method == PaymentMethod.DEBIT_CARD) {
            return random.nextDouble() < 0.05;
        }

        return random.nextDouble() < 0.02;
    }

    private String generateRejectionReason(Payment payment) {
        String[] reasons = {
                "Insufficient funds",
                "Card declined by issuer",
                "Expired card",
                "Invalid CVV",
                "Transaction blocked by fraud detection",
                "3D Secure authentication failed",
                "Currency not supported"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    public record PspResult(boolean success, String transactionId, String errorMessage) {
        public static PspResult success(String transactionId) {
            return new PspResult(true, transactionId, null);
        }

        public static PspResult failed(String errorMessage) {
            return new PspResult(false, null, errorMessage);
        }
    }
}
