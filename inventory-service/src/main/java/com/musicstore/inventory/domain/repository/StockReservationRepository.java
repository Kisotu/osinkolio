package com.musicstore.inventory.domain.repository;

import com.musicstore.inventory.domain.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    List<StockReservation> findByOrderId(Long orderId);
    List<StockReservation> findByProductId(Long productId);
    Optional<StockReservation> findByOrderIdAndProductId(Long orderId, Long productId);
}
