package com.cyclosa.onboarding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu đánh dấu hoàn thành hạng mục Onboarding")
public class CompleteProcessItemRequest {

    @Schema(description = "ID nhân viên thực hiện hoàn thành (nếu không truyền lấy nhân viên hiện tại)")
    private UUID completedByEmployeeId;

    @Schema(description = "Ghi chú kết quả thực hiện / biên bản")
    private String note;
}
