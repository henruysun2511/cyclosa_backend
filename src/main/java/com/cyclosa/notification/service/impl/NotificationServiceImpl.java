package com.cyclosa.notification.service.impl;

import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.notification.dto.request.CreateNotificationRequest;
import com.cyclosa.notification.dto.request.NotificationFilter;
import com.cyclosa.notification.dto.response.NotificationResponse;
import com.cyclosa.notification.dto.response.UnreadCountResponse;
import com.cyclosa.notification.entity.Notification;
import com.cyclosa.notification.enums.NotificationType;
import com.cyclosa.notification.exception.NotificationErrorCode;
import com.cyclosa.notification.mapper.NotificationMapper;
import com.cyclosa.notification.repository.NotificationRepository;
import com.cyclosa.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final NotificationMapper notificationMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "title", "isRead");

    @Override
    @Transactional
    public NotificationResponse send(CreateNotificationRequest request) {
        if (!userService.existsById(request.getUserId())) {
            log.warn("[NOTIFICATION] Không thể gửi thông báo: Không tìm thấy User ID {}", request.getUserId());
            throw new AppException(NotificationErrorCode.RECIPIENT_USER_NOT_FOUND);
        }

        Notification entity = notificationMapper.toEntity(request);
        Notification saved = notificationRepository.save(entity);
        log.info("[NOTIFICATION] Đã gửi thông báo thành công (id={}, userId={}, title='{}')",
                saved.getId(), saved.getUserId(), saved.getTitle());

        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NotificationResponse send(UUID userId, String title, String content, NotificationType type, String actionUrl) {
        return send(userId, title, content, type, actionUrl, null, null);
    }

    @Override
    @Transactional
    public NotificationResponse send(UUID userId, String title, String content, NotificationType type,
                                     String actionUrl, String relatedEntityType, UUID relatedEntityId) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .type(type != null ? type : NotificationType.SYSTEM)
                .actionUrl(actionUrl)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .build();
        return send(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<NotificationResponse> getNotifications(UUID userId, NotificationFilter filter) {
        String kw = PageableUtils.normalizeKeyword(filter.getKeyword());
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);

        Page<Notification> page = notificationRepository.search(
                userId,
                filter.getIsRead(),
                filter.getType(),
                kw,
                pageable
        );
        return PageData.of(page, notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return new UnreadCountResponse(count);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(userId)) {
            throw new AppException(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        notification.markAsRead();
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        log.info("[NOTIFICATION] User {} đã đánh dấu tất cả thông báo là đã đọc", userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(userId)) {
            throw new AppException(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        notificationRepository.delete(notification);
        log.info("[NOTIFICATION] User {} đã xóa thông báo {}", userId, notificationId);
    }
}
