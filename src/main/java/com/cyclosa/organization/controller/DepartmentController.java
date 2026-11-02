package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateDepartmentRequest;
import com.cyclosa.organization.dto.request.DepartmentFilterRequest;
import com.cyclosa.organization.dto.request.MoveDepartmentRequest;
import com.cyclosa.organization.dto.request.PermissionInheritanceRequest;
import com.cyclosa.organization.dto.request.UpdateDepartmentRequest;
import com.cyclosa.organization.dto.response.DepartmentImpactResponse;
import com.cyclosa.organization.dto.response.DepartmentNodeResponse;
import com.cyclosa.organization.dto.response.DepartmentResponse;
import com.cyclosa.organization.dto.response.DepartmentVersionResponse;
import com.cyclosa.organization.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Quản lý phòng ban, cây cơ cấu tổ chức và lịch sử phiên bản hiệu lực")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy danh sách phòng ban theo công ty có phân trang", description = "Hỗ trợ tìm kiếm theo mã hoặc tên phòng ban")
    public ResponseEntity<ApiResponse<PageData<DepartmentResponse>>> getDepartments(@ParameterObject @Valid DepartmentFilterRequest filter) {
        PageData<DepartmentResponse> result = departmentService.getDepartments(filter);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách phòng ban thành công"));
    }

    @GetMapping("/tree")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy cây sơ đồ tổ chức (Org Tree) theo mốc ngày hiệu lực chốt cơ cấu")
    public ResponseEntity<ApiResponse<List<DepartmentNodeResponse>>> getDepartmentTree(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId,
            @Parameter(description = "Ngày chốt cơ cấu (mặc định hôm nay)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        List<DepartmentNodeResponse> result = departmentService.getDepartmentTree(companyId, asOfDate);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy sơ đồ cây tổ chức thành công"));
    }

    @GetMapping("/export")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Xuất file Excel sơ đồ cơ cấu tổ chức theo ngày hiệu lực chốt")
    public ResponseEntity<byte[]> exportOrganizationTree(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId,
            @Parameter(description = "Ngày chốt cơ cấu (mặc định hôm nay)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        byte[] excelBytes = departmentService.exportOrganizationTree(companyId, asOfDate);
        String fileName = "So_do_to_chuc_" + (asOfDate != null ? asOfDate : LocalDate.now()) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy thông tin chi tiết phòng ban theo ID và ngày hiệu lực")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId,
            @Parameter(description = "Ngày xem cơ cấu")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        DepartmentResponse result = departmentService.getDepartmentById(departmentId, asOfDate);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin phòng ban thành công"));
    }

    @GetMapping("/{departmentId}/history")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy toàn bộ lịch sử các phiên bản hiệu lực của một phòng ban")
    public ResponseEntity<ApiResponse<List<DepartmentVersionResponse>>> getDepartmentHistory(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId) {
        List<DepartmentVersionResponse> result = departmentService.getDepartmentHistory(departmentId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy lịch sử phiên bản phòng ban thành công"));
    }

    @GetMapping("/{departmentId}/move-impact")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Xem trước tác động khi chuyển nhánh phòng ban (kiểm tra vòng lặp và đếm số đơn vị con bị cuốn theo)")
    public ResponseEntity<ApiResponse<DepartmentImpactResponse>> calculateMoveImpact(
            @Parameter(description = "ID phòng ban cần chuyển", required = true)
            @PathVariable UUID departmentId,
            @Parameter(description = "ID phòng ban cha dự kiến chuyển tới (để trống nếu chuyển lên root)")
            @RequestParam(required = false) UUID targetParentId) {
        DepartmentImpactResponse result = departmentService.calculateMoveImpact(departmentId, targetParentId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Phân tích tác động điều chuyển thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Tạo mới phòng ban và phiên bản cơ cấu ban đầu")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse result = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Tạo mới phòng ban thành công"));
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Cập nhật thông tin phòng ban (tạo phiên bản mới theo ngày hiệu lực)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        DepartmentResponse result = departmentService.updateDepartment(departmentId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật cơ cấu phòng ban thành công"));
    }

    @PutMapping("/{departmentId}/move")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Điều chuyển vị trí phòng ban trong cây cơ cấu sang phòng ban cha mới")
    public ResponseEntity<ApiResponse<DepartmentResponse>> moveDepartment(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId,
            @Valid @RequestBody MoveDepartmentRequest request) {
        DepartmentResponse result = departmentService.moveDepartment(departmentId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Điều chuyển phòng ban thành công"));
    }

    @PutMapping("/{departmentId}/permission-inheritance")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Bật / tắt cờ kế thừa phân quyền xuống các đơn vị con")
    public ResponseEntity<ApiResponse<Void>> updatePermissionInheritance(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId,
            @Valid @RequestBody PermissionInheritanceRequest request) {
        departmentService.updatePermissionInheritance(departmentId, request);
        return ResponseEntity.ok(ApiResponse.noContent("Cập nhật cờ kế thừa quyền thành công"));
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Giải thể phòng ban (chỉ cho phép khi không còn phòng ban con trực thuộc)")
    public ResponseEntity<ApiResponse<Void>> dissolveDepartment(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId,
            @Parameter(description = "Ngày giải thể")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dissolvedDate) {
        departmentService.dissolveDepartment(departmentId, dissolvedDate);
        return ResponseEntity.ok(ApiResponse.noContent("Giải thể phòng ban thành công"));
    }
}
