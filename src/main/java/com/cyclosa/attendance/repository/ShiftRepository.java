package com.cyclosa.attendance.repository;

import com.cyclosa.attendance.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID>, JpaSpecificationExecutor<Shift> {

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    Optional<Shift> findByIdAndCompanyId(UUID id, UUID companyId);

    List<Shift> findAllByCompanyId(UUID companyId);

    List<Shift> findAllByCompanyIdAndIsActiveTrue(UUID companyId);

    Optional<Shift> findFirstByCompanyIdAndIsActiveTrueOrderByCreatedAtAsc(UUID companyId);

    Optional<Shift> findByCompanyIdAndCode(UUID companyId, String code);
}
