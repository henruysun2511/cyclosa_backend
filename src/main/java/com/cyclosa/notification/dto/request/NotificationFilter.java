package com.cyclosa.notification.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.notification.enums.NotificationType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Tham số tìm kiếm và lọc danh sách thông báo")
public class NotificationFilter extends BaseFilterRequest {

    @Parameter(description = "Lọc theo trạng thái đã đọc (true/false)")
    private Boolean isRead;

    @Parameter(description = "Lọc theo loại thông báo (WORKFLOW, LEAVE, ATTENDANCE, CONTRACT, PAYROLL, ASSET, SYSTEM)")
    private NotificationType type;
}
