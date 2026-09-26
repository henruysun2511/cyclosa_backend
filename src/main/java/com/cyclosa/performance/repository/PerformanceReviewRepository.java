package com.cyclosa.performance.repository;

import com.cyclosa.performance.entity.PerformanceReview;
import com.cyclosa.performance.enums.ReviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, UUID> {

    List<PerformanceReview> findByEvaluationId(UUID evaluationId);

    List<PerformanceReview> findByGoalId(UUID goalId);

    Optional<PerformanceReview> findByGoalIdAndReviewType(UUID goalId, ReviewType reviewType);
}
