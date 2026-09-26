package com.cyclosa.workplace.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi riêng của Module 42: Quản lý Sơ đồ Chỗ ngồi & Không gian Làm việc (42001 - 42035).
 */
@Getter
@RequiredArgsConstructor
public enum WorkplaceErrorCode implements ErrorCode {

    SITE_NOT_FOUND                      (42001, "Không tìm thấy thông tin tòa nhà / cơ sở làm việc", HttpStatus.NOT_FOUND),
    SITE_CODE_ALREADY_EXISTS            (42002, "Mã tòa nhà / cơ sở này đã tồn tại trong công ty", HttpStatus.CONFLICT),
    FLOOR_NOT_FOUND                     (42003, "Không tìm thấy thông tin tầng làm việc", HttpStatus.NOT_FOUND),
    FLOOR_ALREADY_EXISTS                (42004, "Tầng này đã tồn tại trong tòa nhà", HttpStatus.CONFLICT),
    ZONE_NOT_FOUND                      (42005, "Không tìm thấy thông tin khu vực làm việc", HttpStatus.NOT_FOUND),
    ROW_NOT_FOUND                       (42006, "Không tìm thấy thông tin dãy bàn", HttpStatus.NOT_FOUND),
    SEAT_NOT_FOUND                      (42007, "Không tìm thấy thông tin chỗ ngồi làm việc", HttpStatus.NOT_FOUND),
    SEAT_CODE_ALREADY_EXISTS            (42008, "Mã chỗ ngồi này đã tồn tại trong công ty", HttpStatus.CONFLICT),
    SEAT_NOT_AVAILABLE                  (42009, "Chỗ ngồi này hiện không khả dụng (đang có người hoặc bảo trì)", HttpStatus.CONFLICT),
    SEAT_ALREADY_OCCUPIED               (42010, "Chỗ ngồi này đã được gán cho một nhân viên khác", HttpStatus.CONFLICT),
    SEAT_NOT_OCCUPIED                   (42011, "Chỗ ngồi này hiện chưa được gán cho nhân viên nào", HttpStatus.BAD_REQUEST),
    SEAT_NOT_HOTDESK                    (42012, "Chỗ ngồi này không phải là loại bàn linh hoạt (hot-desk)", HttpStatus.BAD_REQUEST),
    EMPLOYEE_ALREADY_HAS_SEAT           (42013, "Nhân viên này đã được gán một chỗ ngồi cố định khác", HttpStatus.CONFLICT),
    ASSET_ALREADY_LINKED                (42014, "Tài sản này đã được liên kết với chỗ ngồi", HttpStatus.CONFLICT),
    ASSET_LINK_NOT_FOUND                (42015, "Không tìm thấy liên kết tài sản tại chỗ ngồi này", HttpStatus.NOT_FOUND),
    NON_DESK_OBJECT_NOT_FOUND           (42016, "Không tìm thấy đối tượng tiện ích / phòng họp", HttpStatus.NOT_FOUND),
    NOT_A_MEETING_ROOM                  (42017, "Đối tượng này không phải là phòng họp", HttpStatus.BAD_REQUEST),
    FLOOR_PLAN_NOT_FOUND                (42018, "Không tìm thấy bản vẽ sơ đồ tầng", HttpStatus.NOT_FOUND),
    HOTDESK_BOOKING_NOT_FOUND           (42019, "Không tìm thấy thông tin đặt bàn hot-desk", HttpStatus.NOT_FOUND),
    HOTDESK_ALREADY_BOOKED              (42020, "Bàn này đã được đặt trong ngày được chọn", HttpStatus.CONFLICT),
    HOTDESK_ALREADY_HAS_BOOKING_FOR_DAY (42021, "Bạn đã có lịch đặt bàn hot-desk trong ngày này", HttpStatus.CONFLICT),
    HOTDESK_BOOKING_INVALID_STATUS      (42022, "Trạng thái đặt bàn không hợp lệ để thực hiện thao tác này", HttpStatus.BAD_REQUEST),
    ROOM_BOOKING_NOT_FOUND              (42023, "Không tìm thấy lịch đặt phòng họp", HttpStatus.NOT_FOUND),
    ROOM_BOOKING_TIME_OVERLAPPING       (42024, "Phòng họp đã kín lịch trong khung giờ này", HttpStatus.CONFLICT),
    ROOM_BOOKING_INVALID_TIME           (42025, "Thời gian bắt đầu và kết thúc đặt phòng không hợp lệ", HttpStatus.BAD_REQUEST),
    ROOM_BOOKING_INVALID_STATUS         (42026, "Trạng thái đặt phòng không hợp lệ để thực hiện thao tác", HttpStatus.BAD_REQUEST),
    ISSUE_TICKET_NOT_FOUND              (42027, "Không tìm thấy phiếu báo cáo sự cố cơ sở vật chất", HttpStatus.NOT_FOUND),
    NAVIGATION_POINT_NOT_FOUND          (42028, "Không tìm thấy điểm xuất phát hoặc điểm đến để điều hướng", HttpStatus.NOT_FOUND),
    NAVIGATION_SAME_POINT               (42029, "Điểm xuất phát và điểm đến không được trùng nhau", HttpStatus.BAD_REQUEST),
    QR_CODE_INVALID                     (42030, "Mã QR không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    CURRENT_USER_NOT_LINKED_EMPLOYEE    (42031, "Tài khoản hiện tại chưa được liên kết với hồ sơ nhân viên", HttpStatus.BAD_REQUEST),
    EMPLOYEE_NOT_FOUND                  (42032, "Không tìm thấy thông tin nhân viên", HttpStatus.NOT_FOUND),
    BRANCH_NOT_FOUND                    (42033, "Không tìm thấy thông tin chi nhánh", HttpStatus.NOT_FOUND),
    EMPLOYEE_NOT_SPECIFIED              (42034, "Chưa xác định thông tin nhân viên thực hiện thao tác", HttpStatus.BAD_REQUEST),
    EMPLOYEE_HAS_NO_ASSIGNED_SEAT       (42035, "Nhân viên chưa được gán vị trí chỗ ngồi làm việc cố định", HttpStatus.BAD_REQUEST),
    NAVIGATION_ORIGIN_NOT_FOUND         (42036, "Không xác định được điểm xuất phát để bắt đầu điều hướng", HttpStatus.NOT_FOUND),
    NAVIGATION_DESTINATION_NOT_FOUND    (42037, "Không xác định được điểm đến để điều hướng", HttpStatus.NOT_FOUND),
    SEAT_IN_MAINTENANCE                 (42038, "Chỗ ngồi đang trong trạng thái bảo trì hoặc bị khóa", HttpStatus.CONFLICT),
    SEAT_NOT_AVAILABLE_FOR_HOTDESK      (42039, "Chỗ ngồi này không mở cho việc đặt bàn linh hoạt hotdesk", HttpStatus.BAD_REQUEST),
    HOTDESK_ALREADY_BOOKED_FOR_DATE     (42040, "Bạn đã có lịch đặt bàn hotdesk trong ngày này", HttpStatus.CONFLICT),
    BOOKING_ALREADY_CANCELLED           (42041, "Lịch đặt chỗ này đã bị hủy trước đó", HttpStatus.BAD_REQUEST),
    HOTDESK_CANNOT_CHECKIN              (42042, "Không thể check-in do trạng thái đặt bàn không hợp lệ", HttpStatus.BAD_REQUEST),
    ROOM_NOT_FOUND                      (42043, "Không tìm thấy phòng họp được chỉ định", HttpStatus.NOT_FOUND),
    ROOM_NOT_BOOKABLE                   (42044, "Phòng này không mở cho việc đặt lịch họp", HttpStatus.BAD_REQUEST),
    ROOM_TIME_CONFLICT                  (42045, "Khung giờ đặt phòng họp bị trùng với cuộc họp khác đã xác nhận", HttpStatus.CONFLICT),
    ROOM_CANNOT_CHECKIN                 (42046, "Không thể check-in phòng họp do trạng thái không hợp lệ", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
