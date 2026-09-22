package com.cyclosa.attendance.repository;

import com.cyclosa.attendance.entity.MonthlyTimesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface MonthlyTimesheetRepository extends JpaRepository<MonthlyTimesheet, UUID>, JpaSpecificationExecutor<MonthlyTimesheet> {

    Optional<MonthlyTimesheet> findByCompanyIdAndEmployeeIdAndMonthAndYear(
            UUID companyId, UUID employeeId, Integer month, Integer year
    );

    Optional<MonthlyTimesheet> findByEmployeeIdAndMonthAndYear(
            UUID employeeId, Integer month, Integer year
    );

    List<MonthlyTimesheet> findAllByCompanyIdAndMonthAndYear(
            UUID companyId, Integer month, Integer year
    );

    List<MonthlyTimesheet> findAllByCompanyIdAndEmployeeIdInAndMonthAndYear(
            UUID companyId, Set<UUID> employeeIds, Integer month, Integer year
    );
}
