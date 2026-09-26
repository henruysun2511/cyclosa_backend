package com.cyclosa.onboarding.repository;

import com.cyclosa.onboarding.entity.OnboardingChecklistTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OnboardingChecklistTemplateItemRepository extends JpaRepository<OnboardingChecklistTemplateItem, UUID> {

    List<OnboardingChecklistTemplateItem> findByTemplateIdOrderByOrderIndexAsc(UUID templateId);

    void deleteByTemplateId(UUID templateId);
}
