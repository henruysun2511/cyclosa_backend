package com.cyclosa.organization.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateJobLevelRequest;
import com.cyclosa.organization.dto.request.UpdateJobLevelRequest;
import com.cyclosa.organization.dto.response.JobLevelResponse;
import com.cyclosa.organization.service.JobLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/job-levels")
@RequiredArgsConstructor
@Tag(name = "01. Cơ cấu tổ chức - Cấp bậc chức danh", description = "APIs quản lý thang cấp bậc chuyên môn / quản lý")
public class JobLevelController {

    private final JobLevelService jobLevelService;

    @GetMapping
    @Operation(summary = "Lấy danh sách cấp bậc theo công ty có phân trang")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<PageData<JobLevelResponse>> getJobLevels(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId,
            @Parameter(description = "Từ khóa tìm kiếm")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(jobLevelService.getJobLevels(companyId, keyword, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy toàn bộ cấp bậc theo công ty đã sắp xếp theo rankOrder tăng dần")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<List<JobLevelResponse>> getAllJobLevels(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId) {
        return ApiResponse.ok(jobLevelService.getAllJobLevelsByCompany(companyId));
    }

    @GetMapping("/{jobLevelId}")
    @Operation(summary = "Lấy thông tin chi tiết một cấp bậc theo ID")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<JobLevelResponse> getJobLevelById(
            @Parameter(description = "ID cấp bậc", required = true)
            @PathVariable UUID jobLevelId) {
        return ApiResponse.ok(jobLevelService.getJobLevelById(jobLevelId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới cấp bậc chức danh")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<JobLevelResponse> createJobLevel(@Valid @RequestBody CreateJobLevelRequest request) {
        return ApiResponse.created(jobLevelService.createJobLevel(request));
    }

    @PutMapping("/{jobLevelId}")
    @Operation(summary = "Cập nhật thông tin cấp bậc")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<JobLevelResponse> updateJobLevel(
            @Parameter(description = "ID cấp bậc", required = true)
            @PathVariable UUID jobLevelId,
            @Valid @RequestBody UpdateJobLevelRequest request) {
        return ApiResponse.ok(jobLevelService.updateJobLevel(jobLevelId, request));
    }

    @DeleteMapping("/{jobLevelId}")
    @Operation(summary = "Xóa mềm cấp bậc")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<Void> deleteJobLevel(
            @Parameter(description = "ID cấp bậc", required = true)
            @PathVariable UUID jobLevelId) {
        jobLevelService.deleteJobLevel(jobLevelId);
        return ApiResponse.ok(null);
    }
}

