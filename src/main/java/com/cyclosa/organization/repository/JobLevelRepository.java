package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.JobLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobLevelRepository extends JpaRepository<JobLevel, UUID> {

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    boolean existsByCodeAndCompanyIdAndIdNot(String code, UUID companyId, UUID id);

    List<JobLevel> findByCompanyIdOrderByRankOrderAsc(UUID companyId);
}
