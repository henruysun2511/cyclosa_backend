# HRM - Thứ tự Code Module Backend (Build Roadmap)

> Thứ tự này khác với sơ đồ **Employee Lifecycle** (luồng nghiệp vụ) — ở đây sắp xếp theo **phụ thuộc kỹ thuật**: module nào bị nhiều module khác gọi tới thì phải code trước.

---

## 1. Nguyên tắc sắp xếp

1. **Module nền tảng trước, module nghiệp vụ sau.** Auth/User/Permission và Organization là điều kiện để bất kỳ API nào khác hoạt động.
2. **Module lõi dữ liệu (Employee) code sớm.** ~90% các bảng ở module khác có FK `employee_id`.
3. **Service dùng chung (Workflow, Notification) code trước nhóm nghiệp vụ cần duyệt/nhắc việc.** Nếu code sau, phải quay lại sửa toàn bộ module đã xong để tích hợp gọi ngược — tốn công gấp đôi.
4. **Nhóm "vận hành lõi" (Attendance, Leave, Contract, Payroll) ưu tiên hơn nhóm mở rộng** — đây là tập tính năng tối thiểu để hệ thống dùng được thật (MVP).
5. **Module phụ thuộc dữ liệu từ module khác thì code sau module đó** (VD: Onboarding cần Asset Management đã có để gọi cấp phát thiết bị).
6. **ESS và Reports không phải 1 giai đoạn riêng** — đây là lớp API mỏng dựng trên module đã có, code song song ngay sau khi module gốc xong, không chờ tới cuối.

---

## 2. Bảng phụ thuộc giữa các module

| Module | Phụ thuộc vào (phải có trước) | Vì sao |
|---|---|---|
| 21. System Admin | *(không phụ thuộc — code đầu tiên)* | Auth/Role/Permission là nền cho mọi API |
| 01. Organization | 21 (users để gán manager_employee_id sau) | Department cần optional FK tới employees, nhưng có thể code song song, chỉ FK thêm sau |
| 04. Employee | 01, 21 | Cần company/department/position để gán, cần users để login |
| 18. Workflow | 04 | Approval Matrix cần resolve theo cơ cấu tổ chức + nhân viên |
| 19. Notification | 21 (users là người nhận) | notifications.user_id trỏ tới users |
| 05. Contract | 04 | contracts.employee_id |
| 06. Attendance & Shift | 04 | attendance_records.employee_id, cần photo_url ở Employee để xác thực khuôn mặt |
| 07. Leave | 04, 18 | leave_requests cần Workflow để duyệt |
| 08. Payroll | 04, 05 (basic_salary), 06 (công thực tế), 07 (phép có lương) | payroll_records cần dữ liệu từ cả 3 module này |
| 02. Recruitment | 01 (department/position), 18 (duyệt manpower request) | job_positions.department_id, manpower_requests qua Workflow |
| 03. Onboarding | 02 (Hiring), 04 (employee vừa tạo), 13 (Asset) | onboarding_processes.employee_id, cấp thiết bị gọi Module 13 |
| 13. Asset Management | 04 | asset_allocations.employee_id — cần có trước Module 03 gọi tới |
| 09. Performance | 04, 01 (kpis.department_id) | goals.employee_id |
| ~~10. Training~~ | *[ĐÃ LƯỢC BỎ]* | Bằng cấp/chứng chỉ lưu tại `employee_documents` |
| ~~11. Benefits~~ | *[ĐÃ LƯỢC BỎ]* | Phụ cấp & bảo hiểm xử lý trực tiếp tại Module 05 Contract & 08 Payroll |
| 12. Reward & Discipline | 04, 08 (reward tiền đẩy sang payroll) | rewards/disciplines.employee_id |
| 14. Career & Talent | 04, 01 (positions), 09 (dữ liệu hiệu suất) | succession_candidates cần performance history |
| ~~15. Engagement~~ | *[ĐÃ LƯỢC BỎ]* | Biểu mẫu khảo sát rời rạc, dùng công cụ chuyên dụng bên ngoài |
| 16. Offboarding | 04, 05, 06, 08, 13, 18 | Cần Contract Termination, Final Payroll, thu hồi Asset, Workflow duyệt |
| 17. ESS | *(tất cả module đã code tới đâu)* | Lớp API mỏng, code song song từng phần |
| 20. Reports | *(tất cả module đã code tới đâu)* | Query tổng hợp, code song song từng phần |
| **02.1 Interview Management** | 02 | Mở rộng bảng `interviews`/`interview_evaluations` đã có, không phải module độc lập |
| **31. Talent Marketplace** | 02, 04 | Đã tích hợp sẵn vào Module 02 Recruitment |
| **~~32. Career Pathing Simulator~~** | *[ĐÃ GỘP VÀO MODULE 14]* | Tích hợp thành tính năng mở rộng của Module 14 Career & Talent |
| ~~**33. Skill Graph & Gap Analysis**~~ | *[ĐÃ LƯỢC BỎ]* | Đánh giá năng lực tích hợp qua KPI/Goal & 360 Feedback ở Module 09 |
| ~~**34. Alumni Network**~~ | *[ĐÃ LƯỢC BỎ]* | Mạng xã hội cựu nhân viên độc lập |
| **35. Compliance Radar** | 05 (Contract), 08 (Payroll), 06 (Attendance) | Rà soát chéo hợp đồng mẫu, ca kíp và quy chế lương |
| **36. Earned Wage Access** | 06 (Attendance — công thực tế), 08 (Payroll) | Tính số dư khả dụng cần dữ liệu công + lương |
| **~~37. Payroll Anomaly Detection~~** | *[ĐÃ GỘP VÀO MODULE 08]* | Hợp nhất thành bước Audit & Anomaly Scan của Module 08 Payroll |
| ~~**38. Time-off Donation**~~ | *[ĐÃ LƯỢC BỎ]* | Không phù hợp Điều 113-114 BLLĐ 2019 |
| **39. AI 1-1 Assistant** | 04, 19 (Notification nhắc lịch) | Độc lập tương đối, có thể code sớm ở Phase 7 |
| **~~40. Manager Effectiveness Score~~** | *[ĐÃ GỘP VÀO MODULE 20]* | Hợp nhất thành phân hệ phân tích hiệu quả quản lý của Module 20 Reports |
| **~~41. What-if Org Simulation~~** | *[ĐÃ GỘP VÀO MODULE 01]* | Hợp nhất thành phân hệ mô phỏng mở rộng của Module 01 Organization |
| **42. Seating Chart / Workplace** | 01 (branch_id), 04 (employee), 13 (Asset), 27→Module 05/16 (transfer/offboarding) | Độc lập tương đối, có thể code song song sớm nếu ưu tiên |

---

## 3. Roadmap theo giai đoạn (Phase)

### Phase 0 — Hạ tầng dự án *(không phải module nghiệp vụ)*

- Khởi tạo repo, cấu trúc project, kết nối database (PostgreSQL)
- Base entity/audit fields (`id`, `created_at`, `updated_at`, `created_by`, `updated_by`), soft-delete middleware
- Cấu hình JWT authentication middleware, cấu trúc response chuẩn, xử lý lỗi tập trung
- CI/CD cơ bản, môi trường dev/staging

**Output:** Project chạy được, DB kết nối, chưa có API nghiệp vụ.

---

### Phase 1 — Nền tảng: System Admin + Organization

**Module 21 (System Admin — phần lõi):** `users`, `roles`, `permissions`, `role_permissions`, `user_roles` + API đăng nhập, middleware kiểm tra permission theo `data_scope`.

**Module 01 (Organization):** `companies`, `branches`, `departments`, `teams`, `positions`, `job_levels`, `cost_centers`.

> `audit_logs`, `system_settings`, `integrations`, `data_import_jobs` (phần còn lại của Module 21) có thể lùi sang Phase 7 — không chặn các module khác.

**Output:** Đăng nhập được, tạo được cơ cấu tổ chức mẫu. Milestone để bắt đầu seed dữ liệu test cho các phase sau.

---

### Phase 2 — Lõi dữ liệu: Employee Management

**Module 04:** `employees`, `employee_personal_info` (gồm `photo_url` cho xác thực khuôn mặt), `employee_employment_info`, `employee_emergency_contacts`, `employee_dependents`, `employee_documents`, `employee_history`.

**Output:** CRUD hồ sơ nhân viên đầy đủ. Từ đây có thể seed nhân viên mẫu để test toàn bộ các module còn lại song song.

---

### Phase 3 — Service dùng chung: Workflow + Notification

**Module 18:** `approval_matrices`, `workflow_instances`, `workflow_approval_steps`, `workflow_delegates`. Xây interface chuẩn để module khác gọi (`createWorkflowInstance(requestType, requestId, employeeId)`).

**Module 19:** `notification_templates`, `notifications`, `reminders`. Xây interface chuẩn (`sendNotification(eventCode, userId, data)`).

**Output:** 2 service dùng chung sẵn sàng — mọi module nghiệp vụ từ Phase 4 trở đi tích hợp thẳng vào đây thay vì tự viết logic duyệt/thông báo riêng.

---

### Phase 4 — Nhóm vận hành lõi (MVP)

Có thể chia song song cho nhiều dev vì ít phụ thuộc chéo lẫn nhau (chỉ cùng phụ thuộc Module 04/18):

- **Module 05 (Contract):** `contract_types`, `contracts`, `contract_amendments`, `contract_terminations`.
- **Module 06 (Attendance & Shift):** `shifts`, `shift_assignments`, `attendance_records` (tích hợp service nhận diện khuôn mặt), `overtime_requests` (qua Workflow), `attendance_corrections` (qua Workflow).
- **Module 07 (Leave):** `leave_types`, `leave_policies`, `leave_balances`, `leave_requests` (qua Workflow).

**Output:** Nhân viên có hợp đồng, chấm công được, xin nghỉ phép được — đủ để demo vòng lặp vận hành hàng ngày.

---

### Phase 5 — Payroll

**Module 08:** `salary_components`, `employee_salary_history`, `salary_advances` (qua Workflow), `payroll_periods`, `payroll_records`, `payroll_record_items`, `payroll_anomalies` (tích hợp tính năng Anomaly Detection của Module 37).

> Phụ thuộc trực tiếp dữ liệu từ Module 05 (basic_salary), 06 (công/OT thực tế), 07 (phép có lương) — bắt buộc code sau Phase 4, không thể làm song song.

**Output:** Chạy được 1 kỳ lương đầy đủ từ đầu đến cuối (tính công → tính lương → quét bất thường/fraud check → duyệt qua Workflow → xuất phiếu lương).

---

### Phase 6 — Tuyển dụng, Onboarding, Tài sản

- **Module 13 (Asset Management)** code trước tiên trong nhóm này vì Module 03 gọi tới nó.
- **Module 02 (Recruitment):** toàn bộ luồng từ Manpower Request đến Hiring/convert-to-employee (gọi ngược vào Module 04).
- **Module 03 (Onboarding):** gọi Module 13 (cấp thiết bị) và Module 04 (account provisioning liên kết employee).

**Output:** Vòng đời "trước khi vào công ty" hoàn chỉnh, khép kín với vòng lặp vận hành ở Phase 4-5.

---

### Phase 7 — Nhóm mở rộng

Độc lập tương đối với nhau, ưu tiên theo giá trị nghiệp vụ:

- **Module 09 (Performance)** — [x] ✅ Hoàn thành (`com.cyclosa.performance`)
- ~~**Module 10 (Training & Development)**~~ — ❌ *[ĐÃ LƯỢC BỎ]* Thuần CRUD, lưu chứng chỉ thay thế bằng `employee_documents` ở Profile/Onboarding.
- ~~**Module 11 (Benefits)**~~ — ❌ *[ĐÃ LƯỢC BỎ]* Thuần CRUD, các khoản phụ cấp và bảo hiểm đã xử lý trực tiếp trong Module 05 (Contract) & 08 (Payroll).
- **Module 12 (Reward & Discipline)** — [x] ✅ Hoàn thành (`com.cyclosa.discipline`) (Khen thưởng, Kỷ luật Điều 123/125/126 BLLĐ, Khiếu nại)
- **Module 14 (Career & Talent)** — [x] ✅ Hoàn thành (`com.cyclosa.talent`) (Lộ trình thăng tiến, Kế hoạch kế nhiệm, Kho nhân tài HiPo, Career Pathing Simulator gộp từ Module 32, ESS MyCareer)
- ~~**Module 15 (Engagement)**~~ — ❌ *[ĐÃ LƯỢC BỎ]* Biểu mẫu khảo sát rời rạc, độc lập với chuỗi cung ứng dữ liệu Core HRM.
- **Module 21 (phần còn lại):** `audit_logs`, `system_settings`, `integrations`, `data_import_jobs`

**Output:** Bộ tính năng đầy đủ ngoài MVP.

---

### Phase 8 — Offboarding — [x] ✅ Hoàn thành

**Module 16:** `resignations`, `terminations`, `exit_interviews`, `offboarding_clearances` (`com.cyclosa.offboarding`).

> Code sau cùng vì gọi tới hầu hết module trước đó: Contract Termination (05), Final Payroll (08), thu hồi Asset (13), khóa tài khoản (03), duyệt qua Workflow (18).

**Output:** Vòng đời nhân viên khép kín hoàn chỉnh từ tuyển dụng đến nghỉ việc.

---

### Phase 9 — Module Đề Xuất Bổ Sung (31-42)

> Toàn bộ nhóm này **không chặn MVP** (Phase 0-6) — chỉ nên bắt đầu sau khi vòng lặp vận hành lõi đã chạy ổn định. Thứ tự trong nhóm dựa trên phụ thuộc dữ liệu ở bảng mục 2.

**Bước 1 — Mở rộng có sẵn, ưu tiên trước (ít phụ thuộc mới):**
- **02.1 Interview Management** — chỉ mở rộng bảng đã có ở Module 02, làm ngay khi rảnh tay, không cần chờ Phase 9
- **42. Seating Chart / Workplace** — phụ thuộc 01/04/13 đã có sẵn từ Phase 1-6, có thể code song song sớm nhất trong nhóm
- **39. AI 1-1 Assistant** — chỉ cần 04 + 19, độc lập với các module 31-41 khác

**Bước 2 — Phụ thuộc dữ liệu vận hành & phát triển nhân sự:**
- ~~**33. Skill Graph & Gap Analysis**~~ — ❌ *[ĐÃ LƯỢC BỎ]* Đánh giá năng lực đã tích hợp qua KPI/Goal & 360 Feedback ở Module 09.
- **31. Talent Marketplace** (đã tích hợp vào Module 02 Recruitment)
- ~~**32. Career Pathing Simulator**~~ — *[ĐÃ GỘP VÀO MODULE 14]* Hợp nhất vào Module 14 quản lý tập trung trong package `com.cyclosa.talent`.

**Bước 4 — Phụ thuộc dữ liệu tài chính/vận hành đã ổn định:**
- **36. Earned Wage Access** (cần 06 + 08 chạy ổn định ít nhất 1-2 kỳ lương thật để tính số dư đúng)
- ~~**37. Payroll Anomaly Detection**~~ — *[ĐÃ GỘP VÀO MODULE 08]* Hợp nhất thành bước kiểm soát tự động trước khi chốt lương trong Module 08.
- ~~**38. Time-off Donation**~~ — ❌ *[ĐÃ LƯỢC BỎ]* Không phù hợp Điều 113-114 BLLĐ 2019.

**Bước 5 — Compliance & Governance:**
- **35. Compliance Radar** (độc lập, ưu tiên theo yêu cầu pháp lý thực tế của công ty)

**Bước 6 — Hoàn thiện các phân hệ mở rộng còn lại:**
- ~~**40. Manager Effectiveness Score**~~ — *[ĐÃ GỘP VÀO MODULE 20]* Hợp nhất thành dashboard phân tích trong Module 20 Reports.
- ~~**34. Alumni Network**~~ — ❌ *[ĐÃ LƯỢC BỎ]* Mạng xã hội cựu nhân viên độc lập.
- ~~**41. What-if Org Simulation**~~ — *[ĐÃ GỘP VÀO MODULE 01]* Hợp nhất thành tính năng mở rộng của Module 01 Organization.

**Output:** Bộ tính năng nâng cao hoàn chỉnh, không ảnh hưởng tiến độ MVP nếu ưu tiên đúng thứ tự trên.

---

### Xuyên suốt mọi Phase — không tách giai đoạn riêng

- **Module 17 (ESS):** Triển khai theo **Kiến trúc phân tán (Cách 2)** — không tạo package backend `com.cyclosa.ess` độc lập. Mỗi module nghiệp vụ sở hữu Controller `/api/v1/my-*` chuyên dụng cho nhân viên tự phục vụ (ví dụ `MyProfileController`, `MyAttendanceController`, `MyLeaveController`, `MyPayrollController`...), tự động filter theo `employee_id` từ token đăng nhập và trả về DTO tinh gọn.
- **Module 20 (Reports):** viết các query báo cáo ngay khi module nguồn có đủ dữ liệu, không dồn hết tới cuối dự án — tránh tình trạng cuối dự án phải quay lại hiểu logic của 20 module cùng lúc.

---

## 4. Bảng tổng hợp theo Phase

| Phase | Module | Có thể làm song song? |
|---|---|---|
| 0 | Hạ tầng dự án | — |
| 1 | 21 (lõi) + 01 *(gồm 41 What-if)* | Có thể tách 2 dev |
| 2 | 04 | Không (chờ Phase 1) |
| 3 | 18 + 19 | Có thể tách 2 dev |
| 4 | 05 + 06 + 07 | Có thể tách 3 dev |
| 5 | 08 *(gồm 37 Anomaly)* | Không (chờ Phase 4 xong cả 3) |
| 6 | 13 → 02 → 03 | 02 và 13 có thể song song, 03 chờ cả hai |
| 7 | 09, 12, 14 *(gồm 32 Career)*, 21(còn lại) *(10, 11, 15 đã lược bỏ)* | Song song được hầu hết, 14 chờ 09 |
| 8 | 16 | Không (chờ 05, 06, 08, 13, 18) |
| 9 | 02.1, 42, 39 → 31 → 36 → 35 *(32 gộp 14; 37 gộp 08; 40 gộp 20; 41 gộp 01; 33, 34, 38 đã lược bỏ)* | Từng bước con có thể song song trong nội bộ |
| Song song mọi lúc | 17, 20 *(gồm 40 Manager Score)* | Làm dần theo từng module đã xong |

---

## 5. Gợi ý phân bổ team (nếu có nhiều hơn 1 backend dev)

- **Dev A (Core/Platform):** Phase 0 → 1 → 2 → 3, sau đó chuyển sang hỗ trợ Phase 5 (Payroll — module phức tạp nhất) và Phase 8 (Offboarding — cần hiểu toàn hệ thống).
- **Dev B:** Từ Phase 4 trở đi nhận Module 06 (Attendance) + 07 (Leave), sau đó Module 09 + 10 ở Phase 7.
- **Dev C:** Từ Phase 4 nhận Module 05 (Contract), Phase 6 nhận 13 + 02 + 03, sau đó Module 11 + 12 + 15 ở Phase 7.
- **Module 17 (ESS) và 20 (Reports):** phân cho dev có băng thông rảnh ở mỗi phase, không gán cố định 1 người — vì nó bám theo tiến độ module gốc.
- **Phase 9 (Module 31-42):** không cần thêm dev riêng — sau khi Phase 8 xong, cả 3 dev A/B/C đều rảnh, phân theo mảng quen thuộc: Dev A nhận 33/31/32 (nền tảng dữ liệu + matching), Dev B nhận 36/37 (đụng Payroll — dev đã quen Module 08), Dev C nhận 42/39/38 (độc lập, ít phụ thuộc), cả 3 luân phiên 35/40/34/41 ở cuối.