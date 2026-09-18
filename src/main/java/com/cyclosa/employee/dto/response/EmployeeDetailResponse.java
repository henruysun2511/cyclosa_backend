package com.cyclosa.employee.dto.response;

import com.cyclosa.common.dto.summary.BranchSummary;
import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.JobLevelSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.enums.EmploymentType;
import com.cyclosa.employee.enums.Gender;
import com.cyclosa.employee.enums.MaritalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết hồ sơ nhân viên đầy đủ")
public class EmployeeDetailResponse {

    @Schema(description = "ID nhân viên")
    private UUID id;

    @Schema(description = "Mã nhân viên", example = "EMP001")
    private String employeeCode;

    @Schema(description = "Thông tin tóm tắt công ty")
    private CompanySummary company;

    @Schema(description = "ID tài khoản người dùng liên kết")
    private UUID userId;

    @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Ngày vào làm")
    private LocalDate hireDate;

    @Schema(description = "Trạng thái lao động")
    private EmploymentStatus employmentStatus;

    // --- Thông tin cá nhân & pháp lý ---
    @Schema(description = "Giới tính")
    private Gender gender;

    @Schema(description = "Ngày sinh")
    private LocalDate dateOfBirth;

    @Schema(description = "Số CCCD / CMND")
    private String nationalIdNumber;

    @Schema(description = "Ngày cấp CCCD")
    private LocalDate nationalIdIssueDate;

    @Schema(description = "Nơi cấp CCCD")
    private String nationalIdIssuePlace;

    @Schema(description = "Mã số thuế cá nhân")
    private String taxCode;

    @Schema(description = "Mã số BHXH")
    private String socialInsuranceNumber;

    @Schema(description = "Số tài khoản ngân hàng")
    private String bankAccountNumber;

    @Schema(description = "Tên ngân hàng")
    private String bankName;

    @Schema(description = "Chi nhánh ngân hàng")
    private String bankBranch;

    @Schema(description = "Tình trạng hôn nhân")
    private MaritalStatus maritalStatus;

    @Schema(description = "Quốc tịch")
    private String nationality;

    @Schema(description = "Email cá nhân")
    private String personalEmail;

    @Schema(description = "Số điện thoại")
    private String phone;

    @Schema(description = "Địa chỉ thường trú")
    private String permanentAddress;

    @Schema(description = "Địa chỉ tạm trú / hiện tại")
    private String currentAddress;

    @Schema(description = "URL ảnh chân dung")
    private String photoUrl;

    // --- Thông tin công việc 3 chiều (Nested Summary Objects) ---
    @Schema(description = "Thông tin tóm tắt đơn vị / phòng ban")
    private OrgUnitSummary organizationalUnit;

    @Schema(description = "Thông tin tóm tắt chi nhánh")
    private BranchSummary branch;

    @Schema(description = "Thông tin tóm tắt vị trí / chức danh")
    private PositionSummary position;

    @Schema(description = "Thông tin tóm tắt cấp bậc công việc")
    private JobLevelSummary jobLevel;

    @Schema(description = "Thông tin tóm tắt người quản lý trực tiếp")
    private EmployeeSummary manager;

    @Schema(description = "Loại hợp đồng")
    private EmploymentType employmentType;

    @Schema(description = "Email công ty", example = "a.nguyen@cyclosa.com")
    private String companyEmail;

    @Schema(description = "Địa điểm làm việc cụ thể")
    private String workLocation;

    @Schema(description = "Ngày hết hạn thử việc")
    private LocalDate probationEndDate;

    // --- Danh sách con ---
    @Schema(description = "Danh sách người phụ thuộc")
    private List<EmployeeDependentResponse> dependents;

    @Schema(description = "Danh sách người liên hệ khẩn cấp")
    private List<EmployeeEmergencyContactResponse> emergencyContacts;

    @Schema(description = "Thời gian tạo hồ sơ")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime updatedAt;
}
