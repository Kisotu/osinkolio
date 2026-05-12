package com.musicstore.inventory.kafka;

import com.musicstore.inventory.domain.entity.Inventory;
import com.musicstore.inventory.domain.entity.InventoryTransaction;
import com.musicstore.inventory.domain.entity.StockReservation;
import com.musicstore.inventory.domain.repository.InventoryRepository;
import com.musicstore.inventory.domain.repository.InventoryTransactionRepository;
import com.musicstore.inventory.domain.repository.StockReservationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository stockReservationRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
        topics = "${kafka.topics.order-created:order.created}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleOrderCreated(Map<String, Object> event) {
        log.info("Received order.created event: {}", event);

        try {
            Long orderId = ((Number) event.get("orderId")).longValue();
            List<Map<String, Object>> items = (List<
                Map<String, Object>
            >) event.get("items");

            for (Map<String, Object> item : items) {
                Long productId = ((Number) item.get("productId")).longValue();
                Integer quantity = ((Number) item.get("quantity")).intValue();

                Inventory inventory = inventoryRepository
                    .findByProductId(productId)
                    .orElse(null);

                if (inventory == null || !inventory.canReserve(quantity)) {
                    log.warn(
                        "Inventory not available for product {}, quantity {}",
                        productId,
                        quantity
                    );
                    publishInventoryFailed(
                        orderId,
                        productId,
                        "Insufficient stock"
                    );
                    continue;
                }

                int before = inventory.getQuantityReserved();
                inventory.reserve(quantity);
                inventoryRepository.save(inventory);

                StockReservation reservation = new StockReservation();
                reservation.setProductId(productId);
                reservation.setOrderId(orderId);
                reservation.setQuantity(quantity);
                stockReservationRepository.save(reservation);

                InventoryTransaction tx = new InventoryTransaction();
                tx.setProductId(productId);
                tx.setOrderId(orderId);
                tx.setTransactionType("RESERVATION");
                tx.setQuantityChange(-quantity);
                tx.setQuantityBefore(before);
                tx.setQuantityAfter(inventory.getQuantityReserved());
                tx.setReason("Order reservation for order: " + orderId);
                transactionRepository.save(tx);

                publishInventoryReserved(orderId, productId, quantity, true);
            }

            log.info("Inventory reservation processed for order: {}", orderId);
        } catch (Exception e) {
            log.error("Error processing order.created event: {}", event, e);
        }
    }

    @KafkaListener(
        topics = "${kafka.topics.order-cancelled:order.cancelled}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleOrderCancelled(Map<String, Object> event) {
        log.info("Received order.cancelled event: {}", event);

        try {
            Long orderId = ((Number) event.get("orderId")).longValue();
            String reason = (String) event.get("reason");

            List<StockReservation> reservations =
                stockReservationRepository.findByOrderId(orderId);

            for (StockReservation reservation : reservations) {
                if (
                    reservation.getStatus() ==
                    com.musicstore.inventory.domain.entity.ReservationStatus.PENDING
                ) {
                    Inventory inventory = inventoryRepository
                        .findByProductId(reservation.getProductId())
                        .orElse(null);

                    if (inventory != null) {
                        int before = inventory.getQuantityReserved();
                        inventory.release(reservation.getQuantity());
                        inventoryRepository.save(inventory);

                        InventoryTransaction tx = new InventoryTransaction();
                        tx.setProductId(reservation.getProductId());
                        tx.setOrderId(orderId);
                        tx.setTransactionType("RELEASE");
                        tx.setQuantityChange(reservation.getQuantity());
                        tx.setQuantityBefore(before);
                        tx.setQuantityAfter(inventory.getQuantityReserved());
                        tx.setReason("Order cancelled: " + reason);
                        transactionRepository.save(tx);
                    }

                    reservation.setStatus(
                        com.musicstore.inventory.domain.entity.ReservationStatus.CANCELLED
                    );
                    stockReservationRepository.save(reservation);
                }
            }

            log.info("Stock released for cancelled order: {}", orderId);
        } catch (Exception e) {
            log.error("Error processing order.cancelled event: {}", event, e);
        }
    }

    @KafkaListener(
        topics = "${kafka.topics.payment-failed:payment.failed}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handlePaymentFailed(Map<String, Object> event) {
        log.info(
            "Received payment.failed event (compensating transaction): {}",
            event
        );

        try {
            Long orderId = ((Number) event.get("orderId")).longValue();

            List<StockReservation> reservations =
                stockReservationRepository.findByOrderId(orderId);

            for (StockReservation reservation : reservations) {
                if (
                    reservation.getStatus() ==
                    com.musicstore.inventory.domain.entity.ReservationStatus.PENDING
                ) {
                    Inventory inventory = inventoryRepository
                        .findByProductId(reservation.getProductId())
                        .orElse(null);

                    if (inventory != null) {
                        int before = inventory.getQuantityReserved();
                        inventory.release(reservation.getQuantity());
                        inventoryRepository.save(inventory);

                        InventoryTransaction tx = new InventoryTransaction();
                        tx.setProductId(reservation.getProductId());
                        tx.setOrderId(orderId);
                        tx.setTransactionType("RELEASE");
                        tx.setQuantityChange(reservation.getQuantity());
                        tx.setQuantityBefore(before);
                        tx.setQuantityAfter(inventory.getQuantityReserved());
                        tx.setReason(
                            "Payment failed - compensating transaction for order: " +
                                orderId
                        );
                        transactionRepository.save(tx);
                    }

                    reservation.setStatus(
                        com.musicstore.inventory.domain.entity.ReservationStatus.CANCELLED
                    );
                    stockReservationRepository.save(reservation);
                }
            }

            log.info(
                "Compensating transaction executed - stock released for failed payment on order: {}",
                orderId
            );
        } catch (Exception e) {
            log.error("Error handling payment.failed event: {}", event, e);
        }
    }

    @KafkaListener(
        topics = "${kafka.topics.payment-completed:payment.completed}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handlePaymentCompleted(Map<String, Object> event) {
        log.info(
            "Received payment.completed event (confirming reservation): {}",
            event
        );

        try {
            Long orderId = ((Number) event.get("orderId")).longValue();

            List<StockReservation> reservations =
                stockReservationRepository.findByOrderId(orderId);

            for (StockReservation reservation : reservations) {
                if (
                    reservation.getStatus() ==
                    com.musicstore.inventory.domain.entity.ReservationStatus.PENDING
                ) {
                    reservation.setStatus(
                        com.musicstore.inventory.domain.entity.ReservationStatus.CONFIRMED
                    );
                    stockReservationRepository.save(reservation);

                    log.info(
                        "Stock reservation confirmed for order: {}, product: {}",
                        orderId,
                        reservation.getProductId()
                    );
                }
            }
        } catch (Exception e) {
            log.error("Error handling payment.completed event: {}", event, e);
        }
    }

    private void publishInventoryReserved(
        Long orderId,
        Long productId,
        Integer quantity,
        Boolean reserved
    ) {
        InventoryReservedEvent event = new InventoryReservedEvent(
            orderId,
            productId,
            quantity,
            reserved,
            Instant.now()
        );
        kafkaTemplate
            .send("inventory.reserved", orderId.toString(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error(
                        "Failed to publish inventory.reserved event: {}",
                        ex.getMessage(),
                        ex
                    );
                }
            });
    }

    private void publishInventoryFailed(
        Long orderId,
        Long productId,
        String reason
    ) {
        InventoryFailedEvent event = new InventoryFailedEvent(
            orderId,
            productId,
            reason,
            Instant.now()
        );
        kafkaTemplate
            .send("inventory.failed", orderId.toString(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error(
                        "Failed to publish inventory.failed event: {}",
                        ex.getMessage(),
                        ex
                    );
                }
            });
    }

    public record InventoryReservedEvent(
        Long orderId,
        Long productId,
        Integer quantity,
        Boolean reserved,
        Instant timestamp
    ) {}

    public record InventoryFailedEvent(
        Long orderId,
        Long productId,
        String reason,
        Instant timestamp
    ) {}
}
