package com.cyclosa.leave.dto.response;

import com.cyclosa.leave.enums.FundingSource;
import com.cyclosa.leave.enums.LeaveCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin loại ngày nghỉ")
public class LeaveTypeResponse {

    private UUID id;
    private UUID companyId;
    private String name;
    private String code;
    private LeaveCategory category;
    private FundingSource fundingSource;
    private BigDecimal fixedDaysPerEvent;
    private Boolean isPaid;
    private Boolean requiresApproval;
    private Boolean isActive;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
