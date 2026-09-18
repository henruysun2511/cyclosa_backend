package com.cyclosa.contract.dto.request;
 
import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.contract.enums.ContractStatus;
import com.cyclosa.contract.enums.ContractType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số tìm kiếm và phân trang hợp đồng lao động")
public class ContractFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "Trạng thái hợp đồng (DRAFT, PENDING_APPROVAL, APPROVED, ACTIVE, EXPIRED, TERMINATED, RENEWED)")
    private ContractStatus status;

    @Schema(description = "Loại hợp đồng (INDEFINITE_TERM, DEFINITE_TERM, PROBATION)")
    private ContractType type;

    @Schema(description = "Ngày bắt đầu từ (yyyy-MM-dd)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateFrom;

    @Schema(description = "Ngày bắt đầu đến (yyyy-MM-dd)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateTo;

    @Schema(description = "Ngày kết thúc từ (yyyy-MM-dd)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDateFrom;

    @Schema(description = "Ngày kết thúc đến (yyyy-MM-dd)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDateTo;
}
