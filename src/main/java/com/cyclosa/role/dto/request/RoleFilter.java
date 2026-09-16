package com.cyclosa.role.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số tìm kiếm và phân trang danh sách vai trò")
public class RoleFilter extends BaseFilterRequest {

    @Parameter(description = "ID công ty — null = chỉ lấy system roles; có giá trị = system + công ty")
    private UUID companyId;

    @Parameter(description = "true = chỉ system roles | false = chỉ custom roles | null = tất cả")
    private Boolean isSystemRole;

    public RoleFilter() {
        setSortBy("name");
        setDirection("asc");
    }
}
