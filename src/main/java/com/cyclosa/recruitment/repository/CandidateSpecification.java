package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.dto.request.CandidateFilter;
import com.cyclosa.recruitment.entity.Candidate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CandidateSpecification {
    public static Specification<Candidate> filter(UUID companyId, CandidateFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getSource() != null) predicates.add(cb.equal(root.get("source"), filter.getSource()));
                if (filter.getStatus() != null) predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                    String p = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                    predicates.add(cb.or(cb.like(cb.lower(root.get("fullName")), p), cb.like(cb.lower(root.get("email")), p), cb.like(root.get("phone"), p)));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
