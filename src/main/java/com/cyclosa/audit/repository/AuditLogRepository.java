package com.cyclosa.audit.repository;

import com.cyclosa.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("SELECT a FROM AuditLog a " +
           "WHERE (:userId IS NULL OR a.userId = :userId) " +
           "  AND (:action IS NULL OR LOWER(a.action) = LOWER(:action)) " +
           "  AND (:entityType IS NULL OR LOWER(a.entityType) = LOWER(:entityType)) " +
           "  AND (:entityId IS NULL OR a.entityId = :entityId) " +
           "  AND (:fromDate IS NULL OR a.createdAt >= :fromDate) " +
           "  AND (:toDate IS NULL OR a.createdAt <= :toDate) " +
           "  AND (:keyword IS NULL " +
           "       OR LOWER(a.action) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "       OR LOWER(a.entityType) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "       OR LOWER(a.ipAddress) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AuditLog> search(
            @Param("userId") UUID userId,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("entityId") UUID entityId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType,
            UUID entityId,
            Pageable pageable
    );
}
