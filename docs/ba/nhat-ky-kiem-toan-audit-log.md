# CYCLOSA — Nghiệp Vụ Nhật Ký Kiểm Toán (Audit Log & Audit Trail)

Tài liệu này mô tả chi tiết yêu cầu nghiệp vụ, kiến trúc kỹ thuật và giải thích vai trò của từng thành phần trong module `com.cyclosa.audit` thuộc hệ thống CYCLOSA HRM.

---

## 1. Tổng Quan Nghiệp Vụ (Business Overview)

### 1.1. Mục đích & Tầm quan trọng
Trong hệ thống quản trị nhân sự doanh nghiệp (HRM), việc lưu vết toàn bộ các thao tác dữ liệu nhạy cảm là yêu cầu **bắt buộc** nhằm:
* **Kiểm toán nội bộ & Tuân thủ (Compliance & Audit):** Đáp ứng các tiêu chuẩn kiểm toán doanh nghiệp (ISO 27001, SOX), luật lao động và bảo vệ dữ liệu cá nhân.
* **Truy vết sự cố & Chống gian lận (Fraud Detection):** Phát hiện các hành vi sửa lương, sửa ngày công, tự cấp quyền, xóa hợp đồng hoặc can thiệp trái phép vào kết quả phê duyệt.
* **Minh bạch hóa lịch sử (Audit Trail):** Xem lại toàn bộ dòng lịch sử của một bản ghi nghiệp vụ (ai tạo, ai sửa, ai duyệt, vào thời điểm nào, từ địa chỉ IP nào, giá trị cũ/mới là gì).

### 1.2. Các Nguyên Tắc Nghiệp Vụ Bắt Buộc (Core Business Rules)
1. **Tính Bất Biến (Immutable / Append-Only):**
   * Bảng `audit_logs` **chỉ cho phép `INSERT` và `SELECT`**.
   * **Tuyệt đối cấm `UPDATE` và `DELETE`**. Không một ai (kể cả Super Admin hay Database Administrator) được phép chỉnh sửa hay xóa log kiểm toán thông qua ứng dụng.
2. **Không làm gián đoạn nghiệp vụ chính (Non-blocking & Fault-Tolerant):**
   * Việc ghi nhật ký kiểm toán phải chạy bất đồng bộ (`@Async`) hoặc trong transaction độc lập (`REQUIRES_NEW`).
   * Nếu có lỗi trong quá trình lưu log (ví dụ: lỗi kết nối mạng, vượt quá dung lượng chuỗi JSON), **nghiệp vụ chính của người dùng vẫn phải thành công bình thường**, không được gây lỗi hoặc rollback transaction chính.
3. **Phân quyền truy cập khắt khe (Read-Only RBAC):**
   * Chỉ có vai trò **Super Admin** và **Kiểm toán viên (Auditor / Compliance)** mới có quyền truy vấn màn hình nhật ký kiểm toán (`audit.view`).
   * Người dùng thông thường và các cấp quản lý không được xem log toàn hệ thống.

---

## 2. Kiến Trúc Kỹ Thuật & Luồng Xử Lý

```mermaid
flowchart TD
    subgraph Client [Client / Trình duyệt]
        Req[Gửi Request: Duyệt đơn, Sửa lương, Phân quyền...]
    end

    subgraph CoreBusiness [Nghiệp vụ chính]
        Ctrl[Business Controller]
        Svc[Business Service]
        DB[(PostgreSQL)]
    end

    subgraph AuditModule [Module com.cyclosa.audit]
        AOP[AuditLogAspect / @Auditable]
        Evt[AuditLogEventListener / AuditLogEvent]
        ASvc[AuditLogServiceImpl]
        ARepo[AuditLogRepository]
        ALog[(Bảng audit_logs)]
    end

    Req --> Ctrl
    Ctrl --> Svc
    Svc --> DB

    %% Cách 1: Qua AOP Aspect
    Ctrl -.->|Intercept @Auditable| AOP
    AOP -.->|Async| ASvc

    %% Cách 2: Qua Event
    Svc -.->|publishEvent| Evt
    Evt -.->|Async| ASvc

    %% Cách 3: Gọi trực tiếp
    Svc -.->|auditLogService.log()| ASvc

    ASvc -->|REQUIRES_NEW| ARepo
    ARepo --> ALog
```

### Điểm nhấn kiến trúc:
1. **Batch Enrichment chống N+1 Query:** Khi truy vấn danh sách log trả về cho Client, Service thu thập toàn bộ `userId` xuất hiện trong trang kết quả, sau đó gọi 1 query duy nhất `userService.getUserSummaries(userIds)` để nạp thông tin người thực hiện (`UserSummary: username, fullName, email, avatarUrl`).
2. **Lưu vết chi tiết Trước / Sau (Diff Tracking):** Hỗ trợ lưu trữ dữ liệu dưới dạng JSON ở hai cột `old_value` và `new_value`, cho phép giao diện Frontend vẽ bảng so sánh thay đổi chi tiết từng trường.

---

## 3. Danh Sách & Giải Thích Chi Tiết Các File Trong Module

Toàn bộ mã nguồn module nằm trong package `com.cyclosa.audit`:

```text
cyclosa_be/src/main/java/com/cyclosa/audit/
├── aop/
│   ├── Auditable.java
│   └── AuditLogAspect.java
├── controller/
│   └── AuditLogController.java
├── dto/
│   ├── request/
│   │   ├── AuditLogCommand.java
│   │   └── AuditLogFilter.java
│   └── response/
│       ├── AuditLogDetailResponse.java
│       └── AuditLogResponse.java
├── entity/
│   └── AuditLog.java
├── enums/
│   └── AuditAction.java
├── event/
│   └── AuditLogEvent.java
├── listener/
│   └── AuditLogEventListener.java
├── mapper/
│   └── AuditLogMapper.java
├── repository/
│   └── AuditLogRepository.java
└── service/
    ├── AuditLogService.java
    └── impl/
        └── AuditLogServiceImpl.java
```

### Bảng giải thích chi tiết từng file:

| STT | Tên File & Đường dẫn | Phân loại | Nhiệm vụ & Vai trò kỹ thuật |
| :---: | :--- | :--- | :--- |
| 1 | [`AuditLog.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/entity/AuditLog.java) | **Entity** | Ánh xạ bảng cơ sở dữ liệu `audit_logs`. Chứa các trường: `userId`, `action`, `entityType`, `entityId`, `oldValue`, `newValue`, `ipAddress`, `userAgent`, `createdAt`. Được đánh index trên `created_at`, `user_id` và `(entity_type, entity_id)` để tối ưu tốc độ tra cứu. |
| 2 | [`AuditAction.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/enums/AuditAction.java) | **Enum** | Danh mục chuẩn các hành động nghiệp vụ cần lưu vết: `CREATE`, `UPDATE`, `DELETE`, `APPROVE`, `REJECT`, `CANCEL`, `LOGIN`, `LOGOUT`, `PASSWORD_CHANGE`, `LOCK_USER`, `UNLOCK_USER`, `ASSIGN_ROLES`, `OVERRIDE`, `EXPORT`, `IMPORT`, `SYSTEM`. |
| 3 | [`AuditLogRepository.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/repository/AuditLogRepository.java) | **Repository** | Cung cấp query động `search(...)` hỗ trợ lọc kết hợp: từ khóa (`keyword`), người thực hiện (`userId`), hành động (`action`), loại thực thể (`entityType`), ID thực thể (`entityId`) và khoảng thời gian (`fromDate` - `toDate`). Hỗ trợ hàm tìm lịch sử theo thực thể: `findByEntityTypeAndEntityIdOrderByCreatedAtDesc`. |
| 4 | [`AuditLogCommand.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/dto/request/AuditLogCommand.java) | **DTO Request** | Đối tượng đóng gói tham số truyền vào nội bộ khi các Service khác cần chủ động ghi log kiểm toán. |
| 5 | [`AuditLogFilter.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/dto/request/AuditLogFilter.java) | **DTO Request** | Kế thừa [`BaseFilterRequest.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/common/dto/request/BaseFilterRequest.java). Nhận tham số tìm kiếm và phân trang từ Client (`page`, `size`, `sortBy`, `direction`, `keyword`, `action`, `entityType`, `fromDate`, `toDate`). |
| 6 | [`AuditLogResponse.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/dto/response/AuditLogResponse.java) | **DTO Response** | DTO trả về cho API danh sách. Áp dụng chuẩn **Nested Summary Object** (chứa [`UserSummary`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/common/dto/summary/UserSummary.java)) để UI hiển thị họ tên, avatar của người thực hiện mà không cần gọi thêm API phụ. |
| 7 | [`AuditLogDetailResponse.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/dto/response/AuditLogDetailResponse.java) | **DTO Response** | DTO trả về cho API xem chi tiết. Bổ sung thêm chuỗi JSON `oldValue`, `newValue` để so sánh trước/sau và chuỗi `userAgent` nhận diện thiết bị. |
| 8 | [`AuditLogEvent.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/event/AuditLogEvent.java) | **Domain Event** | Lớp sự kiện dùng cho cơ chế Event-Driven. Các module khác có thể phát sự kiện này qua Spring Event Bus (`eventPublisher.publishEvent(...)`) để ghi log hoàn toàn độc lập. |
| 9 | [`AuditLogEventListener.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/listener/AuditLogEventListener.java) | **Event Listener** | Lắng nghe `AuditLogEvent` trong background thread (`@Async`) và chuyển tiếp sang `AuditLogService` để lưu vào DB. |
| 10 | [`AuditLogMapper.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/mapper/AuditLogMapper.java) | **Mapper** | MapStruct chuyển đổi giữa `AuditLogCommand`, `AuditLogEvent` sang Entity `AuditLog`, và từ `AuditLog` sang `AuditLogResponse`, `AuditLogDetailResponse`. |
| 11 | [`Auditable.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/aop/Auditable.java) | **Annotation** | Custom annotation đánh dấu lên method (Controller/Service) cần ghi log tự động. Tham số gồm: `action` và `entityType`. |
| 12 | [`AuditLogAspect.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/aop/AuditLogAspect.java) | **AOP Aspect** | Bắt các method có gắn `@Auditable`. Tự động bóc tách: `userId` (từ `SecurityContext`), `IP` và `User-Agent` (từ `HttpServletRequest`), `entityId` (từ tham số method hoặc kết quả trả về) và gọi `AuditLogService` lưu vết. |
| 13 | [`AuditLogService.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/service/AuditLogService.java) | **Service Interface** | Định nghĩa hợp đồng công khai cho module: các hàm `log(...)` đa dạng tham số, `getLogs(filter)`, `getLogById(id)`, `getLogsByEntity(entityType, entityId, pageable)`. |
| 14 | [`AuditLogServiceImpl.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/service/impl/AuditLogServiceImpl.java) | **Service Impl** | Cài đặt logic nghiệp vụ. Sử dụng `@Async` và `@Transactional(propagation = Propagation.REQUIRES_NEW)` cho hàm `log`. Sử dụng `userService.getUserSummaries(...)` để nạp thông tin User dạng batch (chống N+1 query). |
| 15 | [`AuditLogController.java`](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/audit/controller/AuditLogController.java) | **Controller** | Cung cấp 3 API REST tra cứu nhật ký kiểm toán cho Quản trị viên/Kiểm toán viên. Được bảo vệ bởi `@PreAuthorize("@perm.has('audit.view')")` và `@RequirePermission("audit.view")`. |

---

## 4. Hướng Dẫn Sử Dụng Trong Dự Án (Developer Guide)

Các lập trình viên khi phát triển các module khác (như `leave`, `contract`, `payroll`, `role`...) có thể chọn 1 trong 3 cách sau để ghi Audit Log:

### Cách 1: Sử dụng Annotation `@Auditable` (Nhanh & Tự động)
Phù hợp với các API tạo, xóa hoặc cập nhật trạng thái đơn giản:
```java
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    @Auditable(action = "DELETE", entityType = "ROLE")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('role.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa vai trò thành công"));
    }
}
```

### Cách 2: Gọi trực tiếp `AuditLogService` (Ghi rõ giá trị Trước / Sau)
Phù hợp với các logic nhạy cảm cần lưu dữ liệu diff (so sánh trước/sau khi sửa):
```java
@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final AuditLogService auditLogService;

    @Transactional
    public void overrideSalary(UUID employeeId, BigDecimal oldSalary, BigDecimal newSalary) {
        // 1. Thực hiện nghiệp vụ sửa lương
        // ...

        // 2. Ghi nhật ký kiểm toán
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        auditLogService.log(
            currentUserId,
            "OVERRIDE",
            "EMPLOYEE_SALARY",
            employeeId,
            String.format("{\"salary\": %s}", oldSalary),
            String.format("{\"salary\": %s}", newSalary)
        );
    }
}
```

### Cách 3: Bắn Domain Event `AuditLogEvent` (Decoupled Event-Driven)
Phù hợp khi muốn giảm tối đa phụ thuộc mã nguồn:
```java
eventPublisher.publishEvent(AuditLogEvent.builder()
    .userId(currentUserId)
    .action("APPROVE")
    .entityType("LEAVE_REQUEST")
    .entityId(leaveId)
    .oldValue("{\"status\": \"PENDING\"}")
    .newValue("{\"status\": \"APPROVED\"}")
    .build()
);
```

---

## 5. Danh Sách API Endpoints & Phân Quyền

| STT | HTTP Method | Endpoint URI | Mô tả Chức năng | Mã Quyền (Permission) |
| :---: | :---: | :--- | :--- | :---: |
| 1 | `GET` | `/api/v1/audit-logs` | Tra cứu danh sách nhật ký kiểm toán (phân trang, lọc theo keyword, user, action, entity, ngày) | `audit.view` |
| 2 | `GET` | `/api/v1/audit-logs/{id}` | Xem chi tiết 1 bản ghi log (gồm diff `old_value`, `new_value`, thiết bị `userAgent`) | `audit.view` |
| 3 | `GET` | `/api/v1/audit-logs/entity/{entityType}/{entityId}` | Lấy toàn bộ lịch sử thay đổi của một bản ghi nghiệp vụ cụ thể | `audit.view` |
