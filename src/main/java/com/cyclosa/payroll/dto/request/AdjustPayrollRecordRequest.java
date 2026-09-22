package com.cyclosa.payroll.dto.request;

import com.cyclosa.payroll.enums.ComponentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu điều chỉnh hoặc bổ sung khoản mục lương")
public class AdjustPayrollRecordRequest {

    @Schema(description = "ID thành phần lương cấu hình sẵn (nếu có)")
    private UUID salaryComponentId;

    @NotBlank(message = "Tên khoản mục không được để trống")
    @Schema(description = "Tên khoản mục", example = "Thưởng đột xuất dự án")
    private String name;

    @NotNull(message = "Loại khoản mục không được để trống")
    @Schema(description = "Loại khoản mục", example = "BONUS")
    private ComponentType componentType;

    @NotNull(message = "Số tiền không được để trống")
    @Schema(description = "Số tiền điều chỉnh", example = "2000000")
    private BigDecimal amount;

    @Schema(description = "Có tính thuế TNCN hay không", example = "true")
    @Builder.Default
    private Boolean isTaxable = true;

    @Schema(description = "Ghi chú lý do điều chỉnh", example = "Thưởng đóng góp vượt chỉ tiêu tháng 10")
    private String note;
}
