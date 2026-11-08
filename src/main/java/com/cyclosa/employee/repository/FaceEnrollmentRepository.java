package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.FaceEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FaceEnrollmentRepository extends JpaRepository<FaceEnrollment, UUID> {

    Optional<FaceEnrollment> findByEmployeeIdAndRevokedAtIsNull(UUID employeeId);

    Optional<FaceEnrollment> findByEmployeeId(UUID employeeId);

    boolean existsByEmployeeIdAndRevokedAtIsNull(UUID employeeId);

    boolean existsByTemplateRef(String templateRef);
}

