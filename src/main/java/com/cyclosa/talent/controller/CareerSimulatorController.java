package com.cyclosa.talent.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.talent.dto.request.CareerSimulationSaveRequest;
import com.cyclosa.talent.dto.response.*;
import com.cyclosa.talent.service.CareerSimulatorService;
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
@RequestMapping("/api/v1/career-simulator")
@RequiredArgsConstructor
@Tag(name = "Career Simulator", description = "Mô phỏng lộ trình thăng tiến sự nghiệp và phân tích xu hướng thăng tiến")
public class CareerSimulatorController {

    private final CareerSimulatorService careerSimulatorService;

    @GetMapping("/employee/{id}/suggested-paths")
    @PreAuthorize("@perm.has('career_simulation.view')")
    @RequirePermission("career_simulation.view")
    @Operation(summary = "Gợi ý các lộ trình thăng tiến cho một nhân viên dựa trên phân tích hồ sơ tương tự")
    public ResponseEntity<ApiResponse<List<CareerSimulatorSuggestionResponse>>> getSuggestedPaths(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                careerSimulatorService.getSuggestedPaths(effectiveCompanyId, id),
                "Phân tích và gợi ý lộ trình thành công"));
    }

    @GetMapping("/employee/{id}/similar-profiles")
    @PreAuthorize("@perm.has('career_simulation.view')")
    @RequirePermission("career_simulation.view")
    @Operation(summary = "Tìm danh sách các nhân sự có xuất phát điểm và đặc điểm hồ sơ tương đồng")
    public ResponseEntity<ApiResponse<List<SimilarProfileResponse>>> getSimilarProfiles(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                careerSimulatorService.getSimilarProfiles(effectiveCompanyId, id),
                "Lấy danh sách hồ sơ tương đồng thành công"));
    }

    @PostMapping("/employee/{id}/save-path")
    @PreAuthorize("@perm.has('career_simulation.manage')")
    @RequirePermission("career_simulation.manage")
    @Operation(summary = "Lưu lại kịch bản lộ trình mô phỏng sự nghiệp cho nhân viên")
    public ResponseEntity<ApiResponse<CareerSimulationSavedPathResponse>> savePathForEmployee(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id,
            @Valid @RequestBody CareerSimulationSaveRequest request
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        CareerSimulationSavedPathResponse response = careerSimulatorService.savePath(effectiveCompanyId, id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Lưu kịch bản lộ trình thành công"));
    }

    @GetMapping("/department/{id}/trends")
    @PreAuthorize("@perm.has('career_simulation.view')")
    @RequirePermission("career_simulation.view")
    @Operation(summary = "Xem thống kê xu hướng thăng tiến và luân chuyển theo phòng ban")
    public ResponseEntity<ApiResponse<DepartmentCareerTrendResponse>> getDepartmentTrends(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        return ResponseEntity.ok(ApiResponse.ok(
                careerSimulatorService.getDepartmentTrends(effectiveCompanyId, id),
                "Lấy thống kê xu hướng phòng ban thành công"));
    }
}
