# HRM - Cơ sở dữ liệu (Database Design)

> Xem mô tả nghiệp vụ, luồng hoạt động và API tại file `HRM-Mo-Ta-Nghiep-Vu-Chi-Tiet.md`. File này tập trung vào **thiết kế dữ liệu**: quy ước, schema chi tiết từng bảng, danh sách enum, và sơ đồ quan hệ tổng thể.

---

# PHẦN A — QUY ƯỚC & TỔNG QUAN

## A.1 Quy ước thiết kế

- **Khóa chính:** `id` kiểu `UUID`, sinh tự động.
- **Trường audit chuẩn** (có ở mọi bảng, không lặp lại trong từng bảng bên dưới): `created_at`, `updated_at`, `created_by`, `updated_by`.
- **Khóa ngoại:** đặt tên `{table_singular}_id`, kiểu `UUID`.
- **Xóa dữ liệu:** soft-delete (`deleted_at` nullable) cho các bảng nghiệp vụ quan trọng (employees, contracts, payroll_records...), không xóa cứng.
- **Không lưu trường dẫn xuất:** mọi giá trị tính được từ trường khác trong cùng truy vấn (tổng tiền, số ngày, trạng thái trễ/sớm...) đều tính động, **trừ** giá trị mang tính **snapshot pháp lý/lịch sử** bắt buộc phải chốt cứng theo thời điểm (VD: `net_salary`, `total_days` của leave request đã duyệt) — các trường hợp này được đánh dấu rõ ở Phần B.
- **Enum dùng chung** định nghĩa một lần, tái sử dụng ở nhiều bảng thay vì tạo enum trùng ý nghĩa.
- **Ảnh/tài liệu:** chỉ lưu URL trỏ tới object storage (S3/GCS/Azure Blob...), không lưu binary trong database.
- **Multi-company:** các bảng cấp cao (company, user, role...) đều có đường dẫn tới `company_id` trực tiếp hoặc gián tiếp qua department/branch.

## A.2 Danh sách bảng theo module

| # | Module | Bảng |
|---|---|---|
| 01 | Tổ chức | companies, organizational_units, regions, branches, positions, job_levels, cost_centers, org_simulation_scenarios, org_simulation_changes (gộp từ Module 41) |
| 02 | Tuyển dụng | manpower_requests, job_positions, job_postings, candidates, applications, interviews, interview_evaluations, offers, talent_pool, **interview_kits, interview_kit_questions, interview_panel_members, interview_evaluation_scores** (bổ sung — Interview Management, xem Phần C) |
| 03 | Onboarding | onboarding_checklist_templates, onboarding_checklist_template_items, onboarding_processes, onboarding_process_items, account_provisioning, orientation_sessions |
| 04 | Nhân viên | employees, employee_personal_info, employee_employment_info, employee_emergency_contacts, employee_dependents, employee_documents, employee_history |
| 05 | Hợp đồng | contract_types, contracts, contract_amendments, contract_terminations, workforce_restructuring_plans, workforce_restructuring_plan_items |
| 06 | Chấm công | shifts, shift_assignments, attendance_records, overtime_requests, attendance_corrections |
| 07 | Nghỉ phép | leave_types, leave_policies, sick_leave_entitlement_tiers, leave_balances, leave_requests |
| 08 | Lương | salary_components, employee_salary_history, salary_advances, payroll_periods, payroll_records, payroll_record_items, payroll_anomalies (gộp từ Module 37) |
| 09 | Hiệu suất | performance_cycles, kpis, goals, performance_reviews, performance_evaluations |
| 10 | ~~Đào tạo~~ | *[ĐÃ LƯỢC BỎ]* *(Bằng cấp/chứng chỉ lưu tại `employee_documents`)* |
| 11 | ~~Phúc lợi~~ | *[ĐÃ LƯỢC BỎ]* *(Phụ cấp & bảo hiểm xử lý trực tiếp tại Module 05 Contract & 08 Payroll)* |
| 12 | Khen thưởng/Kỷ luật | rewards, disciplines, grievances, labor_union_members |
| 13 | Tài sản | assets, asset_allocations, asset_inventory_checks, asset_inventory_check_items |
| 14 | Career & Talent | career_paths, succession_plans, succession_candidates, internal_talent_pool, career_simulation_saved_paths (gộp từ Module 32) |
| 15 | ~~Khảo sát~~ | *[ĐÃ LƯỢC BỎ]* *(Biểu mẫu khảo sát rời rạc, dùng công cụ chuyên dụng bên ngoài)* |
| 16 | Offboarding | resignations, terminations, exit_interviews, offboarding_clearances |
| 17 | ESS | *(không có bảng riêng — triển khai phân tán qua các Controller `/api/v1/my-*` tại từng module nghiệp vụ)* |
| 18 | Workflow | workflow_definitions, workflow_steps, workflow_conditions, workflow_instances, workflow_approval_steps, workflow_delegates |
| 19 | Notification | notification_templates, notifications, reminders |
| 20 | Reports | report_exports, dashboard_configs, manager_effectiveness_scores, manager_effectiveness_configs (gộp từ Module 40) |
| 21 | System Admin | users, roles, permissions, role_permissions, user_roles, audit_logs, system_settings, integrations, data_import_jobs |
| 22 | AI Features | ai_interaction_logs, ai_conversations, ai_messages, ai_intent_logs, knowledge_base_documents, knowledge_base_chunks, ai_unanswered_questions, ai_screening_logs, ai_bulk_screening_jobs |
| 23 | Lưu kho & Hủy hồ sơ | record_retention_policies, record_metadata_field_configs, record_archive_submissions, record_disposal_proposals, record_disposal_committee_reviews |

| 31 | Talent Marketplace | internal_opportunities, internal_opportunity_interests, internal_assignments |
| 32 | ~~Career Pathing Simulator~~ | *[ĐÃ GỘP VÀO MODULE 14]* *(Bảng `career_simulation_saved_paths` chuyển về Module 14)* |
| 33 | ~~Skill Graph & Gap Analysis~~ | *[ĐÃ LƯỢC BỎ]* *(Đánh giá năng lực đã tích hợp qua KPI/360-Review ở Module 09)* |
| 34 | ~~Alumni Network~~ | *[ĐÃ LƯỢC BỎ]* *(Mạng xã hội cựu nhân viên độc lập)* |
| 35 | Compliance Radar | legal_changes, legal_change_affected_items |
| 36 | Earned Wage Access | wage_access_requests |
| 37 | ~~Payroll Anomaly Detection~~ | *[ĐÃ GỘP VÀO MODULE 08]* *(Bảng `payroll_anomalies` chuyển về Module 08)* |
| 38 | ~~Time-off Donation~~ | *[ĐÃ LƯỢC BỎ]* *(Không phù hợp Điều 113-114 BLLĐ 2019)* |
| 39 | AI 1-1 Assistant | one_on_ones, one_on_one_action_items |
| 40 | ~~Manager Effectiveness Score~~ | *[ĐÃ GỘP VÀO MODULE 20]* *(Bảng chuyển về Module 20)* |
| 41 | ~~What-if Org Simulation~~ | *[ĐÃ GỘP VÀO MODULE 01]* *(Bảng chuyển về Module 01)* |
| 42 | Seating Chart / Workplace | workplace_sites, workplace_floors, workplace_zones, workplace_rows, workplace_seats, workplace_seat_linked_assets, workplace_non_desk_objects, workplace_hotdesk_bookings, workplace_floor_plans |

**Tổng cộng:** 22 module gốc có bảng dữ liệu (**117 bảng**) + **12 module đề xuất bổ sung (31-42, 34 bảng mới)** = **151 bảng**. Module 22 và 23 bổ sung trường mới vào các bảng đã có ở Module 02/04: `job_positions`, `candidates`, `applications`, `employee_documents` (xem chi tiết ở Phần B tương ứng). Chi tiết schema Module 31-42 và phần bổ sung Interview Management (Module 02) xem **Phần C**.

## A.3 Danh sách Enum toàn hệ thống

### Enum dùng chung nhiều module

| Enum | Giá trị | Dùng ở |
|---|---|---|
| `gender_enum` | male, female, other | candidates, employee_personal_info |
| `active_status_enum` | active, inactive | companies, branches, departments, teams, positions, cost_centers, labor_union_members, integrations |
| `approval_status_enum` | draft, pending, approved, rejected, cancelled | overtime_requests, attendance_corrections, leave_requests, salary_advances, resignations, workflow_instances, benefit_claims |
| `employment_type_enum` | full_time, part_time, internship, contract | job_positions, employee_employment_info, leave_policies |
| `family_relationship_enum` | spouse, parent, child, sibling, other | employee_emergency_contacts, employee_dependents |

### Enum riêng theo module

| Module | Enum | Giá trị |
|---|---|---|
| 02 | `manpower_request_status` | pending, approved, rejected |
| 02 | `job_position_status` | open, on_hold, closed |
| 02 | `posting_channel_enum` | website, linkedin, facebook, job_board, referral |
| 02 | `posting_status` | draft, published, closed |
| 02 | `candidate_source_enum` | website, referral, headhunter, job_board, social_media |
| 02 | `candidate_status` | new, in_process, hired, rejected, talent_pool |
| 02 | `application_stage_enum` | screening, interview, offer, hired, rejected |
| 02 | `interview_type_enum` | phone, online, onsite |
| 02 | `interview_status` | scheduled, completed, cancelled, no_show |
| 02 | `interview_recommendation_enum` | strong_yes, yes, no, strong_no |
| 02 | `offer_status` | sent, accepted, rejected, negotiating, expired |
| 03 | `onboarding_item_category` | document, equipment, account, training, other |
| 03 | `onboarding_status` | in_progress, completed, cancelled |
| 03 | `onboarding_item_status` | pending, completed, skipped |
| 03 | `provisioning_status` | pending, provisioned, revoked |
| 03 | `session_status` | scheduled, completed, cancelled |
| 04 | `employment_status_enum` | probation, active, on_leave, resigned, terminated |
| 04 | `marital_status_enum` | single, married, divorced, widowed |
| 04 | `employee_document_type` | degree, certificate, id_card, contract, other |
| 04 | `employee_change_type` | position_change, department_change, salary_change, status_change, other |
| 05 | `contract_status` | draft, active, expired, terminated |
| 05 | `probation_type_enum` | embedded_in_contract, separate_probation_contract |
| 05 | `probation_result_enum` | passed, failed |
| 05 | `renewal_exemption_reason_enum` | none, elderly_worker, foreign_worker, union_officer_term |
| 05 | `termination_ground_enum` | contract_expired, work_completed, mutual_agreement, employee_lawful_resignation, employee_resignation_no_notice_required, employer_lawful_termination, disciplinary_dismissal, employee_unlawful_termination, employer_unlawful_termination, workforce_restructuring, merger_division_sale, employee_death_incapacity, employer_ceased_operation |
| 05 | `termination_initiator_enum` | employee, employer, mutual, automatic |
| 05 | `severance_type_enum` | none, thoi_viec, mat_viec_lam |
| 05 | `restructuring_reason_enum` | restructure_organization, technology_change, economic_reason, merger_division_sale |
| 05 | `restructuring_plan_status_enum` | draft, published, executed |
| 05 | `restructuring_action_enum` | continue_working, retrain, part_time_transfer, retirement, termination |
| 06 | `check_method_enum` | face_recognition, fingerprint, manual, mobile_gps |
| 06 | `work_mode_enum` | onsite, remote, hybrid |
| 07 | `leave_category_enum` | annual, public_holiday, personal_paid, personal_unpaid, sick, maternity |
| 07 | `funding_source_enum` | company, social_insurance_fund |
| 07 | `job_condition_level_enum` | normal, hazardous, special_hazardous |
| 07 | `leave_session_enum` | full_day, morning, afternoon |
| 08 | `salary_component_type` | allowance, bonus, deduction |
| 08 | `payroll_period_status` | open, processing, closed |
| 08 | `payroll_record_status` | draft, approved, paid |
| 09 | `performance_cycle_type` | quarterly, semi_annual, annual |
| 09 | `performance_cycle_status` | draft, active, closed |
| 09 | `goal_status` | not_started, in_progress, completed, cancelled |
| 09 | `review_type_enum` | self, manager |
| 09 | `performance_rating_enum` | excellent, good, satisfactory, needs_improvement, poor |
| 10 | `training_provider_type` | internal, external |
| 10 | `training_registration_status` | registered, attended, absent, cancelled |
| 11 | `benefit_plan_type` | health_insurance, meal_allowance, transport_allowance, other |
| 11 | `enrollment_status` | active, cancelled |
| 12 | `reward_type_enum` | certificate, monetary, promotion_recommendation, other |
| 12 | `violation_category_enum` | general, financial_asset_confidential |
| 12 | `discipline_type_enum` | khien_trach, keo_dai_nang_luong, cach_chuc, sa_thai |
| 12 | `dismissal_ground_enum` | theft_fraud_gambling_assault_drugs, trade_secret_ip_serious_damage_harassment, repeat_violation_during_active_discipline, unauthorized_absence |
| 12 | `discipline_status_enum` | draft, pending_approval, decided, expired |
| 12 | `grievance_category_enum` | workplace_conflict, compensation, policy, harassment, other |
| 12 | `grievance_status` | open, investigating, resolved, closed |
| 13 | `asset_category_enum` | laptop, phone, access_card, furniture, vehicle, other |
| 13 | `asset_status_enum` | in_stock, allocated, under_repair, disposed |
| 13 | `asset_condition_enum` | new, good, fair, damaged |
| 13 | `inventory_result_enum` | matched, missing, damaged |
| 14 | `succession_risk_enum` | low, medium, high |
| 14 | `succession_readiness_enum` | ready_now, ready_1_2_years, ready_3_5_years |
| 15 | `survey_status` | draft, active, closed |
| 15 | `survey_question_type` | rating_scale, single_choice, multiple_choice, text |
| 16 | `resignation_reason_enum` | personal, career_change, relocation, dissatisfaction, other |
| 16 | `termination_reason_enum` | performance, misconduct, redundancy, mutual_agreement, other |
| 16 | `clearance_status` | pending, cleared, issue_found |
| 18 | `workflow_status_enum` | draft, published, archived |
| 02 (bổ sung) | `panel_role_enum` | technical, culture_fit, hiring_manager, hr |
| 02 (bổ sung) | `panel_recommendation_enum` | strong_yes, yes, no, strong_no |
| 31 | `opportunity_type_enum` | short_term_project, open_position |
| 31 | `interest_status_enum` | expressed, shortlisted, selected, rejected |
| 31 | `assignment_status_enum` | active, completed, cancelled |
| 35 | `legal_change_priority_enum` | high, medium, low |
| 35 | `legal_change_status_enum` | new, reviewing, resolved |
| 35 | `affected_item_type_enum` | contract_rule, policy_document, payroll_rule |
| 36 | `wage_access_status_enum` | pending, approved, disbursed, deducted, rejected |
| 37 | `anomaly_type_enum` | salary_spike, abnormal_overtime, duplicate_bank_account, salary_after_termination, mismatched_allowance |
| 37 | `anomaly_risk_level_enum` | high, medium, low |
| 37 | `anomaly_status_enum` | flagged, resolved, false_positive |
| 39 | `one_on_one_status_enum` | scheduled, completed, cancelled |
| 39 | `action_item_status_enum` | open, completed, overdue |
| 41 | `simulation_status_enum` | draft, applied, discarded |
| 41 | `simulation_change_type_enum` | merge_unit, split_unit, transfer_employees, change_cost_center |
| 42 | `seat_type_enum` | dedicated, hot_desk, visitor |
| 42 | `seat_status_enum` | available, occupied, maintenance, blocked |
| 42 | `hotdesk_booking_status_enum` | booked, checked_in, cancelled, no_show |
| 42 | `non_desk_object_type_enum` | meeting_room, entrance, exit, restroom, pantry, printer, locker |
| 18 | `approval_request_type_enum` | leave, overtime, attendance_correction, salary_advance, resignation, contract_amendment, records_archive_submission, records_disposal_proposal, expense, other |
| 18 | `approver_type_enum` | direct_manager, organizational_unit_head, hr_manager, specific_role, specific_employee |
| 18 | `overdue_action_enum` | remind, auto_escalate, auto_reject |
| 18 | `workflow_action_enum` | approved, rejected, delegated, auto_escalated, auto_rejected |
| 19 | `notification_channel_enum` | email, in_app, sms |
| 19 | `reminder_status` | pending, sent, cancelled |
| 21 | `user_status_enum` | active, locked, disabled |
| 21 | `data_scope_enum` | own, team, department, company, all |
| 21 | `integration_type_enum` | accounting, timekeeping_device, social_insurance_portal, sso, other |
| 21 | `import_job_status` | processing, success, failed |
| 22 | `ai_feature_enum` | chatbot, cv_screening, onboarding_ocr |
| 22 | `ai_call_status` | success, low_confidence, failed, timeout |
| 22 | `conversation_status` | active, archived |
| 22 | `message_role_enum` | user, assistant |
| 22 | `ai_response_type` | out_of_scope, function_calling, rag_policy, fallback_no_data |
| 22 | `message_feedback` | helpful, not_helpful |
| 22 | `chat_intent_enum` | in_scope_data, in_scope_policy, out_of_scope |
| 22 | `kb_category_enum` | hr_policy, employee_handbook, system_guide, legal_compliance |
| 22 | `kb_status_enum` | active, archived |
| 22 | `unanswered_status` | pending_review, document_added, dismissed |
| 22 | `ai_confidence_enum` | high, medium, low |
| 22 | `bulk_job_status` | processing, completed, completed_with_errors |
| 02/04 | `document_verification_status` | not_applicable, pending_review, confirmed |
| 04 | `ocr_rejected_reason` | blurry_image, wrong_document, low_confidence, other |
| 23 | `metadata_field_type_enum` | text, number, date, boolean |
| 23 | `disposal_reason_enum` | retention_expired, duplicate, other |
| 23 | `disposal_proposal_status_enum` | pending_committee_review, approved, rejected |
| 23 | `committee_decision_enum` | approve, reject |
| 23 | `archive_status_enum` | not_submitted, pending_review, archived, pending_disposal, disposed |

## A.4 Bảng lõi & vai trò trung tâm

- **`employees`** — mọi module đều tham chiếu qua `employee_id`. Liên kết 1-1 tới `employee_personal_info` (có `photo_url` dùng cho hồ sơ **và** đối chiếu khuôn mặt khi chấm công) và `employee_employment_info`.
- **`organizational_units` / `branches` / `positions` / `job_levels`** (Module 01) — 3 chiều tổ chức **độc lập** (đơn vị / địa lý / vị trí việc làm), tham chiếu tổ chức cho Recruitment, Employee, Payroll, Performance... Mọi module có nhu cầu lọc theo "phòng ban" đều trỏ tới `organizational_unit_id`, không phải `branches`/`positions`.
- **`users`** (Module 21) — gắn `employee_id`, gốc cho toàn bộ xác thực/phân quyền; `notifications.user_id` cũng trỏ về đây.
- **`workflow_definitions`/`workflow_instances`** (Module 18) — `workflow_instances` không FK cứng tới từng loại request, mà dùng cặp `(request_type, request_id)` để tham chiếu động tới bản ghi gốc ở bất kỳ module nào cần phê duyệt; mỗi instance khóa cứng vào 1 phiên bản `workflow_definitions` cụ thể (không đổi dù luồng gốc được publish bản mới sau đó).
- **`contract_terminations`** (Module 05) — bản ghi pháp lý chính thức duy nhất về việc chấm dứt HĐLĐ (căn cứ Điều 34, báo trước, trợ cấp); `resignations`/`terminations` (Module 16) chỉ là lớp yêu cầu/quyết định nghiệp vụ, liên kết sang đây qua `contract_termination_id`.
- **`asset_allocations`** (Module 13) — dùng chung cho cấp phát ở Onboarding (qua `onboarding_process_id`) và thu hồi ở Offboarding.
- **`account_provisioning`** (Module 03) — dùng chung cho tạo tài khoản lúc Onboarding và vô hiệu hóa lúc Offboarding (field `revoked_at`).
- **`payroll_records`** (Module 08) — dùng chung cho lương định kỳ và lương thanh toán cuối khi nghỉ việc (field `is_final_settlement`).
- **`employee_documents`** (Module 04, mở rộng ở Module 23) — vòng đời tài liệu số đầy đủ: upload → OCR (Module 04 **[AI]**) → lưu kho chính thức → theo dõi hạn lưu trữ → đề xuất hủy có hội đồng xét duyệt.

## A.5 Sơ đồ quan hệ tổng thể (ERD dạng text)

```
companies 1───n organizational_units (cây tự tham chiếu qua parent_unit_id, độc lập với 2 cây dưới)
companies 1───n regions 1───n branches                         (Chiều 2 — Địa lý, độc lập)
positions n───1 job_levels                                     (Chiều 3 — danh mục phẳng, độc lập)

employees 1───1 employee_personal_info
employees 1───1 employee_employment_info ──n───1 organizational_units
                                          ──n───1 branches
                                          ──n───1 positions      (3 FK độc lập, không suy ra lẫn nhau)
employees 1───n employee_dependents
employees 1───n employee_emergency_contacts
employees 1───n employee_documents
employees 1───n employee_history

manpower_requests n───1 positions, n───1 organizational_units
job_positions n───1 manpower_requests, n───1 organizational_units
job_postings n───1 job_positions
applications n───1 candidates, n───1 job_positions
interviews n───1 applications
interview_evaluations n───1 interviews
offers n───1 applications
applications 1───1 employees (khi hiring thành công)

onboarding_processes n───1 employees
onboarding_process_items n───1 onboarding_processes
asset_allocations n───1 employees, n───1 assets, n───(0..1)─1 onboarding_processes
account_provisioning n───1 employees, n───(0..1)─1 onboarding_processes

contracts n───1 employees, n───1 contract_types
contract_amendments n───1 contracts
contract_terminations n───1 contracts, n───(0..1)─1 workforce_restructuring_plans
workforce_restructuring_plan_items n───1 workforce_restructuring_plans, n───1 employees

shift_assignments n───1 employees, n───1 shifts
attendance_records n───1 employees
overtime_requests n───1 employees
attendance_corrections n───1 employees, n───(0..1)─1 attendance_records

leave_balances n───1 employees, n───1 leave_types
leave_requests n───1 employees, n───1 leave_types

employee_salary_history n───1 employees
payroll_records n───1 employees, n───1 payroll_periods
payroll_record_items n───1 payroll_records, n───1 salary_components
salary_advances n───1 employees

goals n───1 employees, n───1 performance_cycles, n───(0..1)─1 kpis
performance_reviews n───1 goals
performance_evaluations n───1 employees, n───1 performance_cycles

training_registrations n───1 employees, n───1 courses
courses n───1 training_programs
certifications n───1 employees, n───(0..1)─1 courses

benefit_enrollments n───1 employees, n───1 benefit_plans
benefit_claims n───1 benefit_enrollments

rewards / disciplines / grievances n───1 employees
labor_union_members n───1 employees

resignations / terminations n───1 employees, n───(0..1)─1 contract_terminations
exit_interviews n───1 employees
offboarding_clearances n───1 employees, n───1 organizational_units

workflow_definitions 1───n workflow_steps 1───n workflow_conditions
workflow_instances n───1 workflow_definitions (snapshot phiên bản)
workflow_instances (request_type, request_id) ──→ bản ghi động: leave_requests, overtime_requests,
    attendance_corrections, salary_advances, resignations, contract_amendments,
    record_archive_submissions, record_disposal_proposals...
workflow_approval_steps n───1 workflow_instances, n───1 workflow_steps

record_archive_submissions n───1 employee_documents
record_disposal_proposals n───1 employee_documents
record_disposal_committee_reviews n───1 record_disposal_proposals, n───1 employees

notifications n───1 users
users n───(0..1)─1 employees
user_roles n───1 users, n───1 roles
role_permissions n───1 roles, n───1 permissions
audit_logs n───1 users
```

## A.6 Trường phục vụ xác thực khuôn mặt khi chấm công

| Bảng | Trường | Vai trò |
|---|---|---|
| `employee_personal_info` | `photo_url` | Ảnh chân dung gốc — dùng làm ảnh tham chiếu để đối chiếu khuôn mặt |
| `attendance_records` | `check_in_photo_url` | Ảnh chụp thực tế tại thời điểm check-in |
| `attendance_records` | `check_in_face_match_score` | Điểm tương đồng khuôn mặt do service AI trả về khi check-in |
| `attendance_records` | `check_out_photo_url` | Ảnh chụp thực tế tại thời điểm check-out |
| `attendance_records` | `check_out_face_match_score` | Điểm tương đồng khuôn mặt khi check-out |
| `attendance_records` | `check_in_method` / `check_out_method` | Đánh dấu `face_recognition` khi dùng nhận diện khuôn mặt |

> Luồng: client gửi ảnh chụp lúc check-in/out → backend gọi service nhận diện khuôn mặt so khớp với `employee_personal_info.photo_url` → lưu ảnh + điểm đối chiếu vào `attendance_records`. Điểm dưới ngưỡng (`system_settings`) → từ chối hoặc gắn cờ cần xác minh thủ công.

## A.7 Nghiệp vụ cố tình KHÔNG tạo bảng riêng (tránh dư thừa/dẫn xuất)

| Nghiệp vụ tưởng chừng cần bảng riêng | Vì sao không tạo | Thay bằng |
|---|---|---|
| Organization Structure (sơ đồ cây) | Suy ra từ `organizational_units.parent_unit_id` + `manager_employee_id` | Query dựng cây động |
| Equipment (thiết bị onboarding) | Trùng với cấp phát tài sản | `asset_allocations` (Module 13) |
| Leave History | Chỉ là `leave_requests` đã duyệt | Query `leave_requests WHERE status='approved'` |
| Training Attendance | Trùng mục đích với trạng thái đăng ký | `training_registrations.status` |
| Health Insurance (bảng riêng) | Chỉ là 1 loại benefit plan | `benefit_plans.plan_type='health_insurance'` |
| Contract Renewal (bảng riêng) | Gia hạn = tạo hợp đồng mới có liên kết | `contracts.previous_contract_id` |
| Payslip (bảng riêng) | Là bản thể hiện PDF của 1 payroll_record | `payroll_records.pdf_url` |
| Learning History | Tổng hợp từ 2 nguồn có sẵn | Join `training_registrations` + `certifications` |
| Working Hours/Late/Early/Absent (lưu cứng) | Tính được từ so `attendance_records` với `shifts` | Query động khi cần báo cáo |
| eNPS Score | Điểm tổng hợp từ câu trả lời khảo sát | Tính động từ `survey_answers` |
| Mọi loại "Report" | Là kết quả truy vấn tổng hợp, không phải thực thể | Query trực tiếp, chỉ lưu metadata export ở `report_exports` |
| Learning History (AI) / Remaining leave (AI) | Chatbot Q&A không cần bảng tổng hợp riêng | Function calling query thẳng vào bảng nguồn tương ứng (Module 20 function registry) |
| Approval Matrix tĩnh (bảng cấu hình số cấp cố định) | Không đủ linh hoạt cho điều kiện rẽ nhánh/SLA per-step | `workflow_definitions` + `workflow_steps` + `workflow_conditions` (Module 18) |
| Trợ cấp thôi việc/mất việc làm (cho HR tự nhập số tiền) | Dễ tính sai/tính thiếu theo Điều 46-47 BLLĐ, không truy vết được | Hệ thống tự tính từ `employee_salary_history` + số tháng đã đóng BHTN, lưu vào `contract_terminations.severance_amount`, chỉ override kèm lý do bắt buộc |

## A.8 Ghi chú triển khai

- Nên dùng PostgreSQL để tận dụng kiểu `ENUM` gốc, `JSONB` (cho `config`, `filter_params`, `old_value`/`new_value`), `UUID` built-in, và **extension `pgvector`** cho cột `embedding` ở `knowledge_base_chunks` (Module 22) — không cần thêm vector DB riêng (Pinecone/Weaviate) ở quy mô 1 doanh nghiệp.
- Với bảng có "khoảng hiệu lực" (`employee_salary_history`, `shift_assignments`), cân nhắc ràng buộc không cho chồng lấp `effective_date`/`end_date` cùng 1 `employee_id`.
- Index cho các trường tra cứu thường xuyên: `employees.employee_code`, `attendance_records.(employee_id, work_date)`, `payroll_records.(payroll_period_id, employee_id)`, `workflow_instances.(request_type, request_id)`.
- **Retention job cho dữ liệu AI:** `ai_conversations`/`ai_messages` (12 tháng), `ai_screening_logs` (24 tháng), `ai_interaction_logs` (12 tháng) — cần batch job dọn định kỳ, chi tiết thời hạn xem `HRM-AI-Features-Business-Rules.md` Mục 6. Riêng ảnh gốc dùng cho OCR lưu ở object storage (không phải DB) áp dụng thời hạn theo luật lưu trữ hồ sơ lao động, không xóa theo lịch batch thông thường.

---

# PHẦN B — SCHEMA CHI TIẾT THEO MODULE

## 01. Quản lý Tổ chức

> **Thiết kế 3 chiều độc lập** — xem giải thích đầy đủ ở `HRM-Mo-Ta-Nghiep-Vu-Chi-Tiet.md` Module 01. `organizational_units` (Chiều 1), `regions`/`branches` (Chiều 2), `positions`/`job_levels` (Chiều 3) là 3 nhóm bảng **không có FK chéo lẫn nhau** — chỉ giao nhau tại `employee_employment_info` (Module 04).

`companies`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| tax_code | VARCHAR | |
| address | VARCHAR | |
| phone | VARCHAR | |
| email | VARCHAR | |
| logo_url | VARCHAR | |
| established_date | DATE | |
| status | ENUM(`active_status_enum`) | |

### Chiều 1 — Đơn vị tổ chức

`organizational_units`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| company_id | UUID (FK) | |
| name | VARCHAR | |
| code | VARCHAR | |
| parent_unit_id | UUID (FK self, nullable) | Cây tự tham chiếu, không giới hạn số cấp cứng (thay cho cặp departments/teams cố định 2 cấp trước đây) |
| manager_employee_id | UUID (FK employees, nullable) | |
| cost_center_id | UUID (FK, nullable) | |
| inherits_parent_permissions | BOOLEAN (default true) | Có kế thừa quyền từ đơn vị cha xuống hay bị ghi đè riêng |
| status | ENUM(`active_status_enum`) | |

`cost_centers`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| company_id | UUID (FK) | |
| name | VARCHAR | |
| code | VARCHAR | |
| status | ENUM(`active_status_enum`) | |

### Chiều 2 — Địa lý (độc lập với Đơn vị tổ chức)

`regions`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| company_id | UUID (FK) | |
| name | VARCHAR | VD: Miền Bắc, Miền Nam |
| code | VARCHAR | |
| status | ENUM(`active_status_enum`) | |

`branches`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| region_id | UUID (FK, nullable) | |
| company_id | UUID (FK) | |
| name | VARCHAR | |
| code | VARCHAR | |
| address | VARCHAR | |
| phone | VARCHAR | |
| status | ENUM(`active_status_enum`) | |

### Chiều 3 — Vị trí việc làm (danh mục phẳng, không phụ thuộc đơn vị/địa lý)

`positions`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| job_level_id | UUID (FK) | |
| name | VARCHAR | **Không còn `department_id`** — Position là danh mục dùng chung toàn công ty, việc thuộc đơn vị nào chỉ xác định khi gán cho từng nhân viên (`employee_employment_info`) |
| code | VARCHAR | |
| description | TEXT | |
| status | ENUM(`active_status_enum`) | |

`job_levels`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| code | VARCHAR | |
| rank_order | INT | Thứ tự cấp bậc |

### Tính năng nâng cao — Mô phỏng Tái cơ cấu (gộp từ Module 41)

`org_simulation_scenarios`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| created_by_employee_id | UUID (FK) | |
| status | ENUM(`simulation_status_enum`) | |
| applied_at | DATETIME (nullable) | Khi chuyển thành thay đổi thật trên Module 01 |

`org_simulation_changes`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| org_simulation_scenario_id | UUID (FK) | |
| change_type | ENUM(`simulation_change_type_enum`) | |
| payload | JSONB | Chi tiết thay đổi (VD: `{from_unit_id, to_unit_id, employee_ids: [...]}`) |
| estimated_cost_impact | DECIMAL (nullable) | |
| affected_employee_count | INT | |

---

## 02. Tuyển dụng

`manpower_requests`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| organizational_unit_id | UUID (FK) | |
| requested_by_employee_id | UUID (FK) | |
| position_id | UUID (FK) | |
| quantity | INT | |
| reason | TEXT | |
| expected_start_date | DATE | |
| status | ENUM(`manpower_request_status`) | |
| approved_by_employee_id | UUID (FK, nullable) | |
| approved_at | DATETIME (nullable) | |

`job_positions`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| manpower_request_id | UUID (FK, nullable) | |
| title | VARCHAR | |
| organizational_unit_id | UUID (FK) | |
| job_level_id | UUID (FK) | |
| employment_type | ENUM(`employment_type_enum`) | |
| description | TEXT | |
| requirements | TEXT | |
| requirements_version | INT (default 1) | **[AI]** Tăng lên mỗi khi `requirements` được sửa — dùng vô hiệu hóa cache điểm AI cũ (Module 22) |
| status | ENUM(`job_position_status`) | |

`job_postings`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| job_position_id | UUID (FK) | |
| title | VARCHAR | |
| channel | ENUM(`posting_channel_enum`) | |
| publish_date | DATE (nullable) | |
| close_date | DATE (nullable) | |
| status | ENUM(`posting_status`) | |

`candidates`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| full_name | VARCHAR | |
| email | VARCHAR | |
| phone | VARCHAR | |
| gender | ENUM(`gender_enum`) | |
| date_of_birth | DATE | |
| resume_url | VARCHAR | |
| source | ENUM(`candidate_source_enum`) | |
| status | ENUM(`candidate_status`) | |
| ai_screening_consent | BOOLEAN | **[AI]** Ứng viên đồng ý cho hồ sơ được xử lý bởi AI, xin ngay ở form ứng tuyển |

`applications`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| candidate_id | UUID (FK) | |
| job_position_id | UUID (FK) | |
| applied_at | DATETIME | |
| current_stage | ENUM(`application_stage_enum`) | |
| rejection_reason | TEXT (nullable) | |
| ai_match_score | DECIMAL (nullable) | **[AI]** Điểm phù hợp 0-100 do AI tính, chỉ mang tính gợi ý |
| ai_match_reasoning | TEXT (nullable) | **[AI]** Giải thích điểm mạnh/yếu so với JD — bắt buộc có, không hiển thị điểm trần trụi |
| ai_confidence_level | ENUM(`ai_confidence_enum`, nullable) | **[AI]** Độ tin cậy của kết quả chấm điểm |
| ai_screened_at | DATETIME (nullable) | **[AI]** Thời điểm chạy AI |
| ai_screened_requirements_version | INT (nullable) | **[AI]** Snapshot `job_positions.requirements_version` lúc chấm — dùng phát hiện JD đã đổi kể từ đó |

`interviews`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| application_id | UUID (FK) | |
| round_number | INT | |
| interview_type | ENUM(`interview_type_enum`) | |
| scheduled_at | DATETIME | |
| location_or_link | VARCHAR | |
| status | ENUM(`interview_status`) | |

`interview_evaluations`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| interview_id | UUID (FK) | |
| interviewer_employee_id | UUID (FK) | |
| score | DECIMAL | |
| recommendation | ENUM(`interview_recommendation_enum`) | |
| comments | TEXT | |

`offers`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| application_id | UUID (FK) | |
| offered_salary | DECIMAL | |
| offered_start_date | DATE | |
| offer_letter_url | VARCHAR | |
| status | ENUM(`offer_status`) | |
| responded_at | DATETIME (nullable) | |

`talent_pool`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| candidate_id | UUID (FK) | |
| tag | VARCHAR | |
| note | TEXT | |
| added_at | DATETIME | |

---

## 03. Onboarding

`onboarding_checklist_templates`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| applicable_position_id | UUID (FK, nullable) | |

`onboarding_checklist_template_items`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| template_id | UUID (FK) | |
| title | VARCHAR | |
| category | ENUM(`onboarding_item_category`) | |
| order_index | INT | |
| is_required | BOOLEAN | |

`onboarding_processes`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| checklist_template_id | UUID (FK) | |
| start_date | DATE | |
| status | ENUM(`onboarding_status`) | |
| completed_at | DATETIME (nullable) | |

`onboarding_process_items`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| onboarding_process_id | UUID (FK) | |
| template_item_id | UUID (FK) | |
| status | ENUM(`onboarding_item_status`) | |
| completed_by_employee_id | UUID (FK, nullable) | |
| completed_at | DATETIME (nullable) | |
| note | TEXT | |

`account_provisioning`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| onboarding_process_id | UUID (FK, nullable) | |
| employee_id | UUID (FK) | |
| system_name | VARCHAR | |
| account_username | VARCHAR | |
| status | ENUM(`provisioning_status`) | |
| provisioned_by_employee_id | UUID (FK, nullable) | |
| provisioned_at | DATETIME (nullable) | |
| revoked_at | DATETIME (nullable) | Dùng lại ở Offboarding |

`orientation_sessions`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| onboarding_process_id | UUID (FK) | |
| session_name | VARCHAR | |
| scheduled_at | DATETIME | |
| trainer_employee_id | UUID (FK, nullable) | |
| status | ENUM(`session_status`) | |

---

## 04. Quản lý Nhân viên

`employees`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_code | VARCHAR (unique) | |
| company_id | UUID (FK) | |
| full_name | VARCHAR | |
| hire_date | DATE | |
| employment_status | ENUM(`employment_status_enum`) | |

`employee_personal_info` (1-1 employees)
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK, unique) | |
| gender | ENUM(`gender_enum`) | |
| date_of_birth | DATE | |
| national_id_number | VARCHAR | CMND/CCCD |
| national_id_issue_date | DATE | |
| national_id_issue_place | VARCHAR | |
| tax_code | VARCHAR | |
| marital_status | ENUM(`marital_status_enum`) | |
| nationality | VARCHAR | |
| personal_email | VARCHAR | |
| phone | VARCHAR | |
| permanent_address | VARCHAR | |
| current_address | VARCHAR | |
| photo_url | VARCHAR | **Ảnh chân dung — dùng làm hồ sơ đồng thời là ảnh gốc đối chiếu khuôn mặt khi check-in/out** |

`employee_employment_info` (1-1 employees)
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK, unique) | |
| organizational_unit_id | UUID (FK) | **Chiều 1** — Đơn vị tổ chức, thay cho cặp `department_id`/`team_id` cũ |
| branch_id | UUID (FK) | **Chiều 2** — Địa lý, độc lập hoàn toàn với `organizational_unit_id` |
| position_id | UUID (FK) | **Chiều 3** — Vị trí việc làm, độc lập hoàn toàn với 2 trường trên |
| job_level_id | UUID (FK) | |
| manager_employee_id | UUID (FK employees, nullable) | |
| employment_type | ENUM(`employment_type_enum`) | |
| company_email | VARCHAR | |
| work_location | VARCHAR | |
| probation_end_date | DATE (nullable) | |

`employee_emergency_contacts`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| full_name | VARCHAR | |
| relationship | ENUM(`family_relationship_enum`) | |
| phone | VARCHAR | |
| address | VARCHAR | |

`employee_dependents`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| full_name | VARCHAR | |
| relationship | ENUM(`family_relationship_enum`) | |
| date_of_birth | DATE | |
| national_id_number | VARCHAR (nullable) | |
| tax_deduction_registered | BOOLEAN | Đăng ký giảm trừ gia cảnh thuế TNCN |

`employee_documents`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| document_type | ENUM(`employee_document_type`) | |
| file_url | VARCHAR | |
| uploaded_at | DATETIME | |
| ocr_extracted_data | JSON (nullable) | **[AI]** Dữ liệu thô AI trích xuất từ ảnh giấy tờ, giữ lại đối chiếu |
| ocr_field_confidence | JSON (nullable) | **[AI]** Độ tin cậy từng trường, VD `{"national_id_number": 0.62}` |
| ai_detected_document_type | ENUM(`employee_document_type`, nullable) | **[AI]** Loại giấy tờ AI tự nhận diện — dùng phát hiện `DOCUMENT_TYPE_MISMATCH` |
| ocr_provider | VARCHAR (nullable) | **[AI]** Tên dịch vụ OCR đã dùng |
| ocr_processed_at | DATETIME (nullable) | **[AI]** Thời điểm AI xử lý xong |
| ocr_retry_count | INT (default 0) | **[AI]** Số lần đã thử lại OCR trên cùng file |
| rejected_reason | ENUM(`ocr_rejected_reason`, nullable) | **[AI]** Lý do người dùng bỏ qua OCR, nhập tay |
| verification_status | ENUM(`document_verification_status`) | **[AI]** `not_applicable` cho tài liệu không qua OCR |
| confirmed_by_employee_id | UUID (FK, nullable) | **[AI]** Ai xác nhận dữ liệu OCR đúng trước khi ghi chính thức |
| confirmed_at | DATETIME (nullable) | |

`employee_history`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| change_type | ENUM(`employee_change_type`) | |
| old_value | VARCHAR | |
| new_value | VARCHAR | |
| effective_date | DATE | |
| changed_by_employee_id | UUID (FK) | |

---

## 05. Quản lý Hợp đồng

> Schema hiện thực hóa đầy đủ ràng buộc pháp lý ở `HRM-Mo-Ta-Nghiep-Vu-Chi-Tiet.md` Module 05 (Bộ luật Lao động 2019). Các trường đánh dấu **[Pháp lý]** là bắt buộc để hệ thống tự validate/tự tính, không phải trường tùy chọn.

`contract_types`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| code | VARCHAR | |
| max_duration_months | INT (nullable) | **[Pháp lý]** NULL = không xác định thời hạn; có giá trị = xác định thời hạn, **≤ 36** (Điều 20) |

`contracts`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| contract_type_id | UUID (FK) | |
| contract_number | VARCHAR | |
| previous_contract_id | UUID (FK self, nullable) | Liên kết nếu là gia hạn/chuyển tiếp |
| start_date | DATE | |
| end_date | DATE (nullable) | Null = không xác định thời hạn |
| basic_salary | DECIMAL | Lương chính thức |
| signed_date | DATE | |
| file_url | VARCHAR | |
| status | ENUM(`contract_status`) | |
| is_probation_contract | BOOLEAN (default false) | **[Pháp lý]** true nếu đây là hợp đồng thử việc riêng (Điều 24 phương án b) |
| probation_type | ENUM(`probation_type_enum`, nullable) | **[Pháp lý]** Cách triển khai thử việc — Điều 24 |
| probation_start_date | DATE (nullable) | |
| probation_end_date | DATE (nullable) | **[Pháp lý]** Tính theo bảng thời gian tối đa ở Mục 5.2, theo nhóm công việc của `position_id` |
| probation_salary | DECIMAL (nullable) | **[Pháp lý]** Phải ≥ 85% `basic_salary` (Điều 26) |
| probation_result | ENUM(`probation_result_enum`, nullable) | Kết quả thử việc — bắt buộc có trước khi chuyển tiếp/chấm dứt |
| probation_result_notified_at | DATETIME (nullable) | **[Pháp lý]** Thời điểm thông báo kết quả thử việc bằng văn bản (Điều 27) |
| renewal_count | INT (default 0) | **[Pháp lý]** Số lần đã gia hạn liên tiếp dạng xác định thời hạn — chặn tạo mới nếu ≥ 1 (Điều 20.2), trừ khi `renewal_exemption_reason != none` |
| renewal_exemption_reason | ENUM(`renewal_exemption_reason_enum`, default 'none') | **[Pháp lý]** Miễn trừ giới hạn gia hạn — Điều 149/151/177 |
| auto_converted_to_indefinite | BOOLEAN (default false) | **[Pháp lý]** Đánh dấu hợp đồng được hệ thống tự động chuyển không xác định thời hạn do quá 30 ngày không ký mới (Điều 20.2) |

`contract_amendments`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| contract_id | UUID (FK) | |
| amendment_content | TEXT | |
| effective_date | DATE | |
| file_url | VARCHAR | |

`contract_terminations`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| contract_id | UUID (FK) | |
| termination_ground | ENUM(`termination_ground_enum`) | **[Pháp lý]** 1 trong 13 căn cứ Điều 34 — thay cho enum lý do tự do trước đây |
| initiated_by | ENUM(`termination_initiator_enum`) | employee / employer / mutual / automatic |
| submitted_date | DATE | Ngày nộp đơn/ra quyết định |
| required_notice_days | INT | **[Pháp lý]** Số ngày báo trước tối thiểu theo bảng Điều 35.1/36.2, tự tính theo `contract_type` tại thời điểm nộp — snapshot, không tính lại nếu luật/HĐ thay đổi sau này |
| earliest_valid_last_working_date | DATE | **[Pháp lý]** = `submitted_date + required_notice_days`, tự tính |
| termination_date | DATE | Ngày làm việc cuối cùng thực tế |
| is_notice_sufficient | BOOLEAN | **[Pháp lý]** false nếu `termination_date < earliest_valid_last_working_date` và căn cứ không thuộc nhóm "không cần báo trước" — cảnh báo rủi ro đơn phương chấm dứt trái luật (Điều 39) |
| protected_period_violation | BOOLEAN (default false) | **[Pháp lý]** true nếu hệ thống phát hiện vi phạm thời điểm cấm chấm dứt (Điều 37) — chặn cứng nếu `initiated_by = employer` |
| severance_type | ENUM(`severance_type_enum`) | none / `thoi_viec` (Điều 46) / `mat_viec_lam` (Điều 47) — 2 công thức khác nhau |
| eligible_service_years | DECIMAL (nullable) | **[Pháp lý]** Số năm làm việc được tính trợ cấp = tổng thời gian thực tế − thời gian đã đóng BHTN − thời gian đã được chi trả trợ cấp trước đó |
| average_salary_6_months | DECIMAL (nullable) | **[Pháp lý]** Lương bình quân 6 tháng liền kề, làm cơ sở tính trợ cấp |
| severance_amount | DECIMAL (nullable) | **[Pháp lý]** Số tiền trợ cấp hệ thống tự tính — HR chỉ được override kèm `override_reason` bắt buộc |
| override_reason | TEXT (nullable) | Bắt buộc nếu `severance_amount` bị HR sửa tay khác số hệ thống tính |
| restructuring_plan_id | UUID (FK, nullable) | Liên kết Phương án sử dụng lao động nếu chấm dứt hàng loạt (Điều 44) |
| written_notice_file_url | VARCHAR (nullable) | **[Pháp lý]** File thông báo chấm dứt bằng văn bản (Điều 45) |
| note | TEXT | |

`workforce_restructuring_plans`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| reason | ENUM(`restructuring_reason_enum`) | **[Pháp lý]** Điều 42/43 |
| description | TEXT | |
| public_notice_date | DATE (nullable) | **[Pháp lý]** Ngày công bố — hệ thống chặn `execute-terminations` nếu chưa đủ 15 ngày kể từ ngày này (Điều 44) |
| status | ENUM(`restructuring_plan_status_enum`) | draft / published / executed |
| created_by_employee_id | UUID (FK) | |

`workforce_restructuring_plan_items`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| plan_id | UUID (FK) | |
| employee_id | UUID (FK) | |
| action | ENUM(`restructuring_action_enum`) | continue_working / retrain / part_time_transfer / retirement / termination |
| note | TEXT | |

---

## 06. Chấm công & Ca làm việc

`shifts`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| company_id | UUID (FK) | |
| name | VARCHAR | |
| start_time | TIME | |
| end_time | TIME | |
| break_minutes | INT | |
| is_overnight | BOOLEAN | |

`shift_assignments`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| shift_id | UUID (FK) | |
| effective_date | DATE | |
| end_date | DATE (nullable) | |

`attendance_records`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| work_date | DATE | |
| check_in_time | DATETIME (nullable) | |
| check_in_method | ENUM(`check_method_enum`) | |
| check_in_photo_url | VARCHAR (nullable) | Ảnh chụp thực tế lúc check-in |
| check_in_face_match_score | DECIMAL (nullable) | Điểm đối chiếu khuôn mặt |
| check_in_location | VARCHAR (nullable) | Tọa độ GPS |
| check_out_time | DATETIME (nullable) | |
| check_out_method | ENUM(`check_method_enum`) | |
| check_out_photo_url | VARCHAR (nullable) | |
| check_out_face_match_score | DECIMAL (nullable) | |
| check_out_location | VARCHAR (nullable) | |
| work_mode | ENUM(`work_mode_enum`) | |

> `late/early/absent` **không lưu** — tính động bằng cách so `check_in_time`/`check_out_time` với `shifts` đang hiệu lực tại `work_date`.

`overtime_requests`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| request_date | DATE | |
| start_time | DATETIME | |
| end_time | DATETIME | |
| reason | TEXT | |
| status | ENUM(`approval_status_enum`) | |

`attendance_corrections`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| attendance_record_id | UUID (FK, nullable) | |
| correction_date | DATE | |
| requested_check_in_time | DATETIME (nullable) | |
| requested_check_out_time | DATETIME (nullable) | |
| reason | TEXT | |
| status | ENUM(`approval_status_enum`) | |

---

## 07. Quản lý Nghỉ phép

> Schema phân biệt rõ 4 nhóm bản chất pháp lý khác nhau — xem `HRM-Mo-Ta-Nghiep-Vu-Chi-Tiet.md` Module 07.

`leave_types`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| code | VARCHAR | |
| category | ENUM(`leave_category_enum`) | **[Pháp lý]** annual / public_holiday / personal_paid / personal_unpaid / sick / maternity — Điều 112-115, 139 |
| funding_source | ENUM(`funding_source_enum`) | **[Pháp lý]** company (doanh nghiệp trả lương) / social_insurance_fund (quỹ BHXH chi trả) — quyết định luồng tính vào Payroll ở Module 08 |
| fixed_days_per_event | DECIMAL (nullable) | **[Pháp lý]** Dùng khi `category = personal_paid/personal_unpaid` — số ngày **cố định theo luật** (Điều 115: kết hôn 3, con kết hôn 1, tang chế trực hệ 3, tang chế khác 1), không cho nhân viên tự chọn số ngày |
| is_paid | BOOLEAN | |
| requires_approval | BOOLEAN | |

`leave_policies`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| leave_type_id | UUID (FK) | |
| applicable_employment_type | ENUM(`employment_type_enum`, nullable) | |
| job_condition_level | ENUM(`job_condition_level_enum`, nullable) | **[Pháp lý]** Dùng khi `leave_types.category = annual` — normal (12 ngày) / hazardous (14 ngày) / special_hazardous (16 ngày) theo Điều 113 |
| accrual_days_per_year | DECIMAL | Số ngày cơ bản tương ứng `job_condition_level` |
| seniority_bonus_every_years | INT (default 5) | **[Pháp lý]** Điều 114 — cứ đủ N năm thâm niên +1 ngày |
| seniority_bonus_days | DECIMAL (default 1) | |
| carry_over_max_days | DECIMAL | |

`sick_leave_entitlement_tiers`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| min_bhxh_years | DECIMAL | **[Pháp lý]** Bậc số năm đã đóng BHXH — dưới 15 / 15-30 / từ 30 năm |
| max_bhxh_years | DECIMAL (nullable) | Null = không giới hạn trên |
| normal_condition_max_days | INT | 30 / 40 / 60 ngày tương ứng điều kiện làm việc bình thường |
| hazardous_condition_max_days | INT | +10 ngày mỗi bậc nếu làm nghề nặng nhọc/độc hại |

`leave_balances`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| leave_type_id | UUID (FK) | Chỉ áp dụng cho `category IN (annual, sick)` — nhóm còn lại không có số dư |
| year | INT | |
| total_days | DECIMAL | Tổng phép được cấp — với `annual` đã gồm cả phần cộng thâm niên (Điều 114) và tính theo tỷ lệ tháng làm việc nếu chưa đủ 12 tháng |
| used_days | DECIMAL | Đã dùng — không lưu remaining (= total - used, tính động) |

`leave_requests`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| leave_type_id | UUID (FK) | |
| start_date | DATE | |
| end_date | DATE | |
| session | ENUM(`leave_session_enum`) | |
| total_days | DECIMAL | Với `category = personal_paid/unpaid`: **snapshot từ `leave_types.fixed_days_per_event`**, không tính từ start/end date. Với `annual`: snapshot số ngày công thực trừ (đã loại trừ lễ/cuối tuần) tại thời điểm duyệt |
| reason | TEXT | |
| status | ENUM(`approval_status_enum`) | |

---

## 08. Tiền lương

`salary_components`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| code | VARCHAR | |
| component_type | ENUM(`salary_component_type`) | |
| is_taxable | BOOLEAN | |
| is_insurance_base | BOOLEAN | |

`employee_salary_history`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| basic_salary | DECIMAL | |
| insurance_salary | DECIMAL | Mức lương đóng bảo hiểm |
| effective_date | DATE | |
| end_date | DATE (nullable) | |

`salary_advances`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| request_date | DATE | |
| amount | DECIMAL | |
| reason | TEXT | |
| status | ENUM(`approval_status_enum`) | |
| disbursed_at | DATETIME (nullable) | |

`payroll_periods`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| start_date | DATE | |
| end_date | DATE | |
| pay_date | DATE | |
| status | ENUM(`payroll_period_status`) | |

`payroll_records`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| payroll_period_id | UUID (FK) | |
| employee_id | UUID (FK) | |
| basic_salary | DECIMAL | Snapshot tại kỳ lương |
| actual_work_days | DECIMAL | Lấy từ Attendance |
| overtime_pay | DECIMAL | Snapshot đã tính theo đơn giá tại thời điểm |
| gross_salary | DECIMAL | **Snapshot pháp lý** |
| social_insurance_employee | DECIMAL | Phần BHXH/BHYT/BHTN người lao động đóng |
| social_insurance_employer | DECIMAL | Phần doanh nghiệp đóng |
| personal_income_tax | DECIMAL | |
| net_salary | DECIMAL | **Snapshot pháp lý** — thực lãnh |
| is_final_settlement | BOOLEAN | Đánh dấu kỳ lương thanh toán cuối khi nghỉ việc (Module 16) |
| pdf_url | VARCHAR (nullable) | Phiếu lương |
| status | ENUM(`payroll_record_status`) | |

`payroll_record_items`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| payroll_record_id | UUID (FK) | |
| salary_component_id | UUID (FK) | |
| amount | DECIMAL | |

`payroll_anomalies` *(Rà soát bất thường bảng lương - gộp từ Module 37)*
| Trường | Kiểu | Ghi chú |
|---|---|---|
| payroll_record_id | UUID (FK `payroll_records`) | |
| anomaly_type | ENUM(`anomaly_type_enum`) | Lương tăng đột biến, OT quá trần, nhân viên đã thôi việc, trùng STK ngân hàng... |
| risk_level | ENUM(`anomaly_risk_level_enum`) | LOW / MEDIUM / HIGH |
| detail | JSONB | Số liệu cụ thể phát hiện bất thường (VD: % tăng so với kỳ trước, giờ OT thực tế) |
| status | ENUM(`anomaly_status_enum`) | PENDING / RESOLVED / ADJUSTED |
| resolved_by_employee_id | UUID (FK, nullable) | Kế toán hoặc HR xác nhận giải trình |
| resolved_at | DATETIME (nullable) | |

---

## 09. Quản lý Hiệu suất

`performance_cycles`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| cycle_type | ENUM(`performance_cycle_type`) | |
| start_date | DATE | |
| end_date | DATE | |
| status | ENUM(`performance_cycle_status`) | |

`kpis`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| organizational_unit_id | UUID (FK, nullable) | |
| name | VARCHAR | |
| unit | VARCHAR | |
| description | TEXT | |

`goals`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| performance_cycle_id | UUID (FK) | |
| kpi_id | UUID (FK, nullable) | |
| title | VARCHAR | |
| target_value | VARCHAR | |
| weight_percentage | DECIMAL | |
| status | ENUM(`goal_status`) | |

`performance_reviews`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| goal_id | UUID (FK) | |
| review_type | ENUM(`review_type_enum`) | self hoặc manager — gộp chung 1 bảng |
| reviewer_employee_id | UUID (FK) | |
| score | DECIMAL | |
| comment | TEXT | |
| submitted_at | DATETIME | |

`performance_evaluations`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| performance_cycle_id | UUID (FK) | |
| final_score | DECIMAL | |
| rating | ENUM(`performance_rating_enum`) | |
| finalized_by_employee_id | UUID (FK) | |
| finalized_at | DATETIME | |

---

## 10. ~~Đào tạo & Phát triển~~ *[ĐÃ LƯỢC BỎ KHỎI SCOPE]*
> *Lưu ý: Module này đã được lược bỏ để tinh gọn hệ thống. Bằng cấp, chứng chỉ chuyên môn của nhân sự được quản lý và số hóa trực tiếp tại bảng `employee_documents` (Module Onboarding & Employee Profile).*

---

## 11. ~~Phúc lợi~~ *[ĐÃ LƯỢC BỎ KHỎI SCOPE]*
> *Lưu ý: Module này đã được lược bỏ để tinh gọn hệ thống. Toàn bộ phụ cấp phúc lợi tiền tệ đã được cấu hình trong Hợp đồng (Module 05 Contract) và tự động tính toán vào Bảng lương (Module 08 Payroll qua `salary_components`). Chế độ bảo hiểm bắt buộc đã được tính toán theo luật trong Payroll.*



---

## 12. Khen thưởng & Kỷ luật

`rewards`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| reward_type | ENUM(`reward_type_enum`) | |
| title | VARCHAR | |
| amount | DECIMAL (nullable) | Nếu có, đẩy sang `payroll_record_items` kỳ gần nhất — **duy nhất** dòng tiền hợp pháp nối Module này với Payroll |
| reason | TEXT | |
| decided_by_employee_id | UUID (FK) | |
| decided_date | DATE | |

`disciplines`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| violation_date | DATE | **[Pháp lý]** Ngày xảy ra hành vi vi phạm — làm mốc tính thời hiệu (Điều 123) |
| violation_category | ENUM(`violation_category_enum`) | **[Pháp lý]** general / financial_asset_confidential — quyết định thời hiệu 6 hay 12 tháng |
| statute_of_limitation_deadline | DATE | **[Pháp lý]** Tự tính = `violation_date` + 6 hoặc 12 tháng theo `violation_category`; hệ thống chặn tạo mới nếu ngày hiện tại vượt mốc này |
| evidence_files | JSONB | **[Pháp lý]** Bắt buộc có bằng chứng — mảng URL |
| handbook_reference | VARCHAR | **[Pháp lý]** Điều khoản Nội quy lao động tương ứng hành vi vi phạm (Điều 127.3 — cấm kỷ luật hành vi không có trong nội quy) |
| meeting_date | DATE | Ngày họp xử lý kỷ luật |
| meeting_attendees | JSONB | **[Pháp lý]** Danh sách `{employee_id, role}` — bắt buộc có role `employee` (chính người bị xử lý) và `union_representative` nếu công ty có công đoàn (Điều 122.2-3) |
| discipline_type | ENUM(`discipline_type_enum`) | **[Pháp lý]** Chỉ 4 giá trị hợp pháp theo Điều 124: `khien_trach` / `keo_dai_nang_luong` / `cach_chuc` / `sa_thai` |
| dismissal_ground | ENUM(`dismissal_ground_enum`, nullable) | **[Pháp lý]** Bắt buộc có giá trị nếu `discipline_type = sa_thai` — đúng 1 trong 4 căn cứ Điều 125 |
| salary_extension_months | INT (nullable) | Dùng khi `discipline_type = keo_dai_nang_luong`, tối đa 6 tháng (Điều 124.2) |
| reason | TEXT | |
| decision_date | DATE | |
| decided_by_employee_id | UUID (FK) | |
| status | ENUM(`discipline_status_enum`) | draft / pending_approval / decided / **expired** (đã tự động xóa kỷ luật theo Điều 126) |
| expiry_date | DATE (nullable) | **[Pháp lý]** Tự tính = `decision_date` + 3 tháng (khiển trách) hoặc 6 tháng (kéo dài nâng lương/cách chức) — job nền tự chuyển `status = expired` khi tới hạn nếu không tái phạm |
| file_url | VARCHAR (nullable) | Biên bản họp xử lý kỷ luật |

`grievances`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| submitted_date | DATE | |
| category | ENUM(`grievance_category_enum`) | |
| description | TEXT | |
| status | ENUM(`grievance_status`) | |
| resolution_note | TEXT (nullable) | |
| resolved_at | DATETIME (nullable) | |

`labor_union_members`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| joined_date | DATE | |
| status | ENUM(`active_status_enum`) | |

---

## 13. Quản lý Tài sản

`assets`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| asset_code | VARCHAR (unique) | |
| name | VARCHAR | |
| category | ENUM(`asset_category_enum`) | |
| purchase_date | DATE | |
| purchase_cost | DECIMAL | |
| status | ENUM(`asset_status_enum`) | |

`asset_allocations`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| asset_id | UUID (FK) | |
| employee_id | UUID (FK) | |
| onboarding_process_id | UUID (FK, nullable) | |
| allocated_date | DATE | |
| returned_date | DATE (nullable) | |
| condition_on_allocation | ENUM(`asset_condition_enum`) | |
| condition_on_return | ENUM(`asset_condition_enum`, nullable) | |
| note | TEXT | |

`asset_inventory_checks`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| check_date | DATE | |
| performed_by_employee_id | UUID (FK) | |
| note | TEXT | |

`asset_inventory_check_items`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| inventory_check_id | UUID (FK) | |
| asset_id | UUID (FK) | |
| actual_result | ENUM(`inventory_result_enum`) | |
| note | TEXT | |

---

## 14. Phát triển Nhân sự

`career_paths`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| from_position_id | UUID (FK) | |
| to_position_id | UUID (FK) | |
| description | TEXT | |
| min_years_required | DECIMAL | |

`succession_plans`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| position_id | UUID (FK) | |
| risk_level | ENUM(`succession_risk_enum`) | |
| review_date | DATE | |

`succession_candidates`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| succession_plan_id | UUID (FK) | |
| employee_id | UUID (FK) | |
| readiness | ENUM(`succession_readiness_enum`) | |
| note | TEXT | |

`internal_talent_pool`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| tag | VARCHAR | |
| note | TEXT | |
| added_by_employee_id | UUID (FK) | |

`career_simulation_saved_paths` *(Mô phỏng lộ trình cá nhân - gộp từ Module 32)*
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| target_position_id | UUID (FK, nullable) | Vị trí mục tiêu mong muốn thăng tiến |
| suggested_path | JSONB | Snapshot kết quả gợi ý tại thời điểm lưu (vị trí, % tương đồng, thời gian trung bình) |
| saved_at | DATETIME | |

---

## 15. ~~Khảo sát & Gắn kết~~ *[ĐÃ LƯỢC BỎ KHỎI SCOPE]*
> *Lưu ý: Module này đã được lược bỏ khỏi phạm vi dự án do tính chất biểu mẫu khảo sát rời rạc độc lập (tương tự Google Forms), không thuộc chuỗi cung ứng dữ liệu vận hành Core HRM.*

---

## 16. Nghỉ việc

`resignations`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| submitted_date | DATE | |
| expected_last_working_date | DATE | Nguyện vọng ban đầu của nhân viên — **có thể khác** ngày hợp lệ sớm nhất theo luật, xem `contract_terminations.earliest_valid_last_working_date` |
| personal_reason_category | ENUM(`resignation_reason_enum`) | Lý do **mang tính mô tả/thống kê** (cá nhân/đổi việc/chuyển nhà...) — KHÔNG phải căn cứ pháp lý Điều 34, dùng cho báo cáo turnover (Module 20) |
| status | ENUM(`approval_status_enum`) | |
| contract_termination_id | UUID (FK `contract_terminations`, nullable) | Bản ghi pháp lý chính thức (căn cứ Điều 34, thời hạn báo trước, trợ cấp) được tạo khi đơn được duyệt — xem Module 05 |

`terminations`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| decision_date | DATE | |
| last_working_date | DATE | |
| decision_reason_category | ENUM(`termination_reason_enum`) | Lý do **mang tính mô tả/thống kê** (hiệu suất/vi phạm/tinh giản/thỏa thuận...) — căn cứ pháp lý chính xác nằm ở `contract_terminations.termination_ground` |
| decided_by_employee_id | UUID (FK) | |
| contract_termination_id | UUID (FK `contract_terminations`, nullable) | Bản ghi pháp lý chính thức — xem Module 05 |

`exit_interviews`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| interview_date | DATE | |
| interviewer_employee_id | UUID (FK) | |
| feedback_summary | TEXT | |
| would_recommend_company | BOOLEAN (nullable) | |

`offboarding_clearances`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| organizational_unit_id | UUID (FK) | Đơn vị cần xác nhận bàn giao (kế toán, IT, hành chính...) |
| cleared_by_employee_id | UUID (FK, nullable) | |
| status | ENUM(`clearance_status`) | |
| note | TEXT | |
| cleared_at | DATETIME (nullable) | |

---

## 17. Tự phục vụ Nhân viên (ESS)

Không có bảng riêng — lớp API đọc/ghi trên các bảng module khác, lọc theo `employee_id` của token đăng nhập.

---

## 18. Workflow & Role Designer (nâng cấp từ Approval Engine)

> Thay `approval_matrices` tĩnh bằng **Workflow Designer** có phiên bản, điều kiện rẽ nhánh tham chiếu bất kỳ thuộc tính hồ sơ, và SLA/hành vi quá hạn từng bước — chi tiết nghiệp vụ ở `HRM-Mo-Ta-Nghiep-Vu-Chi-Tiet.md` Module 18.

`workflow_definitions`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| company_id | UUID (FK) | |
| request_type | ENUM(`approval_request_type_enum`) | |
| version | INT | Tăng dần mỗi lần publish — hồ sơ đang chạy giữ nguyên `workflow_definition_id` (snapshot version) đã dùng lúc khởi tạo |
| name | VARCHAR | |
| status | ENUM(`workflow_status_enum`) | draft / published / archived — chỉ 1 bản `published` tại 1 thời điểm cho mỗi `request_type` |
| published_at | DATETIME (nullable) | |
| created_by_employee_id | UUID (FK) | |

`workflow_steps`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workflow_definition_id | UUID (FK) | |
| step_order | INT | |
| name | VARCHAR | |
| approver_type | ENUM(`approver_type_enum`) | direct_manager resolve qua `manager_employee_id`; organizational_unit_head resolve qua `organizational_units.manager_employee_id` (Module 01) |
| specific_approver_employee_id | UUID (FK, nullable) | Dùng khi approver_type = specific_employee |
| specific_role_id | UUID (FK `roles`, nullable) | Dùng khi approver_type = specific_role — tham chiếu bảng `roles` động ở Module 21, không hard-code enum vì mỗi công ty có thể có role tùy biến |
| sla_hours | INT | Thời hạn xử lý bước này |
| overdue_action | ENUM(`overdue_action_enum`) | remind / auto_escalate / auto_reject |

`workflow_conditions`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workflow_definition_id | UUID (FK) | |
| from_step_id | UUID (FK `workflow_steps`, nullable) | Null = điều kiện tại điểm vào workflow |
| to_step_id | UUID (FK `workflow_steps`) | |
| condition_expression | TEXT | Biểu thức tham chiếu **bất kỳ thuộc tính nào** của hồ sơ đang chạy, VD `leave_request.total_days > 5`, `salary_advance.amount > 10000000` — không giới hạn danh sách thuộc tính cố định |
| priority | INT | Thứ tự đánh giá khi nhiều điều kiện cùng khớp |

`workflow_instances`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workflow_definition_id | UUID (FK) | Snapshot đúng phiên bản đã published tại thời điểm khởi tạo — không đổi dù `workflow_definitions` publish bản mới sau đó |
| request_type | ENUM(`approval_request_type_enum`) | |
| request_id | UUID | ID bản ghi gốc ở module nghiệp vụ |
| current_step_id | UUID (FK `workflow_steps`, nullable) | |
| status | ENUM(`approval_status_enum`) | |

`workflow_approval_steps`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workflow_instance_id | UUID (FK) | |
| step_id | UUID (FK `workflow_steps`) | |
| approver_employee_id | UUID (FK, nullable) | Null nếu action = auto_escalated/auto_rejected |
| action | ENUM(`workflow_action_enum`) | approved / rejected / delegated / **auto_escalated** / **auto_rejected** — 2 giá trị sau do job nền quét quá hạn tự sinh, không phải người dùng thao tác |
| comment | TEXT | |
| sla_deadline_at | DATETIME | `acted_at` (nếu có) so với mốc này để xác định có quá hạn hay không |
| acted_at | DATETIME (nullable) | Null nếu đang chờ xử lý |

`workflow_delegates`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| delegator_employee_id | UUID (FK) | |
| delegate_employee_id | UUID (FK) | |
| start_date | DATE | |
| end_date | DATE | |
| reason | TEXT | |

---

## 19. Thông báo

`notification_templates`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| event_code | VARCHAR | VD: `contract.expiring` — không dùng ENUM vì danh sách sự kiện mở rộng liên tục |
| channel | ENUM(`notification_channel_enum`) | |
| subject_template | VARCHAR | |
| body_template | TEXT | |

`notifications`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| user_id | UUID (FK users) | |
| template_id | UUID (FK, nullable) | |
| channel | ENUM(`notification_channel_enum`) | |
| title | VARCHAR | |
| content | TEXT | |
| related_entity_type | VARCHAR | |
| related_entity_id | UUID | |
| is_read | BOOLEAN | |
| sent_at | DATETIME | |
| read_at | DATETIME (nullable) | |

`reminders`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| event_code | VARCHAR | |
| related_entity_type | VARCHAR | |
| related_entity_id | UUID | |
| scheduled_at | DATETIME | |
| status | ENUM(`reminder_status`) | |

---

## 20. Báo cáo & Phân tích

`report_exports`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| report_type | VARCHAR | |
| requested_by_employee_id | UUID (FK) | |
| filter_params | JSON | |
| file_url | VARCHAR | |
| generated_at | DATETIME | |

`dashboard_configs`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| owner_employee_id | UUID (FK, nullable) | Null = dashboard toàn công ty |
| widgets_config | JSON | |

`manager_effectiveness_scores` *(Đánh giá hiệu quả quản lý - gộp từ Module 40)*
| Trường | Kiểu | Ghi chú |
|---|---|---|
| manager_employee_id | UUID (FK) | |
| period | VARCHAR | VD "2026-Q3" |
| overall_score | DECIMAL | |
| breakdown | JSONB | Điểm thành phần: turnover, response_time, one_on_one_frequency, team_performance |

`manager_effectiveness_configs` *(Cấu hình trọng số chỉ số - gộp từ Module 40)*
| Trường | Kiểu | Ghi chú |
|---|---|---|
| metric_key | VARCHAR | VD "turnover_rate" |
| weight | DECIMAL | Trọng số cấu hình được (admin) |

---

## 21. Quản trị Hệ thống

`users`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK, nullable) | Null nếu tài khoản hệ thống/ngoài |
| username | VARCHAR (unique) | |
| email | VARCHAR (unique) | |
| password_hash | VARCHAR | |
| status | ENUM(`user_status_enum`) | |
| last_login_at | DATETIME (nullable) | |

`roles`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| code | VARCHAR | |
| company_id | UUID (FK, nullable) | Null = role hệ thống dùng chung |
| is_system_role | BOOLEAN | |

`permissions`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| code | VARCHAR (unique) | VD: `leave.approve`, `payroll.process` |
| module | VARCHAR | |
| action | VARCHAR | |
| description | TEXT | |

`role_permissions`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| role_id | UUID (FK) | |
| permission_id | UUID (FK) | |
| data_scope | ENUM(`data_scope_enum`) | |

`user_roles`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| user_id | UUID (FK) | |
| role_id | UUID (FK) | |
| company_id | UUID (FK, nullable) | |

`audit_logs`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| user_id | UUID (FK) | |
| action | VARCHAR | |
| entity_type | VARCHAR | |
| entity_id | UUID | |
| old_value | JSON (nullable) | |
| new_value | JSON (nullable) | |
| ip_address | VARCHAR | |

`system_settings`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| setting_key | VARCHAR (unique) | |
| setting_value | TEXT | |
| description | TEXT | |

`integrations`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| name | VARCHAR | |
| integration_type | ENUM(`integration_type_enum`) | |
| config | JSON | |
| status | ENUM(`active_status_enum`) | |
| last_synced_at | DATETIME (nullable) | |

`data_import_jobs`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| entity_type | VARCHAR | |
| file_url | VARCHAR | |
| imported_by_employee_id | UUID (FK) | |
| status | ENUM(`import_job_status`) | |
| result_summary | TEXT | |

---

## 22. AI Features

> Nguồn chi tiết đầy đủ (validate, business rule, luồng xử lý) xem tại `HRM-AI-Features-Business-Rules.md`. Mục này chỉ liệt kê schema. Các trường AI bổ sung vào bảng của module khác (`job_positions`, `candidates`, `applications`, `employee_documents`) đã trình bày ở Module 02/04 tương ứng, đánh dấu **[AI]**.

`ai_interaction_logs`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| feature | ENUM(`ai_feature_enum`) | |
| user_id | UUID (FK, nullable) | Null nếu hệ thống tự chạy (batch) |
| input_summary | TEXT | Tóm tắt đầu vào, không lưu PII chi tiết |
| output_summary | TEXT | |
| model_name | VARCHAR | |
| latency_ms | INT | |
| token_usage | INT (nullable) | Tổng token input+output, theo dõi chi phí |
| status | ENUM(`ai_call_status`) | |
| error_code | VARCHAR (nullable) | |

`ai_conversations`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| user_id | UUID (FK) | |
| title | VARCHAR | Tự sinh từ câu hỏi đầu tiên |
| status | ENUM(`conversation_status`) | |

`ai_messages`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| conversation_id | UUID (FK) | |
| role | ENUM(`message_role_enum`) | |
| content | TEXT | |
| response_type | ENUM(`ai_response_type`) | Nhãn kết quả pipeline: out_of_scope/function_calling/rag_policy/fallback_no_data |
| function_calls | JSON (nullable) | Danh sách function đã gọi + tham số |
| source_documents | JSON (nullable) | Tài liệu đã trích dẫn (nếu response_type = rag_policy) |
| token_usage | INT (nullable) | |
| feedback | ENUM(`message_feedback`, nullable) | |

`ai_intent_logs`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| message_id | UUID (FK) | |
| classified_intent | ENUM(`chat_intent_enum`) | |
| confidence_score | DECIMAL | Phục vụ rà soát/tinh chỉnh classifier |

`knowledge_base_documents`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| title | VARCHAR | |
| category | ENUM(`kb_category_enum`) | |
| file_url | VARCHAR | |
| version | VARCHAR | Phát hiện tài liệu lỗi thời |
| status | ENUM(`kb_status_enum`) | |

`knowledge_base_chunks`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| document_id | UUID (FK) | |
| content | TEXT | Đoạn văn bản đã cắt nhỏ |
| embedding | VECTOR | Dùng `pgvector` cho semantic search |
| chunk_order | INT | |

`ai_unanswered_questions`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| message_id | UUID (FK) | |
| question_text | TEXT | |
| status | ENUM(`unanswered_status`) | HR rà soát định kỳ để bổ sung Knowledge Base |

`ai_screening_logs`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| application_id | UUID (FK) | |
| redacted_input | JSON | Dữ liệu đã loại PII gửi cho AI |
| ai_output | JSON | Kết quả đầy đủ AI trả về |
| triggered_by_employee_id | UUID (FK, nullable) | Null nếu hệ thống tự chạy khi có application mới |
| model_name | VARCHAR | |

`ai_bulk_screening_jobs`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| job_position_id | UUID (FK) | |
| requested_by_employee_id | UUID (FK) | |
| total_applications | INT | |
| succeeded_count | INT | |
| failed_count | INT | |
| status | ENUM(`bulk_job_status`) | |
| failed_items | JSON (nullable) | Danh sách `{application_id, error_code}` các hồ sơ lỗi |

---

## 23. Lưu kho & Hủy hồ sơ (Digital Records Repository)

> Nguồn nghiệp vụ chi tiết: `HRM-Mo-Ta-Nghiep-Vu-Chi-Tiet.md` Module 22 (đánh số 22 ở file nghiệp vụ vì AI Features chưa được liệt kê ở đó — 2 file lệch số thứ tự module ở phần mở rộng, không lệch nội dung).

`record_retention_policies`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| document_type | ENUM(`employee_document_type`) | Tái dùng enum đã có ở Module 04 |
| retention_months | INT | Thời hạn lưu tối thiểu, VD hợp đồng lao động 36 tháng sau khi chấm dứt |
| legal_basis | TEXT | Căn cứ pháp lý, VD "Bộ luật Lao động 2019" |

`record_metadata_field_configs`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| document_type | ENUM(`employee_document_type`) | |
| field_key | VARCHAR | |
| field_label | VARCHAR | |
| field_type | ENUM(`metadata_field_type_enum`) | text / number / date / boolean |
| is_required | BOOLEAN | |

`record_archive_submissions`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_document_id | UUID (FK `employee_documents`) | |
| submitted_by_employee_id | UUID (FK) | |
| submitted_at | DATETIME | |
| metadata_values | JSONB (nullable) | Giá trị các trường đã cấu hình ở `record_metadata_field_configs` |
| status | ENUM(`approval_status_enum`) | Xử lý qua Workflow (Module 18), `request_type = records_archive_submission` |
| reviewed_by_employee_id | UUID (FK, nullable) | |
| reviewed_at | DATETIME (nullable) | |
| retention_until | DATE (nullable) | Tự tính khi duyệt = `reviewed_at` + `record_retention_policies.retention_months` tương ứng |

`record_disposal_proposals`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_document_id | UUID (FK `employee_documents`) | |
| reason | ENUM(`disposal_reason_enum`) | retention_expired / duplicate / other |
| proposed_by_employee_id | UUID (FK) | |
| proposed_at | DATETIME | |
| status | ENUM(`disposal_proposal_status_enum`) | pending_committee_review / approved / rejected |
| disposed_at | DATETIME (nullable) | |

`record_disposal_committee_reviews`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| disposal_proposal_id | UUID (FK) | |
| committee_member_employee_id | UUID (FK) | |
| decision | ENUM(`committee_decision_enum`) | approve / reject |
| comment | TEXT | |
| reviewed_at | DATETIME | |

`employee_documents` — bổ sung trường (ngoài các trường OCR **[AI]** đã có ở Module 04)
| Trường | Kiểu | Ghi chú |
|---|---|---|
| archive_status | ENUM(`archive_status_enum`) | not_submitted / pending_review / archived / pending_disposal / disposed |
| retention_until | DATE (nullable) | Sao chép từ `record_archive_submissions.retention_until` để truy vấn nhanh không cần join |
---

# PHẦN C — MODULE ĐỀ XUẤT BỔ SUNG

> Nguồn nghiệp vụ: `HRM-Mo-Ta-Nghiep-Vu-Chi-Tiet.md` mục 2.1 (Interview Management) và Module 31-42.

## 02 (bổ sung) — Interview Management

`interview_kits`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| job_position_id | UUID (FK, nullable) | Null nếu là kit dùng chung nhiều vị trí |
| name | VARCHAR | |
| interview_type | ENUM(`interview_type_enum`) | Tái dùng enum Module 02, mở rộng thêm giá trị `technical`, `culture_fit`, `final_round` nếu chưa có |

`interview_kit_questions`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| interview_kit_id | UUID (FK) | |
| question_text | TEXT | |
| scoring_criteria | TEXT | Mô tả từng mức điểm 1-5 |
| display_order | INT | |

`interview_panel_members`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| interview_id | UUID (FK `interviews`) | |
| employee_id | UUID (FK) | Người phỏng vấn |
| panel_role | ENUM(`panel_role_enum`) | |

`interview_evaluation_scores` — chuẩn hóa điểm theo từng câu hỏi/panel member (thay cho nhận xét tự do)
| Trường | Kiểu | Ghi chú |
|---|---|---|
| interview_evaluation_id | UUID (FK `interview_evaluations`) | |
| interview_kit_question_id | UUID (FK) | |
| score | INT | Thang 1-5 |

`interviews` — bổ sung trường
| Trường | Kiểu | Ghi chú |
|---|---|---|
| interview_kit_id | UUID (FK, nullable) | |

`interview_evaluations` — bổ sung trường
| Trường | Kiểu | Ghi chú |
|---|---|---|
| panel_member_id | UUID (FK `interview_panel_members`) | |
| recommendation | ENUM(`panel_recommendation_enum`) | |
| notes | TEXT (nullable) | |

---

## 31. Talent Marketplace

`internal_opportunities`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| title | VARCHAR | |
| opportunity_type | ENUM(`opportunity_type_enum`) | |
| department_id | UUID (FK) | |
| required_skills | JSONB (nullable) | Danh sách yêu cầu kỹ năng/tiêu chí chuyên môn (dạng JSON Array) |
| posted_by_employee_id | UUID (FK) | |
| start_date / end_date | DATE (nullable) | Chỉ áp dụng với short_term_project |
| status | ENUM(`job_position_status`) | Tái dùng enum Module 02 |

`internal_opportunity_interests`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| internal_opportunity_id | UUID (FK) | |
| employee_id | UUID (FK) | |
| status | ENUM(`interest_status_enum`) | |
| expressed_at | DATETIME | |

`internal_assignments`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| internal_opportunity_id | UUID (FK) | |
| employee_id | UUID (FK) | |
| status | ENUM(`assignment_status_enum`) | |
| completed_at | DATETIME (nullable) | |

---

## 32. ~~Career Pathing Simulator~~ *[ĐÃ GỘP VÀO MODULE 14]*

> *Lưu ý: Module này đã được hợp nhất hoàn toàn vào Module 14 (Phát triển Nhân sự - Career & Talent). Bảng `career_simulation_saved_paths` và toàn bộ cấu trúc dữ liệu mô phỏng lộ trình sự nghiệp đã được chuyển sang quản lý tập trung tại [Phần B — Mục 14](#14-phát-triển-nhân-sự).*

---

## 33. ~~Skill Graph & Gap Analysis~~ *[ĐÃ LƯỢC BỎ KHỎI SCOPE]*
> *Lưu ý: Module này đã được lược bỏ khỏi phạm vi dự án để tinh gọn hệ thống. Đánh giá năng lực nhân sự đã được phản ánh thực chất và hiệu quả qua kết quả công việc (KPI/Goal) và đánh giá đa chiều tại Module 09 Performance.*

---

## 34. ~~Alumni Network~~ *[ĐÃ LƯỢC BỎ KHỎI SCOPE]*
> *Lưu ý: Module này đã được lược bỏ khỏi phạm vi dự án do bản chất là mạng xã hội cựu nhân viên độc lập (Alumni Portal/Social CRUD), không tác động tới chuỗi vận hành nhân sự và dữ liệu hàng ngày của doanh nghiệp.*

---

## 35. Compliance Radar

`legal_changes`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| title | VARCHAR | |
| legal_document_reference | VARCHAR | VD "Nghị định 145/2020/NĐ-CP" |
| effective_date | DATE | |
| summary | TEXT | |
| priority | ENUM(`legal_change_priority_enum`) | |
| status | ENUM(`legal_change_status_enum`) | |

`legal_change_affected_items`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| legal_change_id | UUID (FK) | |
| affected_item_type | ENUM(`affected_item_type_enum`) | |
| affected_item_id | UUID | FK động tới `contract_types`/`system_settings` (policy)/`salary_components` tùy loại |
| resolution_deadline | DATE | |
| status | ENUM(`approval_status_enum`) | Tái dùng enum chung, theo dõi tiến độ xử lý |

---

## 36. Earned Wage Access

`wage_access_requests`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| employee_id | UUID (FK) | |
| requested_amount | DECIMAL | |
| available_balance_snapshot | DECIMAL | Số dư khả dụng tại thời điểm yêu cầu (snapshot pháp lý — chốt cứng) |
| status | ENUM(`wage_access_status_enum`) | |
| disbursed_at | DATETIME (nullable) | |
| deducted_payroll_period_id | UUID (FK `payroll_periods`, nullable) | Kỳ lương khấu trừ khoản đã ứng |

> Số dư khả dụng (`Earned Wage Balance`) không lưu bảng riêng — tính động từ `attendance_records` (Module 06) và `employee_salary_history` (Module 08) trừ tổng `wage_access_requests` đã disburse trong kỳ.

---

## 37. ~~Payroll Anomaly Detection~~ *[ĐÃ GỘP VÀO MODULE 08]*

> *Lưu ý: Module này đã được hợp nhất hoàn toàn vào Module 08 (Tiền lương - Payroll). Bảng `payroll_anomalies` và các quy tắc quét rủi ro chi trả đã được chuyển sang quản lý tập trung tại [Phần B — Mục 08](#08-tiền-lương).*

---

## 38. ~~Time-off Donation~~ *[ĐÃ LƯỢC BỎ KHỎI SCOPE]*
> *Lưu ý: Module này đã được lược bỏ khỏi phạm vi dự án do không phù hợp quy định Điều 113-114 Bộ luật Lao động 2019 và gây xung đột quyết toán phép tồn khi thôi việc.*

---

## 39. AI Meeting/1-1 Assistant

`one_on_ones`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| manager_employee_id | UUID (FK) | |
| employee_id | UUID (FK) | |
| scheduled_at | DATETIME | |
| recording_consent | BOOLEAN | Bắt buộc true mới cho phép ghi âm |
| recording_url | VARCHAR (nullable) | |
| ai_summary | TEXT (nullable) | |
| status | ENUM(`one_on_one_status_enum`) | |

`one_on_one_action_items`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| one_on_one_id | UUID (FK) | |
| description | TEXT | |
| assignee_employee_id | UUID (FK) | |
| due_date | DATE (nullable) | |
| status | ENUM(`action_item_status_enum`) | |

---

## 40. ~~Manager Effectiveness Score~~ *[ĐÃ GỘP VÀO MODULE 20]*

> *Lưu ý: Module này đã được hợp nhất hoàn toàn vào Module 20 (Báo cáo & Phân tích - Reports & Analytics). Các bảng `manager_effectiveness_scores` và `manager_effectiveness_configs` đã được chuyển sang quản lý tập trung tại [Phần B — Mục 20](#20-báo-cáo--phân-tích).*

---

## 41. ~~What-if Org Simulation~~ *[ĐÃ GỘP VÀO MODULE 01]*

> *Lưu ý: Module này đã được hợp nhất hoàn toàn vào Module 01 (Cơ cấu Tổ chức - Organization). Các bảng `org_simulation_scenarios` và `org_simulation_changes` đã được chuyển sang quản lý tập trung tại [Phần B — Mục 01](#01-quản-lý-tổ-chức).*

---

## 42. Seating Chart / Workplace Management

`workplace_sites`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| branch_id | UUID (FK `branches`) | |
| name | VARCHAR | |
| address | VARCHAR (nullable) | |

`workplace_floors`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workplace_site_id | UUID (FK) | |
| floor_number | VARCHAR | VD "8", "Tầng trệt" |

`workplace_zones`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workplace_floor_id | UUID (FK) | |
| name | VARCHAR | |
| organizational_unit_id | UUID (FK, nullable) | Dùng để tô màu theo phòng ban trên sơ đồ |

`workplace_rows`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workplace_zone_id | UUID (FK) | |
| name | VARCHAR | VD "R02" |

`workplace_seats`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workplace_row_id | UUID (FK) | |
| seat_code | VARCHAR | Tự sinh theo pattern `{Site}-{Floor}-{Zone}-{Row}-{Seat}` |
| seat_type | ENUM(`seat_type_enum`) | |
| status | ENUM(`seat_status_enum`) | |
| amenities | JSONB (nullable) | Danh sách tag: 2 màn hình, docking station... |
| assigned_employee_id | UUID (FK, nullable) | Chỉ set khi `seat_type = dedicated` |
| position_x / position_y | DECIMAL | Tọa độ trên canvas sơ đồ |

`workplace_seat_linked_assets`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workplace_seat_id | UUID (FK) | |
| asset_id | UUID (FK `assets`, Module 13) | |

`workplace_non_desk_objects`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workplace_zone_id | UUID (FK) | |
| object_type | ENUM(`non_desk_object_type_enum`) | |
| name | VARCHAR (nullable) | VD tên phòng họp |
| position_x / position_y | DECIMAL | |
| meeting_room_capacity | INT (nullable) | Chỉ áp dụng nếu object_type = meeting_room |

`workplace_hotdesk_bookings`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workplace_seat_id | UUID (FK) | |
| employee_id | UUID (FK) | |
| booking_date | DATE | |
| status | ENUM(`hotdesk_booking_status_enum`) | |
| checked_in_at | DATETIME (nullable) | |

`workplace_floor_plans`
| Trường | Kiểu | Ghi chú |
|---|---|---|
| workplace_floor_id | UUID (FK) | |
| background_image_url | VARCHAR | Ảnh/PDF mặt bằng nền |
| version | INT | |
| is_published | BOOLEAN | |