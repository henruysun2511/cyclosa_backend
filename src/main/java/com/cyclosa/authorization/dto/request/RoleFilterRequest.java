package com.cyclosa.authorization.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Bộ lọc và phân trang danh sách vai trò")
public class RoleFilterRequest extends BaseFilterRequest {

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "code", "isSystemRole", "createdAt"
    );

    @Parameter(description = "Lọc theo pháp nhân công ty (để trống để lấy vai trò hệ thống hoặc vai trò toàn bộ)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID companyId;

    @Parameter(description = "Lọc vai trò hệ thống mặc định (true) hoặc vai trò tùy chỉnh (false)")
    private Boolean isSystemRole;
}

