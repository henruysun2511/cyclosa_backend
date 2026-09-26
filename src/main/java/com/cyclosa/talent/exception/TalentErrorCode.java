package com.cyclosa.talent.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của Module 14: Phát triển Nhân sự & Quy hoạch Kế nhiệm (14001 - 14020).
 */
@Getter
@RequiredArgsConstructor
public enum TalentErrorCode implements ErrorCode {

    CAREER_PATH_NOT_FOUND               (14001, "Không tìm thấy lộ trình thăng tiến được chỉ định", HttpStatus.NOT_FOUND),
    CAREER_PATH_ALREADY_EXISTS          (14002, "Lộ trình thăng tiến giữa hai vị trí này đã tồn tại", HttpStatus.CONFLICT),
    SAME_FROM_TO_POSITION               (14003, "Vị trí xuất phát và vị trí đích không được trùng nhau", HttpStatus.BAD_REQUEST),
    SUCCESSION_PLAN_NOT_FOUND           (14004, "Không tìm thấy kế hoạch kế nhiệm", HttpStatus.NOT_FOUND),
    SUCCESSION_PLAN_ALREADY_EXISTS      (14005, "Vị trí này đã được thiết lập kế hoạch kế nhiệm", HttpStatus.CONFLICT),
    SUCCESSION_CANDIDATE_NOT_FOUND      (14006, "Không tìm thấy ứng viên kế nhiệm trong kế hoạch", HttpStatus.NOT_FOUND),
    CANDIDATE_IS_CURRENT_HOLDER         (14007, "Ứng viên kế nhiệm không được trùng với người đang đảm nhiệm chính vị trí này", HttpStatus.UNPROCESSABLE_ENTITY),
    CANDIDATE_ALREADY_ADDED             (14008, "Nhân viên này đã được thêm vào danh sách ứng viên kế nhiệm của vị trí", HttpStatus.CONFLICT),
    TALENT_POOL_RECORD_NOT_FOUND        (14009, "Không tìm thấy bản ghi nhân sự trong kho nhân tài", HttpStatus.NOT_FOUND),
    EMPLOYEE_ALREADY_IN_TALENT_POOL     (14010, "Nhân viên này đã được gắn thẻ trong kho nhân tài", HttpStatus.CONFLICT),
    SAVED_SIMULATION_NOT_FOUND          (14011, "Không tìm thấy kịch bản mô phỏng lộ trình đã lưu", HttpStatus.NOT_FOUND),
    FORBIDDEN_SIMULATION_ACCESS         (14012, "Bạn không có quyền truy cập hoặc thao tác trên kịch bản mô phỏng này", HttpStatus.FORBIDDEN),
    CURRENT_USER_NOT_LINKED_EMPLOYEE    (14013, "Tài khoản hiện tại chưa được liên kết với hồ sơ nhân viên trong hệ thống", HttpStatus.BAD_REQUEST),
    POSITION_NOT_FOUND                  (14014, "Không tìm thấy thông tin vị trí / chức danh trong tổ chức", HttpStatus.NOT_FOUND),
    EMPLOYEE_NOT_FOUND                  (14015, "Không tìm thấy thông tin hồ sơ nhân viên", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
