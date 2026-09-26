package com.cyclosa.workplace.dto.filter;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.workplace.enums.IssueSeverity;
import com.cyclosa.workplace.enums.IssueStatus;
import com.cyclosa.workplace.enums.WorkplaceIssueType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc danh sách sự cố cơ sở vật chất")
public class IssueTicketFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo tầng")
    private UUID floorId;

    @Schema(description = "Lọc theo loại sự cố")
    private WorkplaceIssueType issueType;

    @Schema(description = "Lọc theo mức độ nghiêm trọng")
    private IssueSeverity severity;

    @Schema(description = "Lọc theo trạng thái xử lý")
    private IssueStatus status;

    @Schema(description = "Lọc theo người báo cáo")
    private UUID reportedByEmployeeId;
}
