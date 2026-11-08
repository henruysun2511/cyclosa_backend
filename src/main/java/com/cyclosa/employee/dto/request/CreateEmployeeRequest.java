package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.entity.enums.EmploymentType;
import com.cyclosa.employee.entity.enums.Gender;
import com.cyclosa.employee.entity.enums.IdentityDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {

    @NotNull(message = "Công ty không được để trống")
    private UUID companyId;

    @NotBlank(message = "Họ và tên đệm không được để trống")
    @Size(max = 100, message = "Họ và tên đệm tối đa 100 ký tự")
    private String lastName;

    @NotBlank(message = "Tên nhân viên không được để trống")
    @Size(max = 50, message = "Tên nhân viên tối đa 50 ký tự")
    private String firstName;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dateOfBirth;

    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 30, message = "Số điện thoại tối đa 30 ký tự")
    private String phone;

    private String personalEmail;
    private String address;

    @NotNull(message = "Ngày bắt đầu làm việc không được để trống")
    private LocalDate startDate;

    private LocalDate probationEndDate;
    private String personalTaxCode;
    private String socialInsuranceNumber;
    private String portraitUrl;
    private UUID sourceApplicationId;

    // Giấy tờ định danh ban đầu
    private IdentityDocumentType identityDocumentType;
    private String identityDocumentNumber;
    private LocalDate identityIssueDate;
    private String identityIssuePlace;
    private LocalDate identityExpiryDate;

    // Bộ 3 chiều công tác ban đầu
    @NotNull(message = "Phòng ban trực thuộc không được để trống")
    private UUID departmentId;

    @NotNull(message = "Chi nhánh làm việc không được để trống")
    private UUID branchId;

    @NotNull(message = "Chức danh công việc không được để trống")
    private UUID jobTitleId;

    @NotNull(message = "Cấp bậc chức danh không được để trống")
    private UUID jobLevelId;

    private UUID managerEmployeeId;

    @Builder.Default
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    // Tùy chọn cấp tài khoản hệ thống (User)
    @Builder.Default
    private boolean createSystemUser = false;
    private String userEmail;
}

