package com.cyclosa.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJobPositionRequest {
    private UUID companyId;
    @NotBlank(message = "Tiêu đề vị trí tuyển dụng không được để trống")
    private String title;
    @NotNull(message = "Chức danh tham chiếu không được để trống")
    private UUID positionId;
    @NotNull(message = "Phòng ban tuyển dụng không được để trống")
    private UUID departmentId;
    private UUID manpowerRequestId;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    @NotBlank(message = "Mô tả công việc (JD) không được để trống")
    private String description;
    private String requirements;
    private String benefits;
}
