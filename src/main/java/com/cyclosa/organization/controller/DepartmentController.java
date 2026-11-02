package com.cyclosa.organization.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateDepartmentRequest;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@Tag(name = "01. Cơ cấu tổ chức - Sơ đồ & Phòng ban", description = "APIs quản lý phòng ban, cây cơ cấu tổ chức và lịch sử phiên bản hiệu lực")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(summary = "Lấy danh sách phòng ban theo công ty có phân trang")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<PageData<DepartmentResponse>> getDepartments(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId,
            @Parameter(description = "Từ khóa tìm kiếm (mã hoặc tên)")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(departmentService.getDepartments(companyId, keyword, pageable));
    }

    @GetMapping("/tree")
    @Operation(summary = "Lấy cây sơ đồ tổ chức (Org Tree) theo mốc ngày hiệu lực chốt cơ cấu")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<List<DepartmentNodeResponse>> getDepartmentTree(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId,
            @Parameter(description = "Ngày chốt cơ cấu (mặc định hôm nay)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        return ApiResponse.ok(departmentService.getDepartmentTree(companyId, asOfDate));
    }

    @GetMapping("/export")
    @Operation(summary = "Xuất file Excel sơ đồ cơ cấu tổ chức theo ngày hiệu lực chốt")
    @PreAuthorize("@perm.has('org.view')")
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
    @Operation(summary = "Lấy thông tin chi tiết phòng ban theo ID và ngày hiệu lực")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<DepartmentResponse> getDepartmentById(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId,
            @Parameter(description = "Ngày xem cơ cấu")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        return ApiResponse.ok(departmentService.getDepartmentById(departmentId, asOfDate));
    }

    @GetMapping("/{departmentId}/history")
    @Operation(summary = "Lấy toàn bộ lịch sử các phiên bản hiệu lực của một phòng ban")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<List<DepartmentVersionResponse>> getDepartmentHistory(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId) {
        return ApiResponse.ok(departmentService.getDepartmentHistory(departmentId));
    }

    @GetMapping("/{departmentId}/move-impact")
    @Operation(summary = "Xem trước tác động khi chuyển nhánh phòng ban (kiểm tra vòng lặp và đếm số đơn vị con bị cuốn theo)")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<DepartmentImpactResponse> calculateMoveImpact(
            @Parameter(description = "ID phòng ban cần chuyển", required = true)
            @PathVariable UUID departmentId,
            @Parameter(description = "ID phòng ban cha dự kiến chuyển tới (để trống nếu chuyển lên root)")
            @RequestParam(required = false) UUID targetParentId) {
        return ApiResponse.ok(departmentService.calculateMoveImpact(departmentId, targetParentId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới phòng ban và phiên bản cơ cấu ban đầu")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<DepartmentResponse> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        return ApiResponse.created(departmentService.createDepartment(request));
    }

    @PutMapping("/{departmentId}")
    @Operation(summary = "Cập nhật thông tin phòng ban (tạo phiên bản mới theo ngày hiệu lực)")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<DepartmentResponse> updateDepartment(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        return ApiResponse.ok(departmentService.updateDepartment(departmentId, request));
    }

    @PutMapping("/{departmentId}/move")
    @Operation(summary = "Điều chuyển vị trí phòng ban trong cây cơ cấu sang phòng ban cha mới")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<DepartmentResponse> moveDepartment(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId,
            @Valid @RequestBody MoveDepartmentRequest request) {
        return ApiResponse.ok(departmentService.moveDepartment(departmentId, request));
    }

    @PutMapping("/{departmentId}/permission-inheritance")
    @Operation(summary = "Bật / tắt cờ kế thừa phân quyền xuống các đơn vị con")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<Void> updatePermissionInheritance(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId,
            @Valid @RequestBody PermissionInheritanceRequest request) {
        departmentService.updatePermissionInheritance(departmentId, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{departmentId}")
    @Operation(summary = "Giải thể phòng ban (chỉ cho phép khi không còn phòng ban con trực thuộc)")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<Void> dissolveDepartment(
            @Parameter(description = "ID phòng ban", required = true)
            @PathVariable UUID departmentId,
            @Parameter(description = "Ngày giải thể")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dissolvedDate) {
        departmentService.dissolveDepartment(departmentId, dissolvedDate);
        return ApiResponse.ok(null);
    }
}

