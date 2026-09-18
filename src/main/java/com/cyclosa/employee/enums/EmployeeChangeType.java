package com.cyclosa.employee.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmployeeChangeType {
    HIRE("Tiếp nhận nhân sự mới"),
    POSITION_CHANGE("Thay đổi vị trí / Bổ nhiệm chức danh"),
    DEPARTMENT_CHANGE("Điều chuyển phòng ban / đơn vị"),
    BRANCH_CHANGE("Điều chuyển chi nhánh / địa bàn"),
    MANAGER_CHANGE("Thay đổi người quản lý trực tiếp"),
    STATUS_CHANGE("Thay đổi trạng thái làm việc"),
    SALARY_CHANGE("Điều chỉnh lương / chế độ đãi ngộ"),
    OTHER("Biến động khác");

    private final String description;
}
