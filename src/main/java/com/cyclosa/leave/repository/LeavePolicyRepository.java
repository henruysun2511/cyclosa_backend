package com.cyclosa.leave.repository;

import com.cyclosa.leave.entity.LeavePolicy;
import com.cyclosa.leave.enums.JobConditionLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, UUID>, JpaSpecificationExecutor<LeavePolicy> {

    Optional<LeavePolicy> findByIdAndCompanyId(UUID id, UUID companyId);

    List<LeavePolicy> findAllByCompanyId(UUID companyId);

    Optional<LeavePolicy> findByCompanyIdAndLeaveTypeIdAndJobConditionLevel(
            UUID companyId, UUID leaveTypeId, JobConditionLevel jobConditionLevel
    );

    boolean existsByCompanyIdAndLeaveTypeIdAndJobConditionLevel(
            UUID companyId, UUID leaveTypeId, JobConditionLevel jobConditionLevel
    );

    boolean existsByLeaveTypeId(UUID leaveTypeId);
}
