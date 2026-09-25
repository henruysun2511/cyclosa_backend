package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.InternalAssignment;
import com.cyclosa.recruitment.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InternalAssignmentRepository extends JpaRepository<InternalAssignment, UUID> {

    List<InternalAssignment> findByCompanyIdAndEmployeeId(UUID companyId, UUID employeeId);

    List<InternalAssignment> findByOpportunityId(UUID opportunityId);

    Optional<InternalAssignment> findByIdAndCompanyId(UUID id, UUID companyId);

    long countByCompanyIdAndStatus(UUID companyId, AssignmentStatus status);
}
