package com.cyclosa.onboarding.repository;

import com.cyclosa.onboarding.entity.OnboardingChecklistTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingChecklistTemplateRepository extends JpaRepository<OnboardingChecklistTemplate, UUID>, JpaSpecificationExecutor<OnboardingChecklistTemplate> {

    Optional<OnboardingChecklistTemplate> findByIdAndCompanyId(UUID id, UUID companyId);

    List<OnboardingChecklistTemplate> findByCompanyIdAndIsActiveTrue(UUID companyId);

    List<OnboardingChecklistTemplate> findByApplicablePositionIdAndIsActiveTrue(UUID applicablePositionId);

    List<OnboardingChecklistTemplate> findByApplicableDepartmentIdAndIsActiveTrue(UUID applicableDepartmentId);
}
