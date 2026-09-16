package com.cyclosa.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Interface chuẩn cho toàn bộ mã lỗi (Error Code) trong hệ thống CYCLOSA.
 * Mỗi module tự định nghĩa enum mã lỗi riêng implement interface này.
 */
public interface ErrorCode {

    int getCode();

    String getMessage();

    HttpStatus getHttpStatus();
}
