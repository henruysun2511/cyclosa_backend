# Antigravity Agent Rules — CYCLOSA Backend

> **QUY TẮC TỐI CAO:** Trong mọi phiên làm việc trên repository này, AI Agent **BẮT BUỘC PHẢI ĐỌC VÀ TUÂN THỦ 100% BỘ QUY CHUẨN** tại tài liệu:
> 👉 **[docs/BACKEND_STANDARDS.md](file:///f:/Project/CYCLOSA/cyclosa_be/docs/BACKEND_STANDARDS.md)**

---

## 9 NGUYÊN TẮC BẤT KHẢ XÂM PHẠM

1. **Kiến trúc Modular Monolith & Tuyệt đối Không Inject Repository của Module khác:**
   - Khi Module A cần dữ liệu/hành vi của Module B, **CHỈ ĐƯỢC PHÉP INJECT VÀO PUBLIC `*Service` CỦA MODULE B**.
   - **TUYỆT ĐỐI CẤM** inject trực tiếp `*Repository` hoặc truy cập trực tiếp `Entity` của module khác.

2. **Cấu trúc Package Module chuẩn:**
   - Mỗi module gồm: `controller/`, `service/`, `repository/`, `entity/`, `mapper/`, `dto/` (request, response, filter), `exception/`.

3. **Controller & Swagger:**
   - `@Tag(name = "...")` **KHÔNG ĐƯỢC CHỨA TIỀN TỐ SỐ THỨ TỰ** (như `01.`, `02.`, `03.`...).
   - Mọi method phải có `@Operation(summary = "...")`.
   - 100% API responses phải bọc trong `ResponseEntity<ApiResponse<T>>`. Cấm trả về entity hoặc raw object/page.

4. **Phân quyền RBAC & DataScope:**
   - Endpoint nghiệp vụ phải có đủ cặp: `@PreAuthorize("@perm.has('...')")` và `@RequirePermission('...')`.
   - Khi tạo quyền mới, **bắt buộc cập nhật ngay** vào `RolePermissionDataInitializer.seedPermissions()` và `seedRolePermissions()`.
   - Service lọc danh sách phải gọi `perm.getDataScope(...)` và switch-case phạm vi dữ liệu (`OWN`, `TEAM`, `DEPARTMENT`, `COMPANY`, `ALL`).

5. **Entity & Multi-tenancy:**
   - Kế thừa `BaseEntity`, có `@SQLDelete` và `@SQLRestriction("deleted_at IS NULL")`.
   - Quan hệ để `FetchType.LAZY`, fetch dữ liệu bằng `JOIN FETCH` để chống N+1 Query.
   - Truy vấn multi-tenancy an toàn: `AND (r.companyId IS NULL OR r.companyId = :companyId)`.

6. **Mã lỗi đa hình (Modular ErrorCode):**
   - Không ném exception chung chung.
   - Định nghĩa enum mã lỗi riêng trong module implement `ErrorCode`.

7. **Phân trang & Tìm kiếm:**
   - Mọi Filter DTO **BẮT BUỘC KẾ THỪA `BaseFilterRequest`**.
   - Dùng `filter.toPageable(...)` với `ALLOWED_SORT_FIELDS`.
   - Kết quả trả về `PageData<T>` (không trả Spring `Page<T>` thô).

8. **DTO & Batch Enrichment (Chống N+1 Query):**
   - API danh sách (`GET /api/v1/...`): Dùng `Nested Summary Object` (`EmployeeSummary`, `OrgUnitSummary`, `PositionSummary`, `CompanySummary` trong `com.cyclosa.common.dto.summary.*`).
   - Cấm query database lặp trong vòng map DTO. Phải gom set IDs và gọi batch method từ Public Service (`getEmployeeSummaries`, `getUnitSummaries`, `getPositionSummaries`).
   - API chi tiết (`GET /api/v1/.../{id}`): Tách riêng DTO `*DetailResponse`.

9. **Definition of Done:**
   - `./mvnw test-compile` thành công.
   - `./mvnw test` vượt qua 100% unit tests.
