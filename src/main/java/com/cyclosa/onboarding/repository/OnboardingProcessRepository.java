package com.cyclosa.onboarding.repository;

import com.cyclosa.onboarding.entity.OnboardingProcess;
import com.cyclosa.onboarding.enums.OnboardingStatus;
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
public interface OnboardingProcessRepository extends JpaRepository<OnboardingProcess, UUID>, JpaSpecificationExecutor<OnboardingProcess> {

    Optional<OnboardingProcess> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<OnboardingProcess> findByEmployeeIdAndStatus(UUID employeeId, OnboardingStatus status);

    List<OnboardingProcess> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    boolean existsByEmployeeIdAndStatus(UUID employeeId, OnboardingStatus status);

    @Query("SELECT COUNT(p) FROM OnboardingProcess p WHERE p.companyId = :companyId AND p.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") OnboardingStatus status);
}
