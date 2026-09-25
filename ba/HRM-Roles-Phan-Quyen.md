# HRM - Xác định Role & Ma trận Phân quyền

## 1. Danh sách Role đề xuất

| # | Role | Mô tả | Phạm vi dữ liệu |
|---|---|---|---|
| 1 | **Super Admin** | Quản trị toàn hệ thống, cấu hình, phân quyền, tích hợp. Không thao tác nghiệp vụ hàng ngày. | Toàn bộ hệ thống, mọi công ty (multi-company) |
| 2 | **HR Admin / HR Manager** | Trưởng phòng Nhân sự. Có quyền cao nhất trong phạm vi nghiệp vụ HR: duyệt cấp cao, cấu hình chính sách. | Toàn bộ nhân viên trong công ty/chi nhánh phụ trách |
| 3 | **HR Specialist (theo mảng)** | Chuyên viên HR phụ trách từng mảng: Tuyển dụng, C&B (Lương thưởng), Đào tạo, Hành chính nhân sự. Có thể tách thành 4 role con nếu công ty lớn. | Theo mảng phụ trách, toàn bộ nhân viên |
| 4 | **Department Manager / Line Manager** | Trưởng phòng ban trực tiếp. Duyệt các yêu cầu của nhân viên thuộc phòng, đánh giá hiệu suất. | Nhân viên trực thuộc phòng ban mình quản lý |
| 5 | **Team Leader** | Trưởng nhóm. Quyền hạn tương tự Manager nhưng phạm vi nhỏ hơn (cấp team), thường không duyệt các quyết định lớn (hợp đồng, lương). | Nhân viên trong team mình phụ trách |
| 6 | **Employee** | Nhân viên chính thức/thử việc. Chỉ thao tác trên dữ liệu của chính mình qua ESS. | Chỉ dữ liệu cá nhân (self) |
| 7 | **Recruiter** | Có thể là HR hoặc cộng tác viên tuyển dụng. Quản lý tin tuyển dụng, hồ sơ ứng viên, lịch phỏng vấn. | Module Tuyển dụng, Onboarding (giai đoạn đầu) |
| 8 | **Interviewer** | Nhân viên nội bộ được mời phỏng vấn (không thuộc HR). Chỉ xem hồ sơ ứng viên liên quan và nhập đánh giá phỏng vấn. | Chỉ các buổi phỏng vấn được gán |
| 9 | **Candidate (Ứng viên)** | Người dùng ngoài hệ thống công ty, dùng cổng ứng tuyển. | Chỉ hồ sơ/ứng tuyển của chính mình |
| 10 | **Payroll/Accountant (Kế toán lương)** | Bộ phận Kế toán/Tài chính, xử lý và đối soát số liệu lương, thuế, bảo hiểm. Có thể độc lập với HR để tách bạch kiểm soát. | Module Payroll, Benefits (liên quan tài chính) |
| 11 | **IT Admin** | Phụ trách tài khoản hệ thống, tích hợp, thiết bị cấp phát. | Module Onboarding (account provisioning), Asset Management, System Integration |
| 12 | **Executive / Board of Directors (BOD)** | Ban lãnh đạo. Chỉ xem báo cáo, dashboard tổng quan, không thao tác nghiệp vụ chi tiết. | Chỉ xem (read-only) Module Reports & Analytics, dashboard toàn công ty |
| 13 | **Auditor / Compliance** | Kiểm toán nội bộ hoặc tuân thủ. Xem audit log, hồ sơ hợp đồng, dữ liệu lương phục vụ kiểm tra, không có quyền chỉnh sửa. | Read-only trên toàn hệ thống, đặc biệt Audit Log, Contract, Payroll |
| 14 | **Union Representative (Đại diện công đoàn)** *(tùy chọn)* | Đại diện người lao động, xem/xử lý các vụ việc liên quan khen thưởng-kỷ luật-khiếu nại. | Module Reward & Discipline (phần liên quan công đoàn) |

> **Lưu ý:** "Approver" không nên thiết kế là 1 role cố định, mà là **thuộc tính động** gắn theo Approval Matrix (Module 18) — ví dụ "quản lý trực tiếp của nhân viên X" — vì người duyệt thay đổi theo từng nhân viên/phòng ban, không phải một nhóm quyền tĩnh.

---

## 2. Ma trận phân quyền theo Module

Ký hiệu: **F** = Full (CRUD) · **A** = Approve/Xử lý · **V** = View only · **O** = Chỉ dữ liệu của bản thân (Own) · **–** = Không truy cập

| Module | Super Admin | HR Admin | HR Specialist | Dept Manager | Team Leader | Employee | Recruiter | Interviewer | Payroll/Acct | IT Admin | Executive | Auditor |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 01. Tổ chức | F | F | V | V | V | – | – | – | V | – | V | V |
| 02. Tuyển dụng | F | F | F | A (manpower req) | – | – | F | V (giới hạn) | – | – | V | V |
| 03. Onboarding | F | F | F | V | V | O | V | – | – | F (account) | – | V |
| 04. Hồ sơ nhân viên | F | F | F | V (phòng mình) | V (team mình) | O | – | – | V | – | – | V |
| 05. Hợp đồng | F | F | F | V (phòng mình) | – | O | – | – | V | – | V | V |
| 06. Chấm công & Ca | F | F | F | A (phòng mình) | A (team mình) | O | – | – | V | – | V | V |
| 07. Nghỉ phép | F | F | F | A (phòng mình) | A (team mình) | O | – | – | V | – | V | V |
| 08. Lương | F | F (cấu hình) | F (nhập liệu) | V (giới hạn, nếu cần) | – | O (payslip) | – | – | F | – | V (chi phí) | V |
| 09. Hiệu suất | F | F | F | A/F (phòng mình) | A (team mình) | O | – | – | – | – | V | V |
| 10. Đào tạo | F | F | F | A (đề xuất) | V | O | – | – | – | – | V | V |
| 11. Phúc lợi | F | F | F | V | V | O | – | – | V | – | V | V |
| 12. Khen thưởng & Kỷ luật | F | F | F | A (đề xuất) | V | O | – | – | – | – | V | V |
| 13. Tài sản | F | F | F (HR hành chính) | V | V | O | – | – | – | F | – | V |
| 14. Career & Talent | F | F | F | A (đề xuất) | V | O | – | – | – | – | V | V |
| 15. Khảo sát | F | F | F | V (kết quả team) | V | O (trả lời) | – | – | – | – | V | V |
| 16. Offboarding | F | F | F | A (phòng mình) | V | O (resignation) | – | – | V (final payroll) | F (account) | V | V |
| 17. ESS | – | – | – | – | – | F (chính mình) | – | – | – | – | – | – |
| 18. Workflow/Approval | F | F | V | A (theo vai trò duyệt) | A (theo vai trò duyệt) | – (chỉ tạo request) | – | – | – | – | – | V |
| 19. Notification | F | F | V | V | V | V (nhận) | – | – | – | – | – | – |
| 20. Reports & Analytics | F | F | F (theo mảng) | V (phòng mình) | V (team mình) | – | V (tuyển dụng) | – | V (lương) | – | F (toàn công ty) | F |
| 21. System Admin | F | – | – | – | – | – | – | – | – | F (giới hạn IT) | – | V (audit log) |
| 31. Talent Marketplace | F | F | F | A (cơ hội phòng mình) | V | O (ứng tuyển) | – | – | – | – | V | V |
| 32. Career Pathing Simulator | F | F | V | V (team mình) | – | O | – | – | – | – | V | – |
| 33. Skill Graph & Gap Analysis | F | F | F | V (phòng mình) | V (team mình) | O (khai báo) | – | – | – | – | V | – |
| 34. Alumni Network | F | F | F | – | – | – | F | – | – | – | V | – |
| 35. Compliance Radar | F | F (Legal/C&B) | V | – | – | – | – | – | V | – | V | F (view) |
| 36. Earned Wage Access | F | V | – | – | – | O (tạo yêu cầu) | – | – | F | – | V | V |
| 37. Payroll Anomaly Detection | F | V | – | – | – | – | – | – | F | – | – | V |
| 38. Time-off Donation | F | A | F | – | – | O (tặng/nhận) | – | – | – | – | – | – |
| 39. AI 1-1 Assistant | F | – | – | F (team mình) | F (team mình) | O (xem của mình) | – | – | – | – | – | – |
| 40. Manager Effectiveness Score | F | F | V | O (điểm của mình) | O (điểm của mình) | – | – | – | – | – | V | V |
| 41. What-if Org Simulation | F | F | – | – | – | – | – | – | – | – | V | – |
| 42. Seating Chart / Workplace | F | F | F | V (phòng mình) | V (team mình) | O (xem sơ đồ, đặt hot-desk) | – | – | – | F (tài sản gắn liền) | – | – |

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
- **Module 40 (Manager Effectiveness Score):** Dept Manager/Team Leader chỉ xem điểm **của chính mình** (O), không xem được điểm của manager khác — tránh so sánh trực tiếp gây tiêu cực nội bộ; chỉ HR Admin/Executive mới xem bảng so sánh toàn công ty.
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