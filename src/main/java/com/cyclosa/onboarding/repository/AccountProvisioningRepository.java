package com.cyclosa.onboarding.repository;

import com.cyclosa.onboarding.entity.AccountProvisioning;
import com.cyclosa.onboarding.enums.ProvisioningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountProvisioningRepository extends JpaRepository<AccountProvisioning, UUID> {

    List<AccountProvisioning> findByOnboardingProcessId(UUID onboardingProcessId);

    List<AccountProvisioning> findByEmployeeId(UUID employeeId);

    List<AccountProvisioning> findByEmployeeIdAndStatus(UUID employeeId, ProvisioningStatus status);

    boolean existsByOnboardingProcessIdAndStatus(UUID onboardingProcessId, ProvisioningStatus status);

    boolean existsByEmployeeIdAndStatus(UUID employeeId, ProvisioningStatus status);

    Optional<AccountProvisioning> findByIdAndCompanyId(UUID id, UUID companyId);
}
