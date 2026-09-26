package com.cyclosa.offboarding.repository;

import com.cyclosa.offboarding.entity.Resignation;
import com.cyclosa.offboarding.enums.ResignationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResignationRepository extends JpaRepository<Resignation, UUID> {

    @Query("SELECT r FROM Resignation r WHERE " +
           "(:companyId IS NULL OR r.companyId = :companyId) AND " +
           "(:employeeId IS NULL OR r.employeeId = :employeeId) AND " +
           "(:status IS NULL OR r.status = :status)")
    Page<Resignation> searchResignations(
            @Param("companyId") UUID companyId,
            @Param("employeeId") UUID employeeId,
            @Param("status") ResignationStatus status,
            Pageable pageable
    );

    Optional<Resignation> findFirstByEmployeeIdAndStatus(UUID employeeId, ResignationStatus status);

    List<Resignation> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    boolean existsByEmployeeIdAndStatusIn(UUID employeeId, java.util.Collection<ResignationStatus> statuses);
}
