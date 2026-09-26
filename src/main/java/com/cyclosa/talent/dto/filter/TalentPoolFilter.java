package com.cyclosa.talent.dto.filter;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc tìm kiếm nhân sự trong kho nhân tài")
public class TalentPoolFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo công ty")
    private UUID companyId;

    @Schema(description = "Lọc theo nhân viên")
    private UUID employeeId;

    @Schema(description = "Lọc theo thẻ phân loại (HiPo, Key Talent...)")
    private String tag;
}
