package com.cyclosa.auth.repository.specification;

import com.cyclosa.auth.dto.request.UserFilterRequest;
import com.cyclosa.auth.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> build(UserFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Tìm kiếm theo từ khóa (username hoặc email)
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                ));
            }

            // 2. Lọc theo trạng thái
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // 3. Bản ghi chưa bị xóa mềm
            predicates.add(cb.isFalse(root.get("deleted")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

