package com.cyclosa.notification.repository;

import com.cyclosa.notification.entity.Notification;
import com.cyclosa.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n " +
           "WHERE n.userId = :userId " +
           "  AND (:isRead IS NULL OR n.isRead = :isRead) " +
           "  AND (:type IS NULL OR n.type = :type) " +
           "  AND (:keyword IS NULL " +
           "       OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "       OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Notification> search(
            @Param("userId") UUID userId,
            @Param("isRead") Boolean isRead,
            @Param("type") NotificationType type,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByUserIdAndIsReadFalse(UUID userId);

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);
}
