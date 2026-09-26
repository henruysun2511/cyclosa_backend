package com.cyclosa.onboarding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo mới mẫu danh sách Onboarding")
public class CreateChecklistTemplateRequest {

    @Schema(description = "ID công ty (nếu rỗng lấy công ty hiện tại)")
    private UUID companyId;

    @NotBlank(message = "Tên mẫu checklist không được để trống")
    @Size(max = 200, message = "Tên mẫu checklist tối đa 200 ký tự")
    @Schema(description = "Tên mẫu checklist", example = "Onboarding chuẩn cho Kỹ sư phần mềm")
    private String name;

    @Schema(description = "ID chức danh công việc áp dụng (nếu có)")
    private UUID applicablePositionId;

    @Schema(description = "ID phòng ban áp dụng (nếu có)")
    private UUID applicableDepartmentId;

    @Schema(description = "Mô tả chi tiết mục tiêu mẫu checklist")
    private String description;

    @Valid
    @Builder.Default
    @Schema(description = "Danh sách hạng mục công việc mẫu ban đầu")
    private List<CreateTemplateItemRequest> items = new ArrayList<>();
}
