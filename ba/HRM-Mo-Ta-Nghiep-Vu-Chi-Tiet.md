# HRM - Mô tả Nghiệp vụ Chi Tiết HOÀN CHỈNH (Phiên bản 2.0 - Hợp nhất)

> **Phiên bản này kết hợp chi tiết pháp lý từ bản User + chi tiết kỹ thuật payroll & AI từ bản tôi**
> 
> **Dùng cho:** Tài liệu PRD (Product Requirements Document), Design Document, Training materials

---

## 📋 MỤC LỤC
- Module 01-05: Tổ chức, Tuyển dụng (gồm 2.1 Interview Management), Onboarding, Nhân viên, Hợp đồng
- Module 06-12: Chấm công, Nghỉ phép, Lương, Hiệu suất, Đào tạo, Phúc lợi, Kỷ luật
- Module 13-17: Tài sản, Phát triển, Engagement, Offboarding, ESS
- Module 18-22: Workflow, Notification, Reports, Admin, Lưu kho
- Module 23-30: AI Features + Extensions
- **Module 31-41 (ĐỀ XUẤT BỔ SUNG):** Talent Marketplace, Career Pathing, Skill Graph, Alumni Network, Compliance Radar, Earned Wage Access, Payroll Anomaly Detection, Time-off Donation, AI 1-1 Assistant, Manager Effectiveness Score, What-if Org Simulation
- **Module 42 (ĐỀ XUẤT BỔ SUNG):** Quản lý Sơ đồ Chỗ ngồi (Seating Chart / Workplace Management)

---

## 📌 Căn cứ Pháp lý

| Văn bản pháp luật | Module áp dụng | Nội dung chính |
|---|---|---|
| **Bộ luật Lao động 2019** (45/2019/QH14) | 05, 06, 07, 12 | HĐLĐ, thử việc, gia hạn, kỷ luật, chấm dứt |
| **Luật Bảo hiểm xã hội** (hiện hành) | 07, 08 | BHXH/BHYT/BHTN, thai sản |
| **Luật Thuế TNCN** (hiện hành) | 08 | Giảm trừ gia cảnh, tỷ lệ thuế |
| **Nghị định 145/2020/NĐ-CP** | 05, 06, 07, 08, 12 | Chi tiết thi hành BLLĐ |

---

## 01. Quản lý Tổ chức (Organization Management)

> **Mô hình 3 chiều độc lập:** Đơn vị tổ chức ≠ Địa lý (Chi nhánh) ≠ Vị trí việc làm

**Vì sao tách 3 chiều?**
- Thực tế: 1 nhân viên = Phòng Tín dụng (đơn vị) + Chi nhánh Hà Nội (địa lý) + Chuyên viên Thẩm định (vị trí)
- **3 thông tin này không suy ra được lẫn nhau** — không được lồng nhau cứng

**Luồng hoạt động:**

1. **Company** → Tên, mã số thuế, trụ sở chính
2. **Chiều 1: Đơn vị tổ chức** (cây tự tham chiếu)
   - Khối → Phòng → Nhóm... (không giới hạn số cấp)
   - Mặc định **kế thừa quyền xuống đơn vị con** (VD: Khối TT có quyền xem → tự động tất cả Phòng con đều có)
   - Gán **Cost Center** cho từng đơn vị (phục vụ phân bổ chi phí)

3. **Chiều 2: Địa lý** (cây riêng, độc lập chiều 1)
   - Vùng → Chi nhánh (không phụ thuộc Đơn vị tổ chức)

4. **Chiều 3: Vị trí việc làm** (danh mục phẳng)
   - Job Level (cấp bậc: nhân viên, chuyên viên, trưởng nhóm, quản lý, v.v.)
   - Position (chức danh) — **không gắn cứng vào 1 đơn vị nào** (cùng "Chuyên viên Kinh doanh" dùng ở nhiều phòng)

5. **Nhân viên lưu 3 giá trị độc lập:**
   - `organizational_unit_id` (phòng ban)
   - `branch_id` (chi nhánh)
   - `position_id` (chức danh)

6. **Xem trước ảnh hưởng tái cơ cấu** (trước khi đổi đơn vị cha)
   - Số nhân viên ảnh hưởng
   - Số rule phân quyền tham chiếu đơn vị này
   - Số Workflow có bước liên quan

7. **Audit trail:** Mọi tái cơ cấu ghi nhận ngày hiệu lực — **không sửa đè lịch sử**

**API:**
- `POST/GET/PUT/DELETE /api/v1/companies`
- `POST/GET/PUT/DELETE /api/v1/organizational-units`
- `PUT /api/v1/organizational-units/{id}/move` (kèm impact preview)
- `GET /api/v1/organizational-units/{id}/impact-preview`
- `GET /api/v1/organizational-units/tree`
- `POST/GET/PUT/DELETE /api/v1/regions` + `/branches`
- `GET /api/v1/geography/tree`
- `POST/GET/PUT/DELETE /api/v1/positions` + `/job-levels` + `/cost-centers`

---

## 02. Tuyển dụng (Recruitment)

**Mô tả:** Quản lý từ đề xuất nhân sự → đăng tin → sàng lọc → phỏng vấn → offer → hiring

**Luồng hoạt động:**

1. Trưởng phòng tạo **Manpower Request** (vị trí, số lượng, lý do, thời điểm cần)
2. Request qua Workflow (Module 18) được duyệt
3. HR tạo **Job Position** chính thức (mô tả công việc, yêu cầu) — gắn với Manpower Request
4. HR soạn **Job Posting** và đăng lên các kênh (website, job boards, mạng xã hội, giới thiệu nội bộ)
5. **Candidate** nộp Application kèm CV
6. **Module 24 (CV Review)** tự động so sánh CV vs JD → gợi ý match score (nếu bật AI)
7. HR lên lịch **Interview** (có thể nhiều vòng) — chi tiết xem mục **2.1 Interview Management**
8. Sau mỗi buổi, người phỏng vấn nhập **Interview Evaluation** theo Scorecard
9. Nếu đạt tất cả vòng, HR gửi **Offer** (lương, ngày bắt đầu)
10. Ứng viên chấp nhận → **Hiring** tự động:
    - Tạo hồ sơ Employee
    - Kích hoạt Onboarding (Module 03)
11. Ứng viên tốt nhưng không trúng → **Talent Pool** để liên hệ sau

**API:**
- `POST/GET/PUT /api/v1/manpower-requests`
- `PUT /api/v1/manpower-requests/{id}/approve` / `reject`
- `POST/GET/PUT/DELETE /api/v1/job-positions`
- `POST/GET/PUT/DELETE /api/v1/job-postings`
- `PUT /api/v1/job-postings/{id}/publish`
- `POST/GET/PUT/DELETE /api/v1/candidates`
- `POST /api/v1/candidates/{id}/resume`
- `POST/GET /api/v1/applications`
- `PUT /api/v1/applications/{id}/status`
- `POST/GET /api/v1/offers`
- `PUT /api/v1/offers/{id}/respond`
- `POST /api/v1/hiring/{application_id}/convert-to-employee`
- `GET/POST /api/v1/talent-pool`

*(API riêng cho Interview xem mục 2.1)*

---

### 2.1 Interview Management (Quản lý Phỏng vấn — Chi tiết)

> **Mục tiêu:** Chuẩn hóa quy trình phỏng vấn — không chỉ "lên lịch + đánh giá tự do" mà có bộ tiêu chí, hội đồng, và dữ liệu so sánh giữa các ứng viên.

**Luồng hoạt động chi tiết:**

1. **Interview Kit / Scorecard theo vị trí:**
   - HR/Hiring Manager cấu hình bộ câu hỏi chuẩn hóa cho từng `job_position` (kỹ thuật, hành vi, văn hóa...)
   - Mỗi câu hỏi gắn **tiêu chí chấm điểm** cụ thể (thang điểm 1-5, mô tả rõ từng mức)
   - Scorecard có thể tái sử dụng giữa các đợt tuyển cùng vị trí

2. **Interview Types (Loại vòng phỏng vấn):**
   - `phone_screen` — sàng lọc nhanh qua điện thoại
   - `technical` — đánh giá chuyên môn
   - `culture_fit` — đánh giá phù hợp văn hóa
   - `final_round` — vòng cuối với quản lý cấp cao
   - Mỗi loại có thể gắn Scorecard riêng và yêu cầu số lượng interviewer khác nhau

3. **Interview Panel (Hội đồng phỏng vấn):**
   - Một buổi Interview có thể gồm **nhiều interviewer** cùng lúc hoặc theo vòng nối tiếp
   - Mỗi interviewer được gán **vai trò** (kỹ thuật, quản lý trực tiếp, văn hóa, HR) — chấm điểm độc lập trên cùng Scorecard
   - Hệ thống không hiển thị điểm của interviewer khác cho đến khi tất cả đã nộp (tránh ảnh hưởng lẫn nhau)

4. **Lên lịch & Calendar Sync:**
   - Hệ thống tự tìm khung giờ trống chung giữa các interviewer trong panel + ứng viên
   - Gửi lời mời qua email/calendar (kèm link/địa điểm)
   - Tự động nhắc lịch trước buổi phỏng vấn (qua Module 19 Notification)
   - Hỗ trợ dời lịch/hủy, ghi nhận lý do

5. **Nhập đánh giá theo Scorecard:**
   - Sau buổi phỏng vấn, mỗi interviewer chấm điểm từng tiêu chí trong Scorecard (không chỉ nhận xét tự do)
   - Có thể ghi chú thêm (notes) ngoài các tiêu chí chuẩn
   - Khuyến nghị: `strong_yes` / `yes` / `no` / `strong_no`

6. **Feedback Consolidation (Tổng hợp đánh giá):**
   - Hệ thống tổng hợp điểm từ toàn bộ panel cho 1 ứng viên → hiển thị dạng bảng so sánh
   - Cảnh báo nếu điểm giữa các interviewer **chênh lệch bất thường** (ví dụ: 1 người chấm strong_yes, người khác strong_no) → gợi ý HR tổ chức thảo luận (debrief) trước khi ra quyết định
   - So sánh nhiều ứng viên cùng vị trí trên cùng bộ tiêu chí (bảng xếp hạng)

7. **AI Interview Assistant (tùy chọn, tích hợp Analytics Platform):**
   - Dựa trên JD + CV đã parse (Module 23/24), gợi ý câu hỏi nên hỏi sâu thêm (VD: CV thiếu kinh nghiệm X mà JD yêu cầu → gợi ý câu hỏi kiểm tra khả năng học hỏi)
   - Gợi ý câu hỏi dựa trên Scorecard đã cấu hình, không tự sinh câu hỏi ngoài phạm vi tiêu chí

8. **Bias Awareness (Nhận diện thiên lệch):**
   - Hệ thống thống kê phân bổ điểm số theo interviewer theo thời gian (không dùng để phán xét cá nhân, chỉ nhằm phát hiện pattern chấm điểm bất thường — ví dụ 1 interviewer luôn chấm thấp hơn hẳn mặt bằng chung)
   - Hỗ trợ HR rà soát tính nhất quán trong quy trình tuyển dụng, không thay thế đánh giá con người

9. **Kết quả cuối cùng:**
   - HR tổng hợp kết quả toàn bộ vòng → quyết định đi tiếp hay dừng
   - Ghi nhận đầy đủ lịch sử scorecard, panel, feedback vào hồ sơ Application (phục vụ audit và cải thiện quy trình tuyển dụng sau này)

**API:**
- `POST/GET/PUT/DELETE /api/v1/interview-kits` (Scorecard theo vị trí)
- `POST/GET/DELETE /api/v1/interview-kits/{id}/questions`
- `POST/GET /api/v1/interviews`
- `PUT /api/v1/interviews/{id}/schedule` (kèm tìm khung giờ trống)
- `POST/DELETE /api/v1/interviews/{id}/panel-members`
- `PUT /api/v1/interviews/{id}/reschedule` / `cancel`
- `POST /api/v1/interviews/{id}/evaluations` (theo Scorecard, gắn `panel_member_id`)
- `GET /api/v1/interviews/{id}/evaluations/consolidated` (tổng hợp panel, cảnh báo chênh lệch)
- `GET /api/v1/applications/{id}/interview-comparison` (so sánh nhiều ứng viên cùng vị trí)
- `GET /api/v1/interviews/{id}/ai-suggested-questions` (AI Interview Assistant)
- `GET /api/v1/interviewers/{id}/scoring-pattern` (thống kê pattern chấm điểm — nội bộ HR)

---

## 03. Onboarding

**Mô tả:** Chuẩn bị hồ sơ, thiết bị, tài khoản, đào tạo định hướng cho nhân viên mới

**Luồng hoạt động:**

1. Sau Hiring, hệ thống tự động tạo **Onboarding Process**
2. Chọn **Checklist Template** theo vị trí/phòng ban
3. Template được bung thành **Process Items** cụ thể (hồ sơ, thiết bị, tài khoản, đào tạo)
4. HR thu thập **Documents** từ nhân viên mới (CMND, bằng cấp, hợp đồng cũ, v.v.)
5. IT/HR cấp phát thiết bị → Module 13 (Asset Management)
6. IT tạo **Account Provisioning**: email, tài khoản hệ thống cần thiết
7. HR sắp xếp **Orientation/Training** (giới thiệu công ty, nội quy, văn hóa)
8. Từng Process Item được mark hoàn thành
9. 100% checklist xong → hệ thống chuyển Employee status → "Active"

**API:**
- `POST/GET /api/v1/onboarding/processes`
- `GET /api/v1/onboarding/checklist-templates`
- `PUT /api/v1/onboarding/process-items/{id}/complete`
- `POST/GET/DELETE /api/v1/employees/{id}/documents`
- `POST /api/v1/onboarding/{id}/account-provisioning`
- `POST/GET /api/v1/onboarding/{id}/orientation-sessions`
- `GET /api/v1/onboarding/{id}/progress`
- `PUT /api/v1/onboarding/{id}/complete`

---

## 04. Quản lý Nhân viên (Employee Management)

**Mô tả:** Hồ sơ trung tâm (Master Data) lưu toàn bộ thông tin cá nhân, công việc, người phụ thuộc

**Luồng hoạt động:**

1. Hồ sơ tạo tự động từ Hiring hoặc thủ công
2. Nhân viên/HR cập nhật **Personal Info**:
   - CMND/CCCD (ảnh này dùng cho khuôn mặt chấm công Module 06)
   - Ngày sinh, giới tính, hôn nhân, địa chỉ
3. HR cập nhật **Employment Info**:
   - **3 chiều tổ chức** (Organizational Unit, Branch, Position — Module 01)
   - Cấp bậc, quản lý trực tiếp
   - Loại hình làm việc (full-time, part-time, freelance, v.v.)
   - Ngày vào làm
4. Nhân viên khai báo **Emergency Contact** + **Dependents** (người phụ thuộc — dùng tính thuế TNCN Module 08)
5. HR/nhân viên upload **Documents** (bằng cấp, chứng chỉ, hợp đồng cũ)
6. Mọi thay đổi quan trọng ghi vào **Employee History** (audit trail)
7. Cập nhật **status** xuyên suốt vòng đời (probation → active → on_leave → resigned → terminated)

**API:**
- `POST/GET/PUT/DELETE /api/v1/employees`
- `GET/PUT /api/v1/employees/{id}/personal-info`
- `GET/PUT /api/v1/employees/{id}/employment-info`
- `POST/GET/DELETE /api/v1/employees/{id}/emergency-contacts`
- `POST/GET/DELETE /api/v1/employees/{id}/dependents`
- `POST/GET/DELETE /api/v1/employees/{id}/documents`
- `GET /api/v1/employees/{id}/history`
- `GET /api/v1/employees/search?filter=`
- `PUT /api/v1/employees/{id}/status`

---

## 05. Quản lý Hợp đồng (Contract Management)

> **Căn cứ:** Bộ luật Lao động 45/2019/QH14 + Nghị định 145/2020/NĐ-CP
> **Nguyên tắc:** Hệ thống **PHẢI CHẶN CỨng** khi vi phạm pháp luật (không chỉ cảnh báo)

### 5.1 Loại hợp đồng lao động (Điều 20)

Chỉ còn 2 loại hợp pháp:

| Loại | Thời hạn | Ràng buộc |
|---|---|---|
| **Không xác định thời hạn** | Vô hạn | |
| **Xác định thời hạn** | **Tối đa 36 tháng** | Hệ thống **CHẶN** nếu `end_date - start_date > 36 months` |

### 5.2 Thử việc (Điều 24-27)

**Thời gian tối đa theo nhóm công việc:**

| Nhóm | Thời gian |
|---|---|
| Quản lý doanh nghiệp | 180 ngày |
| Cao đẳng trở lên | 60 ngày |
| Trung cấp/công nhân/nhân viên | 30 ngày |
| Công việc khác | 6 ngày |

**Ràng buộc:**
- Lương thử việc **≥ 85%** lương chính thức (CHẶN nếu thấp hơn)
- Chỉ thử việc **1 lần** cho 1 công việc (kiểm tra lịch sử)
- Kết thúc thử việc **bắt buộc thông báo bằng văn bản**

### 5.3 Gia hạn (Điều 20.2) — QUY TẮC NGHIÊM NGẶT

**Luật bắt buộc:**
- HĐLĐ xác định thời hạn **chỉ được gia hạn TỐI ĐA 1 LẦN**
- Sau 1 lần gia hạn, nếu tiếp tục → **PHẢI** ký HĐLĐ không xác định thời hạn
- Hệ thống **CHẶN** tạo HĐLĐ xác định thời hạn lần 2 (trừ: người cao tuổi, nước ngoài, thành viên ban lãnh đạo công đoàn)

**Tự động chuyển không xác định thời hạn:**
- Nếu 30 ngày sau HĐLĐ hết hạn mà 2 bên chưa ký mới nhưng người LĐ vẫn làm việc
- Hệ thống tự động tạo HĐLĐ không xác định thời hạn, cảnh báo HR

### 5.4 Nội dung bắt buộc (Điều 21)

Hệ thống validate hợp đồng phải có:
- Thông tin 2 bên, công việc, địa điểm, thời hạn
- Lương, hình thức/hạn trả, phụ cấp
- Chế độ nâng lương, thời giờ làm việc
- Bảo hộ LĐ, BHXH/BHYT/BHTN, đào tạo

### 5.5 13 Căn cứ chấm dứt (Điều 34-41) — TÀI LIỆU QUAN TRỌNG

**Mỗi lý do chấm dứt phải map đúng 1 căn cứ; hệ thống TỰ TÍNH báo trước + trợ cấp:**

| Căn cứ | Bên xứ | Báo trước | Trợ cấp |
|---|---|---|---|
| Hết hạn HĐLĐ | Tự động | Không | Có (≥12 tháng) |
| Hoàn thành công việc | NSDLĐ | Không | Có |
| Hai bên thỏa thuận | Cả 2 | Theo thỏa thuận | Có |
| NLĐ chấm dứt **đúng luật** (Điều 35.1) | NLĐ | **45/30/3 ngày** | Có |
| NLĐ chấm dứt **không báo trước** (Điều 35.2: bị ngược đãi, không trả lương, quấy rối, mang thai, v.v.) | NLĐ | Không cần | Có |
| NSDLĐ chấm dứt **đúng luật** (Điều 36.1) | NSDLĐ | **45/30/3 ngày** | Có |
| **Sa thải do kỷ luật** (Điều 125) | NSDLĐ | Không | **KHÔNG** |
| NLĐ chấm dứt **trái luật** (Điều 39) | NLĐ | — | **KHÔNG** + Bồi thường |
| NSDLĐ chấm dứt **trái luật** (Điều 39) | NSDLĐ | — | Có + Bồi thường thêm |
| **Thay đổi cơ cấu/công nghệ/kinh tế** (Điều 42) | NSDLĐ | Theo phương án | **Trợ cấp mất việc làm** |
| M&A/Chia tách/Sáp nhập (Điều 43) | NSDLĐ | Theo phương án | Trợ cấp mất việc làm |
| NLĐ chết, mất năng lực, mất tích | Tự động | Không | Có |
| NSDLĐ chấm dứt hoạt động | Tự động | Không | Có |

**Bảng thời hạn báo trước (Điều 35.1, 36.2):**

| HĐLĐ | NLĐ báo trước | NSDLĐ báo trước |
|---|---|---|
| Không xác định thời hạn | 45 ngày | 45 ngày |
| Xác định 12-36 tháng | 30 ngày | 30 ngày |
| Xác định < 12 tháng | 3 ngày làm việc | 3 ngày làm việc |

**Xử lý khi chấm dứt:**
- HR bắt buộc chọn đúng 1 căn cứ từ 13 căn cứ trên
- Hệ thống TỰ TÍNH ngày làm việc cuối cùng = ngày nộp + thời hạn báo trước tối thiểu
- Nếu ngày nghỉ sớm hơn → **CẢNh báo "Đơn phương chấm dứt TRÁI LUẬT"** (nếu căn cứ không phải "không báo trước")

### 5.6 Thời điểm CẤMEDNSDLĐ chấm dứt (Điều 37) — CHẶN CỨng

Hệ thống CHẶN NSDLĐ khởi tạo chấm dứt nếu nhân viên:
- Đang nghỉ ốm đau/điều trị (kiểm tra leave_requests loại SICK đang active)
- Đang nghỉ phép năm/việc riêng đã duyệt
- **Lao động nữ mang thai, nghỉ thai sản, hoặc nuôi con < 12 tháng tuổi**

### 5.7 Trợ cấp Thôi việc vs Mất việc làm (Điều 46-47) — CÔNG THỨC KHÁC NHAU

**KHÔNG được gộp chung 1 công thức:**

| | Thôi việc (Điều 46) | Mất việc làm (Điều 47) |
|---|---|---|
| Áp dụng | Chấm dứt (trừ sa thải, trái luật, tái cơ cấu) | Tài cơ cấu/công nghệ/M&A |
| Điều kiện | ≥ 12 tháng làm việc | ≥ 12 tháng |
| **Công thức** | **0.5 tháng × số năm** | **1 tháng × số năm (tối thiểu 2 tháng)** |
| Lương tính | Bình quân 6 tháng | Bình quân 6 tháng |

**Nguyên tắc bắt buộc:**
- Hệ thống **KHÔNG cho HR tự nhập số tiền**
- Phải **TỰ TÍNH** dựa công thức, lấy dữ liệu từ `employee_salary_history`
- Chỉ cho override thủ công kèm bắt buộc nhập lý do (audit)

### 5.8 Tái cơ cấu/Công nghệ/Kinh tế (Điều 42-44) — PHƯƠNG ÁN BẮT BUỘC

Khi chấm dứt **ảnh hưởng nhiều người cùng lúc:**

1. NSDLĐ **bắt buộc** lập **Phương án sử dụng lao động** ghi rõ:
   - Danh sách tiếp tục/đào tạo/chuyển/nghỉ hưu/chấm dứt
   - Biện pháp + nguồn tài chính

2. **Công bố ít nhất 15 ngày** trước thực hiện (Điều 44)

3. Chỉ sau khi duyệt + đủ 15 ngày mới được tạo hàng loạt Termination

### 5.9 Template Hợp đồng & Generate PDF (NEW)

- **Contract Templates** — kho template theo loại vị trí (full-time, part-time, freelance...)
- **Template Versioning** — quản lý phiên bản, không sửa đè
- **Auto-Generate PDF** — từ template + dữ liệu nhân viên
- **Template Variables** — {employee_name}, {position}, {salary}, {start_date}, {end_date}, {branch}
- **Signature Management** — chữ ký điện tử, lưu PDF đã ký

### 5.10 Ghi đăng ký Hợp đồng (NEW)

Ở VN, HĐLĐ cần **ghi đăng ký tại Sở Lao động:**
- Trạng thái: "chưa ghi" / "đang xử lý" / "đã ghi" / "hết hiệu lực"
- Quản lý: ngày ghi, số công văn Sở Lao động, tên người tiếp nhận
- Cảnh báo: HĐLĐ active >30 ngày chưa ghi → nhắc HR
- Lưu giữ: giấy phản hồi từ Sở Lao động

**API:**
- `POST/GET/PUT/DELETE /api/v1/contracts`
- `GET/POST /api/v1/contract-types`
- `POST /api/v1/contracts/{id}/renew` (chặn nếu vượt 1 lần gia hạn)
- `POST /api/v1/contracts/{id}/amend`
- `POST /api/v1/contracts/{id}/terminate` (bắt buộc truyền `termination_ground` từ 13 căn cứ, tự tính báo trước + trợ cấp)
- `GET /api/v1/contracts/{id}/notice-period-check`
- `GET /api/v1/contracts/{id}/severance-calculation`
- `GET /api/v1/contracts/{id}/versions`
- `GET /api/v1/contracts/expiring?days=30`
- `GET /api/v1/contracts/{id}/pdf`
- `POST/GET/PUT /api/v1/workforce-restructuring-plans`
- `POST /api/v1/workforce-restructuring-plans/{id}/publish` (bắt đầu đếm 15 ngày)
- `POST /api/v1/workforce-restructuring-plans/{id}/execute-terminations`

---

## 06. Chấm công & Ca làm việc (Attendance & Shift)

**Mô tả:** Ghi nhận giờ vào/ra kèm khuôn mặt, quản lý ca, tính giờ làm thêm, xử lý đi trễ/vắng mặt

**Luồng hoạt động:**

1. Admin thiết lập **Shift** (ca hành chính 8h-17h, ca xoay, ca đêm...)
2. HR gán **Shift Assignment** cho nhân viên/phòng ban (theo khoảng thời gian)
3. Nhân viên **Check-in** qua app:
   - Chụp ảnh khuôn mặt + gửi tọa độ GPS
   - Hệ thống so khớp với ảnh gốc ở Module 04
   - Nếu match ≥ ngưỡng → ghi nhận, lưu ảnh + score
   - Nếu < ngưỡng → từ chối hoặc gắn cờ "cần xác minh thủ công"
4. Cuối ca, **Check-out** tương tự
5. Hệ thống so sánh với ca được gán → đi trễ/về sớm/vắng mặt
6. Ngoài giờ → tạo **Overtime Request** để quản lý duyệt
7. Sai dữ liệu → **Attendance Correction** để duyệt

**API:**
- `POST/GET/PUT/DELETE /api/v1/shifts`
- `POST /api/v1/shifts/assign`
- `POST /api/v1/attendance/check-in` (kèm ảnh + tọa độ)
- `POST /api/v1/attendance/check-out`
- `GET /api/v1/attendance/records?employee_id=&date_range=`
- `POST/GET /api/v1/overtime-requests`
- `PUT /api/v1/overtime-requests/{id}/approve` / `reject`
- `POST/GET /api/v1/attendance-corrections`
- `PUT /api/v1/attendance-corrections/{id}/approve`
- `GET /api/v1/attendance/reports/summary?period=`

---

## 07. Quản lý Nghỉ phép (Leave Management)

> **Căn cứ:** Điều 112-115 BLLĐ 2019; Luật BHXH (ốm đau, thai sản)
> **Quan trọng:** 4 nhóm loại phép **HOÀN TOÀN KHÁC NHAU** — không được gộp chung 1 loại

### 7.1 4 nhóm loại phép

**Nhóm 1: Phép Năm (Điều 113)**
- Số ngày cơ bản: **12** (bình thường) / **14** (nặng nhọc) / **16** (đặc biệt nặng nhọc)
- Cộng thêm: **1 ngày/5 năm thâm niên** liên tục
- Không được dùng chung lương (mỗi year reset)
- Có thể carry-over sang năm sau (tùy chính sách)
- Khi chấm dứt: **hoàn tiền phép năm chưa dùng** (lương bình quân ngày × số ngày)

**Nhóm 2: Nghỉ Lễ Tết (Điều 112)**
- **11 ngày cố định/năm** (theo lịch Nhà nước công bố)
- **KHÔNG trừ vào Leave Balance phép năm**
- Tự động áp dụng toàn bộ nhân viên
- Người làm vào lễ Tết → hưởng lương **bình thường** (không có hệ số thêm ở luật hiện hành)

**Nhóm 3: Nghỉ Việc Riêng Hưởng Lương (Điều 115)**
- Số ngày **CỐ ĐỊNH theo sự kiện:**
  - Kết hôn (bản thân): 3 ngày
  - Con kết hôn: 1 ngày
  - Cha/mẹ/vợ/chồng/con chết: 3 ngày
  - Ông bà/anh chị em chết (không lương): 1 ngày
- **Hệ thống tự điền số ngày** — không cho nhập tự do
- Quy trình phê duyệt (vì sự kiện thực tế)

**Nhóm 4: Ốm Đau / Thai Sản (theo Luật BHXH, không phải BLLĐ)**
- **Do quỹ BHXH chi trả**, không phải doanh nghiệp
- **Ốm đau:** 30/40/60 ngày/năm tùy năm đóng BHXH + địa điểm (nặng nhọc = nhiều hơn)
- **Thai sản:**
  - Trước sinh: từ tuần 8 dự kiến sinh (có lương bảo hộ)
  - Sau sinh: 4 tuần (lương bảo hộ 100%)
  - Có thể xin thêm (phần thêm có thể không lương hoặc 50%)
  - **KHÔNG ĐƯỢC** khấu trừ lương doanh nghiệp

### 7.2 Luồng tính chính sác

1. Admin cấu hình **Leave Policy** gắn với từng nhóm, điều kiện (job level, thâm niên, v.v.)
2. Đầu năm, batch job tự động **cộng Leave Balance** (nhóm có dư: phép năm, ốm đau)
   - Nhân viên mới: cấp theo tỷ lệ số tháng đã làm (không cấp đủ nguyên năm)
3. Khi xin nghỉ:
   - **Phép năm/ốm đau:** chọn ngày và số ngày, hệ thống kiểm tra dư
   - **Việc riêng:** chọn loại sự kiện, **hệ thống tự điền số ngày** cứng
   - **Lễ Tết:** tự động, không cần xin
4. Duyệt qua Workflow → trừ số dư (loại trừ lễ/cuối tuần theo lịch công ty)
5. Đồng bộ sang:
   - **Attendance (Module 06):** không tính vắng mặt
   - **Payroll (Module 08):**
     - Phép năm/việc riêng: doanh nghiệp trả lương (không bị khấu trừ)
     - Ốm đau/thai sản: quỹ BHXH chi trả (tính riêng, không nhập chi phí lương doanh nghiệp)
6. Hủy nghỉ (chưa tới ngày): hoàn lại dư

### 7.3 Thanh toán khi chấm dứt (Điều 113.3)

Khi tạo `contract_terminations` (Module 05):
- Hệ thống **TỰ TÍNH** phép năm còn dư chưa dùng
- **Cộng vào lương thanh toán cuối cùng** (không được bỏ qua)
- Tính theo công thức: bình quân ngày × số ngày còn lại

**API:**
- `POST/GET/PUT/DELETE /api/v1/leave-types`
- `POST/GET/PUT /api/v1/leave-policies`
- `GET /api/v1/leave-balances?employee_id=`
- `POST/GET /api/v1/leave-requests`
- `PUT /api/v1/leave-requests/{id}/approve` / `reject` / `cancel`
- `POST /api/v1/leave-balances/recalculate`
- `GET /api/v1/leave/calendar?organizational_unit_id=`
- `GET /api/v1/leave-balances/{employee_id}/unused-annual-leave-payout`

---

## 08. Tiền lương (Payroll) — CHI TIẾT ĐẦY ĐỦ

> **Đây là module phức tạp nhất** — bắt buộc phải chi tiết từng thành phần tính toán

### 8.1 Payroll Calendar & Cấu hình Ngày Lễ (NEW)

**Tại sao cần Payroll Calendar?**
- Tính "ngày công bình thường" trong tháng để làm đơn vị tính lương
- Loại trừ lễ Tết, chủ nhật, thứ 7, ngày lễ âm lịch...

**Quy trình:**
1. Admin cấu hình **Payroll Period** (hàng tháng, nửa tháng...)
   - Ngày bắt đầu, ngày kết thúc
   - Ngày trả lương dự kiến

2. Admin cấu hình **Công ty Holidays:**
   - Danh mục ngày lễ, thứ 7, chủ nhật
   - Lịch âm lịch (Tết, Trung thu, v.v.)
   - Cập nhật hàng năm
   - Có thể override per-branch (chi nhánh khác nhau)

3. Batch job tính **tổng ngày công bình thường** trong tháng
   - `total_working_days = all_days - weekends - holidays`
   - Dùng làm đơn vị cho tính lương

### 8.2 Lương Bổ Sung Chi Tiết (NEW)

**Định nghĩa:** Lương cho giờ làm thêm ngoài ca/ngày/tuần/tháng quy định

**Công thức:**
```
Lương bổ sung = (Lương bình quân ngày / 8h) × số giờ × hệ số

Lương bình quân ngày = Lương tháng / số ngày công bình thường trong tháng
```

**Hệ số lương bổ sung (theo pháp luật Việt Nam):**
- Giờ làm thêm **trong ca hoặc trong ngày thường** (8h-17h, Thứ 2-6): **1.5×**
- Giờ làm thêm **ngoài ca nhưng trong ngày thường** (ngoài 8h-17h, nhưng Thứ 2-6): **1.5×**
- Giờ làm thêm **vào Thứ 7**: **2×**
- Giờ làm thêm **vào Chủ nhật hoặc ngày lễ**: **3×**

**Quy trình:**
1. Nhân viên tạo **Overtime Request** (ngày, khung giờ, lý do, ca nào)
2. Quản lý duyệt qua Workflow (Module 18)
3. Hệ thống ghi nhận giờ làm thêm đã duyệt
4. Khi tính Payroll → tự động tính lương bổ sung dựa công thức

### 8.3 Phụ Cấp Tuổi (NEW)

Một số công ty áp dụng:
- Từ 30 tuổi: +10% lương cơ bản
- Từ 40 tuổi: +15%
- Từ 50 tuổi: +20%

**Cấu hình:**
1. Admin tạo **Age-Based Allowance Policy**
2. Hệ thống tự tính tuổi từ ngày sinh (Module 04)
3. Khi tính Payroll → cộng phụ cấp tuổi tự động

### 8.4 BHXH/BHYT/BHTN Chi Tiết (NEW)

**Quản lý mức BHXH:**
- Bắt buộc (BHXH, BHYT, BHTN)
- Tự nguyện
- Lựa chọn (gói BHYT cao cấp)

**Ngũ năm bảo hiểm (Seniority):**
- **Chưa có** (< 1 tháng): 100%
- **1-5 năm**: 100%
- **Quá 5 năm**: 100% (hoặc tùy quy định)
- Hệ thống tính tự động từ ngày vào làm

**Xử lý tạm dừng/đóng lại:**
- Khi nghỉ thai sản: tạm dừng BHXH
- Khi chấm dứt: dừng từ ngày chấm dứt
- Cảnh báo khi tạm dừng dài ngày

**Kê khai BHXH điện tử:**
- Xuất file danh sách đóng BHXH hàng tháng
- Tải lên cổng BHXH
- Nhận thông báo từ BHXH (thành công/lỗi)

**Tính toán & Căn cứ Pháp lý (Nghị định 73/2024/NĐ-CP & 74/2024/NĐ-CP):**
- **Mức lương cơ sở:** 2.340.000 VNĐ (từ 01/07/2024).
- **Trần đóng BHXH/BHYT:** 20 lần mức lương cơ sở = 46.800.000 VNĐ/tháng.
- **Trần đóng BHTN:** 20 lần mức lương tối thiểu Vùng 1 (4.960.000 VNĐ) = 99.200.000 VNĐ/tháng.
- **Tỷ lệ trích đóng bắt buộc:**
  - **Người lao động (10.5%):** BHXH 8.0%, BHYT 1.5%, BHTN 1.0%.
  - **Doanh nghiệp (21.5%):** BHXH 17.5%, BHYT 3.0%, BHTN 1.0%.

**Trường hợp không đóng BHXH:**
- Nhân viên part-time < 10h/tuần
- Nhân viên 60+ (nữ) / 65+ (nam)
- Hệ thống skip tính BHXH nếu điều kiện thoả

### 8.5 Thuế TNCN Chi Tiết (Luật Thuế TNCN & Nghị quyết 954/2020/UBTVQH14)

**Cấu hình mức giảm trừ gia cảnh:**
- Mức giảm trừ bản thân: 11.000.000 VNĐ/tháng.
- Mức giảm trừ người phụ thuộc: 4.400.000 VNĐ/người phụ thuộc/tháng.
- Miễn thuế tiền ăn giữa ca / ăn trưa: tối đa 730.000 VNĐ/tháng (Thông tư 26/2016/TT-BLĐTBXH). Phụ cấp điện thoại, xăng xe theo quy chế nội bộ công ty được miễn thuế.

**Biểu thuế lũy tiến từng phần 7 bậc (Điều 22 Luật Thuế TNCN):**
- Bậc 1: Đến 5 triệu VNĐ/tháng -> 5%
- Bậc 2: Trên 5 tr đến 10 triệu VNĐ/tháng -> 10% (trừ 0.25 tr)
- Bậc 3: Trên 10 tr đến 18 triệu VNĐ/tháng -> 15% (trừ 0.75 tr)
- Bậc 4: Trên 18 tr đến 32 triệu VNĐ/tháng -> 20% (trừ 1.65 tr)
- Bậc 5: Trên 32 tr đến 52 triệu VNĐ/tháng -> 25% (trừ 3.25 tr)
- Bậc 6: Trên 52 tr đến 80 triệu VNĐ/tháng -> 30% (trừ 5.85 tr)
- Bậc 7: Trên 80 triệu VNĐ/tháng -> 35% (trừ 9.85 tr)

**Tính toán:**
```
Thu nhập chịu thuế = Lương thời gian + Tổng phụ cấp - Phụ cấp ăn trưa miễn thuế (tối đa 730k)
Thu nhập tính thuế = Thu nhập chịu thuế - Các khoản bảo hiểm bắt buộc (BHXH, BHYT, BHTN) - Giảm trừ gia cảnh (bản thân 11tr + người phụ thuộc 4.4tr/người)
Thuế TNCN = Thu nhập tính thuế × Thuế suất lũy tiến từng phần (7 bậc)
```

**Quản lý người phụ thuộc:**
- Nhân viên/HR đăng ký số người phụ thuộc ở Module 04 (vợ/chồng, con, cha mẹ...)
- Hệ thống tự động lấy tổng số người phụ thuộc đã đăng ký để tính giảm trừ gia cảnh.

### 8.6 Luồng Tính Payroll Chi Tiết

1. **Cấu hình thành phần lương (Salary Components):**
   - Định nghĩa các loại phụ cấp (ăn trưa, xăng xe, điện thoại...), thưởng, khấu trừ với cờ `isTaxable`, `isInsuranceBase`, `isRecurring`.
2. **Đề xuất tạm ứng lương (Salary Advance):**
   - Nhân viên gửi đơn tạm ứng qua ESS hoặc HR tạo thay (hạn mức tối đa 50% lương cơ bản hợp đồng).
   - Tự động kích hoạt quy trình phê duyệt (`ApprovalRequestType.SALARY_ADVANCE`) qua Module 21 Workflow Engine.
   - Khi hoàn tất giải ngân, đơn chuyển sang trạng thái sẵn sàng trừ vào kỳ lương kế tiếp.
3. **Mở kỳ lương (Payroll Period):**
   - Admin/HR tạo kỳ lương mới cho tháng/năm (`OPEN`).
4. **Xử lý tính lương hàng loạt (Batch Processing):**
   - Đọc dữ liệu công chuẩn, ngày công thực tế, ngày nghỉ phép hưởng nguyên lương từ snapshot `MonthlyTimesheet` của Module 06.
   - Đọc hợp đồng lao động có hiệu lực (`ContractResponse`), phụ cấp cố định.
   - Tính lương thời gian: `Lương cơ bản / Ngày công chuẩn * (Ngày công thực tế + Ngày phép có lương)`.
   - Tính bảo hiểm bắt buộc theo trần NĐ 73/2024 & NĐ 74/2024.
   - Tính giảm trừ gia cảnh, thu nhập tính thuế, thuế TNCN 7 bậc.
   - Trừ tạm ứng lương đã được giải ngân.
   - Tính lương thực nhận (Net Salary), tạo các khoản mục chi tiết `PayrollRecordItem`.
   - Cập nhật tổng quỹ lương `totalGross`, `totalNet`, `totalInsurance`, `totalTax` và chuyển kỳ sang `PROCESSING`.
5. **Điều chỉnh lương (Adjustments):**
   - HR có thể điều chỉnh hoặc thêm khoản mục thưởng/phụ cấp/khấu trừ thủ công kèm lý do cụ thể trước khi trình duyệt.
6. **Phê duyệt kỳ lương (Payroll Approval):**
   - HR bấm Trình duyệt (`submit-approval`), hệ thống khởi tạo luồng phê duyệt đa cấp `PAYROLL_APPROVAL` qua Module 21 Workflow Engine.
   - WorkflowListener tự động cập nhật trạng thái kỳ lương và bản ghi lương thành `APPROVED` khi luồng duyệt hoàn tất.
7. **Đóng kỳ lương & Chi trả (Close & Disburse):**
   - Admin đóng kỳ lương (`CLOSED`), bản ghi lương chuyển sang `PAID`.
   - Nhân viên có thể tra cứu phiếu lương cá nhân (Payslip) qua ESS `/api/v1/payroll-records/my-payslips`.

**Danh sách REST API chính thức (Chuẩn ApiResponse & PageData):**
- **Thành phần lương (Salary Components):**
  - `GET /api/v1/salary-components`: Danh sách thành phần lương phân trang + filter
  - `POST /api/v1/salary-components`: Tạo mới thành phần lương
  - `GET /api/v1/salary-components/{id}`: Chi tiết thành phần lương
  - `PUT /api/v1/salary-components/{id}`: Cập nhật thành phần lương
  - `DELETE /api/v1/salary-components/{id}`: Xóa mềm thành phần lương
- **Tạm ứng lương (Salary Advance):**
  - `POST /api/v1/salary-advances`: Nhân viên tạo yêu cầu tạm ứng (kèm workflow)
  - `GET /api/v1/salary-advances/my-advances`: Xem danh sách tạm ứng cá nhân
  - `GET /api/v1/salary-advances`: Quản trị viên tra cứu danh sách tạm ứng (DataScope)
  - `GET /api/v1/salary-advances/{id}`: Chi tiết yêu cầu tạm ứng
  - `POST /api/v1/salary-advances/{id}/disburse`: Kế toán xác nhận giải ngân
  - `POST /api/v1/salary-advances/{id}/reject`: Từ chối đơn tạm ứng
  - `POST /api/v1/salary-advances/{id}/cancel`: Nhân viên hủy đơn tạm ứng đang chờ
- **Kỳ tính lương (Payroll Period):**
  - `GET /api/v1/payroll-periods`: Danh sách kỳ lương phân trang + filter
  - `POST /api/v1/payroll-periods`: Tạo mới kỳ tính lương
  - `GET /api/v1/payroll-periods/{id}`: Chi tiết kỳ tính lương kèm lịch sử workflow
  - `POST /api/v1/payroll-periods/{id}/process`: Chạy tính toán lương hàng loạt tự động
  - `POST /api/v1/payroll-periods/{id}/submit-approval`: Trình duyệt kỳ lương qua Workflow Engine
  - `POST /api/v1/payroll-periods/{id}/approve`: Phê duyệt kỳ tính lương
  - `POST /api/v1/payroll-periods/{id}/close`: Khóa và đóng kỳ tính lương
- **Bản ghi lương & Phiếu lương (Payroll Record & Payslip):**
  - `GET /api/v1/payroll-records`: Tra cứu danh sách bảng lương (phân quyền theo DataScope)
  - `GET /api/v1/payroll-records/{id}`: Chi tiết bảng lương nhân viên kèm danh sách mục chi tiết
  - `POST /api/v1/payroll-records/{id}/adjust`: Bổ sung/điều chỉnh khoản mục lương
  - `GET /api/v1/payroll-records/my-payslips`: Tra cứu danh sách phiếu lương cá nhân (ESS)

---

## 09. Quản lý Hiệu suất (Performance Management)

**Mô tả:** Chu kỳ đánh giá, mục tiêu KPI, tự đánh giá, manager review, **360-degree feedback**, coaching

### 9.1 Luồng Đánh giá

1. Admin tạo **Performance Cycle** (theo quý hoặc năm)
2. Nhân viên + quản lý thiết lập **Goal** cá nhân (gắn KPI phòng ban, có trọng số)
3. Trong chu kỳ, cập nhật tiến độ Goal
4. Nhân viên thực hiện **Self Review**
5. Quản lý thực hiện **Manager Review**
6. **360-Degree Feedback (NEW):**
   - **Peer Review**: Feedback từ 3-5 người cùng cấp (ẩn danh)
   - **Upward Feedback**: Feedback từ cấp dưới (nếu là quản lý)
   - Hệ thống tổng hợp các feedback ẩn danh
7. HR tổng hợp thành **Evaluation** cuối cùng (có thể qua calibration)
8. Lưu vào lịch sử, làm căn cứ thưởng KPI + thăng tiến

### 9.2 Coaching & Mentoring (NEW)

1. **Gán Coach/Mentor:**
   - Có thể là người cùng level hoặc cấp cao hơn
   - Lưu từ ngày nào đến ngày nào

2. **Lên lịch Coaching Session:**
   - Định sẵn chủ đề (skill development, behavior change)
   - Tần suất (hàng tuần, hai tuần...)

3. **Ghi chú nội dung:**
   - Sau mỗi session, coach ghi chú bàn luận + action items
   - Nhân viên thêm nhận xét riêng

4. **Follow-up Mục tiêu:**
   - Xác định mục tiêu muốn đạt
   - Theo dõi tiến độ hàng tháng

5. **Đánh giá Hiệu quả:**
   - Sau chu kỳ coaching (VD: 6 tháng), đánh giá cải thiện
   - Có thể có feedback từ quản lý, peer
   - Quyết định tiếp tục hay kết thúc

**API:**
- `POST/GET/PUT /api/v1/performance-cycles`
- `POST/GET/PUT/DELETE /api/v1/kpis`
- `POST/GET/PUT/DELETE /api/v1/goals`
- `POST /api/v1/goals/{id}/reviews`
- `POST /api/v1/performance/{cycle_id}/evaluations`
- `POST /api/v1/performance/{cycle_id}/360-feedback`
- `PUT /api/v1/performance/evaluations/{id}/finalize`
- `GET /api/v1/employees/{id}/performance-history`
- `POST/GET/PUT /api/v1/coaching/assignments`
- `POST/GET /api/v1/coaching/sessions`
- `PUT /api/v1/coaching/{id}/evaluate`

---

## 10. Đào tạo & Phát triển (Training & Development)

**Mô tả:** Quản lý chương trình đào tạo, khóa học, đăng ký, điểm danh, chứng chỉ

**Luồng hoạt động:**

1. HR/L&D lập **Training Program** (theo nhu cầu, dự trù ngân sách)
2. Chọn **Training Provider** và mở **Course** (thời gian, địa điểm/online, số học viên tối đa)
3. Nhân viên tự đăng ký hoặc được chỉ định — tạo **Training Registration**
4. Trong buổi học, cập nhật **điểm danh**
5. Nếu đạt → HR cấp **Certification** (có thể có ngày hết hạn để renew)
6. Cập nhật chi phí thực tế đối chiếu ngân sách
7. Tra cứu lịch sử học tập liên tục

**API:**
- `POST/GET/PUT/DELETE /api/v1/training-programs`
- `POST/GET/PUT/DELETE /api/v1/courses`
- `POST/GET/PUT/DELETE /api/v1/training-providers`
- `POST/GET /api/v1/training-registrations`
- `PUT /api/v1/training-registrations/{id}/attendance-status`
- `POST/GET /api/v1/certifications`
- `GET /api/v1/employees/{id}/learning-history`

---

## 11. Phúc lợi (Benefits)

**Mô tả:** Gói phúc lợi, bảo hiểm sức khỏe, khám sức khỏe định kỳ, wellness program

### 11.1 Benefit Plans cơ bản

1. HR thiết lập **Benefit Plans** (bảo hiểm cao cấp, phụ cấp phúc lợi, khám sức khỏe, v.v.)
2. Nhân viên **Benefit Enrollment** — đăng ký tham gia
3. HR duyệt, kích hoạt
4. Nhân viên nộp **Benefit Claim** (kèm chứng từ)
5. HR/Bảo hiểm duyệt claim → chi trả

### 11.2 Khám Sức Khỏe Định Kỳ (NEW)

1. **Cấu hình chương trình:**
   - Tần suất (hàng năm, 2 năm/lần...)
   - Loại khám (cơ bản, nâng cao tùy tuổi/chức danh)
   - Các mục khám bắt buộc (tim, phổi, gan, thận, máu...)

2. **Lên lịch khám:**
   - Chọn đơn vị khám (bệnh viện hợp tác)
   - Chọn ngày khám (có thể tách theo department)

3. **Lấy kết quả khám:**
   - Đơn vị khám gửi kết quả (PDF)
   - Module 25 (OCR) trích xuất thông tin từ PDF (chỉ số máu, huyết áp, v.v.)
   - Lưu thành **Health Screening Result**

4. **Follow-up bất thường:**
   - Hệ thống gắn cờ kết quả bất thường
   - Gửi thông báo cho nhân viên để tái khám, điều trị
   - Lưu mục follow-up + kết quả tái khám

5. **Tra cứu lịch sử:**
   - Nhân viên xem ở ESS (Module 17)
   - Bác sĩ công ty tra cứu lịch sử

### 11.3 Wellness Program (NEW)

1. **Các chương trình:**
   - Yoga, Pilates (ngày nhất định, có trainer)
   - Gym allowance
   - Khám tâm lý
   - Buổi nói chuyện sức khỏe (nutrition, exercise)
   - Team building, outdoor activities

2. **Đăng ký tham gia:**
   - Nhân viên đăng ký chương trình quan tâm
   - HR quản lý chỗ (nếu giới hạn)

3. **Theo dõi tham dự:**
   - Ghi nhận tham gia (check-in hoặc xác nhận trainer)
   - Tính % tham dự

4. **Chi phí:**
   - Quản lý ngân sách wellness
   - Thống kê ROI

**API:**
- `POST/GET/PUT/DELETE /api/v1/benefit-plans`
- `POST/GET /api/v1/benefit-enrollments`
- `PUT /api/v1/benefit-enrollments/{id}/approve`
- `GET /api/v1/employees/{id}/benefits`
- `POST/GET /api/v1/benefit-claims`
- `PUT /api/v1/benefit-claims/{id}/approve`
- `POST/GET /api/v1/health-screening-programs`
- `POST /api/v1/health-screening/{id}/schedule`
- `POST /api/v1/health-screening/{id}/results`
- `GET /api/v1/employees/{id}/health-screening-history`
- `POST/GET/PUT /api/v1/wellness-programs`
- `POST/GET /api/v1/wellness-enrollments`

---

## 12. Khen thưởng & Kỷ luật (Reward & Discipline)

> **Căn cứ:** Điều 117-127 BLLĐ 2019 — thủ tục quyết định kỷ luật **VÔ HIỆU** nếu sai quy trình (dù hành vi là có thật)

### 12.1 5 Nguyên Tắc BẮTBUỘC Kỷ luật (CHẶN CỨng)

1. **Phải chứng minh được lỗi** — không cho tạo Discipline nếu chưa có bằng chứng
2. **Bắt buộc có mặt NLĐ + tổ chức đại diện NLĐ** (nếu có công đoàn) — không đủ thành phần = không cho "quyết định"
3. **Chỉ 1 hình thức/hành vi** — nếu nhiều vi phạm cùng lúc = áp dụng hình thức cao nhất
4. **CẤMTUYỆT ĐỐI phạt tiền/cắt lương** — không được thêm link giữa Discipline và Payroll khấu trừ
5. **Phải ghi trong Nội quy** — mỗi Discipline tham chiếu đúng 1 điều khoản nội quy

### 12.2 Không được xử lý kỷ luật (CHẶN)

- Đang nghỉ ốm đau/điều trị
- Đang nghỉ phép đã duyệt
- **Lao động nữ mang thai/thai sản/nuôi con < 12 tháng**

### 12.3 Thời hiệu xử lý (Điều 123)

- Tối đa **6 tháng** từ ngày vi phạm (hoặc 12 tháng nếu liên quan tài chính/bí mật)
- Hệ thống **tính hạn**, cảnh báo/chặn nếu hết thời hiệu

### 12.4 4 Hình Thức Kỷ Luật Duy Nhất (Điều 124)

1. Khiển trách
2. Kéo dài thời hạn nâng lương (không quá 6 tháng)
3. Cách chức
4. **Sa thải** (chỉ áp dụng 4 căn cứ cụ thể — Điều 125)

### 12.5 4 Căn Cứ Sa Thải (Điều 125)

- Trộm cắp, tham ô, đánh bạc, cố ý thương tích, sử dụng ma túy tại nơi làm việc
- Tiết lộ bí mật kinh doanh/công nghệ, xâm phạm sở hữu trí tuệ, gây thiệt hại nghiêm trọng, quấy rối tình dục
- Đã bị kỷ luật **tái phạm** (trong thời gian chưa xóa kỷ luật)
- Tự ý bỏ việc **5 ngày liên tục/30 ngày** hoặc **20 ngày liên tục/365 ngày**

### 12.6 Hệ Thống Điểm (Point System) (NEW)

1. **Đăng ký vi phạm thành điểm:**
   - Vắng mặt: -1 điểm
   - Đi muộn: -0.5 điểm
   - Hiệu suất thấp: -0.5 điểm
   - Khen thưởng: +1 điểm

2. **Reset điểm:**
   - Hàng tháng, quý hoặc năm (tùy chính sách)
   - Hoặc tích lũy với decay (điểm cũ giảm theo thời gian)

3. **Threshold & Auto-Escalation:**
   - Tổng điểm < -5 → gợi ý escalate sang kỷ luật
   - Quản lý nhân viên nhận cảnh báo để thảo luận

### 12.7 Xóa Kỷ Luật Tự Động (Điều 126)

- Khiển trách: **3 tháng** (nếu không tái phạm)
- Kéo dài nâng lương/cách chức: **6 tháng**
- Hệ thống tự động sau khoảng thời gian, ghi nhận "đã xóa kỷ luật" (ảnh hưởng xét tái phạm khi sa thải)

**API:**
- `POST/GET /api/v1/rewards`
- `POST/GET /api/v1/disciplines` (bắt buộc: `meeting_attendees`, `evidence_files`, `handbook_reference`)
- `GET /api/v1/disciplines/{id}/statute-of-limitation-check`
- `PUT /api/v1/disciplines/{id}/approve`
- `POST/GET /api/v1/employee-points`
- `PUT /api/v1/employee-points/{id}/record-behavior`
- `GET /api/v1/employee-points/{id}/summary`
- `POST/GET /api/v1/grievances`
- `PUT /api/v1/grievances/{id}/resolve`
- `POST/GET /api/v1/labor-union/members`

---

## 13. Quản lý Tài sản (Asset Management)

**Mô tả:** Đăng ký, cấp phát, thu hồi tài sản công ty

**Luồng hoạt động:**

1. Admin đăng ký **Asset** (laptop, điện thoại, thẻ ra vào, xe, v.v.), gắn mã
2. Cấp phát → ghi nhận người nhận, ngày cấp, tình trạng
3. Hỏng → "under_repair"; sửa xong → "allocated" hoặc "in_stock"
4. 6 tháng/lần → **Inventory Check** (đối chiếu với thực tế)
5. Chuyển phòng hoặc nghỉ việc → **Asset Return** (ngày trả, tình trạng)

**API:**
- `POST/GET/PUT/DELETE /api/v1/assets`
- `POST /api/v1/assets/{id}/allocate`
- `POST /api/v1/assets/{id}/return`
- `GET /api/v1/employees/{id}/assets`
- `POST /api/v1/assets/inventory-check`
- `GET /api/v1/assets/inventory-check/{id}/report`

---

## 14. Phát triển Nhân sự (Career & Talent)

**Mô tả:** Lộ trình thăng tiến, kế hoạch kế nhiệm, Talent Pool nội bộ

**Luồng hoạt động:**

1. HR xây dựng **Career Path** mẫu (bước thăng tiến hợp lý giữa các vị trí)
2. Cho các vị trí trọng yếu → **Succession Plan** (đánh giá rủi ro nếu vị trí trống)
3. Dựa kết quả hiệu suất → đề xuất **Succession Candidates** (phân loại: sẵn sàng ngay / 1-2 năm / 3-5 năm)
4. Nhân sự tiềm năng → **Internal Talent Pool**
5. Định kỳ rà soát, cập nhật mức độ sẵn sàng

**API:**
- `POST/GET/PUT /api/v1/career-paths`
- `POST/GET/PUT /api/v1/succession-plans`
- `POST /api/v1/succession-plans/{id}/candidates`
- `GET/POST /api/v1/internal-talent-pool`
- `GET /api/v1/employees/{id}/career-path`

---

## 15. Khảo sát & Gắn kết (Engagement) [ĐÃ LƯỢC BỎ KHỎI PHẠM VI]

> ⚠️ **Ghi chú kiến trúc:** Module này đã được **lược bỏ** khỏi phạm vi dự án vì đây là tính năng thuần CRUD biểu mẫu khảo sát rời rạc (tương tự Google Forms/Typeform), hoàn toàn độc lập và không liên quan đến chuỗi cung ứng dữ liệu cốt lõi của Core HRM (Hồ sơ, Chấm công, Tính lương, Hợp đồng). Doanh nghiệp sử dụng công cụ chuyên dụng bên ngoài.

**Mô tả:** Đo lường mức độ hài lòng, gắn kết qua khảo sát định kỳ, tính eNPS

**Luồng hoạt động:**

1. HR tạo **Survey** — soạn câu hỏi (thang điểm, chọn 1, chọn nhiều, mở)
2. Chọn đối tượng (toàn công ty hoặc theo phòng ban), khoảng thời gian
3. Gửi lời mời (ẩn danh hoặc không)
4. Nhân viên trả lời
5. Tổng hợp kết quả → **eNPS** (phân nhóm promoter/passive/detractor)
6. HR xem báo cáo để cải thiện văn hóa

**API:**
- `POST/GET/PUT/DELETE /api/v1/surveys`
- `POST/GET/DELETE /api/v1/surveys/{id}/questions`
- `POST /api/v1/surveys/{id}/send`
- `POST /api/v1/surveys/{id}/responses`
- `GET /api/v1/surveys/{id}/results`
- `GET /api/v1/surveys/{id}/enps`

---

## 16. Offboarding (Nghỉ việc)

**Mô tả:** Quản lý toàn bộ quy trình khi nhân viên nghỉ việc

**Luồng hoạt động:**

1. Nhân viên nộp **Resignation** hoặc công ty ra **Termination**
2. Duyệt qua Workflow (đúng pháp lý Module 05)
3. HR lên lịch **Exit Interview** (thu phản hồi)
4. **Asset Return** (Module 13) — thu hồi tài sản
5. **Account Deactivation** (khóa tài khoản IT)
6. **Final Payroll** (tính lương cuối + phép năm chưa dùng + trợ cấp Module 08)
7. **Clearance** (từng phòng xác nhận không nợ)
8. Đóng offboarding → trạng thái Employee = "Terminated/Resigned"

**API:**
- `POST/GET /api/v1/resignations`
- `PUT /api/v1/resignations/{id}/approve`
- `POST/GET /api/v1/terminations`
- `POST/GET /api/v1/exit-interviews`
- `POST /api/v1/offboarding/{id}/asset-return`
- `POST /api/v1/offboarding/{id}/account-deactivation`
- `POST /api/v1/offboarding/{id}/final-payroll-calculation`
- `POST/GET /api/v1/offboarding/{id}/clearance`
- `PUT /api/v1/offboarding/{id}/complete`

---

## 17. Tự phục vụ Nhân viên (Employee Self-Service - ESS)

**Mô tả:** Cổng nhân viên tự tra cứu + quản lý thông tin, không cần HR

**Luồng hoạt động:**

1. Nhân viên đăng nhập ESS
2. Xem/sửa **My Profile** (các trường cho phép)
3. Xem **My Attendance**, **My Contract**, **My Payslip**, **My Benefits**, **My Leave**, **My Training**
4. Tạo **My Leave** + **My Requests** (ứng lương, điều chỉnh công, v.v.)
5. Theo dõi trạng thái xử lý theo thời gian thực

**API:**
- `GET/PUT /api/v1/ess/profile`
- `GET /api/v1/ess/attendance`
- `GET /api/v1/ess/contracts`
- `GET /api/v1/ess/payslips`
- `GET /api/v1/ess/benefits`
- `POST/GET /api/v1/ess/leave-requests`
- `POST/GET /api/v1/ess/requests`
- `GET /api/v1/ess/requests/{id}/status`

---

## 18. Workflow Engine & Role Designer

> **Nâng cấp:** Từ Approval Matrix tĩnh → Dynamic Workflow với điều kiện rẽ nhánh, versioning

**Mô tả:** Engine phê duyệt dùng chung cho toàn HRM, admin tự thiết kế luồng qua drag-drop

**Luồng hoạt động:**

1. Admin thiết kế **Workflow Definition** cho từng `request_type`
   - Thêm **Workflow Steps** theo thứ tự
   - Chọn loại người duyệt (quản lý, trưởng đơn vị, vai trò, người cụ thể)

2. Thêm **Điều kiện rẽ nhánh** giữa các bước
   - VD: `leave_request.total_days > 5` → thêm bước duyệt HR
   - Tham chiếu **bất kỳ thuộc tính nào** của hồ sơ

3. Mỗi bước cấu hình:
   - **Thời hạn xử lý** (theo giờ/ngày làm việc)
   - **Hành vi quá hạn:** `remind` / `auto_escalate` / `auto_reject`

4. **Publish** phiên bản mới (hồ sơ cũ giữ phiên bản cũ)

5. Khi module tạo yêu cầu → gọi Workflow Engine
   - Engine resolve người duyệt cụ thể
   - Đánh giá điều kiện
   - Định tuyến sang bước phù hợp

6. Người duyệt nhận thông báo, xem chi tiết, duyệt/từ chối kèm nhận xét

7. Job nền xử lý quá hạn (nhắc/leo cấp/từ chối), ghi log đầy đủ

8. Hoàn tất → callback về module gốc cập nhật trạng thái

**Điểm mạnh:**
- Admin không cần lập trình
- Điều kiện động (không phải cố định)
- Versioning (không ảnh hưởng hồ sơ đang chạy)
- Delegate approval (ủy quyền khi vắng mặt)

**API:**
- `POST/GET/PUT /api/v1/workflow-definitions`
- `POST /api/v1/workflow-definitions/{id}/publish`
- `POST/GET/PUT/DELETE /api/v1/workflow-definitions/{id}/steps`
- `POST/GET/PUT/DELETE /api/v1/workflow-definitions/{id}/conditions`
- `POST /api/v1/workflows/instances` (nội bộ)
- `PUT /api/v1/workflows/instances/{id}/approve` / `reject`
- `POST /api/v1/workflows/delegates`
- `GET /api/v1/workflows/pending?approver_id=`
- `GET /api/v1/workflows/instances/{id}/history`

---

## 19. Thông báo (Notification)

**Mô tả:** Gửi thông báo tự động cho các sự kiện quan trọng (email/in-app/SMS)

**Luồng hoạt động:**

1. Module trigger sự kiện (VD: `leave.approved`, `contract.expiring`)
2. Notification Service tra cứu **Notification Template** tương ứng
3. Render template với dữ liệu thực tế
4. Gửi thông báo qua kênh cấu hình
5. Với sự kiện định kỳ → job quét hàng ngày, tạo reminder khi tới hạn

**API:**
- `POST /api/v1/notifications/send` (nội bộ)
- `GET/POST /api/v1/notification-templates`
- `GET /api/v1/notifications?user_id=&status=`
- `PUT /api/v1/notifications/{id}/read`
- `POST /api/v1/notifications/reminders/schedule`

---

## 20. Báo cáo & Phân tích (Reports & Analytics)

**Mô tả:** Tổng hợp dữ liệu → báo cáo + dashboard quản trị

**Luồng hoạt động:**

1. Người dùng chọn loại báo cáo (headcount, lương, phễu tuyển, turnover, v.v.)
2. Áp bộ lọc (khoảng thời gian, phòng ban, chi nhánh, level...)
3. Hệ thống truy vấn từ dữ liệu nguồn → trả về bảng/biểu đồ
4. Người dùng lưu bộ lọc thành **Dashboard** cá nhân hoặc dùng chung
5. **Export** ra Excel/PDF; lưu lịch sử export

**Báo cáo mẫu:**
- Headcount (tổng số, theo phòng ban, chi nhánh, level)
- Chấm công (vắng, đi muộn, làm thêm)
- Nghỉ phép (sử dụng, dư, carry-over)
- Lương (tổng chi phí, phân bổ theo phòng ban)
- Phễu tuyển (số ứng viên, tỷ lệ convert, thời gian tuyển)
- Turnover (tỷ lệ nghỉ việc, lý do)
- Performance (phân bổ rating, KPI đạt)

**API:**
- `GET /api/v1/reports/headcount?filter=`
- `GET /api/v1/reports/attendance-summary?filter=`
- `GET /api/v1/reports/leave-summary?filter=`
- `GET /api/v1/reports/payroll-cost?filter=`
- `GET /api/v1/reports/recruitment-funnel?filter=`
- `GET /api/v1/reports/turnover-rate?filter=`
- `GET /api/v1/reports/performance-distribution?filter=`
- `POST /api/v1/reports/{report_id}/export?format=xlsx|pdf`
- `GET/POST /api/v1/dashboards`

---

## 21. Quản trị Hệ thống (System Administration)

**Mô tả:** Người dùng, phân quyền, cấu hình, audit log, tích hợp, multi-company

**Luồng hoạt động:**

1. Super Admin tạo **User** (tài khoản), liên kết Employee
2. Gán **Role** → cấu hình **Permission** + `data_scope`
3. Mọi thao tác quan trọng ghi vào **Audit Log** (trước/sau)
4. Admin cấu hình **System Settings** (múi giờ, ngôn ngữ, ngưỡng, v.v.)
5. **Data Import** hàng loạt (Excel/CSV), validate + báo lỗi
6. **Integration** kết nối hệ thống ngoài (kế toán, máy chấm công, BHXH...)
7. **Multi-company Consolidation** (nếu tập đoàn)

**Phân quyền granular:**
- Mỗi Role có tập Permission (view, create, edit, delete, approve...)
- Mỗi Permission áp dụng phạm vi dữ liệu (own/team/department/company/all)
- VD: HR Manager xem được lương toàn công ty, nhưng nhân viên chỉ xem lương của mình

**API:**
- `POST/GET/PUT/DELETE /api/v1/users`
- `POST/GET/PUT/DELETE /api/v1/roles`
- `POST/GET/PUT/DELETE /api/v1/permissions`
- `PUT /api/v1/roles/{id}/permissions`
- `PUT /api/v1/users/{id}/roles`
- `GET /api/v1/audit-logs?filter=`
- `GET/PUT /api/v1/system-settings`
- `POST /api/v1/data/import`
- `POST/GET/PUT/DELETE /api/v1/integrations`
- `POST /api/v1/integrations/{id}/sync`

---

## 22. Lưu kho & Hủy hồ sơ (Digital Records Repository)

> **Ghi chú:** Khác với "upload file" ở Module 04 — đây là **quản lý thời vòng đời lưu trữ chính thức**

**Mô tả:** Lưu trữ hồ sơ số, tuân thủ thời hạn lưu trữ luật, hội đồng xét hủy

**Luồng hoạt động:**

1. Admin cấu hình **Retention Policy** (lưu tối thiểu bao lâu theo loại tài liệu)
   - VD: HĐLĐ lưu 3 năm sau chấm dứt

2. Admin cấu hình **metadata field config** (tùy biến per document type)

3. HR **Nộp lưu kho** — gửi yêu cầu đưa tài liệu vào lưu trữ chính thức

4. **Duyệt lưu kho** (qua Workflow) → hệ thống tính `retention_until` → trạng thái "archived"

5. Định kỳ, job quét tài liệu **hết hạn lưu trữ** hoặc **bị đánh dấu trùng lặp**

6. HR **Đề xuất hủy** kèm lý do

7. **Hội đồng xét hủy** (nhiều thành viên) biểu quyết — chỉ khi đủ phiếu mới hủy thật

8. Khi hủy, ghi nhận đầy đủ vào audit logs (ai đề xuất, hội đồng là ai, quyết định, thời điểm hủy)

9. **Quản lý quyền truy cập kho** (ai được xem/nộp/duyệt/đề xuất hủy)

**Tại sao cần tách khỏi Module 04?**
- Module 04 "upload file thô" = lưu trữ tạm thời, bất cứ khi nào
- Module 22 "nộp lưu kho" = quy trình chính thức, có phê duyệt, có timeline, có hội đồng xét hủy

**API:**
- `POST/GET/PUT /api/v1/records/retention-policies`
- `POST/GET/PUT /api/v1/records/metadata-field-configs`
- `POST /api/v1/records/{employee_document_id}/submit-for-archive`
- `PUT /api/v1/records/archive-submissions/{id}/approve`
- `GET /api/v1/records/archived?filter=`
- `GET /api/v1/records/expiring-retention`
- `POST /api/v1/records/{employee_document_id}/disposal-proposals`
- `POST /api/v1/records/disposal-proposals/{id}/committee-reviews`
- `PUT /api/v1/records/disposal-proposals/{id}/execute`

---

## 23. CV Parser (Trích xuất thông tin từ CV tự động - AI)

**Mô tả:** Sử dụng AI/NLP để tự động trích xuất thông tin từ CV ứng viên (tên, email, số ĐT, kinh nghiệm, kỹ năng, bằng cấp...)

**Luồng hoạt động:**

1. Ứng viên upload CV (PDF, DOCX, hình ảnh)
2. Hệ thống gọi **AI CV Parser Service:**
   - PDF/DOCX → extract text trực tiếp
   - Hình ảnh → OCR (Module 25) trước
3. AI trích xuất:
   - **Personal**: Tên, Email, Số ĐT, Địa chỉ, Ngày sinh
   - **Contact**: LinkedIn, GitHub, Website
   - **Experience**: Công ty, chức danh, thời gian, mô tả
   - **Education**: Trường, ngành, năm tốt nghiệp, GPA
   - **Skills**: Lập trình, ngôn ngữ, công cụ (kèm level)
   - **Certifications**: Chứng chỉ chuyên môn
   - **Languages**: Ngôn ngữ (level: basic, intermediate, fluent, native)
   - **Volunteering**: Hoạt động tình nguyện
4. Lưu dữ liệu trích xuất kèm **confidence score** (0-100%)
5. HR xem kết quả, chỉnh sửa nếu cần (nếu AI trích < 60%)
6. Dữ liệu dùng cho:
   - Module 24 (CV Review): so sánh với JD
   - Module 02 (Recruitment): tạo hồ sơ Candidate

**Chi tiết kỹ thuật:**
- **Input**: PDF, DOCX, JPG, PNG
- **Output**: JSON với fields được trích xuất
- **Confidence Score**: Mỗi field có score riêng
- **Fallback**: Nếu score < 60% → hiển thị text gốc cho HR chỉnh sửa

**API:**
- `POST /api/v1/cv-parser/upload`
- `GET /api/v1/cv-parser/{candidate_id}/result`
- `PUT /api/v1/cv-parser/{candidate_id}/result`
- `POST /api/v1/cv-parser/{candidate_id}/confirm`

---

## 24. CV Review (Tự động đánh giá CV so sánh với JD - AI)

**Mô tả:** Tự động so sánh CV ứng viên vs Job Description, tính match score, gợi ý

**Luồng hoạt động:**

1. HR tạo **Job Position** với **Job Description** (mô tả công việc, yêu cầu, kỹ năng, kinh nghiệm)
2. Ứng viên nộp **Application** kèm CV
3. Module 23 (CV Parser) trích xuất → lưu vào `parsed_cv`
4. Hệ thống gọi **CV Review Service:**
   - So sánh `parsed_cv` vs `job_description`
   - Tính **Match Score:**
     - **Skills Match** (0-40 điểm): % kỹ năng yêu cầu có trong CV
     - **Experience Match** (0-30 điểm): Kinh nghiệm phù hợp? (VD: JD yêu 5 năm, CV có 4 năm → 80%)
     - **Education Match** (0-20 điểm): Trình độ học vấn đáp ứng?
     - **Location Match** (0-10 điểm): Nếu JD chỉ định địa điểm
5. Tính **tổng Match Score** (0-100%):
   - ≥ 80: **Highly Recommended** → gợi ý phỏng vấn ngay
   - 60-80: **Recommended** → có thể xem xét
   - 40-60: **Marginal** → cần xem thêm
   - < 40: **Not Recommended** → có thể loại
6. Sinh ra **nhận xét tự động** (text):
   - VD: "CV phù hợp: có đầy đủ kỹ năng Java, Spring Boot, SQL. Kinh nghiệm 6 năm phát triển backend (vượt yêu cầu 5 năm). Bằng cấp Đại học CNTT."
   - Hoặc: "Điểm yếu: Chưa có kinh nghiệm Microservices (JD yêu cầu). Nên hỏi thêm về khả năng học hỏi."
7. HR xem **CV Review Result** (match score, phân loại, nhận xét, danh sách kỹ năng match/không match)
8. HR có thể **override score** (với lý do → audit)

**Chi tiết thuật toán:**
- **Semantic Matching**: Embedding (word2vec, BERT) → so sánh kỹ năng ngay cả chính tả khác ("Python" vs "python", "JavaScript" vs "JS")
- **Experience Calculation**: So sánh thời gian + công việc tương tự
- **Education Mapping**: Match bằng cấp ("Đại học Bách khoa" = "University of Engineering")
- **Weighting**: Custom trọng số cho từng tiêu chí

**Multi-language Support:**
- CV viết Tiếng Việt, Tiếng Anh hoặc cả hai
- AI nhận diện ngôn ngữ + dịch (hoặc embedding đa ngôn ngữ)

**API:**
- `POST /api/v1/cv-review/analyze`
- `GET /api/v1/cv-review/{application_id}/result`
- `PUT /api/v1/cv-review/{application_id}/result` (override score)
- `GET /api/v1/cv-review/applications?job_id=&sort=match_score`

---

## 25. OCR (Optical Character Recognition) — Trích xuất từ Ảnh/Scan - AI

**Mô tả:** Sử dụng OCR để trích xuất thông tin từ tài liệu quét/ảnh (CMND, bằng cấp, hóa đơn, v.v.)

**Luồng hoạt động:**

1. HR/nhân viên upload ảnh/scan tài liệu:
   - CMND/CCCD → Module 04
   - Bằng cấp → Module 10
   - Chứng chỉ → Module 10
   - Hóa đơn khám → Module 11
   - BHXH → Module 08
   - Hợp đồng → Module 05

2. Hệ thống gọi **OCR Service:**
   - Nhận diện loại tài liệu (CNN)
   - Trích xuất text
   - Named Entity Recognition (NER) để nhận diện thực thể (tên, số ĐT, email, v.v.)
   - Post-processing (làm sạch, format date)

3. **Trích xuất thông tin chính:**
   - **CMND/CCCD**: Số CMT, họ tên, ngày sinh, giới tính, quê quán, địa chỉ, ngày cấp, nơi cấp
   - **Bằng cấp**: Trường, ngành, xếp loại, năm tốt nghiệp
   - **Chứng chỉ**: Tên chứng chỉ, cấp bởi ai, ngày cấp, ngày hết hạn
   - **Hóa đơn khám**: Ngày khám, bệnh viện, chi phí
   - **Sổ BHXH**: Số sổ, từ-đến ngày
   - **Hợp đồng**: Bên ký, ngày ký, điều khoản chính
   - **Hóa đơn/Receipt**: Số tiền, ngày, nội dung

4. Lưu dữ liệu trích xuất kèm **confidence score**

5. HR xem kết quả, chỉnh sửa nếu cần

6. **Validate thông tin** (VD: ngày sinh từ CMND so khớp với hồ sơ không?)

**Supported Document Types:**

| Loại tài liệu | Thông tin cần trích |
|---|---|
| CMND/CCCD | Số CMT, họ tên, ngày sinh, giới tính, quê quán, địa chỉ, ngày cấp |
| Bằng cấp | Trường, ngành, xếp loại, năm tốt nghiệp |
| Chứng chỉ | Tên chứng chỉ, cấp bởi, ngày cấp, ngày hết hạn |
| Hóa đơn khám | Ngày khám, bệnh viện, chi phí |
| Sổ BHXH | Số sổ, từ-đến ngày |
| Hợp đồng | Bên ký, ngày ký, điều khoản chính |
| Hóa đơn/Receipt | Số tiền, ngày, nội dung |

**API:**
- `POST /api/v1/ocr/extract`
- `GET /api/v1/ocr/{document_id}/result`
- `PUT /api/v1/ocr/{document_id}/result`
- `POST /api/v1/ocr/{document_id}/confirm`

---

## 26. Chatbot & Knowledge Base (Function Calling - AI)

**Mô tả:** Chatbot AI hỗ trợ nhân viên/HR trả lời câu hỏi theo tài liệu doanh nghiệp + có khả năng gọi API (Function Calling)

### 26.1 Knowledge Base Setup

1. Admin tạo **Document Library** — lưu trữ tài liệu:
   - Nội quy công ty
   - Chính sách lương, phúc lợi
   - Hướng dẫn sử dụng ESS
   - Quy trình tuyển dụng, onboarding, offboarding
   - FAQ
   - Bảng công khai (mức lương, phụ cấp)

2. Admin **index documents** — tạo vector embeddings

3. **Phân loại:**
   - Policy Documents
   - Procedure Documents
   - FAQ
   - System Guides

### 26.2 Chatbot Capabilities

**A. Trả lời câu hỏi theo tài liệu (RAG — Retrieval Augmented Generation)**

Khi nhân viên hỏi: *"Chính sách lương tháng 13 như thế nào?"*

1. Chatbot nhận diện ý định: câu hỏi về chính sách lương
2. Search trong Knowledge Base: tìm documents liên quan
3. Retrieve thông tin: lấy đoạn text từ documents
4. Generate answer: dùng LLM tạo câu trả lời
5. Trả lời: kèm source (document nào)

**Ví dụ:**
```
User: Lương tháng 13 được tính như thế nào?

Bot: Theo chính sách công ty:
- Lương tháng 13 = (Tổng lương 1-12) / 12
- Điều kiện: làm việc ≥ 12 tháng liên tục
- Chi trả: trước ngày 25 tháng 12
- Khi chấm dứt năm nay: tính theo tỷ lệ tháng

Tham khảo: Chính sách lương tháng 13 (link)
```

**B. Function Calling — Gọi API trong hội thoại**

Chatbot có khả năng gọi các hàm:

1. **Tra cứu thông tin cá nhân:**
   - `GET /api/v1/ess/profile` — xem thông tin cá nhân
   - `GET /api/v1/ess/payslips` — xem payslip gần nhất
   - `GET /api/v1/ess/leave-balances` — xem số ngày phép còn lại

2. **Tạo request:**
   - `POST /api/v1/ess/leave-requests` — tạo đơn xin phép
   - `POST /api/v1/ess/requests` — tạo request khác

3. **Tra cứu thông tin chung:**
   - `GET /api/v1/company-holidays` — xem lịch nghỉ lễ
   - `GET /api/v1/organizational-units` — xem cơ cấu tổ chức

4. **Quản lý công ty (cho HR):**
   - `GET /api/v1/employees` — tìm nhân viên
   - `GET /api/v1/contracts/expiring?days=30` — xem HĐLĐ sắp hết hạn
   - `GET /api/v1/reports/headcount` — xem số liệu headcount

**Ví dụ Function Calling:**
```
User: Cho tôi xem lịch sử lương 3 tháng gần nhất

Bot: [Gọi API: GET /api/v1/ess/payslips?months=3]

Bot: Dưới đây là 3 tháng gần nhất:
- Tháng 1/2024: 15,000,000 VND (đã nhận)
- Tháng 12/2023: 14,500,000 VND (đã nhận)
- Tháng 11/2023: 14,500,000 VND (đã nhận)

Bạn có muốn xem chi tiết lương tháng nào không?
```

```
User: Tôi muốn xin 5 ngày phép năm vào tháng 3

Bot: Mình sẽ giúp bạn. Trước tiên, mình kiểm tra số phép còn lại.

[Gọi API: GET /api/v1/ess/leave-balances?type=annual]

Bot: Bạn còn 12 ngày phép năm. Sau khi xin 5 ngày, sẽ còn 7 ngày. OK?

User: OK, từ ngày 10-14/3

Bot: [Gọi API: POST /api/v1/ess/leave-requests {
  "leave_type": "annual",
  "start_date": "2024-03-10",
  "end_date": "2024-03-14",
  "days": 5
}]

Bot: Đơn xin phép của bạn đã được gửi đi. Quản lý sẽ duyệt trong vòng 24h.
```

### 26.3 Conversation Features

1. **Multi-turn conversation** — ghi nhớ ngữ cảnh
2. **Intent Detection** — nhận diện ý định
3. **Entity Extraction** — trích xuất thông tin
4. **Language Support** — Tiếng Việt, Tiếng Anh (đa ngôn ngữ)
5. **Confidence Score** — nếu không chắc → gợi ý liên hệ HR

### 26.4 Phân quyền & Security

- Chatbot chỉ trả lời **thông tin công khai** + **thông tin cá nhân của user** (tùy role)
- VD:
  - Nhân viên thường: thông tin cá nhân + chính sách công khai
  - HR: danh sách nhân viên, HĐLĐ sắp hết hạn
  - Quản lý: thông tin team

### 26.5 Logging & Analytics

- Ghi nhận tất cả câu hỏi của người dùng
- Thống kê câu hỏi thường gặp → cập nhật FAQ
- Phát hiện vấn đề không giải quyết được → HR follow up

**API:**
- `POST /api/v1/chatbot/message`
- `GET /api/v1/chatbot/documents`
- `POST /api/v1/chatbot/documents` (admin thêm)
- `PUT /api/v1/chatbot/documents/{id}` (admin cập nhật)
- `GET /api/v1/chatbot/conversation-history`

---

## 27. Quản lý Chuyển Bộ Phận (Transfer Management)

**Mô tả:** Quản lý quy trình chuyển bộ phận của nhân viên

**Luồng hoạt động:**

1. Nhân viên hoặc HR tạo **Transfer Request** (bộ phận mới, lý do, ngày dự kiến)
2. Request qua Workflow duyệt:
   - Manager cũ: xác nhận nhân viên có thể rời
   - Manager mới: xác nhận có nhu cầu tuyển
   - HR: duyệt chính thức
3. Nếu transfer kèm **điều chỉnh lương**:
   - Cấu hình lương mới, ngày effective
   - Tạo Contract Amendment
4. Duyệt xong:
   - Cập nhật `organizational_unit_id`, `manager_id`, có thể `branch_id`
   - Cập nhật lương nếu có
   - Ghi vào `employee_history`
   - Gửi thông báo
5. HR theo dõi **transition period** (thời gian training/handover)

**API:**
- `POST/GET /api/v1/transfer-requests`
- `PUT /api/v1/transfer-requests/{id}/approve` / `reject`
- `POST /api/v1/transfer-requests/{id}/complete`

---

## 28. Quản lý Du Lịch Công Tác (Business Trip Management) [ĐÃ LƯỢC BỎ KHỎI PHẠM VI]

> ⚠️ **Ghi chú kiến trúc:** Module này đã được **lược bỏ** khỏi phạm vi dự án. Bản chất nghiệp vụ quản lý chi phí đi lại, vé máy bay, khách sạn và hoàn ứng (Reimbursement) thuộc về phân hệ **T&E (Travel & Expense) của Kế toán / ERP**, không thuộc Core HRM. Khi đi công tác, nhân viên sử dụng đơn nghỉ/vắng mặt công tác tại Module 07 Leave; các khoản phụ cấp công tác được chi trả trực tiếp qua Module 08 Payroll.

**Mô tả:** Quản lý du lịch công tác: yêu cầu, phê duyệt, chi phí, reimbursement

**Luồng hoạt động:**

1. Nhân viên tạo **Business Trip Request** (địa điểm, ngày, lý do, dự kiến chi phí)
2. Request duyệt từ manager, HR
3. Nếu duyệt:
   - Ghi vào **Trip Plan**
   - Có thể tích hợp booking (travel agency)
   - Quản lý ngân sách/hạn mức chi phí
4. Sau chuyến đi, nhân viên **submit chi tiêu thực tế**:
   - Upload receipt/invoice
   - Module 25 (OCR) trích xuất (số tiền, ngày, nội dung)
   - Nhân viên xác nhận
5. HR/Kế toán **duyệt chi phí**:
   - So sánh với plan
   - Kiểm tra receipt hợp lệ
   - Duyệt hoặc yêu cầu bổ sung
6. **Reimbursement Request** (hoàn tiền)
7. Tiền hoàn cộng vào **lương tháng tiếp theo** (hoặc chuyển khoản riêng)

**API:**
- `POST/GET /api/v1/business-trips`
- `PUT /api/v1/business-trips/{id}/approve`
- `POST/GET /api/v1/business-trips/{id}/expenses`
- `PUT /api/v1/business-trips/{id}/expense/{expense_id}/approve`
- `POST /api/v1/business-trips/{id}/reimbursement`

---

## 29. Quản lý Chính Sách & Quy Định (Policy & Compliance)

**Mô tả:** Lưu trữ chính sách công ty, phát hành mới, xác nhận nhân viên đã đọc, quản lý yêu cầu từ cơ quan

**Luồng hoạt động:**

1. HR tạo **Policy Document** (tiêu đề, nội dung, ngày effective, phạm vi áp dụng, yêu cầu acknowledgement, deadline)
2. HR **phát hành** → gửi thông báo
3. Nhân viên **xác nhận đã đọc** (click "I agree", hệ thống ghi: ai, khi nào, IP)
4. HR theo dõi **confirmation rate** → nhắc nhở nhân viên chưa confirm
5. Khi cập nhật chính sách → tạo **phiên bản mới** (không ghi đè)
6. Nhân viên có thể **tra cứu lịch sử** chính sách

**Yêu cầu từ Cơ quan Nhà nước (NEW):**
1. HR ghi nhận yêu cầu (loại: Sở Lao động, Cảnh sát PCCC, Thuế, v.v.), nội dung, deadline
2. Phân công bộ phận xử lý
3. Theo dõi tiến độ (% hoàn thành, ghi chú)
4. **Submit lên cơ quan** (lưu bằng chứng: ngày, giờ, file, chữ ký)
5. **Lưu phản hồi** từ cơ quan

**API:**
- `POST/GET/PUT /api/v1/policies`
- `POST /api/v1/policies/{id}/publish`
- `POST /api/v1/policies/{id}/acknowledge`
- `GET /api/v1/policies/{id}/versions`
- `GET /api/v1/policies/{id}/acknowledgement-status`
- `POST/GET /api/v1/government-requests`
- `PUT /api/v1/government-requests/{id}/update-status`

---

## 30. Behavior Tracking & Attrition Prediction

**Mô tả:** Theo dõi hành vi nhân viên, dự báo rủi ro nghỉ việc, can thiệp kịp thời

### 30.1 Behavior Metrics

1. **Attendance Behavior**: vắng mặt, đi muộn, tỷ lệ OT, sử dụng phép
2. **Performance Behavior**: rating, khen thưởng, kỷ luật, khiếu nại
3. **Engagement Behavior**: tham gia training, team building, eNPS, feedback sentiment
4. **Career Behavior**: thăng tiến, transfer, thâm niên, tăng lương

### 30.2 Attrition Risk Prediction

Hệ thống **tích hợp ML model** để dự báo:
- **High Risk** (>70%): khả năng nghỉ việc cao
- **Medium Risk** (40-70%): vừa phải
- **Low Risk** (<40%): thấp

**Các factor ảnh hưởng:**
- Vắng mặt nhiều → risk cao
- Hiệu suất thấp → risk cao
- Chưa được promote > 2 năm → risk cao
- Lương tăng thấp → risk cao
- eNPS thấp → risk cao
- Tham gia training ít → risk cao

### 30.3 Quy Trình Can Thiệp

1. Hệ thống gửi **cảnh báo** nếu nhân viên vào **High Risk Group**
2. Manager/HR lên **kế hoạch can thiệp** (tăng lương, thăng tiến, coaching)
3. **Ghi nhận action plan** và **theo dõi kết quả** (risk score có giảm không?)
4. **Exit Interview**: khi nhân viên nghỉ → ghi nhận lý do thực tế → feedback vào model

### 30.4 Dashboard

- **Attrition Risk Summary**: Tổng High/Medium/Low risk, biểu đồ theo department, level
- **Trending**: Trend attrition rate qua thời gian
- **Detailed View**: Danh sách nhân viên, risk score, factor chính, recommended actions

**API:**
- `GET /api/v1/behavior/employee/{id}/metrics`
- `GET /api/v1/attrition-risk/high-risk-employees`
- `GET /api/v1/attrition-risk/employee/{id}/detailed-analysis`
- `POST /api/v1/attrition-risk/employee/{id}/action-plan`
- `GET /api/v1/attrition-risk/dashboard`

---

# 📦 MODULE ĐỀ XUẤT BỔ SUNG (31-41)

> **Ghi chú:** Các module dưới đây là đề xuất mở rộng ngoài phạm vi 30 module gốc, nhằm tăng tính cạnh tranh và giá trị thực tiễn của hệ thống. Đánh số tiếp nối để dễ tham chiếu, không thay đổi cấu trúc 30 module đã chốt.

---

## 31. Thị trường Nhân tài Nội bộ (Internal Talent Marketplace) [ĐÃ GỘP VÀO MODULE 02 TUYỂN DỤNG & HOÀN THÀNH 100%]

> ✅ **Ghi chú kiến trúc:** Module này đã được **tích hợp và hoàn thiện 100% trong Module 02 — Recruitment** (`com.cyclosa.recruitment`).
> - **Cơ chế triển khai:** Cung cấp đầy đủ các Entity (`InternalOpportunity`, `InternalApplication`, `InternalAssignment`), DTOs, Service và REST Controller với 13 API endpoints:
>   - Đăng cơ hội dự án ngắn hạn & vị trí mở nội bộ: `POST/GET/PUT /api/v1/internal-opportunities`
>   - Nhân viên bày tỏ quan tâm (Express Interest): `POST /api/v1/internal-opportunities/{id}/express-interest`
>   - Đánh giá & duyệt ứng viên nội bộ: `PUT /api/v1/internal-applications/{id}/review`
>   - Gợi ý cơ hội: `GET /api/v1/internal-opportunities/recommended`
>   - Phân công nhiệm vụ nội bộ (Assignment): `POST/GET /api/v1/internal-assignments`
>   - Hoàn thành & chấm điểm nhiệm vụ: `PUT /api/v1/internal-assignments/{id}/complete`
>   - Báo cáo thống kê: `GET /api/v1/internal-marketplace/stats`

**Mô tả:** Cho phép nhân viên chủ động tìm kiếm cơ hội phát triển trong nội bộ công ty — dự án ngắn hạn ở phòng ban khác hoặc vị trí trống — mà không cần chờ đề xuất từ manager.

**Luồng hoạt động:**

1. HR/Manager đăng **Internal Opportunity** (dự án ngắn hạn hoặc vị trí trống), gắn kỹ năng yêu cầu, thời gian, phòng ban
2. Nhân viên xem danh sách cơ hội phù hợp với **Skill Profile** của mình (Module 33)
3. Nhân viên **tự ứng tuyển** (Express Interest) — không bắt buộc qua manager hiện tại trước
4. Hệ thống thông báo cho manager hiện tại (để biết, không phải để chặn)
5. Manager của cơ hội mới xem hồ sơ, phỏng vấn nội bộ nếu cần (dùng lại Module 2.1 Interview Management)
6. Nếu chọn:
   - Với dự án ngắn hạn: tạo **Assignment** (không đổi `organizational_unit_id` chính thức, chỉ gắn thêm)
   - Với vị trí chính thức: chuyển qua **Transfer Request** (Module 27)
7. Theo dõi **tỷ lệ tham gia**, **tỷ lệ thành công** của marketplace theo thời gian

**API:**
- `POST/GET/PUT/DELETE /api/v1/internal-opportunities`
- `POST /api/v1/internal-opportunities/{id}/express-interest`
- `GET /api/v1/internal-opportunities/recommended?employee_id=`
- `POST/GET /api/v1/internal-assignments`
- `PUT /api/v1/internal-assignments/{id}/complete`
- `GET /api/v1/internal-marketplace/stats`

---

## 32. Mô phỏng Lộ trình Sự nghiệp (Career Pathing Simulator)

**Mô tả:** Gợi ý lộ trình thăng tiến khả thi cho từng nhân viên, dựa trên dữ liệu thăng tiến thực tế của những người có hồ sơ tương tự trong quá khứ.

**Luồng hoạt động:**

1. Hệ thống phân tích **Employee History** (Module 04) của toàn bộ nhân viên đã từng thăng tiến/chuyển vị trí
2. Với 1 nhân viên cụ thể, tìm nhóm **"hồ sơ tương tự"** (cùng vị trí xuất phát, kỹ năng, thâm niên tương đương)
3. Tổng hợp: "X% nhân viên hồ sơ tương tự sau Y năm đã lên vị trí Z"
4. Hiển thị **lộ trình gợi ý** (1-3 hướng phổ biến nhất) kèm kỹ năng còn thiếu (tham chiếu Module 33 Skill Gap)
5. Nhân viên có thể lưu lộ trình quan tâm, theo dõi tiến độ theo thời gian
6. HR xem tổng hợp xu hướng thăng tiến theo phòng ban (phục vụ quy hoạch nhân sự)

**Lưu ý:** Đây là gợi ý mang tính tham khảo dựa trên dữ liệu lịch sử, không phải cam kết thăng tiến.

**API:**
- `GET /api/v1/career-simulator/employee/{id}/suggested-paths`
- `GET /api/v1/career-simulator/employee/{id}/similar-profiles`
- `POST /api/v1/career-simulator/employee/{id}/save-path`
- `GET /api/v1/career-simulator/department/{id}/trends`

---

## 33. Bản đồ Kỹ năng & Phân tích Khoảng trống (Skill Graph & Gap Analysis)

**Mô tả:** Xây dựng bản đồ kỹ năng toàn công ty (độc lập với chức danh), phát hiện khoảng trống kỹ năng theo phòng ban/dự án, gợi ý đào tạo.

**Luồng hoạt động:**

1. Xây dựng **Skill Taxonomy** (danh mục kỹ năng chuẩn hóa, có phân nhóm: kỹ thuật, mềm, quản lý...)
2. Thu thập **Employee Skill Profile** từ nhiều nguồn:
   - Tự khai báo
   - Trích xuất từ CV đã parse (Module 23)
   - Suy ra từ khóa đào tạo đã hoàn thành (Module 10)
   - Manager xác nhận/đánh giá mức độ (beginner/intermediate/expert)
3. HR/Manager định nghĩa **Required Skills** cho từng vị trí hoặc dự án sắp tới
4. Hệ thống tính **Gap Score** = kỹ năng yêu cầu - kỹ năng hiện có (theo cá nhân, theo team, theo phòng ban)
5. Gợi ý **khóa đào tạo tương ứng** (Module 10) để lấp khoảng trống
6. Dashboard: bản đồ nhiệt (heatmap) kỹ năng theo phòng ban, top khoảng trống toàn công ty

**API:**
- `POST/GET/PUT/DELETE /api/v1/skill-taxonomy`
- `POST/GET/PUT /api/v1/employees/{id}/skill-profile`
- `POST/GET/PUT /api/v1/positions/{id}/required-skills`
- `GET /api/v1/skill-gap/employee/{id}`
- `GET /api/v1/skill-gap/department/{id}`
- `GET /api/v1/skill-gap/company-heatmap`
- `GET /api/v1/skill-gap/{id}/recommended-trainings`

---

## 34. Mạng lưới Cựu Nhân viên (Alumni Network) [ĐÃ LƯỢC BỎ KHỎI PHẠM VI]

> ⚠️ **Ghi chú kiến trúc:** Module này đã được **lược bỏ** khỏi phạm vi dự án do bản chất là diễn đàn mạng xã hội cựu nhân viên độc lập (Alumni Portal/Social CRUD), không ảnh hưởng đến luồng vận hành nhân sự và dữ liệu hàng ngày của doanh nghiệp.

**Mô tả:** Duy trì liên lạc với cựu nhân viên chất lượng tốt sau khi nghỉ việc, phục vụ tuyển dụng lại (boomerang hiring) và giới thiệu ứng viên (referral).

**Luồng hoạt động:**

1. Khi Offboarding (Module 16) hoàn tất, HR đánh giá **Alumni Eligibility** (loại trừ trường hợp sa thải vì kỷ luật nghiêm trọng)
2. Cựu nhân viên đủ điều kiện được mời tham gia **Alumni Portal** (tùy chọn, cần đồng ý)
3. Alumni có thể:
   - Cập nhật thông tin liên hệ, tình trạng công việc hiện tại
   - Xem tin tuyển dụng công khai của công ty
   - Giới thiệu ứng viên (liên kết Module 02 Recruitment — referral)
4. HR gửi bản tin định kỳ (company update, sự kiện alumni)
5. Khi có vị trí phù hợp, HR có thể chủ động liên hệ **Boomerang Candidate**
6. Nếu tuyển lại, liên kết hồ sơ cũ (Employee History) để tham khảo — không tạo hồ sơ mới hoàn toàn

**API:**
- `POST/GET/PUT /api/v1/alumni`
- `POST /api/v1/alumni/{id}/invite`
- `PUT /api/v1/alumni/{id}/opt-in`
- `POST /api/v1/alumni/{id}/referrals`
- `GET /api/v1/alumni/boomerang-candidates`
- `POST /api/v1/alumni/{id}/rehire`

---

## 35. Radar Tuân thủ Pháp lý (Compliance Radar)

**Mô tả:** Theo dõi chủ động các thay đổi trong luật lao động/BHXH/thuế TNCN, tự động rà soát tài liệu/chính sách bị ảnh hưởng trước khi luật có hiệu lực.

**Luồng hoạt động:**

1. Admin cấu hình **Legal Source Feed** (nguồn theo dõi: cổng thông tin pháp luật, văn bản mới ban hành)
2. Hệ thống (hoặc HR nhập thủ công) ghi nhận **Legal Change** mới: văn bản, ngày hiệu lực, phạm vi ảnh hưởng
3. Hệ thống rà soát chéo với:
   - **Contract Rules** đang áp dụng (Module 05)
   - **Policy Documents** hiện hành (Module 29)
   - **Payroll Rules** (Module 08 — thuế, BHXH)
4. Đánh dấu các mục **bị ảnh hưởng**, gán mức độ ưu tiên, deadline cần xử lý trước khi luật hiệu lực
5. Gửi cảnh báo cho HR/Legal team, theo dõi tiến độ xử lý (cập nhật chính sách, sửa hợp đồng mẫu, cấu hình lại rule tính lương)
6. Lưu **lịch sử tuân thủ** — chứng minh công ty đã chủ động cập nhật đúng hạn

**API:**
- `POST/GET/PUT /api/v1/compliance/legal-changes`
- `POST /api/v1/compliance/legal-changes/{id}/impact-scan`
- `GET /api/v1/compliance/legal-changes/{id}/affected-items`
- `PUT /api/v1/compliance/legal-changes/{id}/status`
- `GET /api/v1/compliance/dashboard`

---

## 36. Ứng lương Linh hoạt (Earned Wage Access)

**Mô tả:** Cho phép nhân viên rút một phần lương đã làm (chưa tới kỳ trả lương chính thức), với phí thấp hoặc miễn phí.

**Luồng hoạt động:**

1. Hệ thống tính **Earned Wage Balance** hàng ngày = (lương theo giờ/ngày công đã làm trong kỳ) × ngày công thực tế − các khoản đã ứng trước đó
2. Nhân viên xem số dư khả dụng trên ESS (Module 17)
3. Nhân viên tạo **Wage Advance Request** (số tiền muốn ứng, tối đa theo % số dư khả dụng — cấu hình theo chính sách công ty)
4. Duyệt tự động (nếu trong hạn mức) hoặc qua Workflow nếu vượt ngưỡng
5. Giải ngân qua kênh cấu hình (chuyển khoản, ví điện tử)
6. Khoản đã ứng được **khấu trừ tự động** vào kỳ lương chính thức gần nhất (Module 08)
7. Báo cáo: tần suất sử dụng, tổng chi phí chương trình, tác động đến dòng tiền công ty

**Lưu ý pháp lý:** Cần đảm bảo cơ chế này không bị hiểu là "cho vay lãi suất" hoặc vi phạm quy định trả lương đầy đủ, đúng hạn theo Bộ luật Lao động — nên tham vấn pháp lý trước khi triển khai.

**API:**
- `GET /api/v1/wage-access/employee/{id}/balance`
- `POST/GET /api/v1/wage-access/requests`
- `PUT /api/v1/wage-access/requests/{id}/approve`
- `POST /api/v1/wage-access/requests/{id}/disburse`
- `GET /api/v1/wage-access/program-report`

---

## 37. Phát hiện Bất thường Bảng lương (Payroll Anomaly Detection)

**Mô tả:** Dùng phân tích dữ liệu/ML để phát hiện bất thường trong bảng lương trước khi chi trả, hỗ trợ chống gian lận và phát hiện lỗi tính toán.

**Luồng hoạt động:**

1. Sau khi Module 08 tính xong bảng lương kỳ hiện tại, hệ thống chạy **Anomaly Scan** tự động
2. Các loại bất thường được kiểm tra:
   - Lương tăng/giảm đột biến so với kỳ trước (vượt ngưỡng %)
   - Số giờ OT bất thường (vượt xa trung bình lịch sử của nhân viên/phòng ban)
   - **Trùng số tài khoản ngân hàng** giữa nhiều nhân viên khác nhau
   - Nhân viên đã có trạng thái "Terminated" nhưng vẫn phát sinh lương
   - Phụ cấp/khoản khấu trừ không khớp với hồ sơ cấu hình
3. Mỗi bất thường được gắn **mức độ rủi ro** (cao/trung bình/thấp) và lý do cụ thể
4. HR/Kế toán xem danh sách cảnh báo, xác nhận hợp lệ hoặc yêu cầu điều chỉnh **trước khi duyệt chi trả**
5. Ghi nhận lịch sử xử lý cảnh báo (audit — ai xác nhận, khi nào)

**API:**
- `POST /api/v1/payroll/{period_id}/anomaly-scan`
- `GET /api/v1/payroll/{period_id}/anomalies`
- `PUT /api/v1/payroll/anomalies/{id}/resolve`
- `GET /api/v1/payroll/anomalies/history`

---

## 38. Chia sẻ Ngày phép (Time-off Donation) [ĐÃ LƯỢC BỎ KHỎI PHẠM VI]

> ⚠️ **Ghi chú kiến trúc:** Module này đã được **lược bỏ** khỏi phạm vi dự án. Tính năng nhân viên tặng phép năm cho nhau không phù hợp với quy định tại Điều 113 & 114 Bộ luật Lao động 2019 (ngày phép năm gắn liền với thâm niên và công sức cá nhân người lao động); đồng thời gây xung đột lớn khi tính toán thanh toán tiền phép tồn khi thôi việc và khấu trừ thuế TNCN.

**Mô tả:** Cho phép nhân viên tặng ngày phép năm dư của mình cho đồng nghiệp đang gặp khó khăn (ốm đau dài ngày, biến cố gia đình).

**Luồng hoạt động:**

1. Nhân viên cần hỗ trợ (hoặc HR thay mặt) tạo **Leave Donation Request** — nêu lý do, số ngày cần
2. HR xét duyệt điều kiện nhận (thường yêu cầu đã dùng hết phép cá nhân + có xác nhận y tế/hoàn cảnh nếu cần)
3. Công bố nhu cầu (ẩn danh hoặc công khai theo lựa chọn người nhận) cho đồng nghiệp
4. Nhân viên khác **tặng ngày phép năm dư** của mình (không được tặng quá số dư hiện có, không tặng phép đã âm)
5. Hệ thống trừ ngày phép từ người tặng, cộng vào số dư người nhận — ghi nhận vào **Leave Balance History** (Module 07) của cả hai bên
6. Khi đủ số ngày cần hoặc hết thời hạn kêu gọi → đóng chương trình

**API:**
- `POST/GET /api/v1/leave-donations/requests`
- `PUT /api/v1/leave-donations/requests/{id}/approve`
- `POST /api/v1/leave-donations/requests/{id}/donate`
- `GET /api/v1/leave-donations/requests/{id}/progress`

---

## 39. Trợ lý AI cho Buổi 1-1 (AI Meeting/1-1 Assistant)

**Mô tả:** Hỗ trợ tóm tắt buổi 1-1 giữa manager và nhân viên, trích xuất action items, theo dõi việc thực hiện ở lần gặp sau.

**Luồng hoạt động:**

1. Manager lên lịch **1-1 Meeting** với nhân viên (có thể tích hợp calendar)
2. Trong/sau buổi họp, ghi âm hoặc nhập ghi chú (**cần sự đồng ý của nhân viên** — hiển thị rõ khi bắt đầu ghi âm)
3. Hệ thống AI xử lý:
   - Tóm tắt nội dung chính buổi trò chuyện
   - Trích xuất **Action Items** (việc cần làm, người phụ trách, deadline)
4. Manager/nhân viên xác nhận/chỉnh sửa bản tóm tắt trước khi lưu chính thức
5. Ở buổi 1-1 tiếp theo, hệ thống hiển thị lại **Action Items kỳ trước** để theo dõi tiến độ hoàn thành
6. Dữ liệu 1-1 (không phải nội dung nhạy cảm) có thể tổng hợp phục vụ Module 40 (Manager Effectiveness Score)

**Lưu ý quyền riêng tư:** Ghi âm/tóm tắt chỉ nhân viên và manager trực tiếp xem được, trừ khi có yêu cầu pháp lý hoặc khiếu nại chính thức cần HR can thiệp.

**API:**
- `POST/GET /api/v1/one-on-ones`
- `POST /api/v1/one-on-ones/{id}/recording` (yêu cầu consent flag)
- `POST /api/v1/one-on-ones/{id}/summarize`
- `PUT /api/v1/one-on-ones/{id}/summary` (chỉnh sửa xác nhận)
- `POST/GET /api/v1/one-on-ones/{id}/action-items`
- `PUT /api/v1/one-on-ones/action-items/{id}/complete`

---

## 40. Chỉ số Hiệu quả Quản lý (Manager Effectiveness Score)

**Mô tả:** Đánh giá hiệu quả quản lý của từng manager dựa trên dữ liệu đã có sẵn trong hệ thống, không cần khảo sát bổ sung.

**Luồng hoạt động:**

1. Hệ thống tổng hợp định kỳ (hàng tháng/quý) các chỉ số của từng manager, từ dữ liệu sẵn có:
   - Tỷ lệ nghỉ việc của team (Module 16, 30)
   - Thời gian trung bình phản hồi/duyệt yêu cầu (leave, workflow — Module 07, 18)
   - Tần suất tổ chức 1-1 với nhân viên (Module 39)
   - Kết quả performance review trung bình của team (Module 09)
   - eNPS trong team (nếu survey có phân theo team — Module 15)
2. Tính **Manager Effectiveness Score** tổng hợp (trọng số có thể cấu hình)
3. Hiển thị dashboard cho HR/cấp quản lý cao hơn: xu hướng theo thời gian, so sánh giữa các manager cùng cấp
4. Nếu điểm thấp bất thường hoặc giảm liên tục → gợi ý HR trao đổi/coaching, **không tự động đánh giá kỷ luật**
5. Manager có thể xem điểm số của chính mình + gợi ý cải thiện

**Lưu ý đạo đức:** Chỉ số này mang tính hỗ trợ phát triển, cần minh bạch tiêu chí với manager, tránh dùng làm căn cứ duy nhất cho quyết định nhân sự quan trọng (thăng chức/kỷ luật).

**API:**
- `GET /api/v1/manager-effectiveness/{manager_id}/score`
- `GET /api/v1/manager-effectiveness/{manager_id}/breakdown`
- `GET /api/v1/manager-effectiveness/company-comparison`
- `PUT /api/v1/manager-effectiveness/config-weights` (admin cấu hình trọng số)

---

## 41. Mô phỏng Tái cơ cấu (What-if Org Simulation)

**Mô tả:** Mở rộng "Impact Preview" của Module 01 — cho phép mô phỏng nhiều kịch bản tái cơ cấu trước khi thực thi, xem tác động tổng hợp.

**Luồng hoạt động:**

1. HR/lãnh đạo tạo **Simulation Scenario** (kịch bản: gộp phòng, tách phòng, chuyển nhóm nhân viên, đổi cost center)
2. Trên giao diện kéo-thả, thử nghiệm các thay đổi **mà không ảnh hưởng dữ liệu thật**
3. Hệ thống tính toán ngay:
   - Số nhân viên bị ảnh hưởng theo từng thay đổi
   - Tác động chi phí (Cost Center — Module 01)
   - Số rule phân quyền / Workflow cần cập nhật (tái sử dụng logic Impact Preview đã có ở Module 01)
   - Thời gian thực thi ước tính
4. So sánh nhiều kịch bản song song (Scenario A vs B vs C)
5. Khi chọn được phương án, **áp dụng chính thức** → chuyển thành thao tác thật trên Module 01 (ghi nhận effective date, audit trail như bình thường)
6. Lưu lịch sử các kịch bản đã từng mô phỏng (kể cả kịch bản không chọn) để tham khảo sau

**API:**
- `POST/GET/DELETE /api/v1/org-simulations`
- `POST /api/v1/org-simulations/{id}/changes` (thêm thay đổi vào kịch bản)
- `GET /api/v1/org-simulations/{id}/impact-summary`
- `GET /api/v1/org-simulations/compare?ids=`
- `POST /api/v1/org-simulations/{id}/apply` (chuyển thành thay đổi thật)

---

## 42. Quản lý Sơ đồ Chỗ ngồi (Seating Chart / Workplace Management)

**Mô tả:** Quản lý không gian làm việc vật lý dưới dạng sơ đồ trực quan, cho phép nhân viên tìm vị trí đồng nghiệp, HR/Admin bố trí chỗ ngồi, và hỗ trợ mô hình làm việc linh hoạt (hot-desking).

### 42.1 Phân cấp Không gian (Space Hierarchy)

Mô hình cây 5 cấp, mỗi cấp là 1 entity riêng (tương tự cách Module 01 tách 3 chiều tổ chức — không lồng cứng logic nghiệp vụ vào cấu trúc không gian):

```
Chi nhánh/Tòa nhà (Site/Building)
  └─ Tầng (Floor)
       └─ Khu vực/Phòng (Zone/Room)
            └─ Dãy (Row)
                 └─ Chỗ ngồi (Seat)
```

- **Site/Building**: liên kết với `branch_id` (Module 01 — chiều Địa lý), có thể 1 chi nhánh có nhiều tòa nhà
- **Floor**: thuộc 1 Building, có sơ đồ nền (floor plan) riêng
- **Zone/Room**: khu vực chức năng trong tầng (open space, phòng riêng, zone theo phòng ban)
- **Row**: dãy bàn trong 1 Zone (phục vụ đặt tên có quy tắc + hiển thị dạng lưới)
- **Seat**: đơn vị nhỏ nhất, có tọa độ (x, y) trên sơ đồ

**Quy ước đặt tên (Naming Convention):**
- Mã chỗ ngồi tự sinh theo cấu trúc: `{Site}-{Floor}-{Zone}-{Row}-{Seat}`
- Ví dụ: `HN-F8-Z1-R02-S15` = Hà Nội - Tầng 8 - Zone 1 - Dãy 2 - Ghế 15
- Cho phép admin tùy chỉnh pattern đặt tên theo từng site (không bắt buộc cứng 1 format toàn công ty)

### 42.2 Thuộc tính Chỗ ngồi (Seat Attributes)

| Nhóm thuộc tính | Chi tiết |
|---|---|
| **Loại chỗ ngồi** (`seat_type`) | `dedicated` (cố định — gán cứng 1 nhân viên) / `hot_desk` (linh hoạt — đặt theo ngày/ca) / `visitor` (khách/thực tập sinh) |
| **Trạng thái** (`status`) | `available` / `occupied` / `maintenance` / `blocked` (tạm khóa) |
| **Tiện ích** (`amenities`) | Danh sách tag tùy chọn: 2 màn hình, docking station, standing desk, gần ổ cắm, gần cửa sổ... (danh mục mở, admin tự thêm tag mới) |
| **Tài sản gắn liền** (`linked_assets`) | Liên kết trực tiếp tới **Module 13 (Asset Management)**: mã máy tính, IP phone extension, màn hình phụ — khi asset được cấp phát/thu hồi ở Module 13, trạng thái hiển thị trên sơ đồ tự cập nhật |

### 42.3 Không gian Phụ trợ Phi-chỗ-ngồi (Non-desk Spaces)

Sơ đồ không chỉ hiển thị bàn làm việc mà còn các đối tượng định hướng và tiện ích:

- **Phòng họp (Meeting Room)** — hiển thị trên sơ đồ, có thể click để xem lịch trống/đặt phòng (tích hợp **Module 18 Workflow** nếu cần duyệt phòng lớn, hoặc đặt trực tiếp nếu phòng nhỏ)
- **Tiện ích chung**: cửa ra vào, lối thoát hiểm, WC, pantry, máy in, tủ locker cá nhân — đây là các **Space Object** không có seat_id, chỉ mang tính hiển thị/định hướng, một số loại (locker) có thể gán cho nhân viên tương tự dedicated seat

### 42.4 Trình chỉnh sửa Sơ đồ (Layout Editor — UI/UX)

**Mô tả:** Cho phép Admin/Facility tự thiết kế sơ đồ khớp với mặt bằng thực tế, không cần lập trình.

**Chức năng editor:**
1. Upload **floor plan nền** (ảnh/PDF mặt bằng) làm lớp tham chiếu (background layer), có thể chỉnh độ trong suốt để canh chỉnh
2. Kéo-thả (drag & drop) các object lên canvas: Seat, Row (tạo hàng loạt theo lưới), Zone, Meeting Room, Non-desk Object
3. Vẽ/resize/xoay từng object tự do để khớp với mặt bằng thật (không bắt buộc lưới vuông cứng)
4. Tạo hàng loạt Seat theo Row (VD: "tạo 1 dãy 10 ghế, cách đều X px") — tránh phải kéo thả từng ghế một
5. Gán thuộc tính (42.2) trực tiếp trên canvas khi click chọn từng Seat
6. **Versioning sơ đồ**: mỗi lần chỉnh sửa lớn tạo phiên bản mới, giữ lịch sử (tương tự nguyên tắc audit của Module 01) — hữu ích khi tái bố trí văn phòng
7. Preview chế độ xem của nhân viên (read-only) trước khi publish

### 42.5 Tìm kiếm & Bộ lọc (Search & Filter)

- **Tìm theo tên nhân viên** → tự động highlight (nhấp nháy) vị trí ghế tương ứng trên sơ đồ, tự động pan/zoom tới đúng khu vực
- **Tô màu theo phòng ban** (color-coded by department) — mỗi `organizational_unit` (Module 01) có màu riêng, dễ nhận diện khu vực của team nào
- **Lọc ghế trống** (`status = available`) theo loại (dedicated/hot-desk) — hỗ trợ HR gán chỗ nhanh cho nhân viên mới (liên kết Module 03 Onboarding)
- **Lọc theo tiện ích** (VD: chỉ hiện ghế có 2 màn hình + gần cửa sổ)
- **Lọc theo trạng thái** (available/occupied/maintenance/blocked)

### 42.6 Luồng Hoạt động Tổng thể

1. Admin/Facility dùng **Layout Editor** thiết kế sơ đồ theo cấu trúc 5 cấp, publish sơ đồ chính thức
2. HR **gán chỗ ngồi cố định** cho nhân viên (thủ công, hoặc gợi ý tự động khi Onboarding — Module 03) → cập nhật `status = occupied`, ghi `employee_id` vào Seat
3. Với **hot-desk**: nhân viên tự đặt chỗ theo ngày qua ESS (Module 17) — đặt trước, check-in khi tới, tự giải phóng khi hết ca/không check-in
4. Khi nhân viên **Transfer** (Module 27) hoặc **Offboarding** (Module 16) → hệ thống nhắc HR cập nhật/giải phóng chỗ ngồi tương ứng
5. Khi asset (Module 13) được cấp phát/thu hồi tại 1 chỗ ngồi → đồng bộ hiển thị "tài sản gắn liền" trên sơ đồ
6. Nhân viên bất kỳ dùng tính năng **tìm đồng nghiệp** trên ESS/sơ đồ công khai (giới hạn thông tin hiển thị theo phân quyền — không hiện dữ liệu nhạy cảm, chỉ tên + vị trí ghế)
7. Facility định kỳ rà soát **tỷ lệ sử dụng** (occupancy rate) — đặc biệt với hot-desk, phục vụ quyết định tối ưu diện tích văn phòng

**API:**
- `POST/GET/PUT/DELETE /api/v1/workplace/sites`
- `POST/GET/PUT/DELETE /api/v1/workplace/floors`
- `POST/GET/PUT/DELETE /api/v1/workplace/zones`
- `POST/GET/PUT/DELETE /api/v1/workplace/rows`
- `POST/GET/PUT/DELETE /api/v1/workplace/seats`
- `POST /api/v1/workplace/rows/{id}/bulk-generate-seats` (tạo hàng loạt ghế theo dãy)
- `PUT /api/v1/workplace/seats/{id}/assign` (gán nhân viên — dedicated)
- `PUT /api/v1/workplace/seats/{id}/release`
- `PUT /api/v1/workplace/seats/{id}/status`
- `POST/GET/DELETE /api/v1/workplace/seats/{id}/linked-assets`
- `POST/GET/PUT/DELETE /api/v1/workplace/non-desk-objects` (phòng họp, WC, pantry, locker...)
- `POST/GET /api/v1/workplace/hotdesk-bookings`
- `PUT /api/v1/workplace/hotdesk-bookings/{id}/check-in`
- `PUT /api/v1/workplace/hotdesk-bookings/{id}/cancel`
- `POST/GET /api/v1/workplace/floor-plans` (upload ảnh/PDF nền + version)
- `GET /api/v1/workplace/floor-plans/{id}/versions`
- `GET /api/v1/workplace/search?employee_name=` (tìm nhân viên → trả tọa độ ghế)
- `GET /api/v1/workplace/seats/search?filter=` (lọc theo trạng thái/loại/tiện ích/phòng ban)
- `GET /api/v1/workplace/occupancy-report?filter=`

**Liên kết với module khác:**
| Module | Mối liên hệ |
|---|---|
| 01 (Tổ chức) | `branch_id` cho Site; màu sắc theo `organizational_unit_id` khi hiển thị sơ đồ |
| 03 (Onboarding) | Gợi ý gán chỗ ngồi khi tạo Process Item thiết bị/chỗ làm |
| 13 (Tài sản) | Đồng bộ tài sản gắn với từng Seat |
| 16 (Offboarding) | Nhắc giải phóng chỗ ngồi khi nhân viên nghỉ việc |
| 17 (ESS) | Nhân viên tự đặt hot-desk, tìm đồng nghiệp |
| 27 (Transfer) | Nhắc cập nhật chỗ ngồi khi chuyển bộ phận/chi nhánh |

---

## 📌 Nền tảng Dùng chung: Analytics & Intelligence Platform

> Các module có yếu tố AI/ML/Phân tích dữ liệu (**20, 23, 24, 25, 26, 30, 37, 40**) nên được xây trên **1 nền tảng hạ tầng dùng chung**, thay vì mỗi module tự triển khai riêng lẻ:

| Layer dùng chung | Module nghiệp vụ sử dụng |
|---|---|
| **AI/ML Gateway Service** (1 điểm gọi chung cho toàn hệ thống) | 23, 24, 25, 26, 30, 37, 40 |
| **Feature Store** (tính sẵn đặc trưng hành vi nhân viên, dùng lại nhiều nơi) | 30, 32, 37, 40 |
| **Document Intelligence Engine** (OCR + Parser dùng chung) | 23, 25 |
| **Chuẩn Confidence Score + Human Override + Audit Trail** | Áp dụng cho mọi output AI trong toàn hệ thống |

**Lý do:** Tránh trùng lặp hạ tầng (mỗi module tự làm OCR/embedding riêng), đảm bảo tính nhất quán khi audit quyết định có yếu tố AI.

---

## 📌 GHI CHÚ KIẾN TRÚC CHUNG

### **Architecture Principles:**
- **Event-driven** giữa các module (message queue)
- **Workflow Engine (18)** + **Notification Service (19)** là 2 service dùng chung
- **Batch jobs định kỳ:** tính leave balance, payroll, cảnh báo HĐLĐ, quét đề xuất hủy, dự báo attrition, v.v.

### **Data Management:**
- **3 chiều tổ chức độc lập (01)** — mọi module phải tham chiếu tới `organizational_unit_id`
- **Không cache phân quyền** — đánh giá lại mỗi request (dữ liệu nhạy cảm như lương, kỷ luật)
- **Versioning cơ cấu/luồng** — không sửa đè lịch sử, ghi audit kèm ngày hiệu lực

### **Compliance & Audit:**
- Tuân thủ **Bộ luật Lao động 2019** (Module 05, 07, 12)
- **Audit trail đầy đủ** (Module 21) — giá trị trước/sau, ngày, người, IP
- **Right to be forgotten** (Module 21) — quản lý quyền riêng tư dữ liệu

### **Security:**
- JWT authentication + role-based access control
- Mã hóa dữ liệu nhạy cảm (lương, CMND, số ĐT)
- Mask dữ liệu nhạy cảm trong logs
- 2FA/MFA cho admin, HR
- Rate limiting API

---

**End of Document - HRM v2.0 Hoàn Chỉnh**