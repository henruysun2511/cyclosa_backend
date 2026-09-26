package com.cyclosa.discipline.repository;

import com.cyclosa.discipline.entity.Grievance;
import com.cyclosa.discipline.enums.GrievanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, UUID> {

    @Query("SELECT g FROM Grievance g WHERE " +
           "(:companyId IS NULL OR g.companyId = :companyId) AND " +
           "(:employeeId IS NULL OR g.employeeId = :employeeId) AND " +
           "(:status IS NULL OR g.status = :status)")
    Page<Grievance> searchGrievances(
            @Param("companyId") UUID companyId,
            @Param("employeeId") UUID employeeId,
            @Param("status") GrievanceStatus status,
            Pageable pageable
    );
}
