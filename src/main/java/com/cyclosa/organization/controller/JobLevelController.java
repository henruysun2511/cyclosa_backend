package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateJobLevelRequest;
import com.cyclosa.organization.dto.request.JobLevelFilterRequest;
import com.cyclosa.organization.dto.request.UpdateJobLevelRequest;
import com.cyclosa.organization.dto.response.JobLevelResponse;
import com.cyclosa.organization.service.JobLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/job-levels")
@RequiredArgsConstructor
@Tag(name = "Job Levels", description = "Quản lý thang cấp bậc chuyên môn / quản lý")
public class JobLevelController {

    private final JobLevelService jobLevelService;

    @GetMapping
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy danh sách cấp bậc theo công ty có phân trang", description = "Hỗ trợ lọc từ khóa và phân trang")
    public ResponseEntity<ApiResponse<PageData<JobLevelResponse>>> getJobLevels(@ParameterObject @Valid JobLevelFilterRequest filter) {
        PageData<JobLevelResponse> result = jobLevelService.getJobLevels(filter);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách cấp bậc thành công"));
    }

    @GetMapping("/all")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy toàn bộ cấp bậc theo công ty đã sắp xếp theo rankOrder tăng dần")
    public ResponseEntity<ApiResponse<List<JobLevelResponse>>> getAllJobLevels(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId) {
        List<JobLevelResponse> result = jobLevelService.getAllJobLevelsByCompany(companyId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy toàn bộ cấp bậc thành công"));
    }

    @GetMapping("/{jobLevelId}")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy thông tin chi tiết một cấp bậc theo ID")
    public ResponseEntity<ApiResponse<JobLevelResponse>> getJobLevelById(
            @Parameter(description = "ID cấp bậc", required = true)
            @PathVariable UUID jobLevelId) {
        JobLevelResponse result = jobLevelService.getJobLevelById(jobLevelId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin cấp bậc thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Tạo mới cấp bậc chức danh")
    public ResponseEntity<ApiResponse<JobLevelResponse>> createJobLevel(@Valid @RequestBody CreateJobLevelRequest request) {
        JobLevelResponse result = jobLevelService.createJobLevel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Tạo mới cấp bậc thành công"));
    }

    @PutMapping("/{jobLevelId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Cập nhật thông tin cấp bậc")
    public ResponseEntity<ApiResponse<JobLevelResponse>> updateJobLevel(
            @Parameter(description = "ID cấp bậc", required = true)
            @PathVariable UUID jobLevelId,
            @Valid @RequestBody UpdateJobLevelRequest request) {
        JobLevelResponse result = jobLevelService.updateJobLevel(jobLevelId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật cấp bậc thành công"));
    }

    @DeleteMapping("/{jobLevelId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Xóa mềm cấp bậc")
    public ResponseEntity<ApiResponse<Void>> deleteJobLevel(
            @Parameter(description = "ID cấp bậc", required = true)
            @PathVariable UUID jobLevelId) {
        jobLevelService.deleteJobLevel(jobLevelId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa cấp bậc thành công"));
    }
}
