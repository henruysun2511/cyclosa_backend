package com.cyclosa.notification.dto.response;

import com.cyclosa.notification.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private String title;
    private String content;
    private NotificationType type;
    private String actionUrl;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
