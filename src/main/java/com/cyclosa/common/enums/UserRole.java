package com.cyclosa.common.enums;

import lombok.Getter;

/**
 * Danh mục vai trò người dùng (Roles) trong hệ thống CYCLOSA
 */
@Getter
public enum UserRole {

    // Nhóm 1: Quản trị & Nghiệp vụ Nhân sự
    SUPER_ADMIN("Super Admin", "Quản trị cao nhất toàn hệ thống, cấu hình nền tảng, phân quyền tenant, cấu hình kỹ thuật"),
    HR_ADMIN("HR Admin / HR Manager", "Trưởng phòng Nhân sự, toàn quyền quản trị nghiệp vụ HR trong phạm vi công ty"),
    HR_SPECIALIST("HR Specialist", "Chuyên viên Nhân sự thực thi nghiệp vụ (Hồ sơ, Hợp đồng, C&B, Đào tạo, Khen thưởng - Kỷ luật...)"),
    RECRUITER("Recruiter", "Chuyên viên tuyển dụng, quản lý tin đăng, nguồn ứng viên, điều phối phỏng vấn (không xem lương C&B)"),
    PAYROLL_ACCOUNTANT("Payroll / Accountant", "Kế toán tiền lương, đối soát bảng lương, bảo hiểm, quyết toán thuế TNCN"),

    // Nhóm 2: Cán bộ Quản lý & Vận hành
    EXECUTIVE("Executive / BOD", "Ban Lãnh đạo, xem dashboard BI tổng thể và báo cáo chiến lược"),
    MANAGER("Manager", "Quản lý và phê duyệt đơn từ, đề xuất nhân sự, đánh giá KPI theo phạm vi đơn vị (data_scope + org_unit_id)"),

    // Nhóm 3: Nhân viên & Hỗ trợ chuyên trách
    EMPLOYEE("Employee", "Nhân viên (Chính thức/Thử việc/Thực tập), sử dụng cổng tự phục vụ nhân viên (ESS)"),
    OFFICE_ADMIN("Office Admin / IT & Facility", "Quản trị cơ sở vật chất, mặt bằng, sơ đồ chỗ ngồi và cấp phát/thu hồi tài sản công nghệ"),
    AUDITOR("Auditor / Compliance", "Kiểm toán nội bộ, giám sát Audit Logs, rà soát tính hợp lệ quy trình độc lập");

    private final String title;
    private final String description;

    UserRole(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
