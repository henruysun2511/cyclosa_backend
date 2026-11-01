package com.cyclosa.authorization.dto.response;

import com.cyclosa.common.enums.DataScope;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quyền hiệu lực cuối cùng của người dùng kèm phạm vi dữ liệu rộng nhất")
public class EffectivePermissionResponse {

    @Schema(description = "Mã quyền chức năng", example = "leave.approve")
    private String permissionCode;

    @Schema(description = "Phạm vi dữ liệu cao nhất sau khi gộp vai trò kiêm nhiệm", example = "DEPARTMENT")
    private DataScope highestDataScope;
}

