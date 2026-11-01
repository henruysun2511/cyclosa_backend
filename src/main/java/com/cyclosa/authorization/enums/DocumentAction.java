package com.cyclosa.authorization.enums;

import lombok.Getter;

@Getter
public enum DocumentAction {
    VIEW("Xem tài liệu"),
    DOWNLOAD("Tải xuống tài liệu"),
    UPLOAD("Tải lên tài liệu"),
    EDIT_METADATA("Chỉnh sửa thông tin tài liệu"),
    VERIFY("Xác minh / Phê duyệt tài liệu"),
    DELETE("Xóa tài liệu");

    private final String description;

    DocumentAction(String description) {
        this.description = description;
    }
}

