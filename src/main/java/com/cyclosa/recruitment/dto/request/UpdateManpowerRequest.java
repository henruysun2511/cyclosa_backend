package com.cyclosa.recruitment.dto.request;

import jakarta.validation.constraints.Min;
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
public class UpdateManpowerRequest {
    @NotNull(message = "Phòng ban đề xuất không được để trống")
    private UUID departmentId;
    @NotNull(message = "Vị trí chức danh không được để trống")
    private UUID positionId;
    @NotNull(message = "Số lượng nhân sự cần tuyển không được để trống")
    @Min(value = 1, message = "Số lượng nhân sự cần tuyển tối thiểu là 1")
    private Integer quantity;
    @NotBlank(message = "Lý do tuyển dụng không được để trống")
    private String reason;
    private LocalDate expectedStartDate;
    private String jobDescription;
    private String notes;
}
