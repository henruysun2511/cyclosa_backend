package com.cyclosa.discipline.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.discipline.enums.DisciplineStatus;
import com.cyclosa.discipline.enums.DisciplineType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc tìm kiếm hồ sơ kỷ luật")
public class DisciplineFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo công ty")
    private UUID companyId;

    @Schema(description = "Lọc theo nhân viên bị kỷ luật")
    private UUID employeeId;

    @Schema(description = "Lọc theo trạng thái kỷ luật")
    private DisciplineStatus status;

    @Schema(description = "Lọc theo hình thức kỷ luật")
    private DisciplineType disciplineType;
}
