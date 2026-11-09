package com.cyclosa.workflow.repository;

import com.cyclosa.workflow.entity.WorkflowRequest;
import com.cyclosa.workflow.enums.RequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowRequestRepository extends JpaRepository<WorkflowRequest, UUID>, JpaSpecificationExecutor<WorkflowRequest> {

    Optional<WorkflowRequest> findFirstByRequestTypeAndReferenceIdOrderBySubmittedAtDesc(RequestType requestType, UUID referenceId);

    @Query("SELECT r FROM WorkflowRequest r WHERE r.requestedByUserId = :userId " +
           "AND (:requestType IS NULL OR r.requestType = :requestType) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<WorkflowRequest> findMySubmitted(
            @Param("userId") UUID userId,
            @Param("requestType") RequestType requestType,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByRequestedByUserIdAndCancelledAtIsNull(UUID userId);
}

