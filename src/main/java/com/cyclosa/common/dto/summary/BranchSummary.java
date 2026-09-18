package com.cyclosa.common.dto.summary;

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
@Schema(description = "Thông tin tóm tắt chi nhánh")
public class BranchSummary {

    @Schema(description = "ID chi nhánh")
    private UUID id;

    @Schema(description = "Mã chi nhánh", example = "BR_HN")
    private String code;

    @Schema(description = "Tên chi nhánh", example = "Chi nhánh Hà Nội")
    private String name;
}
