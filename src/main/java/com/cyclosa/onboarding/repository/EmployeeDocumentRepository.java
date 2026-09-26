package com.cyclosa.onboarding.repository;

import com.cyclosa.onboarding.entity.EmployeeDocument;
import com.cyclosa.onboarding.enums.EmployeeDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, UUID> {

    List<EmployeeDocument> findByEmployeeIdOrderByUploadedAtDesc(UUID employeeId);

    List<EmployeeDocument> findByEmployeeIdAndDocumentType(UUID employeeId, EmployeeDocumentType documentType);

    Optional<EmployeeDocument> findByIdAndCompanyId(UUID id, UUID companyId);

    long countByEmployeeIdAndIsVerifiedTrue(UUID employeeId);
}
