package com.cyclosa.organization.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.enums.ActiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin vùng miền kèm danh sách chi nhánh trực thuộc")
public class RegionResponse {
    private UUID id;
    private CompanySummary company;
    private String code;
    private String name;
    private ActiveStatus status;

    @Builder.Default
    private List<BranchResponse> branches = new ArrayList<>();
}
