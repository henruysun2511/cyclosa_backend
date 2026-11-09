package com.cyclosa.workflow.repository;

import com.cyclosa.workflow.entity.WorkflowDefinition;
import com.cyclosa.workflow.enums.RequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID>, JpaSpecificationExecutor<WorkflowDefinition> {

    Optional<WorkflowDefinition> findByCompanyIdAndRequestTypeAndIsActiveTrue(UUID companyId, RequestType requestType);

    List<WorkflowDefinition> findByCompanyIdAndRequestType(UUID companyId, RequestType requestType);

    @Query("SELECT d FROM WorkflowDefinition d WHERE (:companyId IS NULL OR d.companyId = :companyId) " +
           "AND (:requestType IS NULL OR d.requestType = :requestType) " +
           "AND (:isActive IS NULL OR d.isActive = :isActive) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<WorkflowDefinition> search(
            @Param("companyId") UUID companyId,
            @Param("requestType") RequestType requestType,
            @Param("isActive") Boolean isActive,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

