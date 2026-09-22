package com.cyclosa.leave.repository;

import com.cyclosa.leave.entity.LeaveType;
import com.cyclosa.leave.enums.LeaveCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID>, JpaSpecificationExecutor<LeaveType> {

    Optional<LeaveType> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<LeaveType> findByCompanyIdAndCode(UUID companyId, String code);

    Optional<LeaveType> findByCodeAndCompanyIdIsNull(String code);

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    List<LeaveType> findAllByCompanyIdOrCompanyIdIsNull(UUID companyId);

    Optional<LeaveType> findFirstByCategoryAndCompanyId(LeaveCategory category, UUID companyId);

    Optional<LeaveType> findFirstByCategoryAndCompanyIdIsNull(LeaveCategory category);
}
