package com.cyclosa.offboarding.repository;

import com.cyclosa.offboarding.entity.OffboardingClearance;
import com.cyclosa.offboarding.enums.ClearanceStatus;
import com.cyclosa.offboarding.enums.ClearanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OffboardingClearanceRepository extends JpaRepository<OffboardingClearance, UUID> {

    List<OffboardingClearance> findByEmployeeId(UUID employeeId);

    Optional<OffboardingClearance> findByEmployeeIdAndClearanceType(UUID employeeId, ClearanceType clearanceType);

    long countByEmployeeIdAndStatus(UUID employeeId, ClearanceStatus status);
}
