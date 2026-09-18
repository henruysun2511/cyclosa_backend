package com.cyclosa.employee.repository;

import com.cyclosa.employee.dto.request.EmployeeFilter;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.entity.EmployeeEmploymentInfo;
import com.cyclosa.employee.entity.EmployeePersonalInfo;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {

    public static Specification<Employee> filter(EmployeeFilter filter) {
        return (Root<Employee> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join an toàn với personalInfo và employmentInfo
            Join<Employee, EmployeePersonalInfo> personalJoin = root.join("personalInfo", JoinType.LEFT);
            Join<Employee, EmployeeEmploymentInfo> employmentJoin = root.join("employmentInfo", JoinType.LEFT);

            // 1. Lọc theo Công ty
            if (filter.getCompanyId() != null) {
                predicates.add(cb.equal(root.get("companyId"), filter.getCompanyId()));
            }

            // 2. Lọc theo Keyword
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate codeMatch = cb.like(cb.lower(root.get("employeeCode")), pattern);
                Predicate phoneMatch = cb.like(cb.lower(personalJoin.get("phone")), pattern);
                Predicate nationalIdMatch = cb.like(cb.lower(personalJoin.get("nationalIdNumber")), pattern);
                Predicate emailMatch = cb.like(cb.lower(personalJoin.get("personalEmail")), pattern);
                Predicate compEmailMatch = cb.like(cb.lower(employmentJoin.get("companyEmail")), pattern);

                predicates.add(cb.or(nameMatch, codeMatch, phoneMatch, nationalIdMatch, emailMatch, compEmailMatch));
            }

            // 3. Lọc theo Trạng thái
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("employmentStatus"), filter.getStatus()));
            }

            // 4. Lọc theo Ngày vào làm
            if (filter.getHireDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("hireDate"), filter.getHireDateFrom()));
            }
            if (filter.getHireDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("hireDate"), filter.getHireDateTo()));
            }

            // 5. Lọc theo Phòng ban
            if (filter.getOrganizationalUnitId() != null) {
                predicates.add(cb.equal(employmentJoin.get("organizationalUnitId"), filter.getOrganizationalUnitId()));
            }

            // 6. Lọc theo Chi nhánh
            if (filter.getBranchId() != null) {
                predicates.add(cb.equal(employmentJoin.get("branchId"), filter.getBranchId()));
            }

            // 7. Lọc theo Vị trí chức danh
            if (filter.getPositionId() != null) {
                predicates.add(cb.equal(employmentJoin.get("positionId"), filter.getPositionId()));
            }

            // 8. RÀNG BUỘC PHÂN QUYỀN DATASCOPE:
            // a) DataScope OWN: Chỉ xem chính mình
            if (filter.getExactEmployeeId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.getExactEmployeeId()));
            }

            // b) DataScope TEAM: Quản lý xem chính mình và nhân viên dưới quyền
            if (filter.getDirectManagerId() != null) {
                Predicate isSelf = cb.equal(root.get("id"), filter.getDirectManagerId());
                Predicate isSubordinate = cb.equal(employmentJoin.get("managerEmployeeId"), filter.getDirectManagerId());
                predicates.add(cb.or(isSelf, isSubordinate));
            }

            // c) DataScope DEPARTMENT: Xem toàn bộ nhân viên thuộc đơn vị mình và các đơn vị con cháu
            if (filter.getAllowedUnitIds() != null && !filter.getAllowedUnitIds().isEmpty()) {
                predicates.add(employmentJoin.get("organizationalUnitId").in(filter.getAllowedUnitIds()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
