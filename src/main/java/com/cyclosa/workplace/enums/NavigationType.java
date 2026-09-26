package com.cyclosa.workplace.enums;

public enum NavigationType {
    SAME_FLOOR,   // Cùng tầng: chạy thuật toán A* tìm đường ngắn nhất
    CROSS_FLOOR,  // Khác tầng: điều hướng qua Thang máy / Thang bộ
    CROSS_SITE    // Khác tòa nhà/chi nhánh: chỉ dẫn địa chỉ cơ sở
}
