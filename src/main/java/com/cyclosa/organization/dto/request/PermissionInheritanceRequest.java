package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu bật/tắt kế thừa quyền cho phòng ban")
public class PermissionInheritanceRequest {

    @NotNull(message = "Trạng thái kế thừa quyền không được để trống")
    @Schema(description = "Bật (true) hoặc tắt (false) kế thừa quyền xuống các đơn vị con", example = "true")
    private Boolean isPermissionInherited;
}

