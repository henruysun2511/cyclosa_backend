package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.dto.request.JobPostingFilter;
import com.cyclosa.recruitment.entity.JobPosting;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JobPostingSpecification {
    public static Specification<JobPosting> filter(UUID companyId, JobPostingFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getJobPositionId() != null) predicates.add(cb.equal(root.get("jobPositionId"), filter.getJobPositionId()));
                if (filter.getChannel() != null) predicates.add(cb.equal(root.get("channel"), filter.getChannel()));
                if (filter.getStatus() != null) predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                    String p = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("title")), p));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
