package com.cyclosa.onboarding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu khởi tạo quy trình Onboarding cho nhân viên mới")
public class CreateOnboardingProcessRequest {

    @Schema(description = "ID công ty (tùy chọn, mặc định lấy theo tài khoản)")
    private UUID companyId;

    @NotNull(message = "ID nhân viên không được để trống")
    @Schema(description = "ID hồ sơ nhân viên", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID employeeId;

    @NotNull(message = "ID mẫu checklist không được để trống")
    @Schema(description = "ID mẫu checklist áp dụng", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
    private UUID checklistTemplateId;

    @Schema(description = "Ngày bắt đầu tiến trình Onboarding (mặc định hôm nay)")
    private LocalDate startDate;

    @Schema(description = "Ghi chú thêm về quy trình Onboarding")
    private String notes;
}
