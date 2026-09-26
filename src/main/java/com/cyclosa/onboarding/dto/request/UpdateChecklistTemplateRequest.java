package com.cyclosa.onboarding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật mẫu danh sách Onboarding")
public class UpdateChecklistTemplateRequest {

    @Size(max = 200, message = "Tên mẫu checklist tối đa 200 ký tự")
    @Schema(description = "Tên mẫu checklist")
    private String name;

    @Schema(description = "ID chức danh công việc áp dụng")
    private UUID applicablePositionId;

    @Schema(description = "ID phòng ban áp dụng")
    private UUID applicableDepartmentId;

    @Schema(description = "Mô tả chi tiết")
    private String description;

    @Schema(description = "Trạng thái kích hoạt")
    private Boolean isActive;
}
