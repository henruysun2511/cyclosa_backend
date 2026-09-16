package com.cyclosa.organization.dto.response;

import com.cyclosa.common.enums.ActiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(description = "Thông tin trung tâm chi phí")
public class CostCenterResponse {
    private UUID id;
    private UUID companyId;
    private String code;
    private String name;
    private String description;
    private ActiveStatus status;
}
