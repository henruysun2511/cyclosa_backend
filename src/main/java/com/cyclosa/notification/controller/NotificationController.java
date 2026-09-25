package com.cyclosa.notification.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.notification.dto.request.CreateNotificationRequest;
import com.cyclosa.notification.dto.request.NotificationFilter;
import com.cyclosa.notification.dto.response.NotificationResponse;
import com.cyclosa.notification.dto.response.UnreadCountResponse;
import com.cyclosa.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Quản lý hộp thư thông báo người dùng")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping({"", "/my"})
    @PreAuthorize("@perm.has('notification.view')")
    @RequirePermission("notification.view")
    @Operation(summary = "Xem danh sách thông báo", description = "Lấy danh sách thông báo của người dùng hiện tại (hỗ trợ tìm kiếm từ khóa, phân trang và lọc theo trạng thái/loại thông báo)")
    public ResponseEntity<ApiResponse<PageData<NotificationResponse>>> getMyNotifications(
            @Valid NotificationFilter filter
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getNotifications(currentUserId, filter),
                "Lấy danh sách thông báo thành công"
        ));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("@perm.has('notification.view')")
    @RequirePermission("notification.view")
    @Operation(summary = "Lấy số lượng thông báo chưa đọc", description = "Đếm số lượng thông báo chưa đọc của người dùng hiện tại để hiển thị huy hiệu (badge)")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getUnreadCount(currentUserId),
                "Lấy số lượng thông báo chưa đọc thành công"
        ));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("@perm.has('notification.view')")
    @RequirePermission("notification.view")
    @Operation(summary = "Đánh dấu đã đọc", description = "Đánh dấu một thông báo cụ thể là đã đọc")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable UUID id
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.markAsRead(id, currentUserId),
                "Đã đánh dấu thông báo là đã đọc"
        ));
    }

    @PutMapping("/read-all")
    @PreAuthorize("@perm.has('notification.view')")
    @RequirePermission("notification.view")
    @Operation(summary = "Đánh dấu tất cả đã đọc", description = "Đánh dấu toàn bộ thông báo chưa đọc của người dùng là đã đọc")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.ok(ApiResponse.noContent("Đã đánh dấu tất cả thông báo là đã đọc"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('notification.view')")
    @RequirePermission("notification.view")
    @Operation(summary = "Xóa thông báo", description = "Xóa thông báo khỏi hộp thư của người dùng")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable UUID id
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        notificationService.deleteNotification(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.noContent("Đã xóa thông báo thành công"));
    }

    @PostMapping("/send")
    @PreAuthorize("@perm.has('notification.manage')")
    @RequirePermission("notification.manage")
    @Operation(summary = "Gửi thông báo", description = "Gửi thông báo trực tiếp tới người dùng (dành cho Admin hoặc hệ thống nội bộ)")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                notificationService.send(request),
                "Gửi thông báo thành công"
        ));
    }
}
