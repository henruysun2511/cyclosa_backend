package com.cyclosa.payroll.dto.request;

import com.cyclosa.payroll.enums.ComponentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo mới thành phần lương")
public class CreateSalaryComponentRequest {

    @NotBlank(message = "Mã thành phần lương không được để trống")
    @Size(max = 50, message = "Mã thành phần lương tối đa 50 ký tự")
    @Schema(description = "Mã thành phần", example = "LUNCH_ALLOWANCE")
    private String code;

    @NotBlank(message = "Tên thành phần lương không được để trống")
    @Size(max = 200, message = "Tên thành phần lương tối đa 200 ký tự")
    @Schema(description = "Tên thành phần", example = "Phụ cấp ăn trưa")
    private String name;

    @NotNull(message = "Loại thành phần không được để trống")
    @Schema(description = "Loại thành phần lương", example = "ALLOWANCE")
    private ComponentType componentType;

    @Schema(description = "Có chịu thuế TNCN hay không", example = "false")
    @Builder.Default
    private Boolean isTaxable = true;

    @Schema(description = "Có tính vào căn cứ đóng BHXH hay không", example = "false")
    @Builder.Default
    private Boolean isInsuranceBase = false;

    @Schema(description = "Khoản mục định kỳ hàng tháng hay không", example = "true")
    @Builder.Default
    private Boolean isRecurring = true;

    @Schema(description = "Mức tiền mặc định (nếu có)", example = "730000")
    private BigDecimal defaultAmount;
}
