# Backend Standards Rule

Tất cả các hành động lập trình trên dự án CYCLOSA Backend `cyclosa_be` phải tuân thủ nghiêm ngặt tài liệu:
[docs/BACKEND_STANDARDS.md](file:///f:/Project/CYCLOSA/cyclosa_be/docs/BACKEND_STANDARDS.md)

## Checklist Bắt buộc:
1. **Tuyệt đối cấm inject Repository của module khác:** Chỉ được gọi qua `*Service` công khai của module đó.
2. **DTO Filter kế thừa BaseFilterRequest:** Mọi `*Filter` nhận tham số tìm kiếm và phân trang từ client đều phải `extends BaseFilterRequest`.
3. **Response DTO:** Sử dụng Nested Summary Objects (`CompanySummary`, `EmployeeSummary`, `OrgUnitSummary`, `PositionSummary`) cho API danh sách; tách `*DetailResponse` cho API chi tiết.
4. **Batch Enrichment:** Gom IDs theo `Set<UUID>` và query 1 lần để chống N+1.
5. **Controller:** 100% bọc `ResponseEntity<ApiResponse<T>>`, `@Operation`, không có số thứ tự trong `@Tag(name = "...")`.
6. **Bảo mật:** Đầy đủ `@PreAuthorize` và `@RequirePermission`.
7. **Test:** Chạy `./mvnw test` đảm bảo 100% test pass trước khi bàn giao.
