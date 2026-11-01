package com.cyclosa.authorization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu gán vai trò cho người dùng tại một công ty")
public class AssignUserRoleRequest {

    @NotNull(message = "ID vai trò không được để trống")
    @Schema(description = "ID của vai trò cần gán", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID roleId;

    @NotNull(message = "ID công ty không được để trống")
    @Schema(description = "ID công ty nơi vai trò có hiệu lực", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID companyId;
}

