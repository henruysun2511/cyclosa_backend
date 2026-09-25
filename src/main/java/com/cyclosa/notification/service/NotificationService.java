package com.cyclosa.notification.service;

import com.cyclosa.common.response.PageData;
import com.cyclosa.notification.dto.request.CreateNotificationRequest;
import com.cyclosa.notification.dto.request.NotificationFilter;
import com.cyclosa.notification.dto.response.NotificationResponse;
import com.cyclosa.notification.dto.response.UnreadCountResponse;
import com.cyclosa.notification.enums.NotificationType;

import java.util.UUID;

public interface NotificationService {

    NotificationResponse send(CreateNotificationRequest request);

    NotificationResponse send(UUID userId, String title, String content, NotificationType type, String actionUrl);

    NotificationResponse send(UUID userId, String title, String content, NotificationType type, String actionUrl, String relatedEntityType, UUID relatedEntityId);

    PageData<NotificationResponse> getNotifications(UUID userId, NotificationFilter filter);

    UnreadCountResponse getUnreadCount(UUID userId);

    NotificationResponse markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    void deleteNotification(UUID notificationId, UUID userId);
}
