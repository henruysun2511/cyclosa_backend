package com.cyclosa.payroll.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.payroll.enums.ComponentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số lọc thành phần lương")
public class SalaryComponentFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "Lọc theo loại thành phần")
    private ComponentType componentType;

    @Schema(description = "Lọc theo tính chịu thuế")
    private Boolean isTaxable;

    @Schema(description = "Lọc theo tính đóng BHXH")
    private Boolean isInsuranceBase;
}
