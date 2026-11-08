package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.entity.enums.DependentRelationship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDependentRequest {

    @NotBlank(message = "Họ tên người phụ thuộc không được để trống")
    private String fullName;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dateOfBirth;

    @NotNull(message = "Mối quan hệ không được để trống")
    @Builder.Default
    private DependentRelationship relationship = DependentRelationship.OTHER;

    private String identityNumber;
    private LocalDate taxDeductionFrom;
    private LocalDate taxDeductionTo;
}

