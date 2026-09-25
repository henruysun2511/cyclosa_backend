package com.cyclosa.recruitment.exception;

import com.cyclosa.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecruitmentErrorCode implements ErrorCode {
    MANPOWER_REQUEST_NOT_FOUND          (5001, "Không tìm thấy đề xuất tuyển dụng nhân sự", HttpStatus.NOT_FOUND),
    MANPOWER_REQUEST_INVALID_STATUS     (5002, "Trạng thái đề xuất nhân sự không hợp lệ cho thao tác này", HttpStatus.BAD_REQUEST),
    MANPOWER_REQUEST_ALREADY_SUBMITTED  (5003, "Đề xuất nhân sự đã được gửi phê duyệt trước đó", HttpStatus.BAD_REQUEST),
    MANPOWER_REQUEST_NOT_APPROVED       (5004, "Đề xuất nhân sự chưa được phê duyệt, không thể tạo vị trí tuyển dụng", HttpStatus.BAD_REQUEST),

    JOB_POSITION_NOT_FOUND              (5010, "Không tìm thấy vị trí tuyển dụng", HttpStatus.NOT_FOUND),
    JOB_POSITION_INACTIVE               (5011, "Vị trí tuyển dụng hiện không mở tuyển", HttpStatus.BAD_REQUEST),

    JOB_POSTING_NOT_FOUND               (5020, "Không tìm thấy tin đăng tuyển dụng", HttpStatus.NOT_FOUND),
    JOB_POSTING_ALREADY_PUBLISHED       (5021, "Tin đăng tuyển dụng đã được xuất bản", HttpStatus.BAD_REQUEST),

    CANDIDATE_NOT_FOUND                 (5030, "Không tìm thấy thông tin ứng viên", HttpStatus.NOT_FOUND),
    CANDIDATE_EMAIL_EXISTS              (5031, "Email của ứng viên đã tồn tại trong hệ thống tuyển dụng công ty", HttpStatus.CONFLICT),
    CANDIDATE_BLACKLISTED               (5032, "Ứng viên đang nằm trong danh sách đen (Blacklist)", HttpStatus.BAD_REQUEST),

    APPLICATION_NOT_FOUND               (5040, "Không tìm thấy hồ sơ ứng tuyển", HttpStatus.NOT_FOUND),
    APPLICATION_ALREADY_EXISTS          (5041, "Ứng viên đã nộp hồ sơ vào vị trí này và đang được xử lý", HttpStatus.CONFLICT),
    APPLICATION_INVALID_STAGE           (5042, "Giai đoạn hồ sơ ứng tuyển không hợp lệ cho thao tác này", HttpStatus.BAD_REQUEST),
    APPLICATION_NOT_OFFER_ACCEPTED      (5043, "Hồ sơ ứng tuyển chưa chấp nhận Offer, không thể chuyển thành nhân viên", HttpStatus.BAD_REQUEST),
    APPLICATION_ALREADY_HIRED           (5044, "Hồ sơ ứng tuyển này đã được chuyển thành nhân viên trước đó", HttpStatus.BAD_REQUEST),

    INTERVIEW_KIT_NOT_FOUND             (5050, "Không tìm thấy bộ tiêu chí đánh giá phỏng vấn (Interview Kit)", HttpStatus.NOT_FOUND),
    INTERVIEW_QUESTION_NOT_FOUND        (5051, "Không tìm thấy câu hỏi/tiêu chí phỏng vấn", HttpStatus.NOT_FOUND),

    INTERVIEW_NOT_FOUND                 (5060, "Không tìm thấy lịch phỏng vấn", HttpStatus.NOT_FOUND),
    INTERVIEW_INVALID_STATUS            (5061, "Trạng thái buổi phỏng vấn không hợp lệ cho thao tác này", HttpStatus.BAD_REQUEST),
    INTERVIEWER_NOT_IN_PANEL            (5062, "Người phỏng vấn không thuộc hội đồng phỏng vấn của buổi này", HttpStatus.FORBIDDEN),
    INTERVIEW_EVALUATION_EXISTS         (5063, "Người phỏng vấn đã nộp đánh giá cho buổi phỏng vấn này", HttpStatus.CONFLICT),
    INTERVIEW_EVALUATION_NOT_FOUND      (5064, "Chưa tìm thấy đánh giá phỏng vấn", HttpStatus.NOT_FOUND),

    OFFER_NOT_FOUND                     (5070, "Không tìm thấy lời mời nhận việc (Offer)", HttpStatus.NOT_FOUND),
    OFFER_INVALID_STATUS                (5071, "Trạng thái Offer không hợp lệ cho thao tác này", HttpStatus.BAD_REQUEST),
    OFFER_ALREADY_RESPONDED             (5072, "Offer này đã được phản hồi trước đó", HttpStatus.BAD_REQUEST),

    TALENT_POOL_RECORD_NOT_FOUND        (5080, "Không tìm thấy thông tin trong Talent Pool", HttpStatus.NOT_FOUND),
    TALENT_POOL_CANDIDATE_EXISTS        (5081, "Ứng viên đã có trong Talent Pool", HttpStatus.CONFLICT),

    OPPORTUNITY_NOT_FOUND               (5090, "Không tìm thấy cơ hội nội bộ", HttpStatus.NOT_FOUND),
    OPPORTUNITY_INACTIVE                (5091, "Cơ hội nội bộ này hiện không còn mở ứng tuyển", HttpStatus.BAD_REQUEST),
    INTERNAL_APPLICATION_EXISTS         (5092, "Bạn đã nộp đơn bày tỏ quan tâm vào cơ hội này rồi", HttpStatus.CONFLICT),
    INTERNAL_APPLICATION_NOT_FOUND      (5093, "Không tìm thấy đơn ứng tuyển nội bộ", HttpStatus.NOT_FOUND),
    INTERNAL_ASSIGNMENT_NOT_FOUND       (5094, "Không tìm thấy nhiệm vụ phân công nội bộ", HttpStatus.NOT_FOUND),
    INTERNAL_ASSIGNMENT_COMPLETED       (5095, "Nhiệm vụ phân công nội bộ đã hoàn thành hoặc kết thúc", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
