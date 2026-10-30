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

    public static AppException unauthorized(String message) {
        return new AppException(CommonErrorCode.UNAUTHORIZED, message);
    }

    public static AppException internal(String message) {
        return new AppException(CommonErrorCode.INTERNAL_ERROR, message);
    }
}
