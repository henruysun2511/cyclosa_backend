package com.cyclosa.discipline.dto.request;

import com.cyclosa.discipline.enums.GrievanceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGrievanceRequest {

    private UUID employeeId;

    private UUID companyId;

    @NotNull(message = "Danh mục khiếu nại không được để trống")
    private GrievanceCategory category;

    @NotBlank(message = "Nội dung khiếu nại không được để trống")
    private String description;
}
