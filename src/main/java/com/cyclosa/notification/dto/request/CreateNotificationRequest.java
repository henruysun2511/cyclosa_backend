package com.cyclosa.notification.dto.request;

import com.cyclosa.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {

    @NotNull(message = "ID người nhận không được để trống")
    private UUID userId;

    @NotBlank(message = "Tiêu đề thông báo không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    private String content;

    @Builder.Default
    private NotificationType type = NotificationType.SYSTEM;

    private String actionUrl;

    private String relatedEntityType;

    private UUID relatedEntityId;
}
