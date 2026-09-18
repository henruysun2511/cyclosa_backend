package com.cyclosa.organization.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.JobLevelSummary;
import com.cyclosa.common.enums.ActiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tóm tắt vị trí chức danh trong danh sách")
public class PositionResponse {
    private UUID id;
    private CompanySummary company;
    private String code;
    private String name;
    private JobLevelSummary jobLevel;
    private ActiveStatus status;
}
