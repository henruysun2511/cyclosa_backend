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
@Schema(description = "Thông tin tóm tắt công ty / pháp nhân")
public class CompanySummary {

    @Schema(description = "ID công ty", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Mã công ty", example = "CYC_CORP")
    private String code;

    @Schema(description = "Tên công ty", example = "Tập đoàn CYCLOSA")
    private String name;
}
