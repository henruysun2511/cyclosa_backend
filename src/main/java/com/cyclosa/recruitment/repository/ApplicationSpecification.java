package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.dto.request.ApplicationFilter;
import com.cyclosa.recruitment.entity.Application;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ApplicationSpecification {
    public static Specification<Application> filter(UUID companyId, ApplicationFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getJobPositionId() != null) predicates.add(cb.equal(root.get("jobPositionId"), filter.getJobPositionId()));
                if (filter.getCandidateId() != null) predicates.add(cb.equal(root.get("candidateId"), filter.getCandidateId()));
                if (filter.getStage() != null) predicates.add(cb.equal(root.get("stage"), filter.getStage()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
