# CYCLOSA — Quy chuẩn Phát triển Backend & Phân quyền Bảo mật (Master Standards & Agent Rules)

> **Mục đích tài liệu:** Đây là bộ quy tắc và tiêu chuẩn kỹ thuật **BẮT BUỘC** cao nhất của hệ thống Backend `cyclosa_be` (CYCLOSA HRM).
> Toàn bộ các AI Agent và lập trình viên khi khởi tạo module mới hoặc chỉnh sửa mã nguồn hiện tại **BẮT BUỘC PHẢI TUÂN THỦ NGHIÊM NGẶT** từng nguyên tắc dưới đây.

---

## Mục lục
1. [Kiến trúc Modular Monolith & Giao tiếp liên Module](#1-kiến-trúc-modular-monolith--giao-tiếp-liên-module)
2. [Cấu trúc Thư mục Module chuẩn](#2-cấu-trúc-thư-mục-module-chuẩn)
3. [Quy chuẩn Controller & API Documentation (Swagger/OpenAPI)](#3-quy-chuẩn-controller--api-documentation-swaggeropenapi)
4. [Quy chuẩn Phân quyền RBAC & Data Scope](#4-quy-chuẩn-phân-quyền-rbac--data-scope)
5. [Quy chuẩn Entity, JPA Repository & Multi-tenancy](#5-quy-chuẩn-entity-jpa-repository--multi-tenancy)
6. [Quy chuẩn Xử lý Ngoại lệ & Hệ thống Mã lỗi Đa hình (Modular ErrorCode)](#6-quy-chuẩn-xử-lý-ngoại-lệ--hệ-thống-mã-lỗi-đa-hình-modular-errorcode)
7. [Quy chuẩn Phân trang & Tìm kiếm (BaseFilterRequest & PageData)](#7-quy-chuẩn-phân-trang--tìm-kiếm-basefilterrequest--pagedata)
8. [Quy chuẩn DTO & MapStruct](#8-quy-chuẩn-dto--mapstruct)
9. [Checklist Kiểm thử & Hoàn thành Tính năng (Definition of Done)](#9-checklist-kiểm-thử--hoàn-thành-tính-năng-definition-of-done)

---

## 1. Kiến trúc Modular Monolith & Giao tiếp liên Module

Dự án `cyclosa_be` được tổ chức theo mô hình **Modular Monolith**. Mỗi nghiệp vụ lớn là một module độc lập (ví dụ: `auth`, `role`, `organization`, `employee`, `attendance`, `leave`, `payroll`...).

### 1.1. Nguyên tắc Đóng gói (Encapsulation)
- Mỗi module tự quản lý trọn vẹn: `entity`, `repository`, `service`, `mapper`, `dto`, `controller`, và `exception` của riêng mình.
- Dữ liệu nội bộ của module không được để module khác can thiệp tự do.

### 1.2. Nguyên tắc Giao tiếp giữa các Module
1. **Chỉ gọi qua Public Service:**
   - Module A cần dữ liệu hoặc hành vi của Module B **CHỈ ĐƯỢC PHÉP INJECT VÀO `Service` CÔNG KHAI CỦA MODULE B**.
   - **TUYỆT ĐỐI CẤM** việc inject trực tiếp `Repository` hoặc truy cập trực tiếp `Entity` bảng khác của module lân cận.
   ```
   [Module A: Service]  --->  [Module B: Service]  --->  [Module B: Repository]
            |
       (CẤM HOÀN TOÀN)
            x
   [Module B: Repository]
   ```
2. **Giao tiếp bất đồng bộ qua Domain Events (Event-Driven Decoupling):**
   - Khi Module A sinh ra một hành động có tác động phụ (side-effect) sang Module B (ví dụ: Tạo User mới cần kích hoạt phân quyền mặc định ở module Role), sử dụng Spring `ApplicationEventPublisher` phát ra Domain Event (ví dụ: `UserCreatedEvent`).
   - Module B lắng nghe bằng `@EventListener` hoặc `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
   - **Mục tiêu:** Giảm tối đa khớp nối cứng (tight coupling) và triệt tiêu hoàn toàn vòng lặp phụ thuộc (Circular Dependency).
3. **Tuyệt đối không để xảy ra Circular Dependency:**
   - Cấm `ServiceA` inject `ServiceB` đồng thời `ServiceB` lại inject `ServiceA`. Nếu cần chia sẻ logic, tách ra `common` hoặc sử dụng Event bus.

---

## 2. Cấu trúc Thư mục Module chuẩn

Mọi module mới trong package `com.cyclosa.<module_name>` phải tuân thủ chuẩn cấu trúc phân lớp sau:

```
com.cyclosa.<module_name>/
├── controller/                 # REST Controllers (@RestController)
│   └── XxxController.java
├── service/                    # Business Logic Services (@Service, @Transactional)
│   ├── XxxService.java
│   └── impl/XxxServiceImpl.java (nếu cần tách interface)
├── repository/                 # Data Access Layer (JpaRepository + JpaSpecificationExecutor)
│   └── XxxRepository.java
├── entity/                     # JPA Entities (extends BaseEntity)
│   └── Xxx.java
├── mapper/                     # MapStruct Mappers (Spring Component Model)
│   └── XxxMapper.java
├── dto/                        # Data Transfer Objects
│   ├── request/                # DTO nhận từ Client (@Valid)
│   │   ├── CreateXxxRequest.java
│   │   ├── UpdateXxxRequest.java
│   │   └── XxxFilter.java      # Kế thừa BaseFilterRequest
│   └── response/               # DTO trả về cho Client
│       ├── XxxResponse.java
│       └── XxxDetailResponse.java
└── exception/                  # Mã lỗi riêng của module (nếu có)
    └── XxxErrorCode.java       # Implements ErrorCode interface
```

---

## 3. Quy chuẩn Controller & API Documentation (Swagger/OpenAPI)

### 3.1. Đặt tên `@Tag` Swagger: **KHÔNG DÙNG TIỀN TỐ SỐ THỨ TỰ**
- **Quy định bắt buộc:** Tên tag trong `@Tag(name = "...")` chỉ ghi tên danh từ của module, **tuyệt đối không để tiền tố số thứ tự** (như `01.`, `02.`...).
- **Đúng:**
  ```java
  @Tag(name = "Roles", description = "Quản lý vai trò và phân quyền trong hệ thống")
  @Tag(name = "Employees", description = "Quản lý hồ sơ nhân viên")
  ```
- **Sai:**
  ```java
  @Tag(name = "02. Roles", description = "...") // SAI: Tuyệt đối không gắn số thứ tự!
  ```

### 3.2. Cấu trúc Response: **100% sử dụng `ApiResponse<T>`**
Mọi endpoint trả về Client đều phải được bọc trong `ResponseEntity<ApiResponse<T>>`:
- **Trả về dữ liệu thành công (200 OK):**
  ```java
  return ResponseEntity.ok(ApiResponse.ok(data, "Lấy thông tin thành công"));
  ```
- **Tạo mới thành công (201 Created):**
  ```java
  return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.created(data, "Tạo bản ghi thành công"));
  ```
- **Thao tác thành công không trả dữ liệu (200 OK với data rỗng):**
  ```java
  return ResponseEntity.ok(ApiResponse.noContent("Xóa bản ghi thành công"));
  ```
- **Phân trang thành công (200 OK):**
  ```java
  PageData<XxxResponse> result = xxxService.getItems(filter);
  return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách thành công"));
  ```
- **TUYỆT ĐỐI CẤM:** Trả về Entity JPA trực tiếp, raw `Object`, raw `List<T>`, hoặc Spring `Page<T>`. Phải luôn bọc bằng `ApiResponse<PageData<T>>`.

### 3.3. Swagger `@Operation`
Mọi API method đều phải có `@Operation` mô tả rõ ràng tóm tắt và hành vi:
```java
@Operation(summary = "Cập nhật thông tin vai trò", description = "Chỉ cho phép cập nhật vai trò tùy biến của công ty")
```

---

## 4. Quy chuẩn Phân quyền RBAC & Data Scope

Hệ thống CYCLOSA HRM kết hợp **Role-Based Access Control (RBAC)** với **Data Scope** (5 cấp độ: `OWN`, `TEAM`, `DEPARTMENT`, `COMPANY`, `ALL`).

### 4.1. Quy tắc Đặt tên Permission Code
Mã quyền luôn tuân thủ format: `<module>.<action>` (chữ thường, phân cách bằng dấu chấm).
- Ví dụ:
  - `role.view`, `role.create`, `role.update`, `role.delete`, `role.assign`
  - `user.view`, `user.create`, `user.assign_role`
  - `employee.view`, `employee.manage`, `employee.view_salary`
  - `leave.view`, `leave.apply`, `leave.approve`
  - `payroll.view`, `payroll.calculate`, `payroll.approve`

### 4.2. Chốt chặn API tại Controller
Mọi endpoint nghiệp vụ (ngoại trừ public endpoints trong `SecurityConfig`) **BẮT BUỘC** phải có cặp đôi `@PreAuthorize` và `@RequirePermission`:

```java
@RestController
@RequestMapping("/api/v1/{module}")
@RequiredArgsConstructor
@Tag(name = "...", description = "...")
public class DomainController {

    // 1. API Đọc / Xem danh sách (Mặc định yêu cầu scope tối thiểu OWN)
    @GetMapping
    @PreAuthorize("@perm.has('{module}.view')")
    @RequirePermission("{module}.view")
    @Operation(summary = "Xem danh sách {module}")
    public ResponseEntity<ApiResponse<PageData<DomainResponse>>> getList(@Valid DomainFilter filter) { ... }

    // 2. API Xem chi tiết theo ID
    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('{module}.view')")
    @RequirePermission("{module}.view")
    @Operation(summary = "Xem chi tiết {module}")
    public ResponseEntity<ApiResponse<DomainDetailResponse>> getById(@PathVariable UUID id) { ... }

    // 3. API Tạo mới
    @PostMapping
    @PreAuthorize("@perm.has('{module}.create')")
    @RequirePermission("{module}.create")
    @Operation(summary = "Tạo mới {module}")
    public ResponseEntity<ApiResponse<DomainResponse>> create(@Valid @RequestBody CreateRequest req) { ... }

    // 4. API Chỉnh sửa
    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('{module}.update')")
    @RequirePermission("{module}.update")
    @Operation(summary = "Cập nhật {module}")
    public ResponseEntity<ApiResponse<DomainResponse>> update(@PathVariable UUID id, @Valid @RequestBody UpdateRequest req) { ... }

    // 5. API Xóa
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('{module}.delete')")
    @RequirePermission("{module}.delete")
    @Operation(summary = "Xóa {module}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) { ... }

    // 6. API Phê duyệt / Quản trị (Yêu cầu DataScope tối thiểu TEAM / DEPARTMENT / COMPANY)
    @PutMapping("/{id}/approve")
    @PreAuthorize("@perm.has('{module}.approve', T(com.cyclosa.common.enums.DataScope).TEAM)")
    @RequirePermission(value = "{module}.approve", minScope = DataScope.TEAM)
    @Operation(summary = "Phê duyệt yêu cầu")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable UUID id) { ... }
}
```

### 4.3. Lọc Dữ liệu tại Tầng Service theo DataScope
Khi truy vấn danh sách có gắn liền với người dùng / phòng ban, Service **bắt buộc** gọi `SecurityPermissionEvaluator.getDataScope("{module}.view")` và áp dụng `switch-case`:

```java
@Service
@RequiredArgsConstructor
public class DomainServiceImpl implements DomainService {

    private final SecurityPermissionEvaluator perm;
    private final DomainRepository repository;
    private final DomainMapper mapper;

    @Transactional(readOnly = true)
    public PageData<DomainResponse> getList(DomainFilter filter) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        DataScope scope = perm.getDataScope("domain.view");
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);

        Page<Domain> result = switch (scope) {
            case OWN -> repository.findByCreatedBy(currentUserId, pageable);
            case TEAM -> {
                UUID teamId = getTeamIdOfUser(currentUserId);
                yield repository.findByTeamId(teamId, pageable);
            }
            case DEPARTMENT -> {
                UUID deptId = getDepartmentIdOfUser(currentUserId);
                yield repository.findByDepartmentId(deptId, pageable);
            }
            case COMPANY -> {
                UUID companyId = SecurityUtils.getCurrentUserCompanyId();
                yield repository.findByCompanyId(companyId, pageable);
            }
            case ALL -> repository.findAll(pageable);
        };

        return PageData.of(result, mapper::toResponse);
    }
}
```

### 4.4. BẮT BUỘC: Đồng bộ Data Initializer khi có Permission mới
Khi phát triển bất kỳ API nào có gắn mã quyền mới:
1. **Phải khai báo ngay** vào `seedPermissions()` trong [RolePermissionDataInitializer.java](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/common/config/RolePermissionDataInitializer.java).
2. **Gán quyền mặc định** vào các vai trò hệ sinh thái (`SUPER_ADMIN`, `COMPANY_ADMIN`, `HR_ADMIN`, `EMPLOYEE`...) trong `seedRolePermissions()` theo tài liệu BA.
3. *Cảnh báo:* Nếu bỏ qua bước này, bảng `permissions` sẽ thiếu dữ liệu, dẫn tới người dùng bình thường luôn bị lỗi `403 Forbidden` dù tài khoản đã được cấp vai trò.

### 4.5. Bảo vệ Tuyệt đối `SecurityConfig.PUBLIC_ENDPOINTS`
- **Chỉ mở public** những endpoint thực sự không có token: `/api/v1/auth/login`, `/api/v1/auth/activate`, `/api/v1/auth/refresh`, Swagger UI, OpenAPI docs, Health check.
- **TUYỆT ĐỐI CẤM** đưa các endpoint nghiệp vụ (`/api/v1/roles`, `/api/v1/employees`...) vào danh sách public để "tiện test".

---

## 5. Quy chuẩn Entity, JPA Repository & Multi-tenancy

### 5.1. Kế thừa `BaseEntity` & Soft Delete
Mọi Entity chuẩn đều phải kế thừa [BaseEntity.java](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/common/entity/BaseEntity.java) để có sẵn:
`UUID id`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `deletedAt`.

```java
@Entity
@Table(name = "roles")
@SQLDelete(sql = "UPDATE roles SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {
    // fields
}
```

### 5.2. Chống lỗi N+1 Query
- Mặc định toàn bộ các quan hệ `@ManyToOne`, `@OneToMany` phải để `FetchType.LAZY`.
- Khi cần lấy dữ liệu quan hệ kèm theo để trả về Client, **bắt buộc dùng `JOIN FETCH`**:
  ```java
  @Query("SELECT urm FROM UserRoleMapping urm JOIN FETCH urm.role WHERE urm.user.id = :userId")
  List<UserRoleMapping> findByUserIdWithRole(@Param("userId") UUID userId);
  ```

### 5.3. Quy tắc Truy vấn Multi-tenancy An toàn
Khi lọc dữ liệu chia sẻ giữa tài nguyên hệ thống chung (`companyId IS NULL`) và tài nguyên riêng của từng công ty (`companyId = :companyId`):
- **ĐÚNG:**
  ```sql
  AND (r.companyId IS NULL OR r.companyId = :companyId)
  ```
  *(Nếu `:companyId` là null, chỉ lấy tài nguyên hệ thống. Nếu có `:companyId`, lấy tài nguyên hệ thống + tài nguyên riêng của công ty đó).*
- **CẤM KỴ TUYỆT ĐỐI:**
  ```sql
  AND (:companyId IS NULL OR r.companyId = :companyId OR r.companyId IS NULL)
  ```
  *(Mệnh đề `:companyId IS NULL` sẽ biến cả biểu thức thành TRUE khi không truyền companyId, làm rò rỉ toàn bộ dữ liệu của tất cả các công ty khác!)*

---

## 6. Quy chuẩn Xử lý Ngoại lệ & Hệ thống Mã lỗi Đa hình (Modular ErrorCode)

### 6.1. Kiến trúc Đa hình `ErrorCode`
Để tránh file `ErrorCode` dùng chung bị phình to (god object), hệ thống áp dụng thiết kế đa hình:
- [ErrorCode.java](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/common/exception/ErrorCode.java) là một **public interface**:
  ```java
  public interface ErrorCode {
      int getCode();
      String getMessage();
      HttpStatus getHttpStatus();
  }
  ```
- **Lỗi dùng chung hệ thống:** Định nghĩa tại [CommonErrorCode.java](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/common/exception/CommonErrorCode.java) (ví dụ: `INTERNAL_ERROR (5000)`, `VALIDATION_FAILED (4000)`, `RESOURCE_NOT_FOUND (4004)`, `CONFLICT (4009)`, `FORBIDDEN (4003)`, `OPTIMISTIC_LOCK (4029)`).
- **Lỗi đặc thù từng Module:** Khai báo enum riêng bên trong module đó, implements `ErrorCode`:
  - `com.cyclosa.auth.exception.AuthErrorCode implements ErrorCode`
  - `com.cyclosa.role.exception.RoleErrorCode implements ErrorCode`
  - `com.cyclosa.employee.exception.EmployeeErrorCode implements ErrorCode`

### 6.2. Ném lỗi thông qua `AppException`
- Tuyệt đối không ném `RuntimeException` chung chung hoặc nuốt exception.
- Luôn ném `AppException` kết hợp mã lỗi hoặc thông điệp rõ ràng:
  ```java
  // Sử dụng ErrorCode cụ thể của module:
  throw new AppException(RoleErrorCode.SYSTEM_ROLE_CANNOT_BE_MODIFIED);

  // Hoặc dùng static helper methods có sẵn:
  throw AppException.notFound("Không tìm thấy vai trò với ID: " + id);
  throw AppException.conflict("Mã vai trò đã tồn tại trong hệ thống");
  throw AppException.forbidden("Bạn không có quyền thao tác trên tài nguyên này");
  ```

---

## 7. Quy chuẩn Phân trang & Tìm kiếm (BaseFilterRequest & PageData)

### 7.1. DTO Filter kế thừa `BaseFilterRequest`
Mọi DTO nhận tham số tìm kiếm và phân trang từ Client **BẮT BUỘC** phải kế thừa [BaseFilterRequest.java](file:///f:/Project/CYCLOSA/cyclosa_be/src/main/java/com/cyclosa/common/dto/request/BaseFilterRequest.java):

```java
@Getter
@Setter
@Schema(description = "Tham số tìm kiếm vai trò")
public class RoleFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID companyId;

    @Schema(description = "Lọc vai trò hệ thống", example = "true")
    private Boolean isSystemRole;
}
```
*(BaseFilterRequest đã có sẵn: `keyword`, `page`, `size`, `sortBy`, `direction`, và helper method `toPageable()`)*.

### 7.2. Xử lý Phân trang trong Service
Sử dụng trực tiếp method `filter.toPageable()` và chuyển đổi dữ liệu qua `PageData.of()`:

```java
private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "code", "createdAt");

@Transactional(readOnly = true)
public PageData<RoleResponse> getRoles(RoleFilter req) {
    String kw = PageableUtils.normalizeKeyword(req.getKeyword());
    Pageable pageable = req.toPageable("createdAt", ALLOWED_SORT_FIELDS);

    Page<Role> pageResult = roleRepository.search(kw, req.getCompanyId(), req.getIsSystemRole(), pageable);
    return PageData.of(pageResult, roleMapper::toResponse);
}
```

---

## 8. Quy chuẩn DTO & MapStruct

### 8.1. Request DTO & Jakarta Validation
- Mọi trường dữ liệu nhận từ Client phải có Validation annotation (`@NotBlank`, `@NotNull`, `@Size`, `@Email`, `@Pattern`...).
- Thông điệp lỗi bằng tiếng Việt rõ ràng, dễ hiểu:
  ```java
  @NotBlank(message = "Tên vai trò không được để trống")
  @Size(max = 100, message = "Tên vai trò không được vượt quá 100 ký tự")
  @Schema(description = "Tên hiển thị của vai trò", example = "Quản lý nhân sự")
  private String name;
  ```

### 8.2. MapStruct Mapper
- Luôn sử dụng Spring component model:
  ```java
  @Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
  public interface RoleMapper {
      Role toEntity(CreateRoleRequest request);
      RoleResponse toResponse(Role role);
      List<RoleResponse> toResponseList(List<Role> roles);
  }
  ```
- Tuyệt đối không viết mapper thủ công bằng tay lặp qua từng phần tử trong Service.

### 8.3. Chiến lược DTO Response & Dữ liệu quan hệ liên bảng (Nested Summary Object & Detail DTO)

#### 1. API Danh sách (`GET /api/v1/...`): BẮT BUỘC ÁP DỤNG CHIẾN LƯỢC 2 (Nested Summary Object)
- **Quy tắc cốt lõi:** Tuyệt đối không chỉ trả về ID ngoại lai thô (`companyId`, `branchId`, `positionId`...). Phía UI luôn cần hiển thị nhãn (tên, mã) kèm theo mà không cần phải gọi thêm nhiều API phụ.
- **Quy chuẩn Nested Summary:** Thay thế các trường ID phẳng bằng đối tượng tóm tắt thu gọn (`Nested Summary Object`):
  - Thay `UUID companyId` bằng `CompanySummary company` (`{ id, code, name }`)
  - Thay `UUID branchId` bằng `BranchSummary branch` (`{ id, code, name }`)
  - Thay `UUID organizationalUnitId` bằng `OrgUnitSummary organizationalUnit` (`{ id, code, name, unitType }`)
  - Thay `UUID positionId` bằng `PositionSummary position` (`{ id, code, name }`)
  - Thay `UUID jobLevelId` bằng `JobLevelSummary jobLevel` (`{ id, name, rankOrder }`)
  - Thay `UUID costCenterId` bằng `CostCenterSummary costCenter` (`{ id, code, name }`)
  - Thay `UUID managerEmployeeId` bằng `EmployeeSummary manager` (`{ id, employeeCode, fullName, photoUrl, companyEmail }`)
- **Vị trí lưu trữ:** Toàn bộ các DTO Summary dùng chung được đặt tập trung tại `com.cyclosa.common.dto.summary.*` để đảm bảo tái sử dụng toàn hệ thống và triệt tiêu phụ thuộc vòng tròn giữa các module.
- **Kỹ thuật chống N+1 Query (Batch Enrichment):**
  - Trong Service của API danh sách, **TUYỆT ĐỐI KHÔNG** gọi query cơ sở dữ liệu lặp trong vòng lặp chuyển đổi DTO.
  - Phải thu thập tập hợp ID của trang (`Set<UUID> ids = page.getContent().stream().map(...).collect(Collectors.toSet())`).
  - Gọi batch method từ Public Service hoặc Repository (ví dụ: `companyService.getCompanySummaries(companyIds)`) để nạp toàn bộ vào `Map<UUID, SummaryDTO>` trong 1 truy vấn duy nhất `IN (...)`, sau đó gán vào response DTO.

#### 2. API Chi tiết (`GET /api/v1/.../{id}`): BẮT BUỘC TÁCH RIÊNG `*DetailResponse`
- **Không dùng chung DTO:** API danh sách và API xem chi tiết có mục đích và tải trọng dữ liệu khác nhau. API chi tiết bắt buộc phải có DTO riêng mang hậu tố `DetailResponse` (ví dụ: `RoleDetailResponse`, `CompanyDetailResponse`, `BranchDetailResponse`, `PositionDetailResponse`, `OrgUnitDetailResponse`, `EmployeeDetailResponse`).
- **Nội dung DTO chi tiết:** Cung cấp thông tin phong phú, thống kê chuyên sâu, các danh sách quan hệ con (như danh sách quyền chi tiết, số lượng chi nhánh trực thuộc, danh sách người phụ thuộc, liên hệ khẩn cấp...).

#### 3. Bảo mật DTO
- Tuyệt đối không để lộ các trường nhạy cảm (`passwordHash`, `salt`, `secretKey`, `token`...).
- Định danh ID luôn là `UUID`.

---

## 9. Checklist Kiểm thử & Hoàn thành Tính năng (Definition of Done)

Trước khi coi một tính năng hoặc module backend là hoàn thành, **bắt buộc kiểm tra danh sách sau**:

- [ ] **1. Controller & Swagger:**
  - `@Tag(name = "...")` không chứa tiền tố số thứ tự (`01.`, `02.`...).
  - Mọi API method có `@Operation(summary = "...")`.
  - Mọi API method trả về `ResponseEntity<ApiResponse<T>>`.
- [ ] **2. Phân quyền & Bảo mật:**
  - Endpoint có đủ cặp `@PreAuthorize("@perm.has('...')")` và `@RequirePermission('...')`.
  - Toàn bộ mã quyền mới đã được thêm vào `RolePermissionDataInitializer.seedPermissions()` và gán role mặc định trong `seedRolePermissions()`.
  - Service có switch-case xử lý `DataScope` tương ứng.
  - Không mở lộ endpoint nghiệp vụ trong `SecurityConfig.PUBLIC_ENDPOINTS`.
- [ ] **3. Kiến trúc Phân tầng & Đóng gói:**
  - Không inject Repository của module khác; chỉ gọi Public Service.
  - Tác động phụ liên module được giải phóng qua Domain Events (`ApplicationEventPublisher`).
- [ ] **4. DTO & Filter:**
  - Filter DTO kế thừa `BaseFilterRequest`.
  - Dùng `toPageable()` với danh sách `ALLOWED_SORT_FIELDS` hợp lệ.
  - Trả về `PageData<T>` thay vì raw Spring `Page<T>`.
- [ ] **5. Mã lỗi & Entity:**
  - Lỗi riêng của module được định nghĩa trong enum implement `ErrorCode`.
  - Entity kế thừa `BaseEntity`, có soft delete `@SQLDelete` và `@SQLRestriction`.
  - Query quan hệ dùng `JOIN FETCH` (chống N+1).
  - Điều kiện lọc `companyId` an toàn: `AND (r.companyId IS NULL OR r.companyId = :companyId)`.
- [ ] **6. Biên dịch & Kiểm thử tự động:**
  - Chạy `./mvnw test-compile` đạt `BUILD SUCCESS`.
  - Chạy `./mvnw test` vượt qua 100% unit tests.
