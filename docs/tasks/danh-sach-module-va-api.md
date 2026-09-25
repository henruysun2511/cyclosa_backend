# CYCLOSA — Danh Sách Module & Kế Hoạch Triển Khai API (Module & API Roadmap)

> **Mục đích tài liệu:** Quản lý toàn diện lộ trình phát triển backend hệ sinh thái CYCLOSA HRM.
> Bảng theo dõi tiến độ từng Module và từng API với các trạng thái rõ ràng, quy định rõ module ràng buộc (tiền đề bắt buộc phải làm trước) và mã quyền tương ứng.

---

## 📌 Quy ước Trạng thái API (Status Check)

| Ký hiệu | Trạng thái | Ý nghĩa |
| :---: | :--- | :--- |
| ✅ | **Đã hoàn thành** (`[x]`) | API đã được viết xong, pass test, chốt chặn bảo mật và đã kiểm thử thành công. |
| ⏳ | **Chưa làm** (`[ ]`) | API đã được quy hoạch nhưng chưa bắt đầu triển khai code. |
| 🔄 | **Sửa** (`[-]`) | API đã có mã nguồn nhưng cần chỉnh sửa, bổ sung nghiệp vụ, tối ưu hóa hoặc fix bug. |

---

## 🗺️ BẢNG TỔNG QUAN CÁC MODULE VÀ RÀNG BUỘC PHÁT TRIỂN

| STT | Tên Module | Mô tả Chức năng | Module Ràng buộc (Cần làm trước) | Tiến độ API | Trạng thái Module |
| :---: | :--- | :--- | :--- | :---: | :---: |
| **01** | **Authentication & User** (`auth`, `user`) | Xác thực JWT, Refresh Token, OAuth2 Google, kích hoạt tài khoản, quản lý tài khoản User | *Không (Core)* | 13/13 API | ✅ Đã hoàn thành |
| **02** | **Role & Permission** (`role`, `permission`) | Quản trị vai trò (hệ thống/công ty), ma trận quyền RBAC, phân quyền theo 5 cấp DataScope | `01. Auth` | 12/12 API | ✅ Đã hoàn thành |
| **03** | **Organization Structure** (`organization`) | Quản lý Công ty/Chi nhánh, Cây sơ đồ Phòng ban, Tổ/Nhóm, Chức danh & Vị trí công việc | `01. Auth`, `02. Role` | 25/25 API | ✅ Đã hoàn thành |
| **04** | **Employee Profile** (`employee`) | Hồ sơ nhân sự, mã nhân viên, trạng thái làm việc, thông tin liên hệ, ngân hàng, người phụ thuộc, import Excel | `01. Auth`, `02. Role`, `03. Org` | 14/14 API | ✅ Đã hoàn thành |
| **05** | **Contract Management** (`contract`) | Hợp đồng lao động (thử việc/chính thức), phụ lục hợp đồng, lương cơ bản, cảnh báo hết hạn | `04. Employee` | 0/6 API | ⏳ Chưa làm |
| **06** | **Time & Attendance** (`attendance`) | Ca làm việc, phân ca, Check-in/Check-out, bảng công tổng hợp ngày/tháng, giải trình chấm công | `04. Employee`, `03. Org` | 22/22 API | ✅ Hoàn thành |
| **07** | **Leave Management** (`leave`) | Quỹ phép năm, chính sách thâm niên Điều 114, đơn xin nghỉ phép, duyệt đa cấp qua Workflow, thanh toán phép tồn Điều 113.3, tích hợp Timesheet & Payroll | `04. Employee`, `18. Workflow` | 21/21 API | ✅ Đã hoàn thành |
| **08** | **Payroll Management** (`payroll`) | Cấu hình thành phần lương, phụ cấp, tính lương tự động (công + HĐ), tạm ứng lương, duyệt bảng lương qua Workflow, phiếu lương ESS | `05. Contract`, `06. Attendance` | 23/23 API | ✅ Đã hoàn thành |
| **09** | **Recruitment & Onboarding** (`recruitment`) | Tin tuyển dụng, hồ sơ ứng viên (CV), lịch phỏng vấn, đánh giá & tiếp nhận nhân viên (Onboarding) | `03. Org`, `04. Employee` | 0/6 API | ⏳ Chưa làm |
| **10** | **Asset Management** (`asset`) | Danh mục tài sản công ty, cấp phát thiết bị cho nhân viên, thu hồi khi thôi việc, bảo hành báo hỏng | `04. Employee` | 0/5 API | ⏳ Chưa làm |
| **11** | **Performance Evaluation** (`performance`) | Thiết lập kỳ đánh giá KPI/OKR, chỉ số đánh giá, nhân viên tự đánh giá, quản lý duyệt xếp loại | `04. Employee`, `03. Org` | 0/5 API | ⏳ Chưa làm |
| **12** | **Notification & Audit Log** (`notification`, `audit`) | Thông báo In-app (WebSocket/SSE), gửi thông báo Email, nhật ký truy vết thao tác hệ thống | `01. Auth` | 0/4 API | ⏳ Chưa làm |

---

## 📋 DANH SÁCH CHI TIẾT API THEO TỪNG MODULE

### 1. Module Authentication & User (`auth`, `user`)
- **Mô tả:** Cung cấp hạ tầng xác thực an toàn, cấp phát Access/Refresh Token, kích hoạt qua mail, quản lý hồ sơ người dùng.
- **Ràng buộc:** *Làm đầu tiên (Nền tảng của toàn bộ hệ thống).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 1.1 | `POST` | `/api/v1/auth/login` | Đăng nhập tài khoản bằng Email & Password | `Public` | [x] ✅ Đã hoàn thành |
| 1.2 | `POST` | `/api/v1/auth/activate` | Kích hoạt tài khoản mới qua Token gửi từ Email | `Public` | [x] ✅ Đã hoàn thành |
| 1.3 | `POST` | `/api/v1/auth/refresh` | Cấp mới Access Token bằng Refresh Token | `Public` | [x] ✅ Đã hoàn thành |
| 1.4 | `POST` | `/api/v1/auth/logout` | Đăng xuất, hủy phiên và vô hiệu hóa Token | `Authenticated` | [x] ✅ Đã hoàn thành |
| 1.5 | `GET` | `/api/v1/auth/me` | Lấy thông tin tài khoản hiện tại kèm quyền sở hữu | `Authenticated` | [x] ✅ Đã hoàn thành |
| 1.6 | `POST` | `/api/v1/auth/forgot-password` | Gửi yêu cầu và email khôi phục mật khẩu | `Public` | [x] ✅ Đã hoàn thành |
| 1.7 | `POST` | `/api/v1/auth/reset-password` | Đặt lại mật khẩu mới qua mã xác thực từ email | `Public` | [x] ✅ Đã hoàn thành |
| 1.8 | `PUT` | `/api/v1/auth/change-password` | Đổi mật khẩu cá nhân cho người dùng đang đăng nhập | `Authenticated` | [x] ✅ Đã hoàn thành |
| 1.9 | `POST` | `/api/v1/auth/oauth2/google` | Đăng nhập tài khoản bằng Google ID Token | `Public` | [x] ✅ Đã hoàn thành |
| 1.10 | `GET` | `/api/v1/users` | Tìm kiếm và phân trang danh sách người dùng (`UserFilter`) | `user.view` | [x] ✅ Đã hoàn thành |
| 1.11 | `GET` | `/api/v1/users/{id}` | Xem chi tiết thông tin tài khoản theo ID | `user.view` | [x] ✅ Đã hoàn thành |
| 1.12 | `POST` | `/api/v1/users` | Tạo tài khoản người dùng mới & gửi email kích hoạt | `user.create` | [x] ✅ Đã hoàn thành |
| 1.13 | `PUT` | `/api/v1/users/{id}/status` | Khóa hoặc kích hoạt lại tài khoản người dùng | `user.manage_status` | [x] ✅ Đã hoàn thành |

---

### 2. Module Role & Permission (`role`, `permission`)
- **Mô tả:** Hệ thống phân quyền động RBAC kết hợp Data Scope (5 cấp: OWN, TEAM, DEPARTMENT, COMPANY, ALL).
- **Ràng buộc:** `01. Auth` *(Cần User để gán vai trò).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 2.1 | `GET` | `/api/v1/roles` | Lấy danh sách vai trò phân trang (`RoleFilter`) | `role.view` | [x] ✅ Đã hoàn thành |
| 2.2 | `GET` | `/api/v1/roles/{id}` | Lấy chi tiết thông tin một vai trò | `role.view` | [x] ✅ Đã hoàn thành |
| 2.3 | `POST` | `/api/v1/roles` | Tạo mới vai trò tùy biến cho công ty | `role.create` | [x] ✅ Đã hoàn thành |
| 2.4 | `PUT` | `/api/v1/roles/{id}` | Cập nhật thông tin vai trò (chống sửa vai trò hệ thống) | `role.update` | [x] ✅ Đã hoàn thành |
| 2.5 | `DELETE` | `/api/v1/roles/{id}` | Xóa vai trò tùy biến (chống xóa vai trò hệ thống) | `role.delete` | [x] ✅ Đã hoàn thành |
| 2.6 | `GET` | `/api/v1/roles/{id}/permissions` | Lấy ma trận quyền & Data Scope gán cho vai trò | `role.view` | [x] ✅ Đã hoàn thành |
| 2.7 | `PUT` | `/api/v1/roles/{id}/permissions` | Cập nhật danh mục quyền & Data Scope cho vai trò | `role.assign` | [x] ✅ Đã hoàn thành |
| 2.8 | `GET` | `/api/v1/permissions` | Lấy toàn bộ danh mục Permission có trong hệ thống | `role.view` | [x] ✅ Đã hoàn thành |
| 2.9 | `GET` | `/api/v1/permissions/modules` | Lấy danh sách quyền nhóm theo Module chức năng | `role.view` | [x] ✅ Đã hoàn thành |
| 2.10 | `GET` | `/api/v1/users/{id}/roles` | Lấy danh sách vai trò đã gán cho một người dùng | `user.view` | [x] ✅ Đã hoàn thành |
| 2.11 | `PUT` | `/api/v1/users/{id}/roles` | Gán danh sách vai trò cho người dùng theo Công ty | `user.assign_role` | [x] ✅ Đã hoàn thành |
| 2.12 | `GET` | `/api/v1/users/{id}/effective-permissions` | Tính toán gộp quyền thực thi cao nhất (Scope Merging) | `user.view` | [x] ✅ Đã hoàn thành |

---

### 3. Module Organization Structure (`organization`)
- **Mô tả:** Quản trị cấu trúc tổ chức doanh nghiệp: Công ty/Chi nhánh, cây sơ đồ Phòng ban, Tổ/Nhóm làm việc, Vị trí & Chức danh.
- **Ràng buộc:** `01. Auth`, `02. Role` *(Cần tài khoản và quyền để quản trị cơ cấu).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 3.1 | `GET` | `/api/v1/companies` | Danh sách các công ty/chi nhánh trong hệ sinh thái | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.2 | `GET` | `/api/v1/companies/{id}` | Lấy chi tiết thông tin công ty | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.3 | `POST` | `/api/v1/companies` | Tạo mới công ty con / chi nhánh | `organization.manage` | [x] ✅ Đã hoàn thành |
| 3.4 | `PUT` | `/api/v1/companies/{id}` | Cập nhật thông tin công ty | `organization.manage` | [x] ✅ Đã hoàn thành |
| 3.5 | `GET` | `/api/v1/organizational-units/tree` | Lấy cây sơ đồ phân cấp phòng ban (Parent/Child) | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.6 | `GET` | `/api/v1/organizational-units/{id}` | Lấy chi tiết thông tin phòng ban | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.7 | `POST` | `/api/v1/organizational-units` | Tạo phòng ban mới (chỉ định trực thuộc) | `organization.create` | [x] ✅ Đã hoàn thành |
| 3.8 | `PUT` | `/api/v1/organizational-units/{id}` | Cập nhật tên, mã phòng ban hoặc thông tin | `organization.update` | [x] ✅ Đã hoàn thành |
| 3.9 | `DELETE` | `/api/v1/organizational-units/{id}` | Xóa phòng ban (ràng buộc không còn đơn vị con) | `organization.delete` | [x] ✅ Đã hoàn thành |
| 3.10 | `GET` | `/api/v1/organizational-units/{id}/impact-preview` | Dự báo tác động trước khi điều chuyển cây | `organization.manage` | [x] ✅ Đã hoàn thành |
| 3.11 | `PUT` | `/api/v1/organizational-units/{id}/move` | Điều chuyển phòng ban sang nhánh cha mới (chống loop, ghi nhận lịch sử) | `organization.manage` | [x] ✅ Đã hoàn thành |
| 3.12 | `GET` | `/api/v1/organizational-units/{id}/history` | Xem lịch sử phiên bản / tái cơ cấu của phòng ban theo thời gian | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.13 | `GET` | `/api/v1/organizational-units/{id}/descendant-ids` | Lấy toàn bộ ID phòng ban và con cháu (kế thừa phân quyền dữ liệu) | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.14 | `GET` | `/api/v1/geography/tree` | Lấy cây địa lý vùng miền kèm chi nhánh | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.15 | `POST` | `/api/v1/geography/regions` | Tạo mới vùng miền địa lý | `organization.manage` | [x] ✅ Đã hoàn thành |
| 3.16 | `GET` | `/api/v1/geography/branches` | Danh sách chi nhánh làm việc | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.17 | `GET` | `/api/v1/geography/branches/{id}` | Chi tiết chi nhánh làm việc | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.18 | `POST` | `/api/v1/geography/branches` | Tạo mới chi nhánh làm việc (tọa độ GPS & bán kính) | `organization.create` | [x] ✅ Đã hoàn thành |
| 3.19 | `PUT` | `/api/v1/geography/branches/{id}` | Cập nhật thông tin chi nhánh | `organization.update` | [x] ✅ Đã hoàn thành |
| 3.20 | `GET` | `/api/v1/job-levels` | Danh sách cấp bậc công việc theo thứ bậc | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.21 | `POST` | `/api/v1/job-levels` | Tạo mới cấp bậc công việc | `organization.manage` | [x] ✅ Đã hoàn thành |
| 3.22 | `GET` | `/api/v1/positions` | Danh sách chức danh / vị trí công việc có phân trang | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.23 | `GET` | `/api/v1/positions/{id}` | Chi tiết chức danh việc làm | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.24 | `POST` | `/api/v1/positions` | Tạo mới chức danh việc làm | `organization.create` | [x] ✅ Đã hoàn thành |
| 3.25 | `PUT` | `/api/v1/positions/{id}` | Cập nhật chức danh việc làm | `organization.update` | [x] ✅ Đã hoàn thành |
| 3.26 | `GET` | `/api/v1/cost-centers` | Danh sách trung tâm chi phí | `organization.view` | [x] ✅ Đã hoàn thành |
| 3.27 | `POST` | `/api/v1/cost-centers` | Tạo mới trung tâm chi phí | `organization.manage` | [x] ✅ Đã hoàn thành |

---

### 4. Module Employee Profile (`employee`)
- **Mô tả:** Quản lý vòng đời nhân sự từ khi gia nhập đến nghỉ việc, thông tin cá nhân, hồ sơ bảo hiểm, ngân hàng, liên kết User Account.
- **Ràng buộc:** `01. Auth`, `02. Role`, `03. Organization` *(Nhân viên phải thuộc về 1 Phòng ban/Chức danh).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 4.1 | `GET` | `/api/v1/employees` | Lấy danh sách nhân viên phân trang theo DataScope | `employee.view` | [x] ✅ Đã hoàn thành |
| 4.2 | `GET` | `/api/v1/employees/{id}` | Xem chi tiết hồ sơ toàn diện (lọc theo DataScope) | `employee.view` | [x] ✅ Đã hoàn thành |
| 4.3 | `POST` | `/api/v1/employees` | Tiếp nhận nhân sự mới (hồ sơ 3 chiều + User linking) | `employee.create` | [x] ✅ Đã hoàn thành |
| 4.4 | `PUT` | `/api/v1/employees/{id}/personal-info` | Cập nhật thông tin cá nhân, CCCD, thuế, ngân hàng | `employee.update` | [x] ✅ Đã hoàn thành |
| 4.5 | `PUT` | `/api/v1/employees/{id}/employment-info` | Điều chuyển phòng ban, bổ nhiệm chức danh (ghi log lịch sử) | `employee.manage_job` | [x] ✅ Đã hoàn thành |
| 4.6 | `PUT` | `/api/v1/employees/{id}/status` | Đổi trạng thái làm việc (tự động khóa User khi thôi việc/sa thải) | `employee.manage_status`| [x] ✅ Đã hoàn thành |
| 4.7 | `GET` | `/api/v1/employees/{id}/dependents` | Lấy danh sách người phụ thuộc giảm trừ gia cảnh thuế TNCN | `employee.view` | [x] ✅ Đã hoàn thành |
| 4.8 | `POST` | `/api/v1/employees/{id}/dependents` | Đăng ký người phụ thuộc mới cho nhân sự | `employee.update` | [x] ✅ Đã hoàn thành |
| 4.9 | `DELETE`| `/api/v1/employees/{id}/dependents/{depId}` | Xóa thông tin người phụ thuộc | `employee.update` | [x] ✅ Đã hoàn thành |
| 4.10 | `GET` | `/api/v1/employees/{id}/emergency-contacts` | Lấy danh sách người liên hệ khẩn cấp | `employee.view` | [x] ✅ Đã hoàn thành |
| 4.11 | `POST` | `/api/v1/employees/{id}/emergency-contacts` | Thêm người liên hệ khẩn cấp | `employee.update` | [x] ✅ Đã hoàn thành |
| 4.12 | `DELETE`| `/api/v1/employees/{id}/emergency-contacts/{contactId}` | Xóa người liên hệ khẩn cấp | `employee.update` | [x] ✅ Đã hoàn thành |
| 4.13 | `GET` | `/api/v1/employees/{id}/history` | Xem toàn bộ lịch sử biến động công tác (Audit Trail) | `employee.view` | [x] ✅ Đã hoàn thành |
| 4.14 | `POST` | `/api/v1/employees/import` | Nhập danh sách nhân viên hàng loạt bằng Excel | `employee.import` | [x] ✅ Đã hoàn thành |
| 4.15 | `GET` | `/api/v1/employees/import-template` | Tải file Excel mẫu chuẩn (.xlsx) để nhập danh sách nhân viên | `employee.view` | [x] ✅ Đã hoàn thành |

---

### 5. Module Contract Management (`contract`)
- **Mô tả:** Quản lý các loại hợp đồng lao động (thử việc, xác định thời hạn, không xác định thời hạn), phụ lục hợp đồng, mức lương đóng BHXH.
- **Ràng buộc:** `04. Employee` *(Hợp đồng gắn liền với 1 hồ sơ nhân viên cụ thể).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 5.1 | `GET` | `/api/v1/contracts` | Lấy danh sách hợp đồng theo DataScope | `contract.view` | [ ] ⏳ Chưa làm |
| 5.2 | `GET` | `/api/v1/contracts/{id}` | Xem chi tiết hợp đồng & các phụ lục đính kèm | `contract.view` | [ ] ⏳ Chưa làm |
| 5.3 | `POST` | `/api/v1/contracts` | Tạo mới hợp đồng lao động cho nhân sự | `contract.create` | [ ] ⏳ Chưa làm |
| 5.4 | `PUT` | `/api/v1/contracts/{id}` | Cập nhật điều khoản hợp đồng | `contract.update` | [ ] ⏳ Chưa làm |
| 5.5 | `PUT` | `/api/v1/contracts/{id}/terminate` | Thanh lý / Chấm dứt hiệu lực hợp đồng | `contract.terminate` | [ ] ⏳ Chưa làm |
| 5.6 | `GET` | `/api/v1/contracts/expiring` | Cảnh báo danh sách hợp đồng sắp hết hạn trong 30-60 ngày | `contract.view` | [ ] ⏳ Chưa làm |

---

### 6. Module Time & Attendance (`attendance`)
- **Mô tả:** Ca làm việc, phân ca, Check-in/Check-out qua GPS/Wifi, tự động chốt công hàng ngày (Sweep 01:00 AM), tổng hợp bảng công tháng, giải trình bù công qua Workflow.
- **Ràng buộc:** `04. Employee`, `03. Organization`, `Workflow Engine` *(Cần thông tin nhân viên, chi nhánh và luồng duyệt đa cấp).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 6.1 | `GET` | `/api/v1/shifts` | Danh sách ca làm việc của công ty | `attendance.shift.view` | [x] ✅ Hoàn thành |
| 6.2 | `GET` | `/api/v1/shifts/{id}` | Chi tiết ca làm việc (kèm số nhân viên phân ca) | `attendance.shift.view` | [x] ✅ Hoàn thành |
| 6.3 | `POST` | `/api/v1/shifts` | Tạo mới ca làm việc | `attendance.shift.create` | [x] ✅ Hoàn thành |
| 6.4 | `PUT` | `/api/v1/shifts/{id}` | Cập nhật ca làm việc | `attendance.shift.update` | [x] ✅ Hoàn thành |
| 6.5 | `DELETE`| `/api/v1/shifts/{id}` | Xóa ca làm việc | `attendance.shift.delete` | [x] ✅ Hoàn thành |
| 6.6 | `POST` | `/api/v1/shift-assignments/batch` | Phân ca hàng loạt cho nhân sự | `attendance.schedule.manage` | [x] ✅ Hoàn thành |
| 6.7 | `GET` | `/api/v1/shift-assignments/my-schedule`| Xem lịch làm việc cá nhân | Xác thực cá nhân | [x] ✅ Hoàn thành |
| 6.8 | `GET` | `/api/v1/shift-assignments` | Tra cứu lịch phân ca theo DataScope | `attendance.schedule.view` | [x] ✅ Hoàn thành |
| 6.9 | `DELETE`| `/api/v1/shift-assignments/{id}` | Hủy phân ca làm việc | `attendance.schedule.manage` | [x] ✅ Hoàn thành |
| 6.10 | `POST` | `/api/v1/attendance/check-in` | Điểm danh vào ca (Check-in GPS Geofencing) | Xác thực cá nhân | [x] ✅ Hoàn thành |
| 6.11 | `POST` | `/api/v1/attendance/check-out` | Điểm danh ra ca (Check-out GPS, fallback ca đêm) | Xác thực cá nhân | [x] ✅ Hoàn thành |
| 6.12 | `GET` | `/api/v1/attendance/today` | Xem trạng thái điểm danh hôm nay của cá nhân | Xác thực cá nhân | [x] ✅ Hoàn thành |
| 6.13 | `GET` | `/api/v1/attendance/history` | Lịch sử chấm công cá nhân theo tháng | Xác thực cá nhân | [x] ✅ Hoàn thành |
| 6.14 | `GET` | `/api/v1/attendance/records` | Quản lý nhật ký chấm công toàn đơn vị (DataScope) | `attendance.record.view` | [x] ✅ Hoàn thành |
| 6.15 | `POST` | `/api/v1/attendance/explanations` | Gửi đơn giải trình chấm công (chống gửi trùng PENDING) | `attendance.explain.apply` | [x] ✅ Hoàn thành |
| 6.16 | `GET` | `/api/v1/attendance/explanations/my` | Danh sách đơn giải trình của cá nhân | Xác thực cá nhân | [x] ✅ Hoàn thành |
| 6.17 | `GET` | `/api/v1/attendance/explanations` | Danh sách đơn giải trình cần xử lý (DataScope) | `attendance.explain.view` | [x] ✅ Hoàn thành |
| 6.18 | `GET` | `/api/v1/attendance/explanations/{id}` | Chi tiết đơn giải trình và lịch sử luồng duyệt | `attendance.explain.view` | [x] ✅ Hoàn thành |
| 6.19 | `GET` | `/api/v1/timesheets/my-timesheet` | Xem bảng công cá nhân tháng này | Xác thực cá nhân | [x] ✅ Hoàn thành |
| 6.20 | `GET` | `/api/v1/timesheets/summary` | Bảng tổng hợp công toàn đơn vị (DataScope) | `attendance.timesheet.view` | [x] ✅ Hoàn thành |
| 6.21 | `POST` | `/api/v1/timesheets/recalculate` | Tính toán lại bảng công tháng | `attendance.timesheet.manage` | [x] ✅ Hoàn thành |
| 6.22 | `POST` | `/api/v1/timesheets/lock` | Khóa chốt bảng công tháng (bảo vệ snapshot cho Payroll) | `attendance.timesheet.lock` | [x] ✅ Hoàn thành |

---

### 7. Module Leave Management (`leave`)
- **Mô tả:** Danh mục 4 nhóm loại ngày nghỉ theo luật, cấu hình chính sách phép năm & thâm niên (Điều 113, 114 BLLĐ 2019), quản lý quỹ phép năm (Leave Balance), đơn xin nghỉ phép, duyệt đa cấp qua Workflow Engine, thanh toán phép tồn (Điều 113.3) và tích hợp Timesheet & Payroll.
- **Ràng buộc:** `04. Employee`, `18. Workflow` *(Cần nhân viên để cấp quỹ và Workflow Engine để duyệt đơn).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 7.1 | `GET` | `/api/v1/leave-types` | Danh mục loại ngày nghỉ phân trang & bộ lọc | `leave.view` | [x] ✅ Hoàn thành |
| 7.2 | `GET` | `/api/v1/leave-types/{id}` | Lấy chi tiết thông tin một loại ngày nghỉ | `leave.view` | [x] ✅ Hoàn thành |
| 7.3 | `POST` | `/api/v1/leave-types` | Tạo mới loại ngày nghỉ phép theo quy định | `leave.manage` | [x] ✅ Hoàn thành |
| 7.4 | `PUT` | `/api/v1/leave-types/{id}` | Cập nhật cấu hình loại ngày nghỉ phép | `leave.manage` | [x] ✅ Hoàn thành |
| 7.5 | `DELETE` | `/api/v1/leave-types/{id}` | Xóa mềm loại ngày nghỉ (ràng buộc không còn sử dụng) | `leave.manage` | [x] ✅ Hoàn thành |
| 7.6 | `GET` | `/api/v1/leave-policies` | Danh sách chính sách ngày phép công ty | `leave.view` | [x] ✅ Hoàn thành |
| 7.7 | `GET` | `/api/v1/leave-policies/{id}` | Chi tiết cấu hình chính sách ngày phép | `leave.view` | [x] ✅ Hoàn thành |
| 7.8 | `POST` | `/api/v1/leave-policies` | Khai báo chính sách phép theo điều kiện công việc | `leave.manage` | [x] ✅ Hoàn thành |
| 7.9 | `PUT` | `/api/v1/leave-policies/{id}` | Cập nhật chính sách phép năm và thâm niên | `leave.manage` | [x] ✅ Hoàn thành |
| 7.10 | `DELETE` | `/api/v1/leave-policies/{id}` | Xóa cấu hình chính sách nghỉ phép | `leave.manage` | [x] ✅ Hoàn thành |
| 7.11 | `GET` | `/api/v1/leave-balances/my` | Nhân viên tự xem số dư quỹ phép năm (ESS) | Xác thực cá nhân | [x] ✅ Hoàn thành |
| 7.12 | `GET` | `/api/v1/leave-balances` | Quản lý tra cứu quỹ phép toàn đơn vị (DataScope) | `leave.view` | [x] ✅ Hoàn thành |
| 7.13 | `POST` | `/api/v1/leave-balances/recalculate` | Khởi chạy tính toán lại quỹ phép năm theo BLLĐ | `leave.manage` | [x] ✅ Hoàn thành |
| 7.14 | `GET` | `/api/v1/leave-balances/{id}/unused-annual-leave-payout` | Tính thanh toán tiền phép năm chưa nghỉ khi thôi việc | `leave.view` | [x] ✅ Hoàn thành |
| 7.15 | `POST` | `/api/v1/leave-requests` | Tạo đơn xin nghỉ phép (tự động kiểm tra dư & gọi Workflow) | `leave.apply` | [x] ✅ Hoàn thành |
| 7.16 | `GET` | `/api/v1/leave-requests/my` | Xem lịch sử đơn xin nghỉ phép của bản thân (ESS) | Xác thực cá nhân | [x] ✅ Hoàn thành |
| 7.17 | `GET` | `/api/v1/leave-requests` | Danh sách đơn xin nghỉ phép phân quyền DataScope | `leave.view` | [x] ✅ Hoàn thành |
| 7.18 | `GET` | `/api/v1/leave-requests/{id}` | Chi tiết đơn nghỉ phép kèm lịch sử duyệt Workflow | `leave.view` | [x] ✅ Hoàn thành |
| 7.19 | `PUT` | `/api/v1/leave-requests/{id}/approve` | Phê duyệt đơn xin nghỉ phép | `leave.approve` | [x] ✅ Hoàn thành |
| 7.20 | `PUT` | `/api/v1/leave-requests/{id}/reject` | Từ chối đơn xin nghỉ phép kèm lý do giải trình | `leave.approve` | [x] ✅ Hoàn thành |
| 7.21 | `PUT` | `/api/v1/leave-requests/{id}/cancel` | Hủy đơn xin nghỉ phép (chỉ khi ngày nghỉ chưa bắt đầu) | `leave.apply` | [x] ✅ Hoàn thành |

---

### 8. Module Payroll Management (`payroll`)
- **Mô tả:** Cấu hình thành phần lương, phụ cấp, trích nộp bảo hiểm theo NĐ 73/2024 & NĐ 74/2024, thuế TNCN 7 bậc lũy tiến, tính lương tự động từ hợp đồng & chấm công, tạm ứng lương, phê duyệt kỳ lương qua Workflow Engine và tra cứu phiếu lương cá nhân (Payslip ESS).
- **Ràng buộc:** `05. Contract`, `06. Attendance` *(Lương cần dữ liệu hợp đồng lao động và snapshot tổng hợp chấm công tháng).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 8.1 | `GET` | `/api/v1/salary-components` | Phân trang và tìm kiếm thành phần lương (`SalaryComponentFilter`) | `payroll.config` | [x] ✅ Đã hoàn thành |
| 8.2 | `POST` | `/api/v1/salary-components` | Tạo mới thành phần lương / phụ cấp / thưởng | `payroll.config` | [x] ✅ Đã hoàn thành |
| 8.3 | `GET` | `/api/v1/salary-components/{id}` | Lấy chi tiết cấu hình một thành phần lương | `payroll.config` | [x] ✅ Đã hoàn thành |
| 8.4 | `PUT` | `/api/v1/salary-components/{id}` | Cập nhật thông tin thành phần lương | `payroll.config` | [x] ✅ Đã hoàn thành |
| 8.5 | `DELETE` | `/api/v1/salary-components/{id}` | Xóa mềm thành phần lương | `payroll.config` | [x] ✅ Đã hoàn thành |
| 8.6 | `GET` | `/api/v1/payroll-periods` | Danh sách các kỳ tính lương tháng phân trang (`PayrollPeriodFilter`) | `payroll.view` | [x] ✅ Đã hoàn thành |
| 8.7 | `POST` | `/api/v1/payroll-periods` | Mở kỳ tính lương mới (`OPEN`) | `payroll.manage` | [x] ✅ Đã hoàn thành |
| 8.8 | `GET` | `/api/v1/payroll-periods/{id}` | Xem chi tiết kỳ lương kèm tổng quỹ và lịch sử workflow | `payroll.view` | [x] ✅ Đã hoàn thành |
| 8.9 | `POST` | `/api/v1/payroll-periods/{id}/process` | Chạy thuật toán tính toán bảng lương tự động cho toàn bộ nhân sự | `payroll.process` | [x] ✅ Đã hoàn thành |
| 8.10 | `POST` | `/api/v1/payroll-periods/{id}/submit-approval` | Trình duyệt kỳ lương qua Module 21 Workflow Engine | `payroll.approve` | [x] ✅ Đã hoàn thành |
| 8.11 | `POST` | `/api/v1/payroll-periods/{id}/approve` | Phê duyệt kỳ tính lương (đồng bộ duyệt các bản ghi lương) | `payroll.approve` | [x] ✅ Đã hoàn thành |
| 8.12 | `POST` | `/api/v1/payroll-periods/{id}/close` | Khóa và đóng kỳ lương, chuyển trạng thái chi trả thành công | `payroll.manage` | [x] ✅ Đã hoàn thành |
| 8.13 | `GET` | `/api/v1/payroll-records` | Tra cứu bảng lương chi tiết theo DataScope (`PayrollRecordFilter`) | `payroll.view` | [x] ✅ Đã hoàn thành |
| 8.14 | `GET` | `/api/v1/payroll-records/{id}` | Chi tiết bảng lương nhân viên kèm danh sách mục cấu thành (items) | `payroll.view` | [x] ✅ Đã hoàn thành |
| 8.15 | `POST` | `/api/v1/payroll-records/{id}/adjust` | Bổ sung / điều chỉnh khoản mục lương thủ công có giải trình | `payroll.manage` | [x] ✅ Đã hoàn thành |
| 8.16 | `GET` | `/api/v1/payroll-records/my-payslips` | Nhân viên tra cứu danh sách phiếu lương cá nhân (ESS) | `payroll.view` | [x] ✅ Đã hoàn thành |
| 8.17 | `POST` | `/api/v1/salary-advances` | Nhân viên nộp đơn đề xuất xin tạm ứng lương (kèm khởi chạy workflow) | `payroll.advance` | [x] ✅ Đã hoàn thành |
| 8.18 | `GET` | `/api/v1/salary-advances/my-advances` | Tra cứu lịch sử đơn tạm ứng lương cá nhân (ESS) | `payroll.advance` | [x] ✅ Đã hoàn thành |
| 8.19 | `GET` | `/api/v1/salary-advances` | Danh sách đơn tạm ứng lương toàn công ty phân quyền DataScope | `payroll.view` | [x] ✅ Đã hoàn thành |
| 8.20 | `GET` | `/api/v1/salary-advances/{id}` | Xem chi tiết đơn xin tạm ứng lương | `payroll.view` | [x] ✅ Đã hoàn thành |
| 8.21 | `POST` | `/api/v1/salary-advances/{id}/disburse` | Kế toán xác nhận giải ngân tạm ứng tiền mặt / chuyển khoản | `payroll.manage` | [x] ✅ Đã hoàn thành |
| 8.22 | `POST` | `/api/v1/salary-advances/{id}/reject` | Từ chối yêu cầu tạm ứng lương | `payroll.manage` | [x] ✅ Đã hoàn thành |
| 8.23 | `POST` | `/api/v1/salary-advances/{id}/cancel` | Nhân viên hủy đơn tạm ứng đang chờ duyệt | `payroll.advance` | [x] ✅ Đã hoàn thành |

---

### 9. Module Recruitment & Onboarding (`recruitment`)
- **Mô tả:** Đầy đủ nghiệp vụ tuyển dụng (Module 02 & 2.1 Interview Management): Đề xuất tuyển dụng (Manpower Request duyệt qua Workflow), Vị trí & Tin tuyển dụng đa kênh, Quản lý ứng viên & Hồ sơ ứng tuyển (ATS Pipeline), Quản lý bộ phỏng vấn (Interview Kit, tiêu chí câu hỏi, thang điểm Rubric), Lịch phỏng vấn hội đồng (Panel Interview), Chấm điểm độc lập (Blind Grading) & Cảnh báo phân kỳ nhận xét (Divergence Alert), Phân tích mẫu chấm điểm người phỏng vấn (Scoring Pattern & Bias Awareness), Quản lý Offer thư mời việc (duyệt Workflow), Tiếp nhận ứng viên trúng tuyển tự động chuyển đổi thành nhân viên chính thức (`convert-to-employee`), và Ngân hàng hồ sơ ứng viên (Talent Pool).
- **Ràng buộc:** `03. Organization`, `04. Employee`, `18. Workflow` *(Đề xuất tuyển dụng & Offer gắn với Workflow, trúng tuyển chuyển sang Employee).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 9.1 | `GET` | `/api/v1/manpower-requests` | Danh sách đề xuất tuyển dụng (DataScope/Filter) | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.2 | `POST` | `/api/v1/manpower-requests` | Tạo đề xuất tuyển dụng nhân sự mới (gửi duyệt Workflow) | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.3 | `GET` | `/api/v1/manpower-requests/{id}` | Chi tiết đề xuất tuyển dụng kèm trạng thái workflow | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.4 | `PUT` | `/api/v1/manpower-requests/{id}/cancel` | Hủy đề xuất tuyển dụng đang chờ duyệt | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.5 | `GET` | `/api/v1/job-positions` | Danh sách vị trí tuyển dụng đang quản lý | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.6 | `POST` | `/api/v1/job-positions` | Tạo vị trí tuyển dụng mới từ đề xuất đã duyệt | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.7 | `PUT` | `/api/v1/job-positions/{id}/status` | Đóng / mở / tạm dừng vị trí tuyển dụng | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.8 | `GET` | `/api/v1/job-postings` | Danh sách các tin tuyển dụng đa kênh | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.9 | `POST` | `/api/v1/job-postings` | Tạo và xuất bản tin tuyển dụng vị trí công việc | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.10 | `PUT` | `/api/v1/job-postings/{id}/status` | Thay đổi trạng thái đăng tuyển (DRAFT, PUBLISHED, EXPIRED) | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.11 | `GET` | `/api/v1/candidates` | Danh sách hồ sơ ứng viên (Filter keyword, skills, status) | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.12 | `POST` | `/api/v1/candidates` | Tiếp nhận và tạo mới hồ sơ ứng viên (CV, kỹ năng) | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.13 | `GET` | `/api/v1/candidates/{id}` | Chi tiết hồ sơ ứng viên | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.14 | `PUT` | `/api/v1/candidates/{id}` | Cập nhật thông tin hồ sơ ứng viên | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.15 | `GET` | `/api/v1/applications` | Quản lý quy trình ứng tuyển ATS Pipeline | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.16 | `POST` | `/api/v1/applications` | Nộp hồ sơ ứng tuyển vào vị trí công việc | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.17 | `PUT` | `/api/v1/applications/{id}/stage` | Chuyển đổi giai đoạn ứng tuyển (Screening -> Offer -> Hired) | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.18 | `GET` | `/api/v1/interview-kits` | Danh sách bộ câu hỏi phỏng vấn chuẩn hóa (Interview Kits) | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.19 | `POST` | `/api/v1/interview-kits` | Tạo mới bộ phỏng vấn và tiêu chí chấm điểm Rubric | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.20 | `GET` | `/api/v1/interview-kits/{id}` | Chi tiết bộ phỏng vấn kèm danh sách câu hỏi tiêu chí | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.21 | `POST` | `/api/v1/interview-kits/{id}/questions` | Thêm câu hỏi & tiêu chuẩn thang điểm vào Kit | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.22 | `GET` | `/api/v1/interviews` | Lịch phỏng vấn và trạng thái các vòng phỏng vấn | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.23 | `POST` | `/api/v1/interviews` | Lên lịch phỏng vấn, gắn hội đồng và gửi lời mời | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.24 | `POST` | `/api/v1/interviews/{id}/evaluations` | Thành viên hội đồng nộp phiếu chấm điểm (Blind Grading) | `recruitment.evaluate` | [x] ✅ Hoàn thành |
| 9.25 | `GET` | `/api/v1/interviews/{id}/consolidated-feedback` | Xem tổng hợp nhận xét & cảnh báo độ lệch điểm (Divergence Alert) | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.26 | `GET` | `/api/v1/interviewers/{id}/scoring-pattern` | Phân tích xu hướng chấm điểm của giám khảo (Bias Awareness) | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.27 | `GET` | `/api/v1/offers` | Danh sách Offer mời nhận việc | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.28 | `POST` | `/api/v1/offers` | Lập Offer thư mời việc và trình duyệt Workflow | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.29 | `PUT` | `/api/v1/offers/{id}/status` | Cập nhật phản hồi Offer của ứng viên (ACCEPTED, DECLINED) | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.30 | `POST` | `/api/v1/hiring/{application_id}/convert-to-employee` | Tiếp nhận nhân sự: chuyển đổi ứng viên trúng tuyển thành nhân viên | `recruitment.onboard` | [x] ✅ Hoàn thành |
| 9.31 | `GET` | `/api/v1/talent-pool` | Tra cứu ngân hàng dữ liệu ứng viên tiềm năng (Talent Pool) | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.32 | `POST` | `/api/v1/talent-pool` | Lưu trữ ứng viên tiềm năng vào Talent Pool | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.33 | `GET` | `/api/v1/internal-opportunities` | Danh sách cơ hội nội bộ (Dự án ngắn hạn / Vị trí mở) | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.34 | `GET` | `/api/v1/internal-opportunities/{id}` | Chi tiết cơ hội nội bộ | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.35 | `POST` | `/api/v1/internal-opportunities` | Đăng cơ hội dự án ngắn hạn & vị trí mở nội bộ | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.36 | `PUT` | `/api/v1/internal-opportunities/{id}` | Cập nhật thông tin cơ hội nội bộ | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.37 | `PUT` | `/api/v1/internal-opportunities/{id}/status` | Đổi trạng thái cơ hội (OPEN, IN_PROGRESS, FILLED, CANCELLED) | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.38 | `POST` | `/api/v1/internal-opportunities/{id}/express-interest` | Nhân viên tự ứng tuyển / bày tỏ quan tâm cơ hội (Express Interest) | `Authenticated` | [x] ✅ Hoàn thành |
| 9.39 | `GET` | `/api/v1/internal-opportunities/{id}/applications` | Xem danh sách ứng viên nội bộ ứng tuyển cơ hội | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.40 | `GET` | `/api/v1/internal-opportunities/my-applications` | Danh sách cơ hội cá nhân nhân viên đã ứng tuyển | `Authenticated` | [x] ✅ Hoàn thành |
| 9.41 | `PUT` | `/api/v1/internal-applications/{id}/review` | Duyệt / đánh giá đơn ứng tuyển nội bộ (SHORTLISTED, ACCEPTED...) | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.42 | `GET` | `/api/v1/internal-opportunities/recommended` | Gợi ý cơ hội nội bộ phù hợp cho nhân sự | `Authenticated` | [x] ✅ Hoàn thành |
| 9.43 | `POST` | `/api/v1/internal-assignments` | Tạo phân công nhiệm vụ nội bộ khi trúng tuyển (Assignment) | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.44 | `GET` | `/api/v1/internal-assignments` | Tra cứu danh sách phân công nhiệm vụ nội bộ | `recruitment.view` | [x] ✅ Hoàn thành |
| 9.45 | `PUT` | `/api/v1/internal-assignments/{id}/complete` | Hoàn thành & chấm điểm đánh giá nhiệm vụ nội bộ | `recruitment.manage` | [x] ✅ Hoàn thành |
| 9.46 | `GET` | `/api/v1/internal-marketplace/stats` | Báo cáo thống kê thị trường nhân tài nội bộ (Marketplace Stats) | `recruitment.view` | [x] ✅ Hoàn thành |

---

### 10. Module Asset & Equipment Management (`asset`)
- **Mô tả:** Danh mục tài sản trang thiết bị, cấp phát laptop/máy móc cho nhân sự, thu hồi bàn giao khi nghỉ việc, lịch sử bảo dưỡng.
- **Ràng buộc:** `04. Employee` *(Tài sản được bàn giao cho 1 nhân sự cụ thể).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 10.1 | `GET` | `/api/v1/assets` | Danh sách tài sản, thiết bị công ty | `asset.view` | [ ] ⏳ Chưa làm |
| 10.2 | `POST` | `/api/v1/assets` | Khai báo tài sản, máy móc mới | `asset.manage` | [ ] ⏳ Chưa làm |
| 10.3 | `POST` | `/api/v1/asset-assignments` | Cấp phát tài sản cho nhân viên sử dụng | `asset.assign` | [ ] ⏳ Chưa làm |
| 10.4 | `PUT` | `/api/v1/asset-assignments/{id}/return` | Thu hồi / Tiếp nhận bàn giao tài sản hoàn trả | `asset.assign` | [ ] ⏳ Chưa làm |
| 10.5 | `GET` | `/api/v1/assets/my` | Danh sách tài sản thiết bị tôi đang được công ty cấp | `asset.view_own` | [ ] ⏳ Chưa làm |

---

### 11. Module Performance Evaluation / KPI (`performance`)
- **Mô tả:** Thiết lập kỳ đánh giá hiệu suất nhân viên định kỳ, tiêu chí đánh giá, nhân sự tự đánh giá, quản lý trực tiếp đánh giá và xếp loại.
- **Ràng buộc:** `04. Employee`, `03. Organization` *(Đánh giá theo phân cấp phòng ban và nhân sự).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 11.1 | `GET` | `/api/v1/kpi-periods` | Danh sách các kỳ đánh giá hiệu suất | `performance.view` | [ ] ⏳ Chưa làm |
| 11.2 | `POST` | `/api/v1/kpi-periods` | Khởi tạo kỳ đánh giá KPI mới | `performance.manage` | [ ] ⏳ Chưa làm |
| 11.3 | `GET` | `/api/v1/kpi-evaluations/my` | Phiếu đánh giá hiệu suất cá nhân | `performance.view_own` | [ ] ⏳ Chưa làm |
| 11.4 | `PUT` | `/api/v1/kpi-evaluations/{id}/self-review` | Nhân viên nộp bản tự đánh giá KPI | `performance.evaluate` | [ ] ⏳ Chưa làm |
| 11.5 | `PUT` | `/api/v1/kpi-evaluations/{id}/manager-review` | Cấp quản lý chấm điểm và nhận xét (DataScope) | `performance.evaluate` | [ ] ⏳ Chưa làm |

---

### 12. Module Notification & System Audit (`notification`, `audit`)
- **Mô tả:** Trung tâm thông báo In-app (WebSocket/SSE), thông báo đẩy, Email thông báo, nhật ký kiểm toán và ghi nhận vết thao tác hệ thống (Audit Trail).
- **Ràng buộc:** `01. Auth` *(Gắn liền với phiên đăng nhập của người dùng).*

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) | Trạng thái |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 12.1 | `GET` | `/api/v1/notifications/my` | Lấy danh sách thông báo của người dùng hiện tại | `Authenticated` | [x] ✅ Hoàn thành |
| 12.2 | `PUT` | `/api/v1/notifications/{id}/read` | Đánh dấu một thông báo đã đọc | `Authenticated` | [x] ✅ Hoàn thành |
| 12.3 | `PUT` | `/api/v1/notifications/read-all` | Đánh dấu tất cả thông báo là đã đọc | `Authenticated` | [x] ✅ Hoàn thành |
| 12.4 | `GET` | `/api/v1/audit-logs` | Truy vấn nhật ký hành động hệ thống (Audit Trail) | `audit.view` | [x] ✅ Hoàn thành |

---

## 🎯 Thứ tự Ưu tiên Triển khai Đề xuất (Execution Sequence)

Dựa trên cột **Ràng buộc (Cần làm trước)**, thứ tự các giai đoạn (Phase) triển khai tối ưu như sau:

```
[Phase 1: Core Foundation] (Hoàn thành 90%)
  Auth & User ───► Role & Permission

[Phase 2: Master Data & Structure] (Tiếp theo)
  Role & Auth ───► Organization Structure (Company, Department, Team, Position)

[Phase 3: Core HRM]
  Organization ───► Employee Profile ───► Contract Management

[Phase 4: Operations & Timesheet]
  Employee + Org ───► Time & Attendance ───► Leave & Overtime

[Phase 5: Financials]
  Contract + Attendance + Leave/OT ───► Payroll Management

[Phase 6: Talent & Assets]
  Recruitment ───► Asset Management ───► Performance Evaluation ───► Audit & Notification
```
