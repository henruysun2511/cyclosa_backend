package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.enums.EmploymentType;
import com.cyclosa.employee.enums.Gender;
import com.cyclosa.employee.enums.MaritalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateEmployeeRequest {

    private UUID companyId;

    /**
     * Mã nhân viên. Nếu để trống hệ thống sẽ tự sinh (EMP-YYYY-XXXX).
     */
    private String employeeCode;

    @NotBlank(message = "Họ và tên nhân viên không được để trống")
    private String fullName;

    @NotNull(message = "Ngày vào làm không được để trống")
    private LocalDate hireDate;

    private EmploymentStatus employmentStatus = EmploymentStatus.PROBATION;

    /**
     * ID tài khoản người dùng liên kết nếu có
     */
    private UUID userId;

    /**
     * Tự động khởi tạo tài khoản User đăng nhập với email công ty
     */
    private boolean autoCreateUser = false;

    // --- Thông tin cá nhân & pháp lý ---
    private Gender gender;
    private LocalDate dateOfBirth;

    @NotBlank(message = "Số CCCD/Hộ chiếu không được để trống")
    private String nationalIdNumber;
    private LocalDate nationalIdIssueDate;
    private String nationalIdIssuePlace;

    private String taxCode;
    private String socialInsuranceNumber;

    private String bankAccountNumber;
    private String bankName;
    private String bankBranch;

    private MaritalStatus maritalStatus = MaritalStatus.SINGLE;
    private String nationality = "Việt Nam";
    private String personalEmail;
    private String phone;
    private String permanentAddress;
    private String currentAddress;
    private String photoUrl;

    // --- Thông tin công việc 3 chiều ---
    @NotNull(message = "Phòng ban / đơn vị tổ chức không được để trống")
    private UUID organizationalUnitId;

    @NotNull(message = "Chi nhánh làm việc không được để trống")
    private UUID branchId;

    @NotNull(message = "Chức danh / vị trí việc làm không được để trống")
    private UUID positionId;

    private UUID jobLevelId;
    private UUID managerEmployeeId;
    private EmploymentType employmentType = EmploymentType.FULL_TIME;
    private String companyEmail;
    private String workLocation;
    private LocalDate probationEndDate;
}
