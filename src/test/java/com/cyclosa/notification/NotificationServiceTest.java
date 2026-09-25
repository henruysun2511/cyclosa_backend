package com.cyclosa.notification;

import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.notification.dto.request.CreateNotificationRequest;
import com.cyclosa.notification.dto.request.NotificationFilter;
import com.cyclosa.notification.dto.response.NotificationResponse;
import com.cyclosa.notification.dto.response.UnreadCountResponse;
import com.cyclosa.notification.entity.Notification;
import com.cyclosa.notification.enums.NotificationType;
import com.cyclosa.notification.exception.NotificationErrorCode;
import com.cyclosa.notification.mapper.NotificationMapper;
import com.cyclosa.notification.repository.NotificationRepository;
import com.cyclosa.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Gửi thông báo thành công khi user tồn tại")
    void send_Success() {
        CreateNotificationRequest req = CreateNotificationRequest.builder()
                .userId(currentUserId)
                .title("Thông báo mới")
                .content("Nội dung thông báo")
                .type(NotificationType.SYSTEM)
                .build();

        Notification entity = Notification.builder()
                .userId(currentUserId)
                .title("Thông báo mới")
                .content("Nội dung thông báo")
                .type(NotificationType.SYSTEM)
                .build();

        NotificationResponse expectedRes = NotificationResponse.builder()
                .id(UUID.randomUUID())
                .userId(currentUserId)
                .title("Thông báo mới")
                .content("Nội dung thông báo")
                .type(NotificationType.SYSTEM)
                .isRead(false)
                .build();

        when(userService.existsById(currentUserId)).thenReturn(true);
        when(notificationMapper.toEntity(req)).thenReturn(entity);
        when(notificationRepository.save(entity)).thenReturn(entity);
        when(notificationMapper.toResponse(entity)).thenReturn(expectedRes);

        NotificationResponse res = notificationService.send(req);

        assertThat(res).isNotNull();
        assertThat(res.getTitle()).isEqualTo("Thông báo mới");
        verify(notificationRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("Gửi thông báo thất bại khi user không tồn tại -> throw RECIPIENT_USER_NOT_FOUND")
    void send_UserNotFound_ThrowsException() {
        UUID unknownUserId = UUID.randomUUID();
        CreateNotificationRequest req = CreateNotificationRequest.builder()
                .userId(unknownUserId)
                .title("Thông báo")
                .content("Nội dung")
                .build();

        when(userService.existsById(unknownUserId)).thenReturn(false);

        assertThatThrownBy(() -> notificationService.send(req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.RECIPIENT_USER_NOT_FOUND);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tìm kiếm và phân trang danh sách thông báo")
    void getNotifications_Success() {
        NotificationFilter filter = new NotificationFilter();
        filter.setIsRead(false);
        filter.setKeyword("test");

        Notification notif = Notification.builder()
                .userId(currentUserId)
                .title("test title")
                .content("test content")
                .build();

        NotificationResponse responseDto = NotificationResponse.builder()
                .title("test title")
                .build();

        Page<Notification> page = new PageImpl<>(List.of(notif));
        when(notificationRepository.search(eq(currentUserId), eq(false), isNull(), eq("test"), any(Pageable.class)))
                .thenReturn(page);
        when(notificationMapper.toResponse(notif)).thenReturn(responseDto);

        PageData<NotificationResponse> result = notificationService.getNotifications(currentUserId, filter);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getTitle()).isEqualTo("test title");
    }

    @Test
    @DisplayName("Lấy số lượng thông báo chưa đọc")
    void getUnreadCount_Success() {
        when(notificationRepository.countByUserIdAndIsReadFalse(currentUserId)).thenReturn(5L);

        UnreadCountResponse res = notificationService.getUnreadCount(currentUserId);

        assertThat(res.getUnreadCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Đánh dấu đã đọc thành công khi đúng chủ sở hữu")
    void markAsRead_Success() {
        UUID notifId = UUID.randomUUID();
        Notification notif = Notification.builder()
                .userId(currentUserId)
                .title("Tiêu đề")
                .content("Nội dung")
                .isRead(false)
                .build();

        NotificationResponse expectedResponse = NotificationResponse.builder()
                .id(notifId)
                .isRead(true)
                .build();

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));
        when(notificationMapper.toResponse(notif)).thenReturn(expectedResponse);

        NotificationResponse res = notificationService.markAsRead(notifId, currentUserId);

        assertThat(res.getIsRead()).isTrue();
        assertThat(notif.getIsRead()).isTrue();
        assertThat(notif.getReadAt()).isNotNull();
    }

    @Test
    @DisplayName("Đánh dấu đã đọc thất bại khi người khác cố đọc -> ACCESS_DENIED")
    void markAsRead_AccessDenied() {
        UUID notifId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Notification notif = Notification.builder()
                .userId(otherUserId)
                .title("Tiêu đề")
                .content("Nội dung")
                .isRead(false)
                .build();

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));

        assertThatThrownBy(() -> notificationService.markAsRead(notifId, currentUserId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.NOTIFICATION_ACCESS_DENIED);
    }

    @Test
    @DisplayName("Đánh dấu tất cả đã đọc")
    void markAllAsRead_Success() {
        notificationService.markAllAsRead(currentUserId);
        verify(notificationRepository, times(1)).markAllAsReadByUserId(eq(currentUserId), any());
    }

    @Test
    @DisplayName("Xóa thông báo thành công khi đúng chủ sở hữu")
    void deleteNotification_Success() {
        UUID notifId = UUID.randomUUID();
        Notification notif = Notification.builder()
                .userId(currentUserId)
                .title("Tiêu đề")
                .build();

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));

        notificationService.deleteNotification(notifId, currentUserId);

        verify(notificationRepository, times(1)).delete(notif);
    }
}
