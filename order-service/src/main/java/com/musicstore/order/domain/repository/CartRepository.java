package com.musicstore.order.domain.repository;

import com.musicstore.order.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findBySessionId(String sessionId);

    List<Cart> findByExpiresAtBefore(OffsetDateTime expiresAt);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.expiresAt < :now")
    int deleteExpiredCarts(@Param("now") OffsetDateTime now);

    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.expiresAt > :now")
    Optional<Cart> findActiveCartByUserId(@Param("userId") Long userId, @Param("now") OffsetDateTime now);

    @Query("SELECT c FROM Cart c WHERE c.sessionId = :sessionId AND c.expiresAt > :now")
    Optional<Cart> findActiveCartBySessionId(@Param("sessionId") String sessionId, @Param("now") OffsetDateTime now);
}
