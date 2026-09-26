package com.cyclosa.onboarding.repository;

import com.cyclosa.onboarding.entity.OnboardingProcessItem;
import com.cyclosa.onboarding.enums.OnboardingItemCategory;
import com.cyclosa.onboarding.enums.OnboardingItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface OnboardingProcessItemRepository extends JpaRepository<OnboardingProcessItem, UUID> {

    List<OnboardingProcessItem> findByOnboardingProcessIdOrderByOrderIndexAsc(UUID onboardingProcessId);

    @Query("SELECT COUNT(i) FROM OnboardingProcessItem i WHERE i.onboardingProcess.id = :processId")
    long countTotalByProcessId(@Param("processId") UUID processId);

    @Query("SELECT COUNT(i) FROM OnboardingProcessItem i WHERE i.onboardingProcess.id = :processId AND i.status = :status")
    long countByProcessIdAndStatus(@Param("processId") UUID processId, @Param("status") OnboardingItemStatus status);

    @Query("SELECT COUNT(i) FROM OnboardingProcessItem i WHERE i.onboardingProcess.id = :processId AND i.isRequired = true AND i.status != 'COMPLETED'")
    long countIncompleteRequiredItems(@Param("processId") UUID processId);

    List<OnboardingProcessItem> findByOnboardingProcessIdAndCategory(UUID onboardingProcessId, OnboardingItemCategory category);

    @Query("SELECT i.onboardingProcess.id, COUNT(i), SUM(CASE WHEN i.status = 'COMPLETED' THEN 1 ELSE 0 END) " +
           "FROM OnboardingProcessItem i WHERE i.onboardingProcess.id IN :processIds GROUP BY i.onboardingProcess.id")
    List<Object[]> countItemsByProcessIds(@Param("processIds") Collection<UUID> processIds);
}
