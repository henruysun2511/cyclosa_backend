package com.cyclosa.common.exception;

import com.cyclosa.auth.exception.AuthErrorCode;
import com.cyclosa.role.exception.RoleErrorCode;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    // ==========================================
    // Common Exceptions
    // ==========================================
    public static AppException forbidden() {
        return new AppException(CommonErrorCode.FORBIDDEN);
    }

    public static AppException forbidden(String message) {
        return new AppException(CommonErrorCode.FORBIDDEN, message);
    }

    public static AppException notFound(String message) {
        return new AppException(CommonErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static AppException conflict(String message) {
        return new AppException(CommonErrorCode.CONFLICT, message);
    }

    public static AppException validationFailed(String message) {
        return new AppException(CommonErrorCode.VALIDATION_FAILED, message);
    }

    public static AppException badRequest(String message) {
        return new AppException(CommonErrorCode.VALIDATION_FAILED, message);
    }

    public static AppException unauthorized() {
        return new AppException(CommonErrorCode.UNAUTHORIZED);
    }

    // ==========================================
    // Auth & User Exceptions
    // ==========================================
    public static AppException invalidCredentials() {
        return new AppException(AuthErrorCode.INVALID_CREDENTIALS);
    }

    public static AppException accountDisabled() {
        return new AppException(AuthErrorCode.ACCOUNT_DISABLED);
    }

    public static AppException accountPendingActivation() {
        return new AppException(AuthErrorCode.ACCOUNT_PENDING_ACTIVATION);
    }

    public static AppException accountLocked() {
        return new AppException(AuthErrorCode.ACCOUNT_LOCKED);
    }

    public static AppException activationTokenInvalid() {
        return new AppException(AuthErrorCode.ACTIVATION_TOKEN_INVALID);
    }

    public static AppException refreshTokenInvalid() {
        return new AppException(AuthErrorCode.REFRESH_TOKEN_INVALID);
    }

    public static AppException tokenExpired() {
        return new AppException(AuthErrorCode.TOKEN_EXPIRED);
    }

    public static AppException tokenInvalid() {
        return new AppException(AuthErrorCode.TOKEN_INVALID);
    }

    public static AppException tokenRevoked() {
        return new AppException(AuthErrorCode.TOKEN_REVOKED);
    }

    public static AppException userNotFound(UUID id) {
        return new AppException(AuthErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id: " + id);
    }

    public static AppException userEmailExists(String email) {
        return new AppException(AuthErrorCode.USER_EMAIL_EXISTS, "Email \"" + email + "\" đã tồn tại");
    }

    // ==========================================
    // Role & Permission Exceptions
    // ==========================================
    public static AppException roleNotFound(UUID id) {
        return new AppException(RoleErrorCode.ROLE_NOT_FOUND, "Không tìm thấy vai trò với id: " + id);
    }

    public static AppException roleNotFound(String code) {
        return new AppException(RoleErrorCode.ROLE_NOT_FOUND, "Không tìm thấy vai trò với mã: " + code);
    }

    public static AppException roleCodeExists(String code) {
        return new AppException(RoleErrorCode.ROLE_CODE_EXISTS, "Mã vai trò \"" + code + "\" đã tồn tại");
    }

    public static AppException permissionNotFound(UUID id) {
        return new AppException(RoleErrorCode.PERMISSION_NOT_FOUND, "Không tìm thấy quyền với id: " + id);
    }

    public static AppException cannotDeleteSystemRole() {
        return new AppException(RoleErrorCode.CANNOT_DELETE_SYSTEM_ROLE);
    }

    public static AppException cannotModifySystemRoleCode() {
        return new AppException(RoleErrorCode.CANNOT_MODIFY_SYSTEM_ROLE_CODE);
    }

    public static AppException roleInUse(long count) {
        return new AppException(RoleErrorCode.ROLE_IN_USE, "Vai trò đang được gán cho " + count + " người dùng, không thể xóa");
    }
}
