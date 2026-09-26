package com.cyclosa.onboarding.dto.request;

import com.cyclosa.onboarding.enums.OnboardingItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo hạng mục công việc trong mẫu Onboarding")
public class CreateTemplateItemRequest {

    @NotBlank(message = "Tiêu đề hạng mục không được để trống")
    @Size(max = 250, message = "Tiêu đề hạng mục tối đa 250 ký tự")
    @Schema(description = "Tiêu đề công việc", example = "Nộp bản sao công chứng CCCD và bằng đại học")
    private String title;

    @Schema(description = "Mô tả hướng dẫn thực hiện")
    private String description;

    @NotNull(message = "Phân loại hạng mục không được để trống")
    @Schema(description = "Phân loại hạng mục", example = "DOCUMENT")
    private OnboardingItemCategory category;

    @Builder.Default
    @Schema(description = "Thứ tự sắp xếp", example = "1")
    private Integer orderIndex = 0;

    @Builder.Default
    @Schema(description = "Bắt buộc hoàn thành để chốt Onboarding không", example = "true")
    private Boolean isRequired = true;
}
