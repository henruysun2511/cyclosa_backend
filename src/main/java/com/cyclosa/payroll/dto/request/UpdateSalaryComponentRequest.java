package com.cyclosa.payroll.dto.request;

import com.cyclosa.payroll.enums.ComponentType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu cập nhật thành phần lương")
public class UpdateSalaryComponentRequest {

    @Size(max = 200, message = "Tên thành phần lương tối đa 200 ký tự")
    @Schema(description = "Tên thành phần", example = "Phụ cấp ăn trưa văn phòng")
    private String name;

    @Schema(description = "Loại thành phần lương")
    private ComponentType componentType;

    @Schema(description = "Có chịu thuế TNCN hay không")
    private Boolean isTaxable;

    @Schema(description = "Có tính vào căn cứ đóng BHXH hay không")
    private Boolean isInsuranceBase;

    @Schema(description = "Khoản mục định kỳ hàng tháng hay không")
    private Boolean isRecurring;

    @Schema(description = "Mức tiền mặc định")
    private BigDecimal defaultAmount;
}
