package com.cyclosa.common.exception;

import lombok.Getter;

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

    public static AppException forbidden() {
        return new AppException(ErrorCode.FORBIDDEN);
    }

    public static AppException forbidden(String message) {
        return new AppException(ErrorCode.FORBIDDEN, message);
    }

    public static AppException notFound(String message) {
        return new AppException(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static AppException conflict(String message) {
        return new AppException(ErrorCode.CONFLICT, message);
    }

    public static AppException validationFailed(String message) {
        return new AppException(ErrorCode.VALIDATION_FAILED, message);
    }

    public static AppException unauthorized() {
        return new AppException(ErrorCode.UNAUTHORIZED);
    }

    public static AppException invalidCredentials() {
        return new AppException(ErrorCode.INVALID_CREDENTIALS);
    }

    public static AppException accountDisabled() {
        return new AppException(ErrorCode.ACCOUNT_DISABLED);
    }

    public static AppException refreshTokenInvalid() {
        return new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    public static AppException tokenExpired() {
        return new AppException(ErrorCode.TOKEN_EXPIRED);
    }

    public static AppException tokenInvalid() {
        return new AppException(ErrorCode.TOKEN_INVALID);
    }

    public static AppException tokenRevoked() {
        return new AppException(ErrorCode.TOKEN_REVOKED);
    }

    public static AppException userNotFound(java.util.UUID id) {
        return new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id: " + id);
    }

    public static AppException userEmailExists(String email) {
        return new AppException(ErrorCode.USER_EMAIL_EXISTS, "Email \"" + email + "\" đã tồn tại");
    }

    public static AppException roleNotFound(java.util.UUID id) {
        return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy vai trò với id: " + id);
    }

    public static AppException roleNotFound(String code) {
        return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy vai trò với mã: " + code);
    }

    public static AppException roleCodeExists(String code) {
        return new AppException(ErrorCode.CONFLICT, "Mã vai trò \"" + code + "\" đã tồn tại");
    }

    public static AppException permissionNotFound(java.util.UUID id) {
        return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy quyền với id: " + id);
    }

    public static AppException cannotDeleteSystemRole() {
        return new AppException(ErrorCode.FORBIDDEN, "Không thể xóa vai trò mặc định của hệ thống");
    }

    public static AppException cannotModifySystemRoleCode() {
        return new AppException(ErrorCode.FORBIDDEN, "Không thể sửa mã của vai trò mặc định hệ thống");
    }

    public static AppException roleInUse(long count) {
        return new AppException(ErrorCode.CONFLICT, "Vai trò đang được gán cho " + count + " người dùng, không thể xóa");
    }
}
