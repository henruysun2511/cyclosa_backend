# CYCLOSA — Nghiệp Vụ Tuyển Dụng & Phỏng Vấn (Recruitment & Interview Management)

Tài liệu này mô tả chi tiết toàn bộ nghiệp vụ, kiến trúc kỹ thuật, luồng tích hợp liên module và giải thích chi tiết vai trò của từng thành phần trong module `com.cyclosa.recruitment` thuộc hệ thống CYCLOSA HRM theo tài liệu phân tích nghiệp vụ **Module 02 — Tuyển dụng** và **2.1 Interview Management**.

---

## 1. Tổng Quan Nghiệp Vụ (Business Overview)

Module Tuyển dụng quản lý toàn bộ vòng đời tiếp nhận nhân tài của doanh nghiệp, từ khi phát sinh nhu cầu nhân sự cho đến khi chuyển đổi ứng viên trúng tuyển thành nhân viên chính thức trong tổ chức:

```
[1. Đề Xuất Tuyển Dụng] ──(Workflow Duyệt)──► [2. Vị Trí & Đăng Tin]
                                                      │
                                                      ▼
[4. Phỏng Vấn Hội Đồng] ◄──(ATS Pipeline)─── [3. Ứng Viên & Hồ Sơ]
    - Interview Kit & Rubrics                         │
    - Blind Grading                                   ▼
    - Divergence Alert                         [Talent Pool]
    - Bias Awareness
          │
          ▼
[5. Offer Mời Việc] ──(Workflow Duyệt)──► [6. Tiếp Nhận (Convert to Employee)]
                                                  │
                                                  ▼
                                      Tự động tạo Employee Profile
                                      (Mã NV, Hợp đồng, Tài khoản)
```

---

## 2. Các Phân Hệ & Quy Tắc Nghiệp Vụ Cốt Lõi

### 2.1. Đề xuất tuyển dụng (Manpower Request) & Ràng buộc Workflow
- **Bối cảnh:** Trưởng bộ phận lập yêu cầu tuyển thêm nhân sự (bổ sung dự án mới hoặc thay thế nhân sự cũ).
- **Quy tắc:**
  - Mỗi đề xuất mang một mã duy nhất định dạng `MPR-YYYY-xxxx`.
  - Đề xuất bắt buộc phải gắn với một luồng phê duyệt đa cấp của `WorkflowEngine` (`ApprovalRequestType.MANPOWER_REQUEST`).
  - Trạng thái khởi tạo là `PENDING_APPROVAL`. Chỉ khi tất cả các cấp trong quy trình phê duyệt đồng ý, sự kiện `WorkflowCompletedEvent` được phát ra, `RecruitmentWorkflowEventListener` bắt sự kiện và tự động chuyển trạng thái đề xuất thành `APPROVED`.
  - Nếu bị từ chối ở bất kỳ bước nào, trạng thái đề xuất chuyển thành `REJECTED`. Người tạo có quyền `CANCEL` nếu đơn đang chờ duyệt.

### 2.2. Vị trí tuyển dụng (Job Position) & Xuất bản tin tuyển dụng (Job Posting)
- **Vị trí tuyển dụng (Job Position):**
  - Được sinh ra từ một `ManpowerRequest` đã được phê duyệt (`APPROVED`), hoặc tạo độc lập theo hạn ngạch nhân sự năm của công ty.
  - Quản lý định biên: số lượng cần tuyển (`targetHires`), số lượng đã tuyển thành công (`hiredCount`), hạn chót tuyển dụng (`targetDate`).
  - Trạng thái vòng đời: `OPEN` -> `PAUSED` -> `CLOSED`. Khi `hiredCount >= targetHires`, vị trí có thể tự động đóng.
- **Tin tuyển dụng đa kênh (Job Posting):**
  - Một vị trí tuyển dụng có thể được xuất bản lên nhiều kênh tuyển dụng khác nhau (`PostingChannel`: `INTERNAL_PORTAL`, `LINKEDIN`, `VIETNAMWORKS`, `TOPCV`, `FACEBOOK`, `CAREER_SITE`, `OTHER`).
  - Quản lý trạng thái đăng bài (`DRAFT`, `PUBLISHED`, `EXPIRED`, `CLOSED`), ngày hết hạn (`expiredAt`) và đường dẫn đăng tuyển ngoài (`externalUrl`).

### 2.3. Quản lý Ứng viên (Candidate) & ATS Pipeline (Application)
- **Ứng viên (Candidate):**
  - Quản lý thông tin cá nhân: Họ tên, email, số điện thoại, link CV (`cvUrl`), portfolio, hồ sơ mạng xã hội, bộ kỹ năng (`skills`).
  - Nguồn ứng viên (`CandidateSource`): `REFERRAL`, `LINKEDIN`, `WEBSITE`, `JOB_FAIR`, `HEADHUNTER`, v.v.
  - Ngăn chặn trùng lặp hồ sơ thông qua `email` và `phone` trong cùng một công ty.
- **Quy trình ứng tuyển (Application ATS Pipeline):**
  - Một ứng viên có thể ứng tuyển vào một hoặc nhiều vị trí. Mỗi lần ứng tuyển tương ứng với một `Application`.
  - Đường ống ứng tuyển trải qua các giai đoạn nghiêm ngặt (`ApplicationStage`):
    1. `APPLIED`: Nộp hồ sơ.
    2. `SCREENING`: Lọc hồ sơ / Sơ vấn CV.
    3. `INTERVIEW`: Phỏng vấn chuyên môn / Hội đồng.
    4. `OFFER_PENDING`: Đang trình duyệt / gửi thư mời nhận việc.
    5. `OFFER_ACCEPTED`: Ứng viên đồng ý nhận việc.
    6. `HIRED`: Tiếp nhận vào làm việc thành công.
    7. `REJECTED`: Loại hồ sơ.
    8. `WITHDRAWN`: Ứng viên chủ động rút lui.

---

### 2.4. Phân Hệ Phỏng Vấn Nâng Cao (Interview Management 2.1)

#### A. Bộ câu hỏi chuẩn hóa (Interview Kit & Question Criteria)
- Mỗi vị trí công việc có thể chuẩn bị sẵn các **Interview Kit** theo từng vòng (`HR`, `TECHNICAL`, `MANAGEMENT`, `CULTURE_FIT`).
- Bộ Kit gồm danh sách các tiêu chí và câu hỏi phỏng vấn (`InterviewQuestion`), mỗi câu hỏi có:
  - Danh mục năng lực (`category`: Kiến thức, Kỹ năng mềm, Tư duy giải quyết vấn đề, Văn hóa).
  - Trọng số đánh giá (`weight` từ 1 - 5).
  - Thang điểm Rubric chuẩn hóa (`rubricLevels`: tiêu chuẩn điểm 1 đến 5 để người phỏng vấn chấm khách quan).

#### B. Chấm điểm độc lập (Blind Grading)
- Phỏng vấn hội đồng gồm nhiều giám khảo (`InterviewPanelMember`).
- **Quy tắc Blind Grading:** Để chống lại hiệu ứng tâm lý "bầy đàn" (Bandwagon effect) hoặc bị chi phối bởi người có chức vụ cao hơn trong phòng phỏng vấn:
  - Mỗi giám khảo chỉ xem được phiếu chấm của chính mình.
  - Giám khảo chỉ được xem nhận xét và điểm của đồng nghiệp **sau khi chính bản thân đã nộp phiếu đánh giá** (`submitted = true`).

#### C. Cảnh báo phân kỳ nhận xét (Divergence Alert)
- Hệ thống tự động tính toán tổng hợp kết quả (`ConsolidatedFeedbackResponse`):
  - Điểm trung bình cộng có trọng số:
    $$\text{Average Score} = \frac{\sum (\text{Score}_i \times \text{Weight}_i)}{\sum \text{Weight}_i}$$
  - Phân tích độ lệch chuẩn hoặc biên độ chênh lệch:
    $$\Delta = \text{Max(Score)} - \text{Min(Score)}$$
- **Kích hoạt Divergence Alert (`divergenceAlert = true`) khi xảy ra 1 trong 2 tình huống:**
  1. **Xung đột quyết định mạnh mẽ:** Có ít nhất 1 giám khảo đánh giá `STRONG_YES` trong khi 1 giám khảo khác đánh giá `STRONG_NO`.
  2. **Chênh lệch điểm số bất thường:** Điểm số chênh lệch $\Delta \ge 2.0$ trên thang điểm 5.
- Khi cờ này bật, hệ thống hiển thị cảnh báo đỏ trên bảng điều khiển để HR tiến hành phiên họp "Debrief" giải trình trước khi đưa ra quyết định cuối cùng.

#### D. Phân tích xu hướng chấm điểm của giám khảo (Bias Awareness)
- Cung cấp API phân tích thống kê lịch sử chấm điểm của từng giám khảo (`/api/v1/interviewers/{id}/scoring-pattern`):
  - Số lượng phỏng vấn đã tham gia, điểm trung bình toàn bộ lịch sử.
  - Tỷ lệ đưa ra khuyến nghị YES vs NO.
  - **Phát hiện thiên kiến:**
    - Cảnh báo "Hawkish / Quá khắt khe": Điểm trung bình lịch sử $< 2.5/5$ hoặc tỷ lệ NO $> 70\%$.
    - Cảnh báo "Dovish / Quá dễ dãi": Điểm trung bình lịch sử $> 4.2/5$ hoặc tỷ lệ YES $> 90\%$.

---

### 2.5. Lập Thư Mời Nhận Việc (Offer) & Quy Trình Duyệt
- Quản lý mức đãi ngộ: Lương cơ bản (`baseSalary`), phụ cấp (`allowance`), ngày gia nhập dự kiến (`joinDate`), ngày hết hạn phản hồi (`expiryDate`).
- **Quy trình:**
  - Lập Offer ở trạng thái `DRAFT`.
  - Gửi duyệt qua Workflow Engine (`ApprovalRequestType.OFFER_APPROVAL`).
  - Khi được duyệt, Offer chuyển sang `SENT` để gửi cho ứng viên.
  - Ứng viên phản hồi: `ACCEPTED` (chấp thuận) hoặc `DECLINED` (từ chối).

---

### 2.6. Tiếp Nhận Nhân Sự (Convert to Employee)
- **Tích hợp sâu với Core HRM (`EmployeeService.createEmployee`):**
  - Khi ứng viên ở giai đoạn `OFFER_ACCEPTED`, HR thực hiện hành động **"Convert to Employee"** qua API:
    `POST /api/v1/hiring/{application_id}/convert-to-employee`.
  - Hệ thống tự động:
    1. Lấy thông tin họ tên, email, số điện thoại từ hồ sơ `Candidate`.
    2. Lấy phòng ban (`departmentId`), chức danh (`positionId`) từ `JobPosition`.
    3. Kiểm tra số CCCD/CMND (`nationalIdNumber`), ngày bắt đầu làm việc (`hireDate`).
    4. Gọi `employeeService.createEmployee(...)` để tạo mới hồ sơ nhân sự chính thức trong hệ thống Core HRM.
    5. Cập nhật trạng thái `Application` sang `HIRED`.
    6. Cập nhật trạng thái `Candidate` sang `HIRED`.
    7. Tăng số lượng đã tuyển `hiredCount = hiredCount + 1` của `JobPosition`.
    8. Ghi nhận `AuditLog` lưu lại vết chuyển đổi nhân sự.

---

### 2.7. Ngân Hàng Hồ Sơ Ứng Viên (Talent Pool)
- Dành cho những ứng viên tiềm năng chưa trúng tuyển vị trí hiện tại nhưng phù hợp cho các cơ hội tương lai.
- Lưu trữ hồ sơ kèm từ khóa kỹ năng (`tag`), nguồn giới thiệu và ghi chú đánh giá (`notes`).
- Cho phép bộ phận tuyển dụng tìm kiếm lại ứng viên theo từ khóa và kỹ năng khi có vị trí mở mới.

---


### 2.8. Thị trường Nhân tài Nội bộ (Internal Talent Marketplace — Gộp từ Module 31)
- **Mục tiêu:** Thúc đẩy dịch chuyển nhân lực nội bộ, trao quyền cho nhân viên chủ động tìm kiếm các dự án ngắn hạn (Project/Gig Assignment) hoặc vị trí mở (Full-time Open Position) trong công ty mà không bị rào cản bởi cấp quản lý hiện tại.
- **Quy trình hoạt động:**
  1. **Đăng cơ hội nội bộ (Internal Opportunity):** HR hoặc Project Manager tạo cơ hội mới với các thông tin: tiêu đề, phòng ban, người phụ trách (`managerId`), mô tả, kỹ năng yêu cầu (`requiredSkills`), tỷ lệ cam kết thời gian (`commitmentPercentage`), thời gian dự án.
  2. **Bày tỏ quan tâm (Express Interest):** Nhân viên chủ động bấm ứng tuyển và gửi kèm ghi chú kinh nghiệm. Hệ thống tự động bắn Notification In-App báo cho Project Manager và gửi thông báo thông tin (FYI) cho Quản lý trực tiếp của nhân viên.
  3. **Đánh giá & Duyệt:** Project Manager xem danh sách hồ sơ ứng viên nội bộ và chuyển trạng thái (`SHORTLISTED`, `ACCEPTED`, `REJECTED`).
  4. **Phân công nhiệm vụ (Internal Assignment):** Khi chấp nhận ứng viên, hệ thống tạo bản ghi `InternalAssignment`. Số lượng tuyển (`currentHires`) tự động tăng; khi đạt chỉ tiêu (`targetHires`), cơ hội chuyển sang trạng thái `FILLED`.
  5. **Hoàn thành & Đánh giá hiệu suất:** Khi kết thúc dự án ngắn hạn, Manager chấm điểm đánh giá (`performanceRating` trên thang điểm 5.0) và ghi nhận xét đóng góp (`evaluationNote`).
  6. **Báo cáo thống kê:** Cung cấp số liệu tổng quan về thị trường nội bộ (tổng cơ hội mở, số lượt quan tâm, số nhiệm vụ hoàn thành xuất sắc).

## 3. Kiến Trúc Kỹ Thuật & Cấu Trúc Mã Nguồn

Toàn bộ mã nguồn Module Tuyển dụng được đóng gói hoàn chỉnh trong package `com.cyclosa.recruitment`:

```
com.cyclosa.recruitment
├── controller/                      # 10 REST Controllers
│   ├── ApplicationController.java
│   ├── CandidateController.java
│   ├── HiringController.java
│   ├── InterviewController.java
│   ├── InterviewKitController.java
│   ├── JobPositionController.java
│   ├── JobPostingController.java
│   ├── ManpowerRequestController.java
│   ├── OfferController.java
│   └── TalentPoolController.java
├── dto/
│   ├── request/                    # 13 DTO Request & Filters
│   │   ├── ApplicationFilter.java
│   │   ├── CandidateFilter.java
│   │   ├── ConvertToEmployeeRequest.java
│   │   ├── CreateApplicationRequest.java
│   │   ├── CreateCandidateRequest.java
│   │   ├── CreateInterviewKitRequest.java
│   │   ├── CreateInterviewQuestionRequest.java
│   │   ├── CreateInterviewRequest.java
│   │   ├── CreateJobPositionRequest.java
│   │   ├── CreateJobPostingRequest.java
│   │   ├── CreateManpowerRequest.java
│   │   ├── CreateOfferRequest.java
│   │   ├── SubmitEvaluationRequest.java
│   │   ├── TalentPoolFilter.java
│   │   └── UpdateInterviewKitRequest.java
│   └── response/                   # 14 DTO Response
│       ├── ApplicationResponse.java
│       ├── CandidateResponse.java
│       ├── ConsolidatedFeedbackResponse.java
│       ├── ConvertToEmployeeResponse.java
│       ├── InterviewEvaluationResponse.java
│       ├── InterviewKitResponse.java
│       ├── InterviewQuestionResponse.java
│       ├── InterviewResponse.java
│       ├── InterviewerScoringPatternResponse.java
│       ├── JobPositionResponse.java
│       ├── JobPostingResponse.java
│       ├── ManpowerRequestResponse.java
│       ├── OfferResponse.java
│       └── TalentPoolResponse.java
├── entity/                         # 12 JPA Entities (Kế thừa BaseEntity)
│   ├── Application.java
│   ├── Candidate.java
│   ├── Interview.java
│   ├── InterviewEvaluation.java
│   ├── InterviewKit.java
│   ├── InterviewPanelMember.java
│   ├── InterviewQuestion.java
│   ├── JobPosition.java
│   ├── JobPosting.java
│   ├── ManpowerRequest.java
│   ├── Offer.java
│   └── TalentPool.java
├── enums/                          # 11 Enums định nghĩa trạng thái
│   ├── ApplicationStage.java
│   ├── CandidateSource.java
│   ├── CandidateStatus.java
│   ├── InterviewRecommendation.java
│   ├── InterviewStatus.java
│   ├── InterviewType.java
│   ├── JobPositionStatus.java
│   ├── ManpowerRequestStatus.java
│   ├── OfferStatus.java
│   ├── PostingChannel.java
│   └── PostingStatus.java
├── exception/
│   └── RecruitmentErrorCode.java  # 20 mã lỗi chuẩn hóa (Dải mã 5001 - 5081)
├── listener/
│   └── RecruitmentWorkflowEventListener.java # Lắng nghe WorkflowCompletedEvent
├── mapper/                         # 6 MapStruct Mappers
│   ├── ApplicationMapper.java
│   ├── CandidateMapper.java
│   ├── JobPositionMapper.java
│   ├── JobPostingMapper.java
│   ├── ManpowerRequestMapper.java
│   └── OfferMapper.java
├── repository/                     # 12 Spring Data JPA Repositories
│   ├── ApplicationRepository.java
│   ├── CandidateRepository.java
│   ├── InterviewEvaluationRepository.java
│   ├── InterviewKitRepository.java
│   ├── InterviewPanelMemberRepository.java
│   ├── InterviewQuestionRepository.java
│   ├── InterviewRepository.java
│   ├── JobPositionRepository.java
│   ├── JobPostingRepository.java
│   ├── ManpowerRequestRepository.java
│   ├── OfferRepository.java
│   └── TalentPoolRepository.java
└── service/                        # 10 Services & Implementations
    ├── ApplicationService.java & ApplicationServiceImpl.java
    ├── CandidateService.java & CandidateServiceImpl.java
    ├── HiringService.java & HiringServiceImpl.java
    ├── InterviewKitService.java & InterviewKitServiceImpl.java
    ├── InterviewService.java & InterviewServiceImpl.java
    ├── JobPositionService.java & JobPositionServiceImpl.java
    ├── JobPostingService.java & JobPostingServiceImpl.java
    ├── ManpowerRequestService.java & ManpowerRequestServiceImpl.java
    ├── OfferService.java & OfferServiceImpl.java
    └── TalentPoolService.java & TalentPoolServiceImpl.java
```

---

## 4. Danh Sách 32 REST API Endpoints Chi Tiết

| Nhóm chức năng | Method | Endpoint URI | Mã Quyền | Chức năng nghiệp vụ |
| :--- | :---: | :--- | :--- | :--- |
| **Manpower Request** | `GET` | `/api/v1/manpower-requests` | `recruitment.view` | Danh sách đề xuất tuyển dụng theo DataScope |
| | `POST` | `/api/v1/manpower-requests` | `recruitment.manage` | Lập đề xuất tuyển dụng mới & gửi duyệt Workflow |
| | `GET` | `/api/v1/manpower-requests/{id}` | `recruitment.view` | Chi tiết đề xuất kèm trạng thái quy trình phê duyệt |
| | `PUT` | `/api/v1/manpower-requests/{id}/cancel` | `recruitment.manage` | Người tạo hủy đề xuất khi đang chờ duyệt |
| **Job Positions** | `GET` | `/api/v1/job-positions` | `recruitment.view` | Tra cứu danh sách vị trí công việc cần tuyển |
| | `POST` | `/api/v1/job-positions` | `recruitment.manage` | Tạo vị trí tuyển dụng từ đề xuất đã duyệt |
| | `PUT` | `/api/v1/job-positions/{id}/status` | `recruitment.manage` | Đóng/mở/tạm dừng vị trí tuyển dụng |
| **Job Postings** | `GET` | `/api/v1/job-postings` | `recruitment.view` | Danh sách tin đăng tuyển đa kênh |
| | `POST` | `/api/v1/job-postings` | `recruitment.manage` | Xuất bản tin tuyển dụng lên kênh ngoài |
| | `PUT` | `/api/v1/job-postings/{id}/status` | `recruitment.manage` | Cập nhật trạng thái tin tuyển dụng |
| **Candidates** | `GET` | `/api/v1/candidates` | `recruitment.view` | Danh sách hồ sơ ứng viên (lọc theo từ khóa, kỹ năng) |
| | `POST` | `/api/v1/candidates` | `recruitment.manage` | Tiếp nhận và tạo mới hồ sơ ứng viên |
| | `GET` | `/api/v1/candidates/{id}` | `recruitment.view` | Xem chi tiết hồ sơ ứng viên |
| | `PUT` | `/api/v1/candidates/{id}` | `recruitment.manage` | Cập nhật thông tin hồ sơ ứng viên |
| **ATS Applications** | `GET` | `/api/v1/applications` | `recruitment.view` | Theo dõi quy trình ứng tuyển trên ATS Pipeline |
| | `POST` | `/api/v1/applications` | `recruitment.manage` | Ứng tuyển ứng viên vào vị trí công việc |
| | `PUT` | `/api/v1/applications/{id}/stage` | `recruitment.manage` | Chuyển đổi giai đoạn ứng tuyển (Screening -> Hired) |
| **Interview Kits** | `GET` | `/api/v1/interview-kits` | `recruitment.view` | Danh sách bộ câu hỏi phỏng vấn chuẩn hóa |
| | `POST` | `/api/v1/interview-kits` | `recruitment.manage` | Tạo mới bộ phỏng vấn và tiêu chí chấm điểm |
| | `GET` | `/api/v1/interview-kits/{id}` | `recruitment.view` | Chi tiết bộ phỏng vấn kèm danh sách tiêu chí |
| | `POST` | `/api/v1/interview-kits/{id}/questions` | `recruitment.manage` | Thêm câu hỏi & thang điểm Rubric vào Kit |
| **Interviews** | `GET` | `/api/v1/interviews` | `recruitment.view` | Danh sách lịch phỏng vấn theo vị trí/ứng viên |
| | `POST` | `/api/v1/interviews` | `recruitment.manage` | Lên lịch phỏng vấn, chỉ định hội đồng giám khảo |
| | `POST` | `/api/v1/interviews/{id}/evaluations` | `recruitment.evaluate` | Giám khảo nộp phiếu chấm điểm (Blind Grading) |
| | `GET` | `/api/v1/interviews/{id}/consolidated-feedback` | `recruitment.view` | Tổng hợp nhận xét & cảnh báo độ lệch (Divergence) |
| | `GET` | `/api/v1/interviewers/{id}/scoring-pattern` | `recruitment.view` | Phân tích xu hướng chấm điểm của giám khảo (Bias) |
| **Offers** | `GET` | `/api/v1/offers` | `recruitment.view` | Danh sách thư mời nhận việc |
| | `POST` | `/api/v1/offers` | `recruitment.manage` | Tạo Offer thư mời việc & trình duyệt Workflow |
| | `PUT` | `/api/v1/offers/{id}/status` | `recruitment.manage` | Cập nhật phản hồi Offer (ACCEPTED, DECLINED) |
| **Hiring Onboard** | `POST` | `/api/v1/hiring/{application_id}/convert-to-employee` | `recruitment.onboard` | Tiếp nhận ứng viên trúng tuyển chuyển sang nhân viên |
| **Talent Pool** | `GET` | `/api/v1/talent-pool` | `recruitment.view` | Tra cứu kho dữ liệu ứng viên tiềm năng |
| | `POST` | `/api/v1/talent-pool` | `recruitment.manage` | Lưu ứng viên tiềm năng vào Talent Pool |
| **Talent Marketplace** | `GET` | `/api/v1/internal-opportunities` | `recruitment.view` | Danh sách cơ hội nội bộ (Dự án ngắn hạn / Vị trí mở) |
| | `GET` | `/api/v1/internal-opportunities/{id}` | `recruitment.view` | Chi tiết cơ hội nội bộ |
| | `POST` | `/api/v1/internal-opportunities` | `recruitment.manage` | Đăng cơ hội dự án ngắn hạn & vị trí mở nội bộ |
| | `PUT` | `/api/v1/internal-opportunities/{id}` | `recruitment.manage` | Cập nhật thông tin cơ hội nội bộ |
| | `PUT` | `/api/v1/internal-opportunities/{id}/status` | `recruitment.manage` | Đổi trạng thái cơ hội (OPEN, IN_PROGRESS, FILLED, CANCELLED) |
| | `POST` | `/api/v1/internal-opportunities/{id}/express-interest` | `Authenticated` | Nhân viên tự ứng tuyển / bày tỏ quan tâm cơ hội |
| | `GET` | `/api/v1/internal-opportunities/{id}/applications` | `recruitment.view` | Xem danh sách ứng viên nội bộ ứng tuyển cơ hội |
| | `GET` | `/api/v1/internal-opportunities/my-applications` | `Authenticated` | Danh sách cơ hội cá nhân nhân viên đã ứng tuyển |
| | `PUT` | `/api/v1/internal-applications/{id}/review` | `recruitment.manage` | Duyệt / đánh giá đơn ứng tuyển nội bộ (SHORTLISTED, ACCEPTED...) |
| | `GET` | `/api/v1/internal-opportunities/recommended` | `Authenticated` | Gợi ý cơ hội nội bộ phù hợp cho nhân sự |
| | `POST` | `/api/v1/internal-assignments` | `recruitment.manage` | Tạo phân công nhiệm vụ nội bộ khi trúng tuyển (Assignment) |
| | `GET` | `/api/v1/internal-assignments` | `recruitment.view` | Tra cứu danh sách phân công nhiệm vụ nội bộ |
| | `PUT` | `/api/v1/internal-assignments/{id}/complete` | `recruitment.manage` | Hoàn thành & chấm điểm đánh giá nhiệm vụ nội bộ |
| | `GET` | `/api/v1/internal-marketplace/stats` | `recruitment.view` | Báo cáo thống kê thị trường nhân tài nội bộ (Marketplace Stats) |

---

## 5. Bảng Mã Lỗi Chuẩn Hóa (`RecruitmentErrorCode`)

| Mã Lỗi | HTTP Status | Thông Điệp Lỗi | Ý Nghĩa / Nguyên Nhân |
| :---: | :---: | :--- | :--- |
| `5001` | 404 | Đề xuất tuyển dụng không tồn tại | ID đề xuất không hợp lệ |
| `5002` | 400 | Đề xuất tuyển dụng không ở trạng thái hợp lệ để thao tác | Thao tác trên đề xuất đã hoàn tất hoặc bị hủy |
| `5010` | 404 | Vị trí tuyển dụng không tồn tại | ID vị trí tuyển dụng không tìm thấy trong công ty |
| `5011` | 400 | Vị trí tuyển dụng đã đóng hoặc đạt đủ số lượng tuyển | Không thể nộp thêm hồ sơ hoặc đăng tuyển mới |
| `5020` | 404 | Tin tuyển dụng không tồn tại | ID tin đăng tuyển không hợp lệ |
| `5030` | 404 | Hồ sơ ứng viên không tồn tại | ID ứng viên không tìm thấy |
| `5031` | 400 | Email ứng viên đã tồn tại trong hệ thống | Trùng lặp thông tin ứng viên trong công ty |
| `5040` | 404 | Hồ sơ ứng tuyển không tồn tại | ID đơn ứng tuyển không tìm thấy |
| `5041` | 400 | Ứng viên đã ứng tuyển vào vị trí này trước đó | Không cho phép nộp trùng đơn vào cùng vị trí |
| `5050` | 404 | Bộ câu hỏi phỏng vấn (Interview Kit) không tồn tại | ID Kit không hợp lệ |
| `5051` | 404 | Câu hỏi phỏng vấn không tồn tại | ID câu hỏi tiêu chí không hợp lệ |
| `5060` | 404 | Lịch phỏng vấn không tồn tại | ID buổi phỏng vấn không tìm thấy |
| `5061` | 400 | Lịch phỏng vấn đã hoàn tất hoặc bị hủy | Không thể nộp phiếu chấm điểm sau khi đã đóng |
| `5062` | 403 | Bạn không thuộc hội đồng phỏng vấn này | Người nộp không có tên trong danh sách ban giám khảo |
| `5063` | 400 | Bạn đã nộp đánh giá cho buổi phỏng vấn này rồi | Mỗi giám khảo chỉ nộp đánh giá một lần |
| `5070` | 404 | Thư mời việc (Offer) không tồn tại | ID thư mời không tìm thấy |
| `5071` | 400 | Trạng thái Offer không hợp lệ để thao tác | Thao tác trên Offer đã đóng hoặc hết hạn |
| `5080` | 400 | Hồ sơ ứng tuyển chưa được chấp nhận Offer | Không thể Convert to Employee khi ứng viên chưa accept |
| `5081` | 400 | Ứng viên này đã được tiếp nhận thành nhân viên trước đó | Tránh tạo trùng lặp nhân sự trong Core HRM |
| `5090` | 404 | Không tìm thấy cơ hội nội bộ | ID cơ hội không tồn tại trong công ty |
| `5091` | 400 | Cơ hội nội bộ này hiện không còn mở ứng tuyển | Cơ hội đã kết thúc hoặc bị hủy |
| `5092` | 409 | Bạn đã nộp đơn bày tỏ quan tâm vào cơ hội này rồi | Tránh nộp trùng lặp vào cùng 1 cơ hội |
| `5093` | 404 | Không tìm thấy đơn ứng tuyển nội bộ | ID đơn ứng tuyển không hợp lệ |
| `5094` | 404 | Không tìm thấy nhiệm vụ phân công nội bộ | ID phân công nhiệm vụ không tìm thấy |
| `5095` | 400 | Nhiệm vụ phân công nội bộ đã hoàn thành hoặc kết thúc | Không thể cập nhật nhiệm vụ đã đóng |

---

## 6. Kiểm Thử Tự Động (Automated Testing)

Module đi kèm bộ kiểm thử đơn vị tự động tại `RecruitmentServiceTest.java`:
1. `testCreateManpowerRequest_Success`: Kiểm tra tạo đề xuất tuyển dụng và gán mã sinh tự động dạng `MPR-2026-xxxx`.
2. `testHandleWorkflowCompleted_Approved`: Kiểm tra cập nhật trạng thái đề xuất thành `APPROVED` khi luồng duyệt thành công.
3. `testConsolidatedFeedback_DivergenceAlert`: Kiểm tra tự động phát hiện mâu thuẫn nhận xét (`Divergence Alert`) khi 1 giám khảo chấm `STRONG_YES` và 1 giám khảo chấm `STRONG_NO`.
4. `testConvertToEmployee_Success`: Kiểm tra tích hợp chuyển đổi từ `Application` sang tạo mới hồ sơ nhân sự (`Employee`) qua `employeeService.createEmployee(...)`.

Kết quả chạy kiểm thử:
```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
