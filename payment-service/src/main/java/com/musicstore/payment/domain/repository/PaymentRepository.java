package com.musicstore.payment.domain.repository;

import com.musicstore.payment.domain.entity.Payment;
import com.musicstore.payment.domain.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByStatus(PaymentStatus status);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId ORDER BY p.createdAt DESC")
    List<Payment> findAllByOrderId(@Param("orderId") Long orderId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt < :beforeDate")
    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, java.time.OffsetDateTime beforeDate);
}
