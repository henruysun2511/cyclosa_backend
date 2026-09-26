package com.cyclosa.onboarding.dto.filter;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.onboarding.enums.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bộ lọc danh sách quy trình Onboarding")
public class OnboardingProcessFilter extends BaseFilterRequest {

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "ID mẫu checklist")
    private UUID checklistTemplateId;

    @Schema(description = "Trạng thái quy trình: IN_PROGRESS, COMPLETED, CANCELLED")
    private OnboardingStatus status;

    @Schema(description = "Từ ngày bắt đầu")
    private LocalDate startDateFrom;

    @Schema(description = "Đến ngày bắt đầu")
    private LocalDate startDateTo;
}
