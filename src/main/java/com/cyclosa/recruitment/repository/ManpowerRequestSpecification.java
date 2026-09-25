package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.dto.request.ManpowerRequestFilter;
import com.cyclosa.recruitment.entity.ManpowerRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManpowerRequestSpecification {
    public static Specification<ManpowerRequest> filter(UUID companyId, ManpowerRequestFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getDepartmentId() != null) predicates.add(cb.equal(root.get("departmentId"), filter.getDepartmentId()));
                if (filter.getPositionId() != null) predicates.add(cb.equal(root.get("positionId"), filter.getPositionId()));
                if (filter.getStatus() != null) predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                    String p = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                    predicates.add(cb.or(cb.like(cb.lower(root.get("requestCode")), p), cb.like(cb.lower(root.get("reason")), p)));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
