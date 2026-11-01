package com.cyclosa.authorization.security;

import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.enums.UserRole;
import com.cyclosa.authorization.entity.Permission;
import com.cyclosa.authorization.entity.Role;
import com.cyclosa.authorization.entity.RolePermission;
import com.cyclosa.authorization.repository.PermissionRepository;
import com.cyclosa.authorization.repository.RolePermissionRepository;
import com.cyclosa.authorization.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RolePermissionDataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Bắt đầu kiểm tra và khởi tạo dữ liệu mặc định cho Module Phân quyền...");
        Map<String, Permission> permMap = initPermissions();
        initSystemRoles(permMap);
        log.info("Hoàn tất khởi tạo dữ liệu mặc định cho Module Phân quyền.");
    }

    private Map<String, Permission> initPermissions() {
        List<PermissionDefinition> definitions = List.of(
                // USER
                new PermissionDefinition("USER", "VIEW", "user.view", "Xem danh sách và chi tiết người dùng"),
                new PermissionDefinition("USER", "CREATE", "user.create", "Tạo mới tài khoản người dùng"),
                new PermissionDefinition("USER", "UPDATE", "user.update", "Cập nhật thông tin tài khoản"),
                new PermissionDefinition("USER", "DELETE", "user.delete", "Khóa / Xóa tài khoản người dùng"),
                new PermissionDefinition("USER", "ACTIVATE", "user.activate", "Kích hoạt tài khoản nhân viên"),

                // ROLE
                new PermissionDefinition("ROLE", "VIEW", "role.view", "Xem danh sách vai trò và ma trận quyền"),
                new PermissionDefinition("ROLE", "MANAGE", "role.manage", "Tạo, cập nhật và thiết lập quyền cho vai trò"),
                new PermissionDefinition("ROLE", "ASSIGN", "role.assign", "Gán hoặc thu hồi vai trò của người dùng"),

                // ORGANIZATION
                new PermissionDefinition("ORG", "VIEW", "org.view", "Xem cơ cấu tổ chức và sơ đồ phòng ban"),
                new PermissionDefinition("ORG", "MANAGE", "org.manage", "Quản lý cơ cấu tổ chức, phòng ban, chi nhánh"),
                new PermissionDefinition("ORG", "SIMULATE", "org.simulate", "Mô phỏng tái cơ cấu tổ chức"),

                // EMPLOYEE
                new PermissionDefinition("EMPLOYEE", "VIEW", "employee.view", "Xem thông tin hồ sơ nhân viên"),
                new PermissionDefinition("EMPLOYEE", "CREATE", "employee.create", "Thêm mới hồ sơ nhân sự"),
                new PermissionDefinition("EMPLOYEE", "UPDATE", "employee.update", "Cập nhật hồ sơ nhân sự"),
                new PermissionDefinition("EMPLOYEE", "DELETE", "employee.delete", "Xóa / Lưu trữ hồ sơ nhân sự"),

                // LEAVE
                new PermissionDefinition("LEAVE", "VIEW", "leave.view", "Xem đơn nghỉ phép"),
                new PermissionDefinition("LEAVE", "APPLY", "leave.apply", "Tạo và gửi đơn xin nghỉ phép"),
                new PermissionDefinition("LEAVE", "APPROVE", "leave.approve", "Phê duyệt hoặc từ chối đơn nghỉ phép"),
                new PermissionDefinition("LEAVE", "MANAGE", "leave.manage", "Quản trị cấu hình và quỹ phép doanh nghiệp"),

                // ATTENDANCE
                new PermissionDefinition("ATTENDANCE", "VIEW", "attendance.view", "Xem dữ liệu chấm công"),
                new PermissionDefinition("ATTENDANCE", "CHECKIN", "attendance.checkin", "Thực hiện chấm công hàng ngày"),
                new PermissionDefinition("ATTENDANCE", "MANAGE", "attendance.manage", "Chốt ca, đối soát và điều chỉnh chấm công"),

                // PAYROLL
                new PermissionDefinition("PAYROLL", "VIEW", "payroll.view", "Xem bảng tính lương tổng hợp"),
                new PermissionDefinition("PAYROLL", "PROCESS", "payroll.process", "Tính toán, phê duyệt và chi trả lương"),
                new PermissionDefinition("PAYROLL", "VIEW_SALARY", "payroll.view_salary", "Xem chi tiết mức lương và thu nhập nhân sự"),

                // CONTRACT
                new PermissionDefinition("CONTRACT", "VIEW", "contract.view", "Xem hợp đồng lao động"),
                new PermissionDefinition("CONTRACT", "CREATE", "contract.create", "Tạo mới và gia hạn hợp đồng"),
                new PermissionDefinition("CONTRACT", "SIGN", "contract.sign", "Ký duyệt hợp đồng lao động"),

                // RECRUITMENT
                new PermissionDefinition("RECRUITMENT", "VIEW", "recruitment.view", "Xem tin tuyển dụng và hồ sơ ứng viên"),
                new PermissionDefinition("RECRUITMENT", "CREATE", "recruitment.create", "Đăng tin tuyển dụng mới"),
                new PermissionDefinition("RECRUITMENT", "MANAGE", "recruitment.manage", "Điều phối ứng viên, phỏng vấn và đánh giá"),

                // PERFORMANCE
                new PermissionDefinition("PERFORMANCE", "VIEW", "performance.view", "Xem đánh giá hiệu suất và KPI"),
                new PermissionDefinition("PERFORMANCE", "EVALUATE", "performance.evaluate", "Thiết lập và chấm điểm hiệu suất"),

                // WORKPLACE
                new PermissionDefinition("WORKPLACE", "VIEW", "workplace.view", "Xem sơ đồ mặt bằng và chỗ ngồi"),
                new PermissionDefinition("WORKPLACE", "MANAGE", "workplace.manage", "Quản lý cơ sở vật chất và cấp phát tài sản"),

                // AUDIT
                new PermissionDefinition("AUDIT", "VIEW", "audit.view", "Xem nhật ký kiểm toán và báo cáo tuân thủ")
        );

        Map<String, Permission> permMap = new HashMap<>();
        for (PermissionDefinition def : definitions) {
            Permission perm = permissionRepository.findByCode(def.code())
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .resource(def.resource())
                            .action(def.action())
                            .code(def.code())
                            .description(def.description())
                            .build()));
            permMap.put(perm.getCode(), perm);
        }
        log.info("Đã đồng bộ {} quyền chức năng hệ thống", permMap.size());
        return permMap;
    }

    private void initSystemRoles(Map<String, Permission> permMap) {
        for (UserRole userRole : UserRole.values()) {
            String roleCode = userRole.name();
            Optional<Role> roleOpt = roleRepository.findByCodeAndCompanyIdIsNullAndDeletedFalse(roleCode);

            Role role;
            if (roleOpt.isEmpty()) {
                role = roleRepository.save(Role.builder()
                        .code(roleCode)
                        .name(userRole.getTitle())
                        .description(userRole.getDescription())
                        .isSystemRole(true)
                        .deleted(false)
                        .companyId(null)
                        .build());
                log.info("Khởi tạo vai trò hệ thống: code={}, name={}", roleCode, userRole.getTitle());
            } else {
                role = roleOpt.get();
            }

            List<RolePermission> existing = rolePermissionRepository.findAllByRoleId(role.getId());
            if (existing.isEmpty()) {
                assignDefaultPermissionsForRole(role, userRole, permMap);
            }
        }
    }

    private void assignDefaultPermissionsForRole(Role role, UserRole userRole, Map<String, Permission> permMap) {
        List<RolePermission> toSave = new ArrayList<>();

        switch (userRole) {
            case SUPER_ADMIN -> {
                for (Permission p : permMap.values()) {
                    toSave.add(buildRolePerm(role, p, DataScope.ALL));
                }
            }
            case HR_ADMIN -> {
                grantPerm(toSave, role, permMap, "user.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "user.create", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "user.update", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "user.activate", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "role.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "role.manage", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "role.assign", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "org.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "org.manage", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "employee.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "employee.create", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "employee.update", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "employee.delete", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "leave.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "leave.approve", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "leave.manage", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "attendance.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "attendance.manage", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "contract.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "contract.create", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "contract.sign", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "performance.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "performance.evaluate", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "recruitment.view", DataScope.COMPANY);
            }
            case HR_SPECIALIST -> {
                grantPerm(toSave, role, permMap, "employee.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "employee.create", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "employee.update", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "contract.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "contract.create", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "leave.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "leave.approve", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "attendance.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "attendance.manage", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "org.view", DataScope.COMPANY);
            }
            case RECRUITER -> {
                grantPerm(toSave, role, permMap, "recruitment.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "recruitment.create", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "recruitment.manage", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "employee.view", DataScope.COMPANY);
            }
            case PAYROLL_ACCOUNTANT -> {
                grantPerm(toSave, role, permMap, "payroll.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "payroll.process", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "payroll.view_salary", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "attendance.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "employee.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "contract.view", DataScope.COMPANY);
            }
            case EXECUTIVE -> {
                grantPerm(toSave, role, permMap, "employee.view", DataScope.ALL);
                grantPerm(toSave, role, permMap, "org.view", DataScope.ALL);
                grantPerm(toSave, role, permMap, "payroll.view", DataScope.ALL);
                grantPerm(toSave, role, permMap, "attendance.view", DataScope.ALL);
                grantPerm(toSave, role, permMap, "leave.view", DataScope.ALL);
                grantPerm(toSave, role, permMap, "performance.view", DataScope.ALL);
                grantPerm(toSave, role, permMap, "audit.view", DataScope.ALL);
            }
            case MANAGER -> {
                grantPerm(toSave, role, permMap, "employee.view", DataScope.DEPARTMENT);
                grantPerm(toSave, role, permMap, "leave.view", DataScope.DEPARTMENT);
                grantPerm(toSave, role, permMap, "leave.approve", DataScope.DEPARTMENT);
                grantPerm(toSave, role, permMap, "attendance.view", DataScope.DEPARTMENT);
                grantPerm(toSave, role, permMap, "performance.view", DataScope.DEPARTMENT);
                grantPerm(toSave, role, permMap, "performance.evaluate", DataScope.DEPARTMENT);
                grantPerm(toSave, role, permMap, "org.view", DataScope.DEPARTMENT);
            }
            case EMPLOYEE -> {
                grantPerm(toSave, role, permMap, "employee.view", DataScope.OWN);
                grantPerm(toSave, role, permMap, "leave.view", DataScope.OWN);
                grantPerm(toSave, role, permMap, "leave.apply", DataScope.OWN);
                grantPerm(toSave, role, permMap, "attendance.view", DataScope.OWN);
                grantPerm(toSave, role, permMap, "attendance.checkin", DataScope.OWN);
                grantPerm(toSave, role, permMap, "payroll.view", DataScope.OWN);
                grantPerm(toSave, role, permMap, "performance.view", DataScope.OWN);
            }
            case OFFICE_ADMIN -> {
                grantPerm(toSave, role, permMap, "workplace.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "workplace.manage", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "employee.view", DataScope.COMPANY);
                grantPerm(toSave, role, permMap, "org.view", DataScope.COMPANY);
            }
            case AUDITOR -> {
                grantPerm(toSave, role, permMap, "audit.view", DataScope.ALL);
                grantPerm(toSave, role, permMap, "employee.view", DataScope.ALL);
                grantPerm(toSave, role, permMap, "contract.view", DataScope.ALL);
                grantPerm(toSave, role, permMap, "payroll.view", DataScope.ALL);
                grantPerm(toSave, role, permMap, "role.view", DataScope.ALL);
            }
        }

        if (!toSave.isEmpty()) {
            rolePermissionRepository.saveAll(toSave);
            log.info("Đã gán {} quyền mặc định cho vai trò {}", toSave.size(), role.getCode());
        }
    }

    private void grantPerm(List<RolePermission> list, Role role, Map<String, Permission> permMap, String code, DataScope scope) {
        Permission p = permMap.get(code);
        if (p != null) {
            list.add(buildRolePerm(role, p, scope));
        }
    }

    private RolePermission buildRolePerm(Role role, Permission p, DataScope scope) {
        return RolePermission.builder()
                .role(role)
                .permission(p)
                .dataScope(scope)
                .build();
    }

    private record PermissionDefinition(String resource, String action, String code, String description) {}
}
