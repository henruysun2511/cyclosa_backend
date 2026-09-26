package com.cyclosa.workplace.enums;

public enum HotdeskBookingStatus {
    BOOKED,      // Đã đặt trước
    CONFIRMED,   // Xác nhận đặt bàn thành công
    CHECKED_IN,  // Đã check-in có mặt
    CANCELLED,   // Đã hủy bởi nhân viên hoặc quản lý
    EXPIRED      // Quá giờ quy định không check-in, tự động giải phóng
}
