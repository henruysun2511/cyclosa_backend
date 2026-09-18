package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.enums.FamilyRelationship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateDependentRequest {

    @NotBlank(message = "Họ tên người phụ thuộc không được để trống")
    private String fullName;

    @NotNull(message = "Mối quan hệ không được để trống")
    private FamilyRelationship relationship;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dateOfBirth;

    private String nationalIdNumber;

    private boolean taxDeductionRegistered = true;
}
