package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.ProbationEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProbationEvaluationRepository extends JpaRepository<ProbationEvaluation, UUID> {

    List<ProbationEvaluation> findAllByEmployeeIdOrderByEvaluationDateDesc(UUID employeeId);

    Optional<ProbationEvaluation> findTopByEmployeeIdOrderByEvaluationDateDesc(UUID employeeId);
}

