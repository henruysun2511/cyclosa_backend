package com.cyclosa.common.enums;

import lombok.Getter;

/**
 * Danh mục vai trò người dùng (Roles) trong hệ thống CYCLOSA
 * Tham chiếu thiết kế tại ba/HRM-Roles-Phan-Quyen.md
 */
@Getter
public enum UserRole {

    SUPER_ADMIN("Super Admin", "Quản trị toàn hệ thống, cấu hình, phân quyền, tích hợp đa công ty"),
    HR_ADMIN("HR Admin / HR Manager", "Trưởng phòng Nhân sự, quản lý toàn bộ nghiệp vụ HR trong công ty/chi nhánh"),
    HR_SPECIALIST("HR Specialist", "Chuyên viên Nhân sự theo mảng: Tuyển dụng, C&B, Đào tạo, Hành chính nhân sự"),
    DEPARTMENT_MANAGER("Department Manager / Line Manager", "Trưởng phòng ban trực tiếp, duyệt yêu cầu và đánh giá nhân viên trong phòng"),
    TEAM_LEADER("Team Leader", "Trưởng nhóm, quản lý công việc và duyệt yêu cầu trong phạm vi team phụ trách"),
    EMPLOYEE("Employee", "Nhân viên chính thức/thử việc, sử dụng cổng tự phục vụ nhân viên (ESS)"),
    RECRUITER("Recruiter", "Chuyên viên tuyển dụng/Cộng tác viên, quản lý tin tuyển dụng và hồ sơ ứng viên"),
    INTERVIEWER("Interviewer", "Người tham gia phỏng vấn, xem hồ sơ ứng viên được gán và nhập phiếu đánh giá"),
    CANDIDATE("Candidate", "Ứng viên ngoài hệ thống nộp hồ sơ qua cổng tuyển dụng"),
    PAYROLL_ACCOUNTANT("Payroll / Accountant", "Kế toán lương, xử lý và đối soát số liệu lương, thuế, bảo hiểm"),
    IT_ADMIN("IT Admin", "Quản trị công nghệ thông tin, cấp phát tài khoản hệ thống và thiết bị tài sản"),
    EXECUTIVE("Executive / BOD", "Ban lãnh đạo công ty/tập đoàn, xem báo cáo phân tích và dashboard tổng thể"),
    AUDITOR("Auditor / Compliance", "Kiểm toán nội bộ/Ban tuân thủ, giám sát audit log và tính hợp lệ chính sách"),
    UNION_REPRESENTATIVE("Union Representative", "Đại diện công đoàn, giám sát các sự vụ khen thưởng, kỷ luật, khiếu nại"),
    FACILITY_ADMIN("Facility Admin", "Quản trị cơ sở vật chất, sơ đồ chỗ ngồi và phân bổ không gian làm việc");

    private final String title;
    private final String description;

    UserRole(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
