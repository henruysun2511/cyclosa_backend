package com.cyclosa.organization.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateJobTitleRequest;
import com.cyclosa.organization.dto.request.UpdateJobTitleRequest;
import com.cyclosa.organization.dto.response.JobTitleResponse;
import com.cyclosa.organization.service.JobTitleService;
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
@RequestMapping("/api/v1/job-titles")
@RequiredArgsConstructor
@Tag(name = "01. Cơ cấu tổ chức - Chức danh công việc", description = "APIs quản lý danh mục chức danh vị trí công việc")
public class JobTitleController {

    private final JobTitleService jobTitleService;

    @GetMapping
    @Operation(summary = "Lấy danh sách chức danh theo công ty có phân trang")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<PageData<JobTitleResponse>> getJobTitles(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId,
            @Parameter(description = "Từ khóa tìm kiếm")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(jobTitleService.getJobTitles(companyId, keyword, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy toàn bộ chức danh theo công ty (cho dropdown)")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<List<JobTitleResponse>> getAllJobTitles(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId) {
        return ApiResponse.ok(jobTitleService.getAllJobTitlesByCompany(companyId));
    }

    @GetMapping("/{jobTitleId}")
    @Operation(summary = "Lấy thông tin chi tiết một chức danh theo ID")
    @PreAuthorize("@perm.has('org.view')")
    public ApiResponse<JobTitleResponse> getJobTitleById(
            @Parameter(description = "ID chức danh", required = true)
            @PathVariable UUID jobTitleId) {
        return ApiResponse.ok(jobTitleService.getJobTitleById(jobTitleId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới chức danh công việc")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<JobTitleResponse> createJobTitle(@Valid @RequestBody CreateJobTitleRequest request) {
        return ApiResponse.created(jobTitleService.createJobTitle(request));
    }

    @PutMapping("/{jobTitleId}")
    @Operation(summary = "Cập nhật thông tin chức danh")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<JobTitleResponse> updateJobTitle(
            @Parameter(description = "ID chức danh", required = true)
            @PathVariable UUID jobTitleId,
            @Valid @RequestBody UpdateJobTitleRequest request) {
        return ApiResponse.ok(jobTitleService.updateJobTitle(jobTitleId, request));
    }

    @DeleteMapping("/{jobTitleId}")
    @Operation(summary = "Xóa mềm chức danh")
    @PreAuthorize("@perm.has('org.manage')")
    public ApiResponse<Void> deleteJobTitle(
            @Parameter(description = "ID chức danh", required = true)
            @PathVariable UUID jobTitleId) {
        jobTitleService.deleteJobTitle(jobTitleId);
        return ApiResponse.ok(null);
    }
}

