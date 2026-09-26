# HRM — Tổng hợp Quy trình Nghiệp vụ (Process Inventory)

> Tổng hợp toàn bộ quy trình (process/workflow) xuất hiện trong hệ thống, trích từ `HRM-Mo-Ta-Nghiep-Vu-Chi-Tiet.md` (42 module). Cột **"Qua Workflow?"** cho biết quy trình có định tuyến qua **Module 18 (Workflow Engine)** để duyệt hay không — đây là thông tin quan trọng để backend biết request nào cần gọi `createWorkflowInstance()`.

**Ký hiệu cột "Qua Workflow?":** ✅ Có · ❌ Không (tự động/không cần duyệt) · ⚙️ Tùy điều kiện (chỉ duyệt khi vượt ngưỡng)

---

## 1. Tuyển dụng & Gia nhập

| # | Quy trình | Module | Người khởi tạo | Qua Workflow? | Ghi chú |
|---|---|---|---|---|---|
| 1 | Đề xuất & duyệt Manpower Request | 02 | Trưởng phòng | ✅ | Điều kiện tiên quyết để mở Job Position |
| 2 | Đăng tin tuyển dụng đa kênh | 02 | HR/Recruiter | ❌ | Publish trực tiếp |
| 3 | Sàng lọc CV tự động (AI match score) | 02, 24 | Hệ thống (tự động khi có Application) | ❌ | Gợi ý, không tự quyết định |
| 4 | Lên lịch & tổ chức phỏng vấn (đa vòng, panel) | 2.1 | HR/Recruiter | ❌ | Bản thân lịch không cần duyệt, nhưng… |
| 5 | Chấm điểm phỏng vấn theo Scorecard | 2.1 | Interviewer | ❌ | Tổng hợp điểm panel → HR quyết định tiếp/dừng |
| 6 | Soạn & gửi Offer | 02 | HR/Recruiter | ❌ | Có thể thêm bước duyệt nội bộ nếu công ty yêu cầu |
| 7 | Hiring → tạo Employee tự động | 02 | Hệ thống (khi Offer accepted) | ❌ | Trigger tự động |
| 8 | Onboarding Checklist (hồ sơ, thiết bị, tài khoản, orientation) | 03 | Hệ thống (tự động sau Hiring) | ❌ | Từng item tick thủ công, không qua Workflow |
| 9 | Cấp phát thiết bị khi Onboarding | 03, 13 | IT/HR | ❌ | Gọi trực tiếp Module 13 |
| 10 | Account Provisioning (email, hệ thống) | 03 | IT Admin | ❌ | |

## 2. Hồ sơ, Hợp đồng & Chuyển đổi

| # | Quy trình | Module | Người khởi tạo | Qua Workflow? | Ghi chú |
|---|---|---|---|---|---|
| 11 | Cập nhật hồ sơ nhân viên | 04 | HR/Nhân viên | ❌ | Trừ thay đổi nhạy cảm có thể cấu hình duyệt riêng |
| 12 | Tạo/gia hạn hợp đồng | 05 | HR | ❌ | Nhưng **gia hạn tối đa 1 lần** bị chặn cứng theo luật, không phải do Workflow |
| 13 | Sửa đổi hợp đồng (Amendment) | 05 | HR | ❌ | |
| 14 | Chấm dứt hợp đồng (Termination) | 05, 16 | HR | ✅ | Phải đúng 1 trong 13 căn cứ pháp lý (Điều 34-41) |
| 15 | Phương án sử dụng lao động (tái cơ cấu) | 5.8 | HR/Ban lãnh đạo | ✅ | Bắt buộc công bố ≥15 ngày trước khi thực hiện |
| 16 | Chuyển bộ phận (Transfer) | 27 | Nhân viên/HR | ✅ | 3 cấp duyệt: manager cũ, manager mới, HR |
| 17 | Điều chỉnh lương kèm Transfer | 27, 05 | HR | ✅ | Tạo Contract Amendment đi kèm |

## 3. Vận hành hàng ngày

| # | Quy trình | Module | Người khởi tạo | Qua Workflow? | Ghi chú |
|---|---|---|---|---|---|
| 18 | Check-in/Check-out (chấm công) | 06 | Nhân viên | ❌ | Xác thực khuôn mặt/GPS |
| 19 | Yêu cầu làm thêm giờ (Overtime) | 06 | Nhân viên | ✅ | Manager duyệt |
| 20 | Điều chỉnh công (Attendance Correction) | 06 | Nhân viên | ✅ | |
| 21 | Xin nghỉ phép (Leave Request) | 07 | Nhân viên | ✅ | ⚙️ Số bước duyệt tùy `total_days` (điều kiện rẽ nhánh Module 18) |
| 22 | Cộng phép năm đầu kỳ | 07 | Hệ thống (batch job) | ❌ | Tự động theo Leave Policy |
| 23 | Hủy đơn nghỉ phép (trước ngày nghỉ) | 07 | Nhân viên | ❌ | Hoàn lại số dư tự động |

## 4. Lương thưởng & Tài chính

| # | Quy trình | Module | Người khởi tạo | Qua Workflow? | Ghi chú |
|---|---|---|---|---|---|
| 24 | Chạy kỳ lương (mở kỳ → tính → duyệt → phát hành) | 08 | Admin/Payroll | ✅ (bước rà soát & phê duyệt HR trưởng) | 10 bước, batch processing toàn công ty |
| 25 | Ứng lương (Salary Advance) | 08 | Nhân viên | ✅ | |
| 26 | Ứng lương linh hoạt (Earned Wage Access) | 36 | Nhân viên | ⚙️ | Tự động duyệt nếu trong hạn mức, qua Workflow nếu vượt ngưỡng |
| 27 | Quét bất thường bảng lương (Anomaly Scan) | 08 | Hệ thống (tự động sau khi tính lương) | ❌ | HR/Kế toán xác nhận thủ công trước khi duyệt chốt kỳ lương |
| 28 | Đối soát & xuất Bank Transfer file | 08 | Payroll/Accountant | ❌ | |

## 5. Hiệu suất & Phát triển

| # | Quy trình | Module | Người khởi tạo | Qua Workflow? | Ghi chú |
|---|---|---|---|---|---|
| 29 | Chu kỳ đánh giá hiệu suất (Goal → Self Review → Manager Review → Evaluation) | 09 | HR (mở cycle), Nhân viên/Manager (thực hiện) | ❌ | Có bước "calibration" tổng hợp, không phải Workflow approval chuẩn |
| 30 | 360-Degree Feedback | 09 | HR/Manager | ❌ | |
| 31 | Coaching/Mentoring Session | 09 | Manager/Coach | ❌ | |
| 34 | Succession Planning & rà soát Candidate | 14 | HR | ❌ | |
| 35 | Ứng tuyển cơ hội nội bộ (Talent Marketplace) | 31 | Nhân viên | ❌ (Express Interest tự do) | Manager mới tự chọn, không phải Workflow chuẩn |
| 36 | Mô phỏng lộ trình sự nghiệp (Career Simulator) | 14 | Nhân viên (xem/lưu) | ❌ | Gợi ý tham khảo, tính năng mở rộng của Module 14 |
| 38 | Buổi 1-1 & Action Items | 39 | Manager | ❌ | Cần consent ghi âm từ nhân viên |
| 39 | Tính điểm Manager Effectiveness | 20 (gộp từ 40) | Hệ thống (định kỳ) | ❌ | Chỉ gợi ý coaching, không tự động kỷ luật |

## 6. Phúc lợi & Đãi ngộ *[ĐÃ LƯỢC BỎ]*

> *Lưu ý: Module 11 (Phúc lợi) và Module 38 (Chia sẻ ngày phép) đã được lược bỏ khỏi phạm vi triển khai. Phụ cấp và bảo hiểm xử lý trực tiếp tại Module 05 Contract & 08 Payroll.*

## 7. Kỷ luật & Khiếu nại

| # | Quy trình | Module | Người khởi tạo | Qua Workflow? | Ghi chú |
|---|---|---|---|---|---|
| 44 | Xử lý kỷ luật lao động | 12 | HR | ✅ | Bắt buộc đủ thành phần (NLĐ + công đoàn nếu có) mới ra quyết định |
| 45 | Xóa kỷ luật tự động (hết thời hiệu) | 12.7 | Hệ thống (batch job) | ❌ | |
| 46 | Xử lý khiếu nại (Grievance) | 12 | Nhân viên | ❌ | Có trạng thái open → investigating → resolved |

## 8. Tài sản & Không gian làm việc

| # | Quy trình | Module | Người khởi tạo | Qua Workflow? | Ghi chú |
|---|---|---|---|---|---|
| 48 | Cấp phát/thu hồi tài sản | 13 | IT/HR Admin | ❌ | |
| 49 | Kiểm kê tài sản định kỳ | 13 | IT/HR Admin | ❌ | 6 tháng/lần |
| 50 | Thiết kế sơ đồ chỗ ngồi (Layout Editor) | 42 | Admin/Facility | ❌ | Có versioning, không phải duyệt |
| 51 | Gán/giải phóng chỗ ngồi cố định | 42 | HR | ❌ | |
| 52 | Đặt chỗ hot-desk theo ngày | 42 | Nhân viên | ❌ | Tự đặt, tự check-in qua ESS |

## 9. Nghỉ việc (Offboarding)

| # | Quy trình | Module | Người khởi tạo | Qua Workflow? | Ghi chú |
|---|---|---|---|---|---|
| 53 | Nộp đơn nghỉ việc / ra quyết định chấm dứt | 16 | Nhân viên/Công ty | ✅ | Phải đúng pháp lý Module 05 |
| 54 | Exit Interview | 16 | HR | ❌ | |
| 55 | Thu hồi tài sản khi Offboarding | 16, 13 | HR/IT | ❌ | |
| 56 | Khóa tài khoản IT | 16, 03 | IT Admin | ❌ | |
| 57 | Tính lương cuối (Final Payroll) | 16, 08 | Payroll | ❌ | Gồm phép chưa dùng + trợ cấp |
| 58 | Clearance từng phòng ban | 16 | Các phòng ban liên quan | ❌ | Xác nhận không nợ |

## 10. Tuân thủ Pháp lý (Compliance Radar)

| # | Quy trình | Module | Người khởi tạo | Qua Workflow? | Ghi chú |
|---|---|---|---|---|---|
| 66 | Rà soát ảnh hưởng thay đổi pháp luật (Compliance Radar) | 35 | Hệ thống/HR | ❌ | Gắn cờ cho HR xử lý trước deadline hiệu lực |

> *Lưu ý: Module 29 (Quản lý Chính sách & Yêu cầu cơ quan nhà nước) đã được lược bỏ khỏi phạm vi triển khai để tinh gọn hệ thống. Doanh nghiệp lưu trữ chính sách trên kho đám mây dùng chung.*

## 11. Nền tảng dùng chung (không phải quy trình nghiệp vụ độc lập)

| # | Quy trình | Module | Ghi chú |
|---|---|---|---|
| 67 | Thiết kế Workflow Definition (drag-drop, điều kiện rẽ nhánh) | 18 | Đây chính là **engine tạo ra cột "Qua Workflow?" ✅** ở các bảng trên |
| 68 | Xử lý quá hạn duyệt (remind/escalate/auto-reject) | 18 | Job nền chạy định kỳ |
| 69 | Ủy quyền duyệt khi vắng mặt (Delegate) | 18 | |
| 70 | Gửi thông báo theo sự kiện | 19 | `sendNotification(eventCode, userId, data)` — mọi quy trình ✅ ở trên đều gọi tới đây |
| 71 | Xuất báo cáo & dashboard | 20 | Query tổng hợp, không phải luồng duyệt |
| 72 | Import dữ liệu hàng loạt | 21 | Super Admin/HR Admin |
| 73 | Nộp lưu kho hồ sơ (Archive Submission) | 22 | ✅ Qua Workflow (`request_type = records_archive_submission`) |
| 74 | Đề xuất & duyệt hủy hồ sơ hết hạn lưu | 22 | ✅ Qua hội đồng xét duyệt (`committee_reviews`), không phải Workflow chuẩn |
| 75 | Mô phỏng tái cơ cấu (What-if) | 01 (gộp từ 41) | ❌ Chỉ thật sự tạo thay đổi khi "Apply" — lúc đó mới chạy lại quy trình tái cơ cấu chuẩn ở mục 2 |

## 12. AI Features (chạy nền, hỗ trợ các quy trình trên)

| # | Quy trình | Module | Kích hoạt bởi | Ghi chú |
|---|---|---|---|---|
| 76 | CV Parser (trích xuất CV tự động) | 23 | Khi có Application mới | Hỗ trợ quy trình #1-7 |
| 77 | CV Review (so khớp CV-JD) | 24 | Khi có Application mới hoặc HR yêu cầu bulk screening | |
| 78 | OCR trích xuất tài liệu/hóa đơn | 25 | Khi upload document/receipt | Dùng ở Onboarding (#10), Business Trip (#62) |
| 79 | Chatbot trả lời chính sách (RAG + Function Calling) | 26 | Nhân viên hỏi qua chat | Có thể trực tiếp *thực hiện* hành động (VD: tạo Leave Request) thay vì chỉ trả lời |
| 80 | Behavior Tracking & Attrition Risk Prediction | 30 | Batch job định kỳ | Sinh cảnh báo cho quy trình can thiệp giữ chân nhân viên (không có trong danh sách trên vì là quy trình "mềm", không có API chuẩn hóa cứng) |

---

## 📌 Nhận xét tổng hợp

- **Tổng cộng ~80 quy trình** được xác định rõ ràng trong tài liệu, trải trên 42 module.
- **~24 quy trình đi qua Module 18 (Workflow Engine)** để duyệt — đây là nhóm cần đảm bảo tích hợp `createWorkflowInstance()` đúng chuẩn khi code backend (khớp với nguyên tắc Phase 3 trong `HRM-Backend-Build-Roadmap.md`: code Workflow trước để nhóm này gọi vào ngay từ đầu, tránh sửa lại sau).
- **Nhóm không qua Workflow nhưng có bước "duyệt thủ công" riêng** (VD: Benefit Claim, Anomaly Scan, Disposal Committee Review) — đây là các luồng duyệt **đặc thù, không dùng chung engine**, cần lưu ý khi thiết kế UI vì không xuất hiện trong "Hộp thư chờ duyệt" tổng hợp của Module 18.
- **Nhóm hoàn toàn tự động (batch job)**: cộng phép năm, xóa kỷ luật hết hạn, quét bất thường lương, tính phụ cấp tuổi — nên rà soát kỹ lịch chạy job (cron schedule) khi lên kế hoạch vận hành.
- **3 quy trình có ràng buộc pháp lý chặn cứng** (không thể bỏ qua bằng cấu hình Workflow thông thường): Chấm dứt hợp đồng đúng 13 căn cứ (#14), Phương án sử dụng lao động công bố ≥15 ngày (#15), Kỷ luật đủ thành phần + đúng 1 hình thức (#44) — 3 quy trình này cần validate ở tầng nghiệp vụ (business rule), không chỉ dựa vào Approval Matrix.