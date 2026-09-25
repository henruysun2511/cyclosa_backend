# HRM - Validate & Lỗi nghiệp vụ theo API

> File này mô tả chi tiết quy tắc validate đầu vào và các lỗi nghiệp vụ (business rule) cho các API quan trọng/phức tạp nhất của từng module. Các API CRUD đơn giản (GET danh sách, DELETE...) không lặp lại ở đây — áp dụng theo quy ước chung ở Mục 1.

---

## 1. Quy ước chung

### 1.1 Phân loại lỗi & HTTP status code

| Loại lỗi | HTTP Status | Mã lỗi (prefix) | Khi nào dùng |
|---|---|---|---|
| Lỗi định dạng đầu vào | 400 | `VALIDATION_ERROR` | Thiếu trường bắt buộc, sai kiểu dữ liệu, sai format (email, ngày tháng...), vượt độ dài cho phép |
| Chưa xác thực | 401 | `UNAUTHENTICATED` | Token thiếu/hết hạn/không hợp lệ |
| Không đủ quyền | 403 | `FORBIDDEN` | Có token nhưng không có permission, hoặc ngoài `data_scope` được gán |
| Không tìm thấy tài nguyên | 404 | `{ENTITY}_NOT_FOUND` | ID tham chiếu (FK) không tồn tại trong hệ thống |
| Trùng lặp | 409 | `DUPLICATE_{FIELD}` | Vi phạm ràng buộc unique (mã nhân viên, email, số CMND...) |
| Vi phạm quy tắc nghiệp vụ | 422 | `{RULE_CODE}` | Dữ liệu đúng định dạng, tồn tại, nhưng vi phạm logic nghiệp vụ (VD: số dư phép không đủ) |
| Tài nguyên đang khóa | 423 | `RESOURCE_LOCKED` | Thao tác trên bản ghi đã ở trạng thái không cho phép sửa (VD: kỳ lương đã đóng) |
| Lỗi hệ thống | 500 | `INTERNAL_ERROR` | Lỗi không lường trước, không lộ chi tiết ra client |

### 1.2 Cấu trúc response lỗi chuẩn

```json
{
  "success": false,
  "error": {
    "code": "POSITION_NOT_FOUND",
    "message": "Vị trí công việc không tồn tại",
    "field": "position_id",
    "details": {}
  }
}
```

Với lỗi validate nhiều trường cùng lúc, trả về mảng `errors`:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Dữ liệu đầu vào không hợp lệ",
    "errors": [
      { "field": "full_name", "code": "REQUIRED", "message": "Họ tên không được để trống" },
      { "field": "date_of_birth", "code": "INVALID_DATE", "message": "Ngày sinh không hợp lệ" }
    ]
  }
}
```

### 1.3 Nguyên tắc validate 2 lớp

1. **Lớp 1 — Validate định dạng (400):** kiểm tra ngay khi nhận request, chưa chạm database. Required, type, length, regex, range.
2. **Lớp 2 — Validate nghiệp vụ (404/409/422):** chạy sau lớp 1, có truy vấn database để kiểm tra tồn tại/trùng lặp/trạng thái hợp lệ. Đây là phần được yêu cầu chi tiết trong file này — VD điển hình: tạo nhân viên với `position_id` thì phải kiểm tra `position_id` có tồn tại trong bảng `positions` và đang `active` hay không.

---

## 2. Module 01 — Quản lý Tổ chức

### `POST /api/v1/departments` — Tạo phòng ban

**Validate định dạng (400):**
| Trường | Quy tắc |
|---|---|
| name | Bắt buộc, string, 2-100 ký tự |
| branch_id | Bắt buộc, UUID |
| parent_department_id | UUID, không bắt buộc |
| manager_employee_id | UUID, không bắt buộc |

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `branch_id` phải tồn tại trong `branches` | 404 `BRANCH_NOT_FOUND` |
| `branch_id` phải đang `active` | 422 `BRANCH_INACTIVE` |
| Nếu có `parent_department_id`: phải tồn tại và **cùng `branch_id`** | 404 `PARENT_DEPARTMENT_NOT_FOUND` / 422 `PARENT_DEPARTMENT_BRANCH_MISMATCH` |
| `parent_department_id` không được trỏ vòng lặp (VD phòng A là cha của B, B không được là cha của A) | 422 `CIRCULAR_DEPARTMENT_HIERARCHY` |
| Nếu có `manager_employee_id`: phải tồn tại trong `employees` và `employment_status = active` | 404 `EMPLOYEE_NOT_FOUND` / 422 `MANAGER_NOT_ACTIVE` |
| `code` (nếu client gửi) không được trùng trong cùng `branch_id` | 409 `DUPLICATE_DEPARTMENT_CODE` |

### `DELETE /api/v1/departments/{id}` — Xóa phòng ban

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Phòng ban còn nhân viên đang `active` (`employee_employment_info.department_id`) | 422 `DEPARTMENT_HAS_ACTIVE_EMPLOYEES` |
| Phòng ban còn `Team` hoặc `Department` con trực thuộc | 422 `DEPARTMENT_HAS_CHILDREN` |
| Phòng ban có `Position` đang gắn `manpower_requests` ở trạng thái `pending` | 422 `DEPARTMENT_HAS_PENDING_REQUESTS` |

---

## 3. Module 02 — Tuyển dụng

### `POST /api/v1/manpower-requests` — Tạo yêu cầu tuyển dụng

**Validate định dạng (400):**
| Trường | Quy tắc |
|---|---|
| department_id | Bắt buộc, UUID |
| position_id | Bắt buộc, UUID |
| quantity | Bắt buộc, số nguyên dương, 1-100 |
| expected_start_date | Bắt buộc, ngày phải ≥ ngày hiện tại |

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `department_id` tồn tại và active | 404 `DEPARTMENT_NOT_FOUND` |
| `position_id` tồn tại và **thuộc đúng `department_id`** đã chọn | 404 `POSITION_NOT_FOUND` / 422 `POSITION_DEPARTMENT_MISMATCH` |
| Người gửi request (`requested_by_employee_id`, suy từ token) phải thuộc chính `department_id` đó hoặc là quản lý của phòng ban đó | 403 `FORBIDDEN_DEPARTMENT_SCOPE` |
| Không được trùng 1 manpower_request khác đang `pending` cho cùng `position_id` | 409 `DUPLICATE_PENDING_MANPOWER_REQUEST` |

### `POST /api/v1/applications` — Ứng viên nộp hồ sơ

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `job_position_id` tồn tại và `status = open` | 404 `JOB_POSITION_NOT_FOUND` / 422 `JOB_POSITION_CLOSED` |
| `candidate_id` (hoặc email nếu ứng viên mới) không được trùng application cho **cùng vị trí đó** khi đã có 1 application đang `screening/interview/offer` | 409 `DUPLICATE_APPLICATION` |
| File CV (`resume_url`) phải là định dạng PDF/DOCX, dung lượng ≤ 10MB | 400 `INVALID_RESUME_FORMAT` |

### `POST /api/v1/hiring/{application_id}/convert-to-employee` — Chuyển ứng viên thành nhân viên

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `application_id` tồn tại | 404 `APPLICATION_NOT_FOUND` |
| Application phải có `offers` ở trạng thái `accepted` | 422 `OFFER_NOT_ACCEPTED` |
| Application chưa từng được convert trước đó (tránh tạo trùng employee) | 409 `APPLICATION_ALREADY_CONVERTED` |
| Email/CMND của candidate không được trùng với 1 employee đang `active` khác | 409 `DUPLICATE_EMPLOYEE_IDENTITY` |

---

## 4. Module 03 — Onboarding

### `POST /api/v1/onboarding/processes` — Khởi tạo quy trình onboarding

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `employee_id` tồn tại | 404 `EMPLOYEE_NOT_FOUND` |
| Nhân viên chưa có `onboarding_process` nào đang `in_progress` | 409 `ONBOARDING_ALREADY_IN_PROGRESS` |
| `checklist_template_id` tồn tại | 404 `CHECKLIST_TEMPLATE_NOT_FOUND` |

### `PUT /api/v1/onboarding/process-items/{id}/complete` — Hoàn thành 1 checklist item

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Item tồn tại và thuộc `onboarding_process` đang `in_progress` | 404 `PROCESS_ITEM_NOT_FOUND` / 422 `ONBOARDING_PROCESS_CLOSED` |
| Item chưa ở trạng thái `completed` trước đó | 422 `ITEM_ALREADY_COMPLETED` |
| Nếu `category = account`, phải có `account_provisioning` tương ứng ở trạng thái `provisioned` mới cho phép đánh dấu hoàn thành | 422 `ACCOUNT_NOT_PROVISIONED_YET` |

---

## 5. Module 04 — Quản lý Nhân viên

### `POST /api/v1/employees` — Tạo nhân viên *(ví dụ điển hình)*

**Validate định dạng (400):**
| Trường | Quy tắc |
|---|---|
| full_name | Bắt buộc, 2-255 ký tự |
| date_of_birth | Bắt buộc, định dạng ngày hợp lệ, phải trong quá khứ |
| national_id_number | Bắt buộc, đúng định dạng CMND/CCCD (9 hoặc 12 số) |
| department_id | Bắt buộc, UUID |
| position_id | Bắt buộc, UUID |
| hire_date | Bắt buộc, ngày hợp lệ |
| personal_email | Không bắt buộc, đúng định dạng email nếu có |

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Tuổi tính từ `date_of_birth` tại `hire_date` phải ≥ 15 (tuổi lao động tối thiểu theo luật) | 422 `EMPLOYEE_UNDER_MINIMUM_AGE` |
| `department_id` phải tồn tại trong `departments` và đang `active` | 404 `DEPARTMENT_NOT_FOUND` |
| `position_id` phải tồn tại trong `positions`, đang `active`, **và thuộc đúng `department_id`** đã chọn — đây chính là kiểm tra "vị trí có tồn tại và có hợp lệ với phòng ban không" | 404 `POSITION_NOT_FOUND` / 422 `POSITION_DEPARTMENT_MISMATCH` |
| `job_level_id` suy ra từ `position.job_level_id`, không cần client gửi — nếu client vẫn gửi và khác với vị trí đã chọn | 422 `JOB_LEVEL_MISMATCH` |
| `national_id_number` không được trùng với nhân viên khác đang `employment_status != terminated` | 409 `DUPLICATE_NATIONAL_ID` |
| `company_email` (nếu client tự đặt thay vì hệ thống sinh) không được trùng email đã tồn tại ở `users` | 409 `DUPLICATE_EMAIL` |
| Nếu có `manager_employee_id`: phải tồn tại, đang `active`, và **không được trùng chính nhân viên đang tạo** (không thể tự làm quản lý của mình) | 404 `MANAGER_NOT_FOUND` / 422 `SELF_MANAGEMENT_NOT_ALLOWED` |
| `manager_employee_id` không được tạo thành vòng lặp báo cáo (A quản lý B, B quản lý A) | 422 `CIRCULAR_REPORTING_LINE` |

### `PUT /api/v1/employees/{id}/status` — Đổi trạng thái nhân viên

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Không cho chuyển trực tiếp từ `terminated` sang `active` (phải tạo hồ sơ mới nếu tuyển lại) | 422 `INVALID_STATUS_TRANSITION` |
| Chuyển sang `terminated` yêu cầu đã có `contract_termination` hoặc `resignation`/`termination` đã duyệt | 422 `TERMINATION_RECORD_REQUIRED` |
| Chuyển sang `on_leave` yêu cầu có `leave_request` đang `approved` bao trùm ngày hiện tại | 422 `NO_ACTIVE_LEAVE_FOUND` |

### `POST /api/v1/employees/{id}/dependents` — Thêm người phụ thuộc

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `national_id_number` của người phụ thuộc (nếu có) không được trùng với chính `national_id_number` của nhân viên khác trong hệ thống (tránh khai trùng người phụ thuộc ở 2 hồ sơ) | 409 `DEPENDENT_ALREADY_REGISTERED_ELSEWHERE` |
| `date_of_birth` không được sau ngày hiện tại | 400 `INVALID_DATE` |
| Nếu `relationship = child`: `date_of_birth` phải sau `date_of_birth` của nhân viên (con không thể lớn tuổi hơn cha/mẹ) | 422 `INVALID_CHILD_BIRTH_DATE` |

---

## 6. Module 05 — Quản lý Hợp đồng

### `POST /api/v1/contracts` — Tạo hợp đồng

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `employee_id` tồn tại | 404 `EMPLOYEE_NOT_FOUND` |
| `contract_type_id` tồn tại | 404 `CONTRACT_TYPE_NOT_FOUND` |
| Nhân viên **không được có 2 hợp đồng cùng `status = active` cùng lúc** | 409 `ACTIVE_CONTRACT_ALREADY_EXISTS` |
| `end_date` (nếu có) phải sau `start_date` | 400 `INVALID_DATE_RANGE` |
| Nếu `contract_type.max_duration_months` có giá trị, khoảng `start_date`-`end_date` không được vượt quá | 422 `CONTRACT_DURATION_EXCEEDS_LIMIT` |
| `basic_salary` phải ≥ mức lương tối thiểu vùng đã cấu hình ở `system_settings` | 422 `SALARY_BELOW_MINIMUM_WAGE` |

### `POST /api/v1/contracts/{id}/renew` — Gia hạn hợp đồng

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Hợp đồng gốc phải tồn tại và `status IN (active, expired)` | 404 `CONTRACT_NOT_FOUND` / 422 `CONTRACT_NOT_RENEWABLE` |
| Hợp đồng gốc chưa từng được gia hạn trước đó (chưa có contract khác trỏ `previous_contract_id` về nó) | 409 `CONTRACT_ALREADY_RENEWED` |
| Theo luật lao động: hợp đồng xác định thời hạn chỉ được gia hạn tối đa 1 lần liên tiếp trước khi bắt buộc chuyển không xác định thời hạn — nếu vi phạm | 422 `MAX_FIXED_TERM_RENEWAL_EXCEEDED` |

---

## 7. Module 06 — Chấm công & Ca làm việc

### `POST /api/v1/attendance/check-in` — Chấm công vào

**Validate định dạng (400):**
| Trường | Quy tắc |
|---|---|
| photo (base64/multipart) | Bắt buộc nếu `check_in_method = face_recognition` |
| location | Bắt buộc nếu `work_mode = remote` |

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Nhân viên chưa check-in trong `work_date` hiện tại (không cho check-in 2 lần/ngày) | 409 `ALREADY_CHECKED_IN_TODAY` |
| Nhân viên phải có `shift_assignment` hiệu lực cho `work_date` hiện tại | 422 `NO_SHIFT_ASSIGNED` |
| Gọi service nhận diện khuôn mặt, `face_match_score` phải ≥ ngưỡng cấu hình (`system_settings.face_match_threshold`) | 422 `FACE_MATCH_FAILED` (kèm `details.score` trả về để FE hiển thị, không tự động chặn cứng — có thể gắn cờ `needs_manual_review`) |
| `employee_personal_info.photo_url` phải tồn tại (nhân viên đã có ảnh gốc) — nếu chưa có | 422 `REFERENCE_PHOTO_NOT_SET` |
| Nếu `work_mode = remote`, tọa độ GPS phải nằm trong bán kính cho phép (nếu công ty bật ràng buộc địa lý) | 422 `LOCATION_OUT_OF_RANGE` |

### `POST /api/v1/attendance/check-out` — Chấm công ra

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Phải đã có `check_in_time` trong ngày trước khi check-out | 422 `CHECK_IN_REQUIRED_BEFORE_CHECKOUT` |
| Đã check-out trong ngày rồi thì không cho check-out lại | 409 `ALREADY_CHECKED_OUT_TODAY` |
| `check_out_time` phải sau `check_in_time` | 422 `INVALID_CHECKOUT_TIME` |

### `POST /api/v1/overtime-requests` — Tạo yêu cầu làm thêm giờ

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `start_time`/`end_time` phải nằm ngoài khung giờ ca chính thức đã gán (`shift_assignments`) | 422 `OVERTIME_OVERLAPS_REGULAR_SHIFT` |
| Tổng giờ OT trong tháng không vượt quá giới hạn luật lao động (VD 40 giờ/tháng) — nếu vượt | 422 `MONTHLY_OVERTIME_LIMIT_EXCEEDED` |
| Không được trùng khung giờ với 1 overtime_request khác đã `approved`/`pending` của cùng nhân viên | 409 `DUPLICATE_OVERTIME_REQUEST` |

---

## 8. Module 07 — Quản lý Nghỉ phép

### `POST /api/v1/leave-requests` — Tạo đơn nghỉ phép *(ví dụ điển hình về kiểm tra số dư)*

**Validate định dạng (400):**
| Trường | Quy tắc |
|---|---|
| leave_type_id | Bắt buộc, UUID |
| start_date, end_date | Bắt buộc, `end_date ≥ start_date` |

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `leave_type_id` tồn tại | 404 `LEAVE_TYPE_NOT_FOUND` |
| `start_date` không được là ngày trong quá khứ (trừ khi role HR tạo hộ có ghi chú lý do) | 422 `LEAVE_DATE_IN_PAST` |
| Tính số ngày công thực (loại trừ cuối tuần/ngày lễ) trong khoảng `start_date`-`end_date`, so với `leave_balances.total_days - used_days` của `leave_type_id` năm hiện tại — nếu không đủ | 422 `INSUFFICIENT_LEAVE_BALANCE` (kèm `details.available_days`, `details.requested_days`) |
| Không được trùng khoảng ngày với 1 leave_request khác đang `pending`/`approved` của cùng nhân viên | 409 `OVERLAPPING_LEAVE_REQUEST` |
| Nếu `leave_type.requires_approval = false`, hệ thống tự động duyệt, không tạo Workflow Instance | *(không lỗi — auto-approve)* |
| Ngày nghỉ không được trùng ngày nhân viên đã có `attendance_records.check_in_time` (đã đi làm ngày đó) | 422 `CANNOT_REQUEST_LEAVE_ON_WORKED_DAY` |

### `PUT /api/v1/leave-requests/{id}/cancel` — Hủy đơn nghỉ phép

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Chỉ hủy được đơn đang `pending` hoặc `approved` **và** `start_date` chưa tới | 422 `LEAVE_ALREADY_STARTED` |
| Người hủy phải là chính chủ đơn hoặc HR | 403 `FORBIDDEN` |

---

## 9. Module 08 — Tiền lương

### `POST /api/v1/payroll/{period_id}/process` — Chạy tính lương *(ví dụ điển hình về ràng buộc trạng thái)*

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `payroll_period_id` tồn tại | 404 `PAYROLL_PERIOD_NOT_FOUND` |
| Kỳ lương phải đang `status = open` (không cho chạy lại nếu đã `closed`) | 423 `PAYROLL_PERIOD_LOCKED` |
| Mọi nhân viên `active` trong kỳ phải có `employee_salary_history` hiệu lực — nếu thiếu ít nhất 1 nhân viên | 422 `MISSING_SALARY_HISTORY` (kèm `details.employee_ids`) |
| Dữ liệu `attendance_records` của kỳ phải đã đầy đủ (không còn ngày thiếu chấm công chưa xử lý `attendance_corrections`) — nếu còn correction `pending` | 422 `PENDING_ATTENDANCE_CORRECTIONS_EXIST` |
| Không được chạy `process` 2 lần cho cùng 1 kỳ khi đã có kết quả `payroll_records` ở trạng thái `approved`/`paid` | 409 `PAYROLL_ALREADY_PROCESSED` |

### `PUT /api/v1/payroll/{period_id}/approve` — Duyệt kỳ lương

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Kỳ lương phải đang `status = processing` (đã chạy `process` trước đó) | 422 `PAYROLL_NOT_YET_PROCESSED` |
| Toàn bộ `payroll_records` trong kỳ phải ở trạng thái `draft` mới cho duyệt hàng loạt | 422 `INVALID_RECORD_STATUS` |

### `POST /api/v1/salary-advances` — Yêu cầu ứng lương

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `amount` không được vượt quá % lương cơ bản cho phép ứng (cấu hình ở `system_settings`, VD tối đa 50%) | 422 `ADVANCE_AMOUNT_EXCEEDS_LIMIT` |
| Nhân viên không được có quá 1 `salary_advance` đang `pending`/`approved` chưa `disbursed` cùng lúc | 409 `PENDING_ADVANCE_ALREADY_EXISTS` |
| Nhân viên phải có `employment_status = active` và đã qua thời gian thử việc (`probation_end_date` đã qua) | 422 `EMPLOYEE_STILL_ON_PROBATION` |

---

## 10. Module 09 — Quản lý Hiệu suất

### `POST /api/v1/goals` — Tạo mục tiêu

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `performance_cycle_id` tồn tại và đang `status = active` | 404 `CYCLE_NOT_FOUND` / 422 `CYCLE_NOT_ACTIVE` |
| Nếu có `kpi_id`, phải tồn tại và thuộc đúng `department_id` của nhân viên (hoặc `department_id IS NULL` = KPI dùng chung) | 404 `KPI_NOT_FOUND` / 422 `KPI_DEPARTMENT_MISMATCH` |
| Tổng `weight_percentage` của tất cả Goal thuộc cùng nhân viên + cùng cycle không được vượt quá 100% | 422 `TOTAL_WEIGHT_EXCEEDS_100_PERCENT` |

### `PUT /api/v1/performance/evaluations/{id}/finalize` — Chốt điểm đánh giá

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Phải có đủ `performance_reviews` loại `self` **và** `manager` cho tất cả Goal của nhân viên trong cycle | 422 `REVIEWS_INCOMPLETE` |
| Không cho chốt điểm 2 lần cho cùng 1 nhân viên/cycle | 409 `EVALUATION_ALREADY_FINALIZED` |

---

## 11. Module 10 — Đào tạo & Phát triển

### `POST /api/v1/training-registrations` — Đăng ký khóa học

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `course_id` tồn tại và `start_date` chưa tới | 404 `COURSE_NOT_FOUND` / 422 `REGISTRATION_CLOSED` |
| Số lượng đăng ký hiện tại (`status IN (registered, attended)`) chưa đạt `capacity` | 422 `COURSE_FULL` |
| Nhân viên chưa đăng ký khóa này trước đó (tránh trùng) | 409 `ALREADY_REGISTERED` |

---

## 12. Module 11 — Phúc lợi

### `POST /api/v1/benefit-enrollments` — Đăng ký phúc lợi

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `benefit_plan_id` tồn tại | 404 `BENEFIT_PLAN_NOT_FOUND` |
| Nhân viên chưa có `enrollment` khác đang `active` cho cùng `benefit_plan_id` | 409 `ALREADY_ENROLLED` |
| Nếu có `dependent_id`: phải thuộc đúng nhân viên đang đăng ký | 404 `DEPENDENT_NOT_FOUND` / 403 `DEPENDENT_OWNERSHIP_MISMATCH` |
| Nhân viên phải đã qua thời gian thử việc nếu `benefit_plan` yêu cầu | 422 `EMPLOYEE_STILL_ON_PROBATION` |

### `POST /api/v1/benefit-claims` — Yêu cầu chi trả

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `benefit_enrollment_id` phải đang `active` | 422 `ENROLLMENT_NOT_ACTIVE` |
| `amount` không được vượt hạn mức còn lại của gói trong năm (tổng các claim `approved` trước đó + claim hiện tại) | 422 `CLAIM_EXCEEDS_ANNUAL_LIMIT` |

---

## 13. Module 12 — Khen thưởng & Kỷ luật

### `POST /api/v1/disciplines` — Lập biên bản kỷ luật

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `employee_id` tồn tại và đang `active` | 404 `EMPLOYEE_NOT_FOUND` |
| Nếu `discipline_type = termination`, bắt buộc phải gửi kèm qua Workflow duyệt cấp cao trước khi thực thi (không cho thực thi trực tiếp) | 422 `TERMINATION_REQUIRES_APPROVAL` |

---

## 14. Module 13 — Quản lý Tài sản

### `POST /api/v1/assets/{id}/allocate` — Cấp phát tài sản

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Tài sản phải đang `status = in_stock` | 422 `ASSET_NOT_AVAILABLE` |
| `employee_id` tồn tại và đang `active` | 404 `EMPLOYEE_NOT_FOUND` |
| Tài sản không được có `asset_allocation` khác đang mở (`returned_date IS NULL`) | 409 `ASSET_ALREADY_ALLOCATED` |

### `POST /api/v1/assets/{id}/return` — Thu hồi tài sản

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Phải có `asset_allocation` đang mở (`returned_date IS NULL`) cho tài sản này | 422 `NO_OPEN_ALLOCATION_FOUND` |
| `condition_on_return` bắt buộc phải cung cấp | 400 `VALIDATION_ERROR` |

---

## 15. Module 14 — Phát triển Nhân sự

### `POST /api/v1/succession-plans/{id}/candidates` — Thêm ứng viên kế nhiệm

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `employee_id` không được trùng với người đang giữ `position_id` của chính succession plan đó | 422 `CANDIDATE_IS_CURRENT_HOLDER` |
| Nhân viên không được trùng lặp trong cùng 1 succession plan | 409 `CANDIDATE_ALREADY_ADDED` |

---

## 16. Module 15 — Khảo sát & Gắn kết

### `POST /api/v1/surveys/{id}/responses` — Nộp câu trả lời khảo sát

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Survey phải đang `status = active` và trong khoảng `start_date`-`end_date` | 422 `SURVEY_NOT_ACTIVE` |
| Nếu `is_anonymous = false`: nhân viên chỉ được trả lời 1 lần/survey | 409 `ALREADY_RESPONDED` |
| Phải trả lời đủ toàn bộ câu hỏi bắt buộc | 422 `INCOMPLETE_ANSWERS` (kèm `details.missing_question_ids`) |

---

## 17. Module 16 — Nghỉ việc

### `POST /api/v1/resignations` — Nộp đơn xin nghỉ việc

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `expected_last_working_date` phải cách ngày nộp đơn tối thiểu theo số ngày báo trước quy định trong `contracts` hiện tại (VD 30/45 ngày tùy loại hợp đồng) | 422 `INSUFFICIENT_NOTICE_PERIOD` |
| Nhân viên không được có `resignation`/`termination` khác đang `pending`/`approved` chưa xử lý xong | 409 `OFFBOARDING_ALREADY_IN_PROGRESS` |

### `PUT /api/v1/offboarding/{id}/complete` — Hoàn tất offboarding

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Toàn bộ `offboarding_clearances` liên quan phải ở trạng thái `cleared` | 422 `CLEARANCE_INCOMPLETE` (kèm `details.pending_departments`) |
| Toàn bộ `asset_allocations` của nhân viên phải đã có `returned_date` | 422 `ASSETS_NOT_RETURNED` |
| `account_provisioning` của nhân viên phải đã `revoked` | 422 `ACCOUNTS_NOT_REVOKED` |
| `payroll_records.is_final_settlement` của kỳ cuối phải đã `status = paid` | 422 `FINAL_PAYROLL_NOT_COMPLETED` |

---

## 18. Module 18 — Workflow & Approval Engine

### `PUT /api/v1/workflows/instances/{id}/approve` — Duyệt 1 bước workflow

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `workflow_instance` phải đang `status = pending` | 422 `WORKFLOW_ALREADY_CLOSED` |
| Người gọi API phải đúng là `approver_employee_id` được resolve ở `current_level`, hoặc là người được ủy quyền hợp lệ (`workflow_delegates`) tại thời điểm hiện tại | 403 `NOT_AUTHORIZED_APPROVER` |
| Không cho duyệt 2 lần cùng 1 cấp bởi cùng 1 người | 409 `STEP_ALREADY_ACTED` |

### `POST /api/v1/workflows/delegates` — Thiết lập ủy quyền

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `delegate_employee_id` không được trùng chính `delegator_employee_id` | 422 `CANNOT_DELEGATE_TO_SELF` |
| Khoảng `start_date`-`end_date` không được chồng lấp với 1 delegate khác đang hiệu lực của cùng `delegator_employee_id` | 409 `OVERLAPPING_DELEGATION` |

---

## 19. Module 19 — Thông báo

### `POST /api/v1/notifications/send` — Gửi thông báo *(nội bộ)*

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `event_code` phải có `notification_template` tương ứng đã cấu hình | 404 `TEMPLATE_NOT_FOUND` |
| `user_id` (người nhận) phải tồn tại và `status = active` | 404 `USER_NOT_FOUND` / 422 `USER_INACTIVE` — nếu inactive thì bỏ qua gửi, không phải lỗi chặn toàn bộ batch |

---

## 20. Module 20 — Báo cáo & Phân tích

### `POST /api/v1/reports/{report_type}/export` — Xuất báo cáo

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `report_type` phải nằm trong danh sách báo cáo hệ thống hỗ trợ | 404 `REPORT_TYPE_NOT_SUPPORTED` |
| `filter_params` (khoảng ngày) không được vượt quá giới hạn tối đa cho phép export (VD 2 năm) để tránh quá tải | 422 `EXPORT_RANGE_TOO_LARGE` |
| Người dùng chỉ được export dữ liệu trong `data_scope` được gán (VD Dept Manager không export được toàn công ty) | 403 `FORBIDDEN` |

---

## 21. Module 21 — Quản trị Hệ thống

### `POST /api/v1/users` — Tạo tài khoản người dùng

**Validate định dạng (400):**
| Trường | Quy tắc |
|---|---|
| username | Bắt buộc, 4-50 ký tự, chỉ chữ/số/dấu chấm gạch dưới |
| email | Bắt buộc, đúng định dạng email |
| password | Bắt buộc khi tạo thủ công (không qua SSO), tối thiểu 8 ký tự, có chữ hoa/số |

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| `username` không trùng | 409 `DUPLICATE_USERNAME` |
| `email` không trùng | 409 `DUPLICATE_EMAIL` |
| Nếu có `employee_id`: phải tồn tại và **chưa có `user` nào khác gắn với employee này** (1 nhân viên chỉ 1 tài khoản) | 404 `EMPLOYEE_NOT_FOUND` / 409 `EMPLOYEE_ALREADY_HAS_ACCOUNT` |

### `PUT /api/v1/users/{id}/roles` — Gán role cho user

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| Mọi `role_id` trong danh sách phải tồn tại | 404 `ROLE_NOT_FOUND` |
| Nếu role có `company_id` cụ thể (không phải role hệ thống dùng chung), `company_id` của role phải khớp với `company_id` được truyền — không cho gán role công ty A cho user thuộc công ty B | 422 `ROLE_COMPANY_MISMATCH` |
| Không được tự thu hồi role `Super Admin` của chính mình nếu là Super Admin cuối cùng trong hệ thống | 422 `CANNOT_REMOVE_LAST_SUPER_ADMIN` |

### `POST /api/v1/data/import` — Nhập dữ liệu hàng loạt

**Validate nghiệp vụ:**
| Kiểm tra | Lỗi trả về |
|---|---|
| File phải đúng định dạng CSV/XLSX, đúng template cột quy định cho `entity_type` | 400 `INVALID_FILE_FORMAT` |
| Từng dòng dữ liệu được validate độc lập theo quy tắc của entity tương ứng (VD import employee thì áp dụng toàn bộ rule ở Mục 5) — dòng lỗi được liệt kê chi tiết, **không chặn toàn bộ file** nếu chỉ vài dòng lỗi | 422 `PARTIAL_IMPORT_FAILURE` (kèm `details.failed_rows: [{row: 5, errors: [...]}]`) |

---

## 22. Ghi chú triển khai validate

- **Validate định dạng nên dùng schema validation library** (Joi/Zod/class-validator...) chạy ở tầng middleware, tách biệt khỏi controller — trả lỗi 400 sớm nhất có thể, không chạm database.
- **Validate nghiệp vụ nên đặt ở tầng Service**, không đặt ở Controller hay Repository — để tái sử dụng logic khi cùng 1 rule được gọi từ nhiều API (VD kiểm tra `position_id` hợp lệ dùng ở cả tạo mới lẫn cập nhật nhân viên).
- **Ưu tiên kiểm tra theo thứ tự chi phí tăng dần:** validate định dạng (không tốn DB) → kiểm tra tồn tại (1 query đơn giản) → kiểm tra ràng buộc phức tạp (join nhiều bảng, tính toán) — để fail nhanh, tiết kiệm tài nguyên.
- **Với thao tác có race condition** (2 người cùng duyệt 1 workflow, cùng cấp phát 1 tài sản...), nên dùng transaction + row lock (`SELECT ... FOR UPDATE`) thay vì chỉ kiểm tra rồi ghi, để tránh lỗi `409` bị bỏ sót khi 2 request tới gần như đồng thời.
- **Mã lỗi (`error.code`) nên ổn định, không đổi theo ngôn ngữ** — phần `message` mới là chỗ để đa ngôn ngữ (i18n) theo `Accept-Language` header, để frontend luôn switch theo `code` chứ không parse `message`.
