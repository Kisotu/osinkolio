package com.musicstore.notification.domain.repository;

import com.musicstore.notification.domain.entity.Notification;
import com.musicstore.notification.domain.entity.NotificationStatus;
import com.musicstore.notification.domain.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByOrderId(Long orderId, Pageable pageable);

    Page<Notification> findByType(NotificationType type, Pageable pageable);

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);

    Page<Notification> findByRecipientEmail(String email, Pageable pageable);
}
