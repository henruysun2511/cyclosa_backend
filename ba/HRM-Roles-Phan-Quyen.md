# HRM - Xác định Role & Ma trận Phân quyền

## 1. Danh sách 10 Role chuẩn hóa sau tinh gọn

### Nhóm 1: Quản trị & Nghiệp vụ Nhân sự
| # | Role | Mã Role (Code) | Mô tả | Phạm vi dữ liệu (Data Scope) |
|---|---|---|---|---|
| 1 | **Super Admin** | `SUPER_ADMIN` | Quản trị cao nhất toàn hệ thống, cấu hình nền tảng, phân quyền tenant, cấu hình kỹ thuật | Toàn bộ hệ thống, mọi công ty (`ALL`) |
| 2 | **HR Admin / HR Manager** | `HR_ADMIN` | Trưởng phòng Nhân sự, toàn quyền quản trị nghiệp vụ HR trong phạm vi công ty | Toàn bộ nhân viên trong công ty (`COMPANY`) |
| 3 | **HR Specialist** | `HR_SPECIALIST` | Chuyên viên Nhân sự thực thi nghiệp vụ (Hồ sơ, Hợp đồng, C&B, Đào tạo, Khen thưởng - Kỷ luật...) | Toàn công ty theo mảng phụ trách (`COMPANY`) |
| 4 | **Recruiter** | `RECRUITER` | Chuyên viên tuyển dụng, quản lý tin đăng, nguồn ứng viên, điều phối phỏng vấn (không xem lương C&B) | Phân hệ Tuyển dụng & Onboarding (`COMPANY`) |
| 5 | **Payroll / Accountant** | `PAYROLL_ACCOUNTANT` | Kế toán tiền lương, đối soát bảng lương, bảo hiểm, quyết toán thuế TNCN — độc lập với HR | Module Payroll, Chi phí nhân sự (`COMPANY`) |

### Nhóm 2: Cán bộ Quản lý & Vận hành
| # | Role | Mã Role (Code) | Mô tả | Phạm vi dữ liệu (Data Scope) |
|---|---|---|---|---|
| 6 | **Executive / BOD** | `EXECUTIVE` | Ban Lãnh đạo (BOD/CEO), xem dashboard BI tổng thể và báo cáo phân tích chiến lược | Chỉ xem (Read-only) toàn công ty (`COMPANY` / `ALL`) |
| 7 | **Manager** | `MANAGER` | Quản lý và phê duyệt đơn từ, đề xuất nhân sự, đánh giá KPI — *(gộp Department Manager + Team Leader)* | Xác định động bằng `data_scope` (`DEPARTMENT` / `TEAM`) + `org_unit_id` |

### Nhóm 3: Nhân viên & Hỗ trợ Chuyên trách
| # | Role | Mã Role (Code) | Mô tả | Phạm vi dữ liệu (Data Scope) |
|---|---|---|---|---|
| 8 | **Employee** | `EMPLOYEE` | Nhân viên (Chính thức/Thử việc/Thực tập), sử dụng cổng tự phục vụ nhân viên (ESS) | Chỉ dữ liệu của bản thân (`OWN`) |
| 9 | **Office Admin** | `OFFICE_ADMIN` | Quản trị cơ sở vật chất, mặt bằng, sơ đồ chỗ ngồi và cấp phát/thu hồi tài sản công nghệ — *(gộp IT Admin + Facility Admin)* | Module Tài sản, Mặt bằng & Chỗ ngồi (`COMPANY`) |
| 10 | **Auditor / Compliance** | `AUDITOR` | Kiểm toán nội bộ, giám sát Audit Logs, rà soát tính hợp lệ quy trình độc lập với Admin | Read-only Audit Logs và Báo cáo tuân thủ (`ALL` / `COMPANY`) |

> 💡 **Ghi chú các điểm tinh gọn & hợp nhất:**
> - **Gộp `DEPARTMENT_MANAGER` & `TEAM_LEADER` thành `MANAGER`:** Tránh phân mảnh role theo cấp bậc tĩnh. Quyền quản lý và phê duyệt được quyết định linh hoạt qua cấp độ `data_scope` (`DEPARTMENT` hoặc `TEAM`) gắn với `org_unit_id` của cán bộ quản lý.
> - **Gộp `IT_ADMIN` & `FACILITY_ADMIN` thành `OFFICE_ADMIN`:** Hợp nhất quản trị tài sản trang thiết bị làm việc, hạ tầng công nghệ và mặt bằng/sơ đồ chỗ ngồi (Module 13 & 42).
> - **Chuyển `INTERVIEWER` thành cơ chế gán hội đồng (Panel Assignment):** Người tham gia phỏng vấn là nhân sự/quản lý nội bộ được chỉ định theo từng buổi qua bảng `interview_panel_members`, thao tác qua quyền chấm điểm Scorecard được cấp theo phiên thay vì cấp role tĩnh.
> - ~~**Candidate (Ứng viên):**~~ *[ĐÃ LOẠI BỎ]* Ứng viên nộp hồ sơ qua Landing Page bên ngoài (bảng `candidates`), không phải tài khoản người dùng nội bộ (`users`).
> - ~~**Union Representative (Đại diện công đoàn):**~~ *[ĐÃ LOẠI BỎ]* Đại diện công đoàn là nhân sự kiêm nhiệm, tham gia cuộc họp kỷ luật qua danh sách thành phần họp (`meeting_attendees`).

---

## 2. Ma trận phân quyền theo Module (10 Roles)

Ký hiệu: **F** = Full (CRUD) · **A** = Approve/Xử lý · **V** = View only · **O** = Chỉ dữ liệu của bản thân (Own) · **–** = Không truy cập

| Module | Super Admin | HR Admin | HR Specialist | Recruiter | Payroll Acct | Executive | Manager | Employee | Office Admin | Auditor |
|---|---|---|---|---|---|---|---|---|---|---|
| 01. Tổ chức & What-if | F | F | V | – | V | V | V | – | – | V |
| 02. Tuyển dụng | F | F | F | F | – | V | A (đề xuất) | – | – | V |
| 03. Onboarding | F | F | F | V | – | – | V | O | F (tài sản/setup) | V |
| 04. Hồ sơ nhân viên | F | F | F | – | V | – | V (đơn vị) | O | V (liên hệ) | V |
| 05. Hợp đồng | F | F | F | – | V | V | V (đơn vị) | O | – | V |
| 06. Chấm công & Ca | F | F | F | – | V | V | A (đơn vị) | O | – | V |
| 07. Nghỉ phép | F | F | F | – | V | V | A (đơn vị) | O | – | V |
| 08. Lương & Chi phí | F | F (cấu hình) | F (nhập liệu) | – | F | V (chi phí) | V (quỹ lương) | O (payslip) | – | V |
| 09. Hiệu suất (KPI/OKR) | F | F | F | – | – | V | A/F (đơn vị) | O | – | V |
| 12. Khen thưởng - Kỷ luật | F | F | F | – | – | V | A (đề xuất) | O | – | V |
| 13. Quản lý Tài sản | F | F | V | – | – | – | V | O | F | V |
| 14. Career & Talent | F | F | F | – | – | V | A (đề xuất) | O | – | V |
| 16. Offboarding | F | F | F | – | V (final pay) | V | A (đơn vị) | O (đơn xin) | F (thu hồi tài sản) | V |
| 17. ESS (Tự phục vụ) | – | – | – | – | – | – | – | F (cá nhân) | – | – |
| 18. Workflow/Approval | F | F | V | – | – | – | A (theo ma trận) | – (tạo request) | – | V |
| 19. Thông báo nội bộ | F | F | V | – | – | – | V | V (nhận) | V | – |
| 20. Báo cáo & Phân tích | F | F | F (mảng) | V (tuyển dụng) | V (lương) | F (toàn cty) | V (đơn vị) | – | V (tài sản) | F |
| 21. Quản trị hệ thống | F | – | – | – | – | – | – | – | V (phân bổ) | V (audit log) |
| 31. Talent Marketplace | F | F | F | – | – | V | A (dự án) | O (ứng tuyển) | – | V |
| 35. Compliance Radar | F | F (Legal) | V | – | V | V | – | – | – | F (view) |
| 36. Ứng lương linh hoạt | F | V | – | – | F | V | – | O (yêu cầu) | – | V |
| 39. AI 1-1 Assistant | F | – | – | – | – | – | F (đơn vị) | O (của mình) | – | – |
| 42. Sơ đồ chỗ ngồi | F | F | F | – | – | – | V (đơn vị) | O (xem/hotdesk) | F (layout/ghế) | – |

---

## 3. Nguyên tắc thiết kế phân quyền (Authorization Design)

1. **Role-Based Access Control (RBAC)** kết hợp **Data Scope**: mỗi role gắn với quyền thao tác (permission) trên từng module, đồng thời giới hạn theo phạm vi dữ liệu (own / team / department / company / all).
2. **Dynamic Approver:** người duyệt xác định qua Organization Structure (Module 01) + Approval Matrix (Module 18), không hard-code theo role.
3. **Row-level security cho Employee/ESS:** mọi API Module 17 phải tự động filter theo `employee_id` của token đăng nhập, không cho phép truyền tham số để xem dữ liệu người khác.
4. **Tách bạch Payroll khỏi HR thường:** dữ liệu lương nên có nhóm quyền riêng, kể cả HR Specialist thông thường cũng không nên xem được lương người khác nếu không thuộc mảng C&B.
5. **Audit toàn bộ hành động Approve/Reject/Sửa lương/Sửa hợp đồng** — bắt buộc ghi Audit Log (Module 21), Auditor chỉ có quyền xem, không chỉnh sửa.
6. **Multi-company:** nếu tập đoàn nhiều công ty con, cần thêm chiều `company_id` vào mọi permission check — 1 HR Admin công ty A mặc định không thấy dữ liệu công ty B trừ khi được gán thêm quyền.
7. **Permission nên định nghĩa dạng `module:action`** (VD: `payroll:process`, `leave:approve`, `employee:view_salary`) để linh hoạt gán vào Role tùy biến, thay vì cố định cứng role ↔ quyền.

## 3.1 Ghi chú phân quyền cho Module Đề Xuất Bổ Sung (31-42)

- **Interview Management (2.1, mở rộng Module 02):** dùng lại nguyên role **Interviewer** đã có — chỉ mở rộng phạm vi: Interviewer giờ chấm điểm theo Scorecard (`interview_kits`) thay vì nhận xét tự do, quyền vẫn giới hạn "chỉ các buổi phỏng vấn được gán" như cũ.
- **Module 42 (Seating Chart):** không tạo role mới — dùng **HR Admin/IT Admin** cho việc thiết kế sơ đồ (Layout Editor), **Dept Manager/Team Leader** chỉ xem (V) khu vực phòng/team mình để hỗ trợ định hướng nhân viên mới. Nếu công ty có bộ phận Hành chính/Facility riêng biệt với HR, cân nhắc thêm role **Facility Admin** (F trên Module 42, V trên Module 01/04) thay vì gán toàn quyền cho HR Admin.
- **Manager Effectiveness Score (Đã gộp vào Module 20):** Dept Manager/Team Leader chỉ xem điểm **của chính mình** (O), không xem được điểm của manager khác — tránh so sánh trực tiếp gây tiêu cực nội bộ; chỉ HR Admin/Executive mới xem bảng so sánh toàn công ty. Cấu hình trọng số chỉ dành cho Super Admin/HR Admin.
- **What-if Org Simulation (Đã gộp vào Module 01):** Quyền tạo/chạy kịch bản mô phỏng và áp dụng vào sơ đồ thật chỉ dành riêng cho **Super Admin & HR Admin** (F), **Executive** (V để xem báo cáo tác động).
- **Module 39 (AI 1-1 Assistant):** dữ liệu ghi âm/tóm tắt chỉ 2 bên (manager + nhân viên) truy cập — kể cả HR Admin mặc định không có quyền V trừ khi có khiếu nại chính thức cần can thiệp (ghi nhận qua Module 12 Grievance).
- **Module 35 (Compliance Radar):** nên giới hạn F cho HR Specialist mảng C&B/Legal thay vì mọi HR Specialist, tương tự nguyên tắc tách bạch Payroll ở mục 3.4.

---

## 4. Bảng thiết kế dữ liệu Role/Permission (đề xuất backend)

```
roles            (id, name, description, company_id, is_system_role)
permissions      (id, code, module, action, description)   -- VD: code = "leave.approve"
role_permissions (role_id, permission_id, data_scope)       -- data_scope: own/team/department/company/all
user_roles       (user_id, role_id, company_id, branch_id)  -- hỗ trợ multi-company
```

**API liên quan:**
- `POST/GET/PUT/DELETE /api/v1/roles`
- `POST/GET/PUT/DELETE /api/v1/permissions`
- `PUT /api/v1/roles/{id}/permissions` – gán quyền cho role (kèm data_scope)
- `PUT /api/v1/users/{id}/roles` – gán role cho user
- `GET /api/v1/users/{id}/effective-permissions` – tính toán quyền thực tế của user (dùng để check ở middleware)