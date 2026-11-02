package com.cyclosa.organization.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin phân tích tác động khi điều chuyển hoặc giải thể phòng ban")
public class DepartmentImpactResponse {

    @Schema(description = "ID phòng ban cần tác động")
    private UUID departmentId;

    @Schema(description = "Tên phòng ban")
    private String departmentName;

    @Schema(description = "ID phòng ban cha dự kiến chuyển tới")
    private UUID targetParentDepartmentId;

    @Schema(description = "Tên phòng ban cha dự kiến")
    private String targetParentDepartmentName;

    @Schema(description = "Thao tác có hợp lệ không (false nếu tạo thành vòng lặp hoặc vi phạm ràng buộc)")
    private Boolean isValid;

    @Schema(description = "Lý do nếu thao tác không hợp lệ")
    private String invalidReason;

    @Schema(description = "Tổng số phòng ban con sẽ bị di chuyển cùng theo nhánh")
    @Builder.Default
    private Integer affectedSubDepartmentCount = 0;

    @Schema(description = "Tổng số nhân sự trực tiếp và gián tiếp bị ảnh hưởng")
    @Builder.Default
    private Integer affectedEmployeeCount = 0;

    @Schema(description = "Danh sách ID các phòng ban con bị cuốn theo")
    @Builder.Default
    private List<UUID> affectedDepartmentIds = new ArrayList<>();
}

