package com.cyclosa.auth.dto.request;

import com.cyclosa.auth.enums.UserStatus;
import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Schema(description = "Bộ lọc và phân trang danh sách tài khoản người dùng")
public class UserFilterRequest extends BaseFilterRequest {

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "email", "status", "lastLoginAt", "createdAt"
    );

    @Parameter(description = "Lọc theo trạng thái tài khoản", example = "ACTIVE")
    private UserStatus status;
}

