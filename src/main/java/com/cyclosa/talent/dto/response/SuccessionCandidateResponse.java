package com.cyclosa.talent.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.talent.enums.SuccessionReadiness;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin ứng viên kế nhiệm")
public class SuccessionCandidateResponse {

    @Schema(description = "ID ứng viên trong kế hoạch")
    private UUID id;

    @Schema(description = "ID kế hoạch kế nhiệm")
    private UUID successionPlanId;

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "Thông tin tóm tắt nhân viên")
    private EmployeeSummary employee;

    @Schema(description = "Mức độ sẵn sàng đảm nhiệm")
    private SuccessionReadiness readiness;

    @Schema(description = "Ghi chú đánh giá")
    private String note;

    @Schema(description = "Thời điểm thêm")
    private LocalDateTime createdAt;
}
