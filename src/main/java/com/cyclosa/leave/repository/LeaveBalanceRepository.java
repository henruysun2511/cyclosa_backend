package com.cyclosa.leave.repository;

import com.cyclosa.leave.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID>, JpaSpecificationExecutor<LeaveBalance> {

    Optional<LeaveBalance> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<LeaveBalance> findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(
            UUID companyId, UUID employeeId, UUID leaveTypeId, int year
    );

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
            UUID employeeId, UUID leaveTypeId, int year
    );

    List<LeaveBalance> findAllByEmployeeIdAndYear(UUID employeeId, int year);

    List<LeaveBalance> findAllByCompanyIdAndYear(UUID companyId, int year);

    List<LeaveBalance> findAllByCompanyIdAndEmployeeId(UUID companyId, UUID employeeId);

    boolean existsByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(
            UUID companyId, UUID employeeId, UUID leaveTypeId, int year
    );
}
