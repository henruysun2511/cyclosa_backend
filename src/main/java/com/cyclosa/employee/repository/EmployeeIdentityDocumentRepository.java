package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.EmployeeIdentityDocument;
import com.cyclosa.employee.entity.enums.IdentityDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeIdentityDocumentRepository extends JpaRepository<EmployeeIdentityDocument, UUID> {

    List<EmployeeIdentityDocument> findAllByEmployeeId(UUID employeeId);

    boolean existsByDocumentTypeAndDocumentNumber(IdentityDocumentType documentType, String documentNumber);

    boolean existsByDocumentTypeAndDocumentNumberAndIdNot(IdentityDocumentType documentType, String documentNumber, UUID id);
}

