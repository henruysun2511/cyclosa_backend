package com.cyclosa.offboarding.repository;

import com.cyclosa.offboarding.entity.ExitInterview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExitInterviewRepository extends JpaRepository<ExitInterview, UUID> {

    Optional<ExitInterview> findByEmployeeId(UUID employeeId);

    @Query("SELECT e FROM ExitInterview e WHERE " +
           "(:companyId IS NULL OR e.companyId = :companyId) AND " +
           "(:employeeId IS NULL OR e.employeeId = :employeeId)")
    Page<ExitInterview> searchExitInterviews(
            @Param("companyId") UUID companyId,
            @Param("employeeId") UUID employeeId,
            Pageable pageable
    );
}
