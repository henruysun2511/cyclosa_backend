package com.cyclosa.workplace.enums;

public enum IssueStatus {
    REPORTED,     // Mới tiếp nhận phản ánh
    OPEN,         // Mở tiếp nhận
    IN_PROGRESS,  // Kỹ thuật viên đang xử lý
    RESOLVED,     // Đã khắc phục xong
    CLOSED,       // Đã đóng sự cố
    CANCELLED     // Đã hủy / thông tin trùng lặp
}
