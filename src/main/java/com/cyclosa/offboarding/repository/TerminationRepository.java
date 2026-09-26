package com.cyclosa.offboarding.repository;

import com.cyclosa.offboarding.entity.Termination;
import com.cyclosa.offboarding.enums.TerminationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TerminationRepository extends JpaRepository<Termination, UUID> {

    @Query("SELECT t FROM Termination t WHERE " +
           "(:companyId IS NULL OR t.companyId = :companyId) AND " +
           "(:employeeId IS NULL OR t.employeeId = :employeeId) AND " +
           "(:status IS NULL OR t.status = :status)")
    Page<Termination> searchTerminations(
            @Param("companyId") UUID companyId,
            @Param("employeeId") UUID employeeId,
            @Param("status") TerminationStatus status,
            Pageable pageable
    );

    java.util.List<Termination> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    java.util.Optional<Termination> findFirstByEmployeeIdAndStatus(UUID employeeId, TerminationStatus status);

    boolean existsByEmployeeIdAndStatusIn(UUID employeeId, java.util.Collection<TerminationStatus> statuses);
}

