package com.cyclosa.organization.dto.response;

import com.cyclosa.common.enums.ActiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(description = "Thông tin chi tiết công ty")
public class CompanyResponse {
    private UUID id;
    private String code;
    private String name;
    private String taxCode;
    private String email;
    private String phone;
    private String address;
    private String logoUrl;
    private ActiveStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
