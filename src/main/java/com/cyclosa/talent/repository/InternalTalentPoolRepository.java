package com.cyclosa.talent.repository;

import com.cyclosa.talent.entity.InternalTalentPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InternalTalentPoolRepository extends JpaRepository<InternalTalentPool, UUID>, JpaSpecificationExecutor<InternalTalentPool> {

    boolean existsByCompanyIdAndEmployeeIdAndTag(UUID companyId, UUID employeeId, String tag);

    Optional<InternalTalentPool> findByIdAndCompanyId(UUID id, UUID companyId);
}
