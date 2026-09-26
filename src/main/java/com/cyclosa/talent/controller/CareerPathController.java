package com.cyclosa.talent.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.talent.dto.filter.CareerPathFilter;
import com.cyclosa.talent.dto.request.CreateCareerPathRequest;
import com.cyclosa.talent.dto.request.UpdateCareerPathRequest;
import com.cyclosa.talent.dto.response.CareerPathResponse;
import com.cyclosa.talent.service.CareerPathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Career Paths", description = "Quản lý lộ trình thăng tiến chuẩn giữa các chức danh trong tổ chức")
public class CareerPathController {

    private final CareerPathService careerPathService;

    @PostMapping("/career-paths")
    @PreAuthorize("@perm.has('career_path.manage')")
    @RequirePermission("career_path.manage")
    @Operation(summary = "Thiết lập lộ trình thăng tiến chuẩn mới giữa hai vị trí")
    public ResponseEntity<ApiResponse<CareerPathResponse>> createCareerPath(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CreateCareerPathRequest request
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        CareerPathResponse response = careerPathService.createCareerPath(effectiveCompanyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Thiết lập lộ trình thăng tiến thành công"));
    }

    @GetMapping("/career-paths")
    @PreAuthorize("@perm.has('career_path.view')")
    @RequirePermission("career_path.view")
    @Operation(summary = "Lấy danh sách các lộ trình thăng tiến chuẩn (có lọc và phân trang)")
    public ResponseEntity<ApiResponse<PageData<CareerPathResponse>>> getCareerPaths(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @ModelAttribute CareerPathFilter filter
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                careerPathService.getCareerPaths(effectiveCompanyId, filter),
                "Lấy danh sách lộ trình thăng tiến thành công"));
    }

    @GetMapping("/career-paths/{id}")
    @PreAuthorize("@perm.has('career_path.view')")
    @RequirePermission("career_path.view")
    @Operation(summary = "Xem chi tiết một lộ trình thăng tiến")
    public ResponseEntity<ApiResponse<CareerPathResponse>> getCareerPathById(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                careerPathService.getCareerPathById(effectiveCompanyId, id),
                "Lấy chi tiết lộ trình thăng tiến thành công"));
    }

    @PutMapping("/career-paths/{id}")
    @PreAuthorize("@perm.has('career_path.manage')")
    @RequirePermission("career_path.manage")
    @Operation(summary = "Cập nhật thông tin lộ trình thăng tiến")
    public ResponseEntity<ApiResponse<CareerPathResponse>> updateCareerPath(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCareerPathRequest request
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                careerPathService.updateCareerPath(effectiveCompanyId, id, request),
                "Cập nhật lộ trình thăng tiến thành công"));
    }

    @DeleteMapping("/career-paths/{id}")
    @PreAuthorize("@perm.has('career_path.manage')")
    @RequirePermission("career_path.manage")
    @Operation(summary = "Xóa mềm một lộ trình thăng tiến")
    public ResponseEntity<ApiResponse<Void>> deleteCareerPath(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        careerPathService.deleteCareerPath(effectiveCompanyId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa lộ trình thăng tiến thành công"));
    }

    @GetMapping("/employees/{id}/career-path")
    @PreAuthorize("@perm.has('career_path.view')")
    @RequirePermission("career_path.view")
    @Operation(summary = "Tra cứu các lộ trình thăng tiến chuẩn khả thi từ vị trí hiện tại của nhân viên")
    public ResponseEntity<ApiResponse<List<CareerPathResponse>>> getCareerPathsForEmployee(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                careerPathService.getCareerPathsForEmployee(effectiveCompanyId, id),
                "Lấy danh sách lộ trình thăng tiến khả dụng thành công"));
    }
}
