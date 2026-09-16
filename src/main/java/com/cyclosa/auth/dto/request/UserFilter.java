package com.cyclosa.auth.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.common.enums.UserStatus;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Tham số tìm kiếm và phân trang danh sách tài khoản người dùng")
public class UserFilter extends BaseFilterRequest {

    @Parameter(description = "Trạng thái tài khoản (ACTIVE, PENDING_ACTIVATION, LOCKED, DEACTIVATED)")
    private UserStatus status;
}
