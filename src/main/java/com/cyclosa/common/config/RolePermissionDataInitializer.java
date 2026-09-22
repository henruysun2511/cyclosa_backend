package com.cyclosa.common.config;

import com.cyclosa.permission.entity.Permission;
import com.cyclosa.permission.repository.PermissionRepository;
import com.cyclosa.role.entity.Role;
import com.cyclosa.role.entity.RolePermission;
import com.cyclosa.role.entity.UserRoleMapping;
import com.cyclosa.role.repository.RolePermissionRepository;
import com.cyclosa.role.repository.RoleRepository;
import com.cyclosa.role.repository.UserRoleMappingRepository;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.enums.UserRole;
import com.cyclosa.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final UserRepository userRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("[DataInitializer] Khởi tạo dữ liệu vai trò & phân quyền hệ thống CYCLOSA...");

        Map<String, Permission> permMap = seedPermissions();
        Map<String, Role> roleMap = seedRoles();
        seedRolePermissions(roleMap, permMap);
        seedDefaultAdmin(roleMap.get("SUPER_ADMIN"));

        log.info("[DataInitializer] Hoàn tất khởi tạo vai trò & phân quyền.");
    }

    private Map<String, Permission> seedPermissions() {
        // Danh mục quyền chuẩn theo ba/HRM-Roles-Phan-Quyen.md
        List<Permission> defaultPerms = List.of(
                // 01. Organization
                p("org.view", "organization", "view", "Xem thông tin cơ cấu tổ chức"),
                p("org.manage", "organization", "manage", "Quản lý cơ cấu tổ chức (công ty, phòng ban, chức vụ)"),
                p("organization.view", "organization", "view", "Xem cơ cấu tổ chức, phòng ban, chi nhánh"),
                p("organization.create", "organization", "create", "Tạo mới phòng ban, chi nhánh, chức danh"),
                p("organization.update", "organization", "update", "Cập nhật phòng ban, chi nhánh, chức danh"),
                p("organization.delete", "organization", "delete", "Xóa phòng ban, chi nhánh, chức danh"),
                p("organization.manage", "organization", "manage", "Toàn quyền quản trị cơ cấu tổ chức và tái cấu trúc"),

                // 02. Recruitment
                p("recruitment.view", "recruitment", "view", "Xem tin tuyển dụng và ứng viên"),
                p("recruitment.manage", "recruitment", "manage", "Quản lý tuyển dụng, đăng tin, lọc ứng viên"),
                p("recruitment.request", "recruitment", "request", "Tạo đề xuất tuyển dụng nhân sự"),
                p("recruitment.interview", "recruitment", "interview", "Chấm điểm và đánh giá phỏng vấn"),

                // 03. Onboarding
                p("onboarding.view", "onboarding", "view", "Xem quy trình onboarding"),
                p("onboarding.manage", "onboarding", "manage", "Quản lý tiến trình onboarding và cấp tài khoản/thiết bị"),

                // 04. Employee
                p("employee.view", "employee", "view", "Xem hồ sơ nhân viên"),
                p("employee.create", "employee", "create", "Tiếp nhận hồ sơ nhân viên mới"),
                p("employee.update", "employee", "update", "Cập nhật thông tin hồ sơ nhân sự"),
                p("employee.manage", "employee", "manage", "Thêm mới, cập nhật hồ sơ nhân sự"),
                p("employee.manage_job", "employee", "manage_job", "Điều chuyển công tác, bổ nhiệm chức danh"),
                p("employee.update_status", "employee", "update_status", "Cập nhật trạng thái làm việc nhân sự"),
                p("employee.view_salary", "employee", "view_salary", "Xem thông tin lương trong hồ sơ"),

                // 05. Contract
                p("contract.view", "contract", "view", "Xem hợp đồng lao động"),
                p("contract.create", "contract", "create", "Soạn thảo hợp đồng mới"),
                p("contract.update", "contract", "update", "Cập nhật dự thảo hợp đồng"),
                p("contract.delete", "contract", "delete", "Xóa dự thảo hợp đồng"),
                p("contract.approve", "contract", "approve", "Phê duyệt hợp đồng lao động"),
                p("contract.terminate", "contract", "terminate", "Thanh lý và chấm dứt hợp đồng lao động"),
                p("contract.manage", "contract", "manage", "Soạn thảo, ký kết và gia hạn hợp đồng"),
                p("contract.config", "contract", "config", "Cấu hình mẫu hợp đồng và mail-merge"),

                // 06. Attendance
                p("attendance.view", "attendance", "view", "Xem dữ liệu chấm công và ca làm việc"),
                p("attendance.view_own", "attendance", "view_own", "Xem dữ liệu chấm công cá nhân"),
                p("attendance.shift.view", "attendance", "view", "Xem danh mục ca làm việc"),
                p("attendance.shift.create", "attendance", "create", "Tạo ca làm việc mới"),
                p("attendance.shift.update", "attendance", "update", "Cập nhật ca làm việc"),
                p("attendance.shift.delete", "attendance", "delete", "Xóa ca làm việc"),
                p("attendance.schedule.view", "attendance", "view", "Xem bảng phân ca làm việc"),
                p("attendance.schedule.manage", "attendance", "manage", "Phân ca làm việc cho nhân viên"),
                p("attendance.record", "attendance", "record", "Thực hiện chấm công Check-in / Check-out hàng ngày"),
                p("attendance.record.view", "attendance", "view", "Quản lý xem nhật ký chấm công toàn đơn vị"),
                p("attendance.explain.apply", "attendance", "apply", "Gửi đơn giải trình chấm công"),
                p("attendance.explain.view", "attendance", "view", "Xem danh sách đơn giải trình chấm công"),
                p("attendance.timesheet.view", "attendance", "view", "Xem bảng công tổng hợp toàn đơn vị"),
                p("attendance.timesheet.manage", "attendance", "manage", "Tổng hợp, tính toán lại bảng công"),
                p("attendance.timesheet.lock", "attendance", "lock", "Khóa chốt bảng công tháng"),
                p("attendance.checkin", "attendance", "checkin", "Thực hiện chấm công hàng ngày"),
                p("attendance.approve", "attendance", "approve", "Duyệt đơn giải trình chấm công, làm thêm giờ"),
                p("attendance.manage", "attendance", "manage", "Quản lý cấu hình ca, lịch làm việc"),

                // 07. Leave
                p("leave.view", "leave", "view", "Xem số dư phép và danh sách đơn nghỉ"),
                p("leave.apply", "leave", "apply", "Tạo đơn xin nghỉ phép"),
                p("leave.approve", "leave", "approve", "Phê duyệt hoặc từ chối đơn xin nghỉ phép"),
                p("leave.manage", "leave", "manage", "Cấu hình chính sách và cấp ngày phép năm"),

                // 08. Payroll
                p("payroll.view", "payroll", "view", "Xem bảng lương và phiếu lương"),
                p("payroll.process", "payroll", "process", "Tính toán, chốt bảng lương và chi trả"),
                p("payroll.manage", "payroll", "manage", "Quản lý kỳ tính lương tháng"),
                p("payroll.approve", "payroll", "approve", "Phê duyệt bảng lương tháng"),
                p("payroll.config", "payroll", "config", "Cấu hình thành phần lương và biểu thuế"),
                p("payroll.advance", "payroll", "advance", "Đơn xin tạm ứng tiền lương"),

                // 09. Performance
                p("performance.view", "performance", "view", "Xem KPI, mục tiêu và kết quả đánh giá"),
                p("performance.evaluate", "performance", "evaluate", "Đánh giá hiệu suất nhân viên"),
                p("performance.manage", "performance", "manage", "Tạo chu kỳ và cấu hình đánh giá hiệu suất"),

                // 13. Asset
                p("asset.view", "asset", "view", "Xem danh mục tài sản thiết bị"),
                p("asset.manage", "asset", "manage", "Cấp phát, thu hồi và kiểm kê tài sản"),

                // 18. Workflow
                p("workflow.view", "workflow", "view", "Xem danh sách và tiến trình phê duyệt"),
                p("workflow.approve", "workflow", "approve", "Phê duyệt các yêu cầu quy trình động"),
                p("workflow.manage", "workflow", "manage", "Cấu hình ma trận duyệt và luồng workflow"),
                p("workflow.delegate", "workflow", "delegate", "Thiết lập và quản lý ủy quyền phê duyệt"),

                // 20. Reports
                p("report.view", "report", "view", "Xem báo cáo phân tích và dashboard"),
                p("report.export", "report", "export", "Xuất file báo cáo tổng hợp"),

                // 21. System Admin & RBAC
                p("role.view", "role", "view", "Xem danh sách và chi tiết vai trò, phân quyền"),
                p("role.create", "role", "create", "Tạo vai trò tùy biến mới"),
                p("role.update", "role", "update", "Cập nhật thông tin vai trò"),
                p("role.delete", "role", "delete", "Xóa vai trò tùy biến"),
                p("role.assign", "role", "assign", "Gán quyền hạn và phạm vi dữ liệu cho vai trò"),
                p("user.view", "user", "view", "Xem danh sách và chi tiết tài khoản người dùng"),
                p("user.manage", "user", "manage", "Tạo, khóa, mở khóa tài khoản nhân viên"),
                p("user.assign_role", "user", "assign_role", "Gán vai trò cho người dùng"),
                p("admin.role.manage", "system_admin", "manage", "Quản lý vai trò và phân quyền (RBAC)"),
                p("admin.user.manage", "system_admin", "manage", "Quản trị tài khoản người dùng"),
                p("admin.audit.view", "system_admin", "view", "Xem nhật ký hệ thống (Audit Log)")
        );

        Map<String, Permission> map = new HashMap<>();
        for (Permission p : defaultPerms) {
            Permission existing = permissionRepository.findByCode(p.getCode()).orElse(null);
            if (existing == null) {
                existing = permissionRepository.save(p);
            }
            map.put(existing.getCode(), existing);
        }
        return map;
    }

    private Map<String, Role> seedRoles() {
        Map<String, Role> map = new HashMap<>();
        for (UserRole ur : UserRole.values()) {
            String code = ur.name();
            Role existing = roleRepository.findByCodeAndCompanyIdIsNull(code)
                    .orElseGet(() -> roleRepository.findByCode(code).orElse(null));

            if (existing == null) {
                existing = Role.builder()
                        .name(ur.getTitle())
                        .code(code)
                        .description(ur.getDescription())
                        .isSystemRole(true)
                        .build();
                existing = roleRepository.save(existing);
            }
            map.put(code, existing);
        }
        return map;
    }

    private void seedRolePermissions(Map<String, Role> roleMap, Map<String, Permission> permMap) {
        Role superAdmin = roleMap.get("SUPER_ADMIN");
        if (superAdmin != null && rolePermissionRepository.findByRoleId(superAdmin.getId()).isEmpty()) {
            List<RolePermission> rps = new ArrayList<>();
            for (Permission p : permMap.values()) {
                rps.add(RolePermission.builder().role(superAdmin).permission(p).dataScope(DataScope.ALL).build());
            }
            rolePermissionRepository.saveAll(rps);
        }

        Role employee = roleMap.get("EMPLOYEE");
        if (employee != null && rolePermissionRepository.findByRoleId(employee.getId()).isEmpty()) {
            List<RolePermission> rps = new ArrayList<>();
            List<String> empPermCodes = List.of(
                    "attendance.checkin", "attendance.view", "attendance.view_own",
                    "attendance.record", "attendance.explain.apply",
                    "leave.view", "leave.apply",
                    "contract.view",
                    "workflow.view", "workflow.delegate",
                    "payroll.view", "payroll.advance", "performance.view"
            );
            for (String code : empPermCodes) {
                Permission p = permMap.get(code);
                if (p != null) {
                    rps.add(RolePermission.builder().role(employee).permission(p).dataScope(DataScope.OWN).build());
                }
            }
            rolePermissionRepository.saveAll(rps);
        }

        Role deptManager = roleMap.get("DEPARTMENT_MANAGER");
        if (deptManager != null && rolePermissionRepository.findByRoleId(deptManager.getId()).isEmpty()) {
            List<RolePermission> rps = new ArrayList<>();
            List<String> dmPermCodes = List.of(
                    "employee.view", "attendance.view", "attendance.view_own", "attendance.approve",
                    "attendance.shift.view", "attendance.schedule.view", "attendance.record.view",
                    "attendance.explain.view", "attendance.timesheet.view",
                    "leave.view", "leave.approve", "contract.view",
                    "payroll.view", "payroll.advance",
                    "performance.view", "performance.evaluate",
                    "recruitment.request", "workflow.view", "workflow.approve", "workflow.delegate"
            );
            for (String code : dmPermCodes) {
                Permission p = permMap.get(code);
                if (p != null) {
                    rps.add(RolePermission.builder().role(deptManager).permission(p).dataScope(DataScope.DEPARTMENT).build());
                }
            }
            rolePermissionRepository.saveAll(rps);
        }

        Role hrAdmin = roleMap.get("HR_ADMIN");
        if (hrAdmin != null && rolePermissionRepository.findByRoleId(hrAdmin.getId()).isEmpty()) {
            List<RolePermission> rps = new ArrayList<>();
            for (Permission p : permMap.values()) {
                if (!p.getModule().equals("system_admin")) {
                    rps.add(RolePermission.builder().role(hrAdmin).permission(p).dataScope(DataScope.COMPANY).build());
                }
            }
            rolePermissionRepository.saveAll(rps);
        }
    }

    private void seedDefaultAdmin(Role superAdminRole) {
        String adminEmail = "admin@cyclosa.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .username("admin")
                    .fullName("Quản trị hệ thống CYCLOSA")
                    .passwordHash(passwordEncoder.encode("Admin@123456"))
                    .status(UserStatus.ACTIVE)
                    .version(0)
                    .build();

            admin = userRepository.save(admin);

            if (superAdminRole != null) {
                UserRoleMapping mapping = UserRoleMapping.builder()
                        .user(admin)
                        .role(superAdminRole)
                        .build();
                userRoleMappingRepository.save(mapping);
            }
            log.info("[DataInitializer] Đã tạo tài khoản quản trị mặc định: {} / Admin@123456", adminEmail);
        }
    }

    private Permission p(String code, String module, String action, String description) {
        return Permission.builder()
                .code(code)
                .module(module)
                .action(action)
                .description(description)
                .build();
    }
}
