package com.cyclosa.onboarding.dto.request;

import com.cyclosa.onboarding.enums.OnboardingItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật hạng mục trong mẫu Onboarding")
public class UpdateTemplateItemRequest {

    @Size(max = 250, message = "Tiêu đề hạng mục tối đa 250 ký tự")
    @Schema(description = "Tiêu đề công việc")
    private String title;

    @Schema(description = "Mô tả hướng dẫn thực hiện")
    private String description;

    @Schema(description = "Phân loại hạng mục")
    private OnboardingItemCategory category;

    @Schema(description = "Thứ tự sắp xếp")
    private Integer orderIndex;

    @Schema(description = "Bắt buộc hoàn thành hay không")
    private Boolean isRequired;
}
