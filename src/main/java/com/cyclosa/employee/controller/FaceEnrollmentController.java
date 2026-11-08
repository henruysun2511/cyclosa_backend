package com.cyclosa.employee.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.employee.dto.request.EnrollFaceRequest;
import com.cyclosa.employee.dto.response.FaceEnrollmentResponse;
import com.cyclosa.employee.service.FaceEnrollmentService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Biometric Face Enrollment", description = "Quản lý dữ liệu sinh trắc khuôn mặt nhận diện chấm công")
public class FaceEnrollmentController {

    private final FaceEnrollmentService faceEnrollmentService;

    @GetMapping("/employees/{employeeId}/face-enrollments")
    @PreAuthorize("@perm.has('employee.face_manage')")
    @RequirePermission("employee.face_manage")
    @Operation(summary = "Xem thông tin mẫu sinh trắc khuôn mặt của nhân viên")
    public ResponseEntity<ApiResponse<FaceEnrollmentResponse>> getFaceEnrollment(@PathVariable UUID employeeId) {
        FaceEnrollmentResponse result = faceEnrollmentService.getFaceEnrollment(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin sinh trắc khuôn mặt thành công"));
    }

    @PostMapping("/employees/{employeeId}/face-enrollments")
    @PreAuthorize("@perm.has('employee.face_manage')")
    @RequirePermission("employee.face_manage")
    @Operation(summary = "Đăng ký mẫu nhận diện khuôn mặt cho nhân viên")
    public ResponseEntity<ApiResponse<FaceEnrollmentResponse>> enrollFace(
            @PathVariable UUID employeeId,
            @Valid @RequestBody EnrollFaceRequest request) {
        FaceEnrollmentResponse result = faceEnrollmentService.enrollFace(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Đăng ký mẫu sinh trắc khuôn mặt thành công"));
    }

    @PostMapping("/face-enrollments/{enrollmentId}/revoke")
    @PreAuthorize("@perm.has('employee.face_manage')")
    @RequirePermission("employee.face_manage")
    @Operation(summary = "Thu hồi mẫu sinh trắc khuôn mặt của nhân viên")
    public ResponseEntity<ApiResponse<Void>> revokeFaceEnrollment(
            @PathVariable UUID enrollmentId,
            @RequestParam(required = false) String reason) {
        faceEnrollmentService.revokeFaceEnrollment(enrollmentId, reason);
        return ResponseEntity.ok(ApiResponse.ok(null, "Thu hồi mẫu sinh trắc thành công"));
    }

    @GetMapping("/me/face-enrollment")
    @Operation(summary = "Xem trạng thái đăng ký khuôn mặt của tôi")
    public ResponseEntity<ApiResponse<FaceEnrollmentResponse>> getMyFaceEnrollment() {
        FaceEnrollmentResponse result = faceEnrollmentService.getMyFaceEnrollment();
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin sinh trắc khuôn mặt của tôi thành công"));
    }

    @PostMapping("/me/face-enrollment")
    @Operation(summary = "Nhân viên tự đăng ký khuôn mặt qua ứng dụng di động/webcam")
    public ResponseEntity<ApiResponse<FaceEnrollmentResponse>> enrollMyFace(
            @Valid @RequestBody EnrollFaceRequest request) {
        FaceEnrollmentResponse result = faceEnrollmentService.enrollMyFace(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Đăng ký khuôn mặt thành công"));
    }

    @DeleteMapping("/me/face-enrollment")
    @Operation(summary = "Nhân viên yêu cầu thu hồi và xóa mẫu sinh trắc khuôn mặt của mình")
    public ResponseEntity<ApiResponse<Void>> revokeMyFaceEnrollment() {
        faceEnrollmentService.revokeMyFaceEnrollment();
        return ResponseEntity.ok(ApiResponse.ok(null, "Yêu cầu xóa dữ liệu sinh trắc thành công"));
    }
}

