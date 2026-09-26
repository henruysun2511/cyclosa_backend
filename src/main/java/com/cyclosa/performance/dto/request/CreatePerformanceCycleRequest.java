package com.cyclosa.performance.dto.request;

import com.cyclosa.performance.enums.PerformanceCycleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreatePerformanceCycleRequest {

    @NotBlank(message = "Tên kỳ đánh giá không được để trống")
    private String name;

    @NotNull(message = "Loại kỳ đánh giá không được để trống")
    private PerformanceCycleType cycleType;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    private UUID companyId;

    private String description;
}
