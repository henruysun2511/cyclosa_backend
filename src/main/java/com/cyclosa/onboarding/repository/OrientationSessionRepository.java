package com.cyclosa.onboarding.repository;

import com.cyclosa.onboarding.entity.OrientationSession;
import com.cyclosa.onboarding.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrientationSessionRepository extends JpaRepository<OrientationSession, UUID> {

    List<OrientationSession> findByOnboardingProcessIdOrderByScheduledAtAsc(UUID onboardingProcessId);

    List<OrientationSession> findByCompanyIdAndStatus(UUID companyId, SessionStatus status);

    Optional<OrientationSession> findByIdAndCompanyId(UUID id, UUID companyId);
}
