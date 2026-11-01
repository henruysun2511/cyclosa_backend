package com.cyclosa.media.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi module Quản lý Tệp tin & Đa phương tiện (Media - Dải mã 1300 - 1399)
 */
@Getter
@RequiredArgsConstructor
public enum MediaErrorCode implements ErrorCode {

    FILE_EMPTY              (1301, "Tệp tin tải lên không được để trống", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE          (1302, "Dung lượng tệp tin vượt quá giới hạn tối đa cho phép (5MB)", HttpStatus.PAYLOAD_TOO_LARGE),
    INVALID_FILE_TYPE       (1303, "Định dạng tệp không được hỗ trợ. Chỉ chấp nhận các định dạng ảnh (.jpg, .jpeg, .png, .webp)", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED           (1304, "Tải tệp tin lên hệ thống lưu trữ Cloudinary thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_FAILED           (1305, "Xóa tệp tin trên hệ thống lưu trữ Cloudinary thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    PUBLIC_ID_REQUIRED      (1306, "Mã định danh tệp tin (publicId) không được để trống", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

