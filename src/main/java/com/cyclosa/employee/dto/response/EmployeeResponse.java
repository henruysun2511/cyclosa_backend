package com.cyclosa.employee.dto.response;

import com.cyclosa.common.dto.summary.BranchSummary;
import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.enums.EmploymentType;
import com.cyclosa.employee.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Phản hồi danh sách nhân viên (Chiến lược tóm tắt đối tượng lồng nhau)")
public class EmployeeResponse {

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

    // Trích xuất từ Personal Info
    @Schema(description = "Giới tính")
    private Gender gender;

    @Schema(description = "Ngày sinh")
    private LocalDate dateOfBirth;

    @Schema(description = "Số điện thoại cá nhân")
    private String phone;

    @Schema(description = "Email cá nhân")
    private String personalEmail;

    @Schema(description = "URL ảnh chân dung đại diện")
    private String photoUrl;

    // Trích xuất từ Employment Info (Nested Summary Objects)
    @Schema(description = "Thông tin tóm tắt đơn vị / phòng ban")
    private OrgUnitSummary organizationalUnit;

    @Schema(description = "Thông tin tóm tắt chi nhánh")
    private BranchSummary branch;

    @Schema(description = "Thông tin tóm tắt chức danh / vị trí")
    private PositionSummary position;

    @Schema(description = "Thông tin tóm tắt người quản lý trực tiếp")
    private EmployeeSummary manager;

    @Schema(description = "Loại hợp đồng lao động")
    private EmploymentType employmentType;

    @Schema(description = "Email công ty", example = "a.nguyen@cyclosa.com")
    private String companyEmail;

    @Schema(description = "Thời gian tạo hồ sơ")
    private LocalDateTime createdAt;
}
