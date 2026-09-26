# HRM - Danh sách trang Frontend (Sitemap & Mô tả)

> Tổng hợp toàn bộ trang giao diện của hệ thống, chia theo 4 khu vực (portal): **Admin/HR Portal** (web, dùng bởi HR/Quản lý/Admin), **ESS Portal** (web + mobile, dùng bởi toàn bộ nhân viên), **Candidate Portal** (web, dùng bởi ứng viên bên ngoài), và **Trang chung** (auth, dashboard).
> Quy ước cột "Vai trò truy cập" tham chiếu theo `HRM-Roles-Phan-Quyen.md`.

---

## 0. Trang chung (Auth & Dashboard)

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Đăng nhập | Xác thực người dùng vào hệ thống | Form email/username + mật khẩu, nút quên mật khẩu, (tùy chọn) đăng nhập SSO | Tất cả |
| Quên mật khẩu / Đặt lại mật khẩu | Khôi phục quyền truy cập khi quên mật khẩu | Form nhập email, form nhập mã OTP/token, form mật khẩu mới | Tất cả |
| Dashboard tổng quan | Trang chủ sau đăng nhập, nội dung khác nhau theo role: HR thấy số liệu toàn công ty, Manager thấy số liệu team, Employee thấy widget cá nhân | Thẻ số liệu nhanh (headcount, đơn chờ duyệt, sinh nhật trong tháng...), biểu đồ tóm tắt, danh sách việc cần làm (pending approvals), thông báo mới | Tất cả (nội dung tùy role) |
| Trung tâm thông báo | Xem toàn bộ thông báo hệ thống đã gửi tới người dùng | Danh sách thông báo (đã đọc/chưa đọc), bộ lọc theo loại, nút đánh dấu đã đọc | Tất cả |

---

## 1. Admin/HR Portal — theo module

### Module 01 — Quản lý Tổ chức

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách công ty/chi nhánh | Quản lý danh sách Company và Branch | Bảng danh sách, nút thêm mới, form sửa (tên, mã số thuế, địa chỉ) | Super Admin, HR Admin |
| Sơ đồ tổ chức | Xem cấu trúc phòng ban dạng cây trực quan | Cây org chart kéo-thả hoặc dạng biểu đồ, click để xem chi tiết phòng ban, nút mở rộng/thu gọn | Super Admin, HR Admin (view: Manager, Executive) |
| Quản lý phòng ban / nhóm | CRUD Department và Team | Bảng danh sách phân cấp, form thêm/sửa (tên, phòng ban cha, trưởng phòng/nhóm), modal xác nhận xóa | Super Admin, HR Admin |
| Quản lý chức danh & cấp bậc | CRUD Position và Job Level | 2 tab: Chức danh (gắn phòng ban + cấp bậc) và Cấp bậc (thứ tự rank), bảng danh sách, form thêm/sửa | Super Admin, HR Admin |
| Quản lý Cost Center | CRUD trung tâm chi phí phục vụ phân bổ lương | Bảng danh sách, form thêm/sửa | Super Admin, HR Admin |
| Mô phỏng tái cơ cấu (What-if Scenario) | Thiết kế & so sánh kịch bản tái cơ cấu (gộp từ Module 41) | Canvas kéo-thả sơ đồ tổ chức, bảng so sánh kịch bản, tính toán tác động chi phí/nhân sự, nút Apply | Super Admin, HR Admin |

### Module 02 — Tuyển dụng

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách yêu cầu tuyển dụng | Xem/quản lý các Manpower Request | Bảng danh sách kèm trạng thái, bộ lọc theo phòng ban/trạng thái, nút tạo mới, nút duyệt/từ chối | HR Admin, HR Specialist, Dept Manager (tạo & xem của phòng mình) |
| Chi tiết yêu cầu tuyển dụng | Xem chi tiết 1 Manpower Request, lịch sử duyệt | Thông tin request, timeline phê duyệt, nút hành động | HR Admin, HR Specialist, Dept Manager, Executive (view) |
| Danh sách vị trí tuyển dụng | Quản lý Job Position đang mở | Bảng danh sách, bộ lọc trạng thái (open/on_hold/closed), nút tạo vị trí mới từ Manpower Request đã duyệt | HR Admin, Recruiter |
| Đăng tin tuyển dụng | Soạn và đăng Job Posting lên các kênh | Form soạn tin (mô tả, yêu cầu), chọn kênh đăng (đa lựa chọn), lịch đăng/đóng tin, xem trước tin | HR Admin, Recruiter |
| Danh sách ứng viên | Xem toàn bộ Candidate trong hệ thống | Bảng danh sách, bộ lọc theo nguồn/trạng thái/vị trí ứng tuyển, tìm kiếm theo tên/email, nút xem CV | HR Admin, Recruiter |
| Pipeline tuyển dụng (Kanban) | Theo dõi trực quan Application theo từng giai đoạn | Bảng Kanban các cột: Sàng lọc → Phỏng vấn → Offer → Hired/Rejected, thẻ ứng viên kéo-thả giữa cột | HR Admin, Recruiter |
| Chi tiết hồ sơ ứng viên | Xem toàn bộ thông tin 1 ứng viên và các Application liên quan | Thông tin cá nhân, CV viewer, lịch sử ứng tuyển, lịch phỏng vấn, ghi chú nội bộ | HR Admin, Recruiter, Interviewer (giới hạn) |
| Quản lý Interview Kit (Scorecard) | Cấu hình bộ câu hỏi + tiêu chí chấm điểm theo vị trí/loại phỏng vấn | Bảng danh sách kit, form thêm câu hỏi kèm mô tả tiêu chí từng mức điểm | HR Admin, Dept Manager |
| Lịch phỏng vấn | Xếp lịch và quản lý các buổi Interview, gán hội đồng (panel) nhiều người | Calendar view tự tìm khung giờ trống chung, form đặt lịch (chọn loại phỏng vấn, panel members + vai trò), nút dời/hủy lịch | HR Admin, Recruiter |
| Đánh giá phỏng vấn | Nhập điểm theo từng tiêu chí trong Scorecard (không chỉ nhận xét tự do) | Form chấm điểm theo câu hỏi (thang 1-5), khuyến nghị (strong_yes...strong_no), ô ghi chú thêm — điểm người khác trong panel ẩn cho tới khi tất cả đã nộp | Interviewer, HR Admin, Recruiter |
| Tổng hợp đánh giá phỏng vấn | So sánh điểm giữa các thành viên panel cho 1 ứng viên, so sánh nhiều ứng viên cùng vị trí | Bảng tổng hợp điểm theo panel member, cảnh báo chênh lệch bất thường, bảng xếp hạng ứng viên | HR Admin, Recruiter, Dept Manager |
| Soạn & gửi Offer | Tạo và gửi thư mời làm việc | Form nhập mức lương đề nghị, ngày bắt đầu, mẫu thư offer, nút gửi, theo dõi trạng thái phản hồi | HR Admin, Recruiter |
| Talent Pool | Danh sách ứng viên tiềm năng lưu trữ cho tương lai | Bảng danh sách, gắn thẻ/tag, tìm kiếm theo kỹ năng | HR Admin, Recruiter |

### Candidate Portal (bên ngoài, không cần đăng nhập hệ thống nội bộ)

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Trang danh sách tin tuyển dụng | Ứng viên xem các vị trí đang mở | Danh sách job card, bộ lọc theo phòng ban/địa điểm, ô tìm kiếm | Candidate (public) |
| Trang chi tiết tin tuyển dụng | Xem mô tả công việc, yêu cầu chi tiết | Nội dung mô tả, nút "Ứng tuyển ngay" | Candidate (public) |
| Form ứng tuyển | Nộp hồ sơ ứng tuyển | Form thông tin cá nhân, upload CV, nút gửi | Candidate (public) |
| Theo dõi trạng thái ứng tuyển | Ứng viên xem tiến trình hồ sơ đã nộp | Thanh trạng thái (đã nộp/đang xem xét/phỏng vấn/kết quả), lịch sử cập nhật | Candidate |

### Module 03 — Onboarding

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách quy trình Onboarding | Theo dõi tiến độ hòa nhập của các nhân viên mới | Bảng danh sách kèm % tiến độ, bộ lọc theo trạng thái | HR Admin, IT Admin |
| Chi tiết Onboarding | Xử lý từng checklist item của 1 nhân viên mới | Danh sách checklist theo nhóm (hồ sơ/thiết bị/tài khoản/đào tạo), checkbox hoàn thành, ô ghi chú, nút cấp tài khoản | HR Admin, IT Admin |
| Quản lý mẫu Checklist | Tạo/sửa các Onboarding Checklist Template | Bảng danh sách template, form thêm item (tên, danh mục, bắt buộc/không) | HR Admin |

### Module 04 — Quản lý Nhân viên

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách nhân viên | Danh mục toàn bộ nhân viên công ty | Bảng danh sách (ảnh, tên, phòng ban, chức danh, trạng thái), bộ lọc đa tiêu chí, tìm kiếm, xuất Excel | HR Admin, HR Specialist, Dept Manager (phòng mình), Team Leader (team mình) |
| Hồ sơ chi tiết nhân viên | Xem/sửa toàn bộ thông tin 1 nhân viên | Tab: Thông tin cá nhân (kèm ảnh chân dung dùng cho xác thực khuôn mặt), Thông tin công việc, Người phụ thuộc, Liên hệ khẩn cấp, Tài liệu, Lịch sử thay đổi | HR Admin, HR Specialist (full); Manager/Team Leader/Employee (view giới hạn) |
| Thêm nhân viên mới | Tạo hồ sơ Employee thủ công (ngoài luồng Hiring) | Form nhiều bước: thông tin cá nhân → công việc → tài liệu | HR Admin, HR Specialist |

### Module 05 — Quản lý Hợp đồng

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách hợp đồng | Quản lý toàn bộ hợp đồng lao động | Bảng danh sách, bộ lọc theo loại/trạng thái, cảnh báo hợp đồng sắp hết hạn (badge màu) | HR Admin, HR Specialist |
| Chi tiết hợp đồng | Xem chi tiết, lịch sử phiên bản 1 hợp đồng | Thông tin hợp đồng, timeline các bản sửa đổi/gia hạn, file PDF viewer, nút gia hạn/sửa đổi/chấm dứt | HR Admin, HR Specialist |
| Tạo/gia hạn hợp đồng | Form tạo hợp đồng mới hoặc gia hạn | Form chọn loại hợp đồng, ngày hiệu lực, lương cơ bản, upload file ký | HR Admin, HR Specialist |

### Module 06 — Chấm công & Ca làm việc

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Quản lý ca làm việc | CRUD Shift và gán ca cho nhân viên | Bảng danh sách ca, form thêm/sửa (giờ bắt đầu/kết thúc), giao diện gán ca hàng loạt theo phòng ban | HR Admin, HR Specialist |
| Bảng chấm công | Xem dữ liệu chấm công theo nhân viên/phòng ban/khoảng thời gian | Bảng lưới (ngày x nhân viên), đánh dấu trễ/sớm/vắng bằng màu, bộ lọc, xuất báo cáo | HR Admin, HR Specialist, Dept Manager, Team Leader |
| Danh sách yêu cầu làm thêm giờ | Duyệt các Overtime Request | Bảng danh sách chờ duyệt/đã duyệt, nút duyệt/từ chối kèm nhận xét | Dept Manager, Team Leader, HR Admin |
| Danh sách yêu cầu điều chỉnh công | Duyệt các Attendance Correction | Bảng danh sách, chi tiết đề xuất giờ vào/ra, nút duyệt/từ chối | Dept Manager, HR Admin |

### Module 07 — Quản lý Nghỉ phép

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Quản lý loại phép & chính sách | CRUD Leave Type và Leave Policy | Bảng danh sách, form cấu hình (số ngày cấp/năm, điều kiện) | HR Admin |
| Lịch nghỉ phép nhóm | Xem trực quan ai đang nghỉ trong phòng ban/team | Calendar view nhiều người, màu theo loại phép | Dept Manager, Team Leader, HR Admin |
| Danh sách đơn nghỉ phép chờ duyệt | Duyệt Leave Request của nhân viên | Bảng danh sách, chi tiết đơn (loại phép, ngày, số dư còn lại), nút duyệt/từ chối | Dept Manager, Team Leader, HR Admin |

### Module 08 — Tiền lương

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Cấu hình thành phần lương | CRUD Salary Component (phụ cấp/thưởng/khấu trừ) | Bảng danh sách, form thêm/sửa (loại, chịu thuế/tính bảo hiểm hay không) | HR Admin, Payroll/Accountant |
| Quản lý lương nhân viên | Xem/cập nhật lương cơ bản theo từng giai đoạn hiệu lực | Bảng lịch sử lương của 1 nhân viên, form thêm mức lương mới | HR Admin, Payroll/Accountant |
| Danh sách kỳ lương | Quản lý các Payroll Period | Bảng danh sách kỳ lương kèm trạng thái, nút mở kỳ mới | Payroll/Accountant, HR Admin |
| Bảng tính lương chi tiết (kỳ lương) | Xem/duyệt kết quả tính lương toàn bộ nhân viên trong 1 kỳ | Bảng lưới (nhân viên x các khoản lương), nút chạy tính lương, nút duyệt, xuất file chuyển khoản | Payroll/Accountant, HR Admin |
| Danh sách yêu cầu ứng lương | Duyệt Salary Advance | Bảng danh sách chờ duyệt, nút duyệt/từ chối | Payroll/Accountant, Dept Manager |
| Phiếu lương (Payslip viewer) | Xem chi tiết phiếu lương của 1 nhân viên trong 1 kỳ | Bảng chi tiết các khoản thu nhập/khấu trừ, nút tải PDF | Payroll/Accountant, HR Admin, Employee (bản thân qua ESS) |
| Rà soát bất thường bảng lương | Xem danh sách anomaly kiểm tra rủi ro trước khi duyệt chi trả (gộp từ Module 37) | Bảng danh sách cảnh báo kèm mức rủi ro, chi tiết lý do, nút xác nhận giải trình/yêu cầu điều chỉnh | Payroll/Accountant, HR Admin |

### Module 09 — Quản lý Hiệu suất

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách chu kỳ đánh giá | Quản lý Performance Cycle | Bảng danh sách, nút tạo chu kỳ mới | HR Admin, HR Specialist |
| Thiết lập KPI & Goal | Tạo khung KPI phòng ban và Goal cá nhân | Bảng KPI theo phòng ban, form tạo Goal (gắn KPI, trọng số) | HR Admin, Dept Manager, Employee (tạo goal cá nhân) |
| Trang tự đánh giá (Self review) | Nhân viên tự chấm điểm từng Goal | Danh sách Goal, form nhập điểm + nhận xét cho từng mục | Employee |
| Trang đánh giá của quản lý (Manager review) | Quản lý chấm điểm nhân viên | Danh sách nhân viên cần đánh giá, form nhập điểm + nhận xét, xem self-review đối chiếu | Dept Manager, Team Leader |
| Kết quả đánh giá hiệu suất | Xem tổng hợp điểm cuối cùng | Bảng điểm theo nhân viên/phòng ban, biểu đồ phân bổ rating, nút chốt điểm (finalize) | HR Admin, Dept Manager (xem team) |

### Module 10 — ~~Đào tạo & Phát triển~~ *[ĐÃ LƯỢC BỎ]*
> *Module đã được lược bỏ khỏi phạm vi triển khai. Việc số hóa chứng chỉ nhân viên được tích hợp trực tiếp vào màn hình Hồ sơ nhân viên / Onboarding (mục Documents).*

### Module 11 — ~~Phúc lợi~~ *[ĐÃ LƯỢC BỎ]*
> *Module đã được lược bỏ khỏi phạm vi triển khai. Các chế độ phúc lợi tiền tệ, phụ cấp và bảo hiểm được tích hợp trực tiếp vào màn hình Quản lý Hợp đồng & Bảng lương.*

### Module 12 — Khen thưởng & Kỷ luật

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách khen thưởng | Quản lý Reward | Bảng danh sách, form tạo mới (loại thưởng, số tiền nếu có) | HR Admin, Dept Manager |
| Danh sách kỷ luật | Quản lý Discipline | Bảng danh sách, form lập biên bản, đính kèm file | HR Admin, Dept Manager |
| Danh sách khiếu nại | Xử lý Grievance | Bảng danh sách theo trạng thái, chi tiết vụ việc, form phản hồi/xử lý | HR Admin |

### Module 13 — Quản lý Tài sản

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách tài sản | Quản lý kho Asset | Bảng danh sách, bộ lọc theo loại/trạng thái, form thêm tài sản mới | IT Admin, HR Admin |
| Cấp phát / thu hồi tài sản | Xử lý Asset Allocation | Form chọn nhân viên + tài sản, ghi nhận tình trạng, nút thu hồi | IT Admin, HR Admin |
| Kiểm kê tài sản | Thực hiện Asset Inventory Check định kỳ | Danh sách tài sản cần kiểm, đánh dấu khớp/thiếu/hỏng cho từng món, báo cáo kết quả | IT Admin, HR Admin |

### Module 14 — Phát triển Nhân sự

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Sơ đồ lộ trình thăng tiến | Xem/cấu hình Career Path giữa các vị trí | Sơ đồ dạng cây/luồng giữa các chức danh | HR Admin, Dept Manager (view) |
| Kế hoạch kế nhiệm | Quản lý Succession Plan cho vị trí trọng yếu | Bảng danh sách vị trí trọng yếu kèm mức rủi ro, danh sách ứng viên kế nhiệm gắn mức độ sẵn sàng | HR Admin, Executive (view) |
| Talent Pool nội bộ | Theo dõi nhân sự tiềm năng | Bảng danh sách, gắn thẻ/tag, ghi chú | HR Admin, Dept Manager |
| Xu hướng & Mô phỏng thăng tiến | Xem tổng hợp xu hướng thăng tiến và mô phỏng lộ trình (gộp từ Module 32) | Biểu đồ Sankey/dòng chảy vị trí, bộ lọc phòng ban, kịch bản lộ trình | HR Admin, Executive |

### Module 15 — ~~Khảo sát & Gắn kết~~ *[ĐÃ LƯỢC BỎ]*
> *Module đã được lược bỏ khỏi phạm vi triển khai. Doanh nghiệp sử dụng công cụ khảo sát chuyên dụng bên ngoài.*

### Module 16 — Nghỉ việc

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách đơn nghỉ việc | Quản lý Resignation/Termination | Bảng danh sách kèm trạng thái, nút duyệt | HR Admin, Dept Manager |
| Chi tiết quy trình Offboarding | Theo dõi checklist bàn giao khi 1 nhân viên nghỉ | Checklist thu hồi tài sản, khóa tài khoản, clearance từng phòng ban, tính lương cuối | HR Admin, IT Admin, Payroll/Accountant |
| Form phỏng vấn thôi việc | Ghi nhận Exit Interview | Form câu hỏi phản hồi, ô nhận xét tổng hợp | HR Admin |

### Module 18 — Workflow (dùng chung, xuất hiện lồng trong nhiều trang)

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Hộp thư chờ duyệt (Approval Inbox) | Tổng hợp TẤT CẢ yêu cầu đang chờ người dùng duyệt, bất kể loại (phép, OT, ứng lương, nghỉ việc...) | Danh sách gộp theo loại request, bộ lọc, thao tác duyệt/từ chối nhanh ngay trên danh sách | Mọi role có quyền duyệt (Manager, Team Leader, HR Admin) |
| Cấu hình ma trận phê duyệt | Thiết lập Approval Matrix cho từng loại request | Bảng cấu hình theo loại request + số cấp + loại người duyệt | Super Admin, HR Admin |
| Thiết lập ủy quyền duyệt | Cấu hình Workflow Delegate khi vắng mặt | Form chọn người ủy quyền, khoảng thời gian | Mọi role có quyền duyệt |

### Module 20 — Báo cáo & Phân tích

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Dashboard Báo cáo tổng hợp | Trang landing các loại báo cáo | Danh sách card báo cáo theo nhóm (nhân sự, chấm công, lương, tuyển dụng...) | HR Admin, Executive, Auditor |
| Báo cáo Headcount | Số lượng nhân viên theo thời gian/phòng ban | Biểu đồ đường/cột, bảng chi tiết, bộ lọc, nút xuất Excel/PDF | HR Admin, Executive |
| Báo cáo Chấm công & Nghỉ phép | Tổng hợp công, tỷ lệ đi trễ, phép đã dùng | Bảng tổng hợp, biểu đồ, bộ lọc | HR Admin, Dept Manager |
| Báo cáo Chi phí lương | Tổng chi phí lương theo phòng ban/thời gian | Biểu đồ cột chồng theo thành phần lương, bảng chi tiết | HR Admin, Payroll/Accountant, Executive |
| Báo cáo Phễu tuyển dụng | Số lượng ứng viên qua từng giai đoạn | Biểu đồ phễu (funnel chart), bảng chi tiết theo vị trí | HR Admin, Recruiter, Executive |
| Báo cáo Tỷ lệ nghỉ việc (Turnover) | Turnover rate theo thời gian/phòng ban/lý do | Biểu đồ đường, bảng phân loại theo lý do nghỉ | HR Admin, Executive |
| Báo cáo Phân bổ hiệu suất | Phân bổ rating hiệu suất toàn công ty | Biểu đồ cột/phân phối theo rating, bộ lọc phòng ban | HR Admin, Executive |
| Báo cáo Hiệu quả Quản lý (Manager Effectiveness) | So sánh điểm hiệu quả quản lý, breakdown chỉ số (turnover, duyệt đơn, họp 1-1, KPI team) | Bảng xếp hạng manager, biểu đồ breakdown từng chỉ số, form slider/cấu hình trọng số (gộp từ Module 40) | HR Admin, Executive, Super Admin |

### Module 21 — Quản trị Hệ thống

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Quản lý người dùng | CRUD User, gán role | Bảng danh sách user, form thêm/sửa, chọn role gán kèm | Super Admin |
| Quản lý Role & Permission | Cấu hình Role và gán Permission theo data scope | Bảng danh sách role, ma trận checkbox permission x data scope | Super Admin |
| Nhật ký hệ thống (Audit Log) | Tra cứu lịch sử thao tác trong hệ thống | Bảng danh sách log, bộ lọc theo user/hành động/thời gian, xem chi tiết giá trị trước/sau | Super Admin, Auditor |
| Cài đặt hệ thống | Cấu hình chung (múi giờ, ngưỡng nhận diện khuôn mặt, kỳ lương mặc định...) | Form các nhóm cài đặt theo tab | Super Admin |
| Quản lý tích hợp | Cấu hình kết nối hệ thống ngoài (kế toán, máy chấm công, BHXH điện tử) | Bảng danh sách tích hợp, form cấu hình kết nối, nút đồng bộ thủ công, trạng thái lần đồng bộ gần nhất | Super Admin, IT Admin |
| Nhập dữ liệu hàng loạt | Import dữ liệu từ Excel/CSV | Form chọn loại dữ liệu, upload file, bảng preview + báo lỗi từng dòng, nút xác nhận import | Super Admin, HR Admin |

---

## 1.5 Admin/HR Portal — Module Đề Xuất Bổ Sung (31-42)

### Module 31 — Thị trường Nhân tài Nội bộ

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách cơ hội nội bộ | Quản lý Internal Opportunity đăng tuyển | Bảng danh sách, form tạo (loại, kỹ năng yêu cầu, thời gian), trạng thái | HR Admin, Dept Manager |
| Danh sách người quan tâm | Xem nhân viên đã Express Interest cho 1 cơ hội | Bảng danh sách kèm % match kỹ năng, nút shortlist/chọn | HR Admin, Dept Manager |
| Thống kê Marketplace | Tỷ lệ tham gia, tỷ lệ thành công theo thời gian | Biểu đồ xu hướng, bảng theo phòng ban | HR Admin, Executive |

### Module 32 — ~~Career Pathing Simulator~~ *[ĐÃ GỘP VÀO MODULE 14]*
> *Module đã được hợp nhất hoàn toàn vào Module 14 (Phát triển Nhân sự). Màn hình xu hướng thăng tiến và mô phỏng lộ trình được quản lý tập trung trong phân hệ Career & Talent.*

### Module 33 — ~~Skill Graph & Gap Analysis~~ *[ĐÃ LƯỢC BỎ]*
> *Module đã được lược bỏ khỏi phạm vi triển khai. Đánh giá năng lực tích hợp qua KPI/Goal & 360 Feedback ở Module 09.*

### Module 34 — ~~Alumni Network~~ *[ĐÃ LƯỢC BỎ]*
> *Module đã được lược bỏ khỏi phạm vi triển khai. Doanh nghiệp không triển khai mạng xã hội cựu nhân viên độc lập.*

### Module 35 — Compliance Radar

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách thay đổi pháp lý | Theo dõi Legal Change mới, mức độ ảnh hưởng | Bảng danh sách kèm priority, deadline xử lý | HR Admin, Auditor (view) |
| Chi tiết rà soát ảnh hưởng | Xem các mục bị ảnh hưởng (hợp đồng/chính sách/rule lương) | Danh sách affected items kèm trạng thái xử lý, nút cập nhật | HR Admin |

### Module 36 — Earned Wage Access

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Danh sách yêu cầu ứng lương | Duyệt/theo dõi Wage Access Request | Bảng danh sách, bộ lọc trạng thái, nút duyệt/giải ngân | HR Admin, Payroll/Accountant |
| Báo cáo chương trình ứng lương | Tần suất sử dụng, tổng chi phí | Biểu đồ, bảng tổng hợp | Payroll/Accountant, Executive |

### Module 37 — ~~Payroll Anomaly Detection~~ *[ĐÃ GỘP VÀO MODULE 08]*
> *Module đã được hợp nhất hoàn toàn vào Module 08 (Tiền lương). Màn hình rà soát bất thường bảng lương được tích hợp trực tiếp vào phân hệ quản lý kỳ lương.*

### Module 38 — ~~Time-off Donation~~ *[ĐÃ LƯỢC BỎ]*
> *Module đã được lược bỏ khỏi phạm vi triển khai do không phù hợp quy định Điều 113-114 Bộ luật Lao động 2019.*

### Module 39 — AI Meeting/1-1 Assistant

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Lịch sử 1-1 & Action Items | Manager xem lại các buổi 1-1 đã tổ chức với nhân viên | Danh sách buổi họp, tóm tắt AI, danh sách action items kèm trạng thái | Dept Manager, Team Leader |

### Module 40 — ~~Manager Effectiveness Score~~ *[ĐÃ GỘP VÀO MODULE 20]*
> *Module đã được hợp nhất hoàn toàn vào Module 20 (Báo cáo & Phân tích). Màn hình Dashboard hiệu quả quản lý và cấu hình trọng số chỉ số được tích hợp trực tiếp vào phân hệ báo cáo quản trị.*

### Module 41 — ~~What-if Org Simulation~~ *[ĐÃ GỘP VÀO MODULE 01]*
> *Module đã được hợp nhất hoàn toàn vào Module 01 (Cơ cấu Tổ chức). Màn hình Trình mô phỏng tái cơ cấu và so sánh kịch bản tổ chức được tích hợp trực tiếp vào phân hệ quản lý sơ đồ tổ chức.*

### Module 42 — Quản lý Sơ đồ Chỗ ngồi

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Trình chỉnh sửa Sơ đồ (Layout Editor) | Thiết kế sơ đồ chỗ ngồi khớp mặt bằng thực tế | Canvas kéo-thả, upload floor plan nền, tạo hàng loạt ghế theo dãy, gán thuộc tính từng ghế, versioning | Super Admin, HR Admin, IT Admin |
| Danh sách chỗ ngồi | Quản lý/gán chỗ ngồi cố định cho nhân viên | Bảng danh sách kèm bộ lọc trạng thái/loại/tiện ích, nút gán/giải phóng | HR Admin, IT Admin |
| Quản lý phòng họp & tiện ích chung | CRUD Non-desk Object (phòng họp, WC, pantry, locker...) | Bảng danh sách, form thêm/sửa vị trí trên sơ đồ | HR Admin, IT Admin |
| Báo cáo tỷ lệ sử dụng chỗ ngồi | Theo dõi occupancy rate, đặc biệt hot-desk | Biểu đồ tỷ lệ sử dụng theo site/tầng/thời gian | HR Admin, Executive |

---

## 2. ESS Portal (nhân viên tự phục vụ — web & mobile)

| Trang | Mô tả | Thành phần chính | Vai trò truy cập |
|---|---|---|---|
| Trang chủ ESS | Dashboard cá nhân của nhân viên | Widget: công hôm nay, số phép còn lại, thông báo mới, việc cần làm | Employee |
| Hồ sơ của tôi | Xem/sửa các trường được phép tự cập nhật | Tab thông tin cá nhân (số điện thoại, địa chỉ, ảnh chân dung), thông tin công việc (chỉ xem) | Employee |
| Chấm công của tôi | Check-in/check-out và xem lịch sử chấm công | Nút check-in/out (mở camera xác thực khuôn mặt), lịch sử theo ngày/tháng, tổng hợp giờ công | Employee |
| Nghỉ phép của tôi | Xem số dư và tạo đơn xin nghỉ | Widget số dư theo loại phép, form tạo đơn, danh sách đơn đã gửi kèm trạng thái | Employee |
| Hợp đồng của tôi | Xem hợp đồng hiện tại và lịch sử | Thông tin hợp đồng, file PDF viewer | Employee |
| Phiếu lương của tôi | Xem/tải phiếu lương các kỳ | Danh sách kỳ lương, chi tiết từng khoản, nút tải PDF | Employee |
| Yêu cầu của tôi | Trang tổng hợp mọi loại yêu cầu đã gửi (nghỉ phép, OT, ứng lương, điều chỉnh công...) | Danh sách gộp theo loại, trạng thái xử lý, timeline duyệt | Employee |
| Đánh giá hiệu suất của tôi | Xem Goal, thực hiện tự đánh giá, xem kết quả | Danh sách goal, form self-review, kết quả đánh giá các kỳ trước | Employee |
| Cơ hội nội bộ | Xem/ứng tuyển cơ hội nội bộ phù hợp (Module 31) | Danh sách cơ hội gợi ý theo phòng ban/vị trí, nút "Express Interest" | Employee |
| Lộ trình sự nghiệp của tôi | Xem gợi ý Career Path dựa trên hồ sơ tương tự (Module 14) | Sơ đồ lộ trình gợi ý theo thâm niên/hiệu suất, nút lưu lộ trình quan tâm | Employee |
| Ứng lương của tôi | Xem số dư khả dụng, tạo yêu cầu ứng lương (Module 36) | Widget số dư, form tạo yêu cầu, lịch sử đã ứng | Employee |
| Buổi 1-1 của tôi | Xem lịch sử 1-1 với manager, action items (Module 39) | Danh sách buổi họp, tóm tắt, action items kèm trạng thái hoàn thành | Employee |
| Sơ đồ chỗ ngồi | Tìm vị trí đồng nghiệp, đặt hot-desk (Module 42) | Sơ đồ tương tác (pan/zoom), tìm kiếm theo tên → highlight ghế, tô màu theo phòng ban, form đặt hot-desk theo ngày, nút check-in | Employee |

---

## 3. Ghi chú thiết kế Frontend

- **Điều hướng theo role:** Menu sidebar hiển thị khác nhau tùy role đăng nhập — Employee thuần chỉ thấy menu ESS; Manager thấy thêm mục "Team" (duyệt, chấm công/nghỉ phép team); HR/Admin thấy đầy đủ menu quản trị.
- **Trang dùng chung nhiều nơi:** "Hộp thư chờ duyệt" (Workflow) nên có mặt ở cả Admin Portal lẫn thu gọn dạng widget trong ESS Portal cho Manager — vì Manager vẫn cần duyệt ngay trên mobile.
- **Ưu tiên phát triển frontend theo roadmap module đã thống nhất trước đó**: nhóm trang Auth + Organization + Employee làm trước, sau đó tới Attendance/Leave/Payroll (nhóm vận hành lõi), rồi Recruitment/Onboarding, cuối cùng là nhóm mở rộng và Reports.
- **Trang Check-in/Check-out** cần ưu tiên thiết kế responsive/mobile-first vì đây là trang nhân viên thao tác hàng ngày nhiều nhất, và cần quyền truy cập camera + GPS của thiết bị.