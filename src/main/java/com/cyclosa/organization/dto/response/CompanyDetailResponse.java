package com.cyclosa.organization.dto.response;

import com.cyclosa.common.enums.ActiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết toàn diện của công ty / pháp nhân")
public class CompanyDetailResponse {

    private UUID id;
    private String code;
    private String name;
    private String taxCode;
    private String email;
    private String phone;
    private String address;
    private String logoUrl;
    private ActiveStatus status;

    @Schema(description = "Tổng số chi nhánh trực thuộc")
    private long totalBranches;

    @Schema(description = "Tổng số phòng ban / đơn vị trực thuộc")
    private long totalUnits;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
