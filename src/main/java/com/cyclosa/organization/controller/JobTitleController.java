package com.cyclosa.organization.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateJobTitleRequest;
import com.cyclosa.organization.dto.request.JobTitleFilterRequest;
import com.cyclosa.organization.dto.request.UpdateJobTitleRequest;
import com.cyclosa.organization.dto.response.JobTitleResponse;
import com.cyclosa.organization.service.JobTitleService;
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
@RequestMapping("/api/v1/job-titles")
@RequiredArgsConstructor
@Tag(name = "Job Titles", description = "Quản lý danh mục chức danh vị trí công việc")
public class JobTitleController {

    private final JobTitleService jobTitleService;

    @GetMapping
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy danh sách chức danh theo công ty có phân trang", description = "Hỗ trợ lọc từ khóa và phân trang")
    public ResponseEntity<ApiResponse<PageData<JobTitleResponse>>> getJobTitles(@ParameterObject @Valid JobTitleFilterRequest filter) {
        PageData<JobTitleResponse> result = jobTitleService.getJobTitles(filter);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách chức danh thành công"));
    }

    @GetMapping("/all")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy toàn bộ chức danh theo công ty (cho dropdown)")
    public ResponseEntity<ApiResponse<List<JobTitleResponse>>> getAllJobTitles(
            @Parameter(description = "ID công ty", required = true)
            @RequestParam UUID companyId) {
        List<JobTitleResponse> result = jobTitleService.getAllJobTitlesByCompany(companyId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách chức danh thành công"));
    }

    @GetMapping("/{jobTitleId}")
    @PreAuthorize("@perm.has('org.view')")
    @RequirePermission("org.view")
    @Operation(summary = "Lấy thông tin chi tiết một chức danh theo ID")
    public ResponseEntity<ApiResponse<JobTitleResponse>> getJobTitleById(
            @Parameter(description = "ID chức danh", required = true)
            @PathVariable UUID jobTitleId) {
        JobTitleResponse result = jobTitleService.getJobTitleById(jobTitleId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy thông tin chức danh thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Tạo mới chức danh công việc")
    public ResponseEntity<ApiResponse<JobTitleResponse>> createJobTitle(@Valid @RequestBody CreateJobTitleRequest request) {
        JobTitleResponse result = jobTitleService.createJobTitle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Tạo mới chức danh thành công"));
    }

    @PutMapping("/{jobTitleId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Cập nhật thông tin chức danh")
    public ResponseEntity<ApiResponse<JobTitleResponse>> updateJobTitle(
            @Parameter(description = "ID chức danh", required = true)
            @PathVariable UUID jobTitleId,
            @Valid @RequestBody UpdateJobTitleRequest request) {
        JobTitleResponse result = jobTitleService.updateJobTitle(jobTitleId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật thông tin chức danh thành công"));
    }

    @DeleteMapping("/{jobTitleId}")
    @PreAuthorize("@perm.has('org.manage')")
    @RequirePermission("org.manage")
    @Operation(summary = "Xóa mềm chức danh")
    public ResponseEntity<ApiResponse<Void>> deleteJobTitle(
            @Parameter(description = "ID chức danh", required = true)
            @PathVariable UUID jobTitleId) {
        jobTitleService.deleteJobTitle(jobTitleId);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa chức danh thành công"));
    }
}
