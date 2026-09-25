package com.cyclosa.recruitment.dto.request;

import com.cyclosa.employee.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvertToEmployeeRequest {

    private UUID branchId;

    private UUID jobLevelId;

    private UUID managerEmployeeId;

    @NotBlank(message = "Số CCCD/Hộ chiếu không được để trống khi tiếp nhận nhân viên")
    private String nationalIdNumber;

    private LocalDate dateOfBirth;

    private Gender gender;

    private String permanentAddress;

    private String currentAddress;

    private String companyEmail;

    private LocalDate hireDate;

    @Builder.Default
    private boolean autoCreateUser = true;

    private String notes;
}
