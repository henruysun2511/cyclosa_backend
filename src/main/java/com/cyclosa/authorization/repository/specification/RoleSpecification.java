package com.cyclosa.authorization.repository.specification;

import com.cyclosa.authorization.dto.request.RoleFilterRequest;
import com.cyclosa.authorization.entity.Role;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class RoleSpecification {

    private RoleSpecification() {}

    public static Specification<Role> filterBy(RoleFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Luôn chỉ lấy role chưa bị xóa mềm
            predicates.add(cb.isFalse(root.get("deleted")));

            if (request == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // 2. Tìm kiếm theo từ khóa (Mã hoặc Tên)
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String search = "%" + request.getKeyword().trim().toLowerCase() + "%";
                Predicate codePredicate = cb.like(cb.lower(root.get("code")), search);
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), search);
                predicates.add(cb.or(codePredicate, namePredicate));
            }

            // 3. Lọc theo companyId (Nếu có truyền companyId thì lấy role của company đó HOẶC role hệ thống)
            if (request.getCompanyId() != null) {
                Predicate compPredicate = cb.equal(root.get("companyId"), request.getCompanyId());
                Predicate sysPredicate = cb.isNull(root.get("companyId"));
                predicates.add(cb.or(compPredicate, sysPredicate));
            }

            // 4. Lọc theo isSystemRole
            if (request.getIsSystemRole() != null) {
                predicates.add(cb.equal(root.get("isSystemRole"), request.getIsSystemRole()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

