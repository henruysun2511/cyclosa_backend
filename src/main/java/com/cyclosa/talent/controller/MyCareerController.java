package com.cyclosa.talent.controller;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.talent.dto.request.CareerSimulationSaveRequest;
import com.cyclosa.talent.dto.response.CareerPathResponse;
import com.cyclosa.talent.dto.response.CareerSimulationSavedPathResponse;
import com.cyclosa.talent.dto.response.CareerSimulatorSuggestionResponse;
import com.cyclosa.talent.dto.response.SimilarProfileResponse;
import com.cyclosa.talent.exception.TalentErrorCode;
import com.cyclosa.talent.service.CareerPathService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "My Career", description = "Cổng tự phục vụ nhân viên (ESS) về lộ trình thăng tiến và mô phỏng sự nghiệp")
public class MyCareerController {

    private final CareerPathService careerPathService;
    private final CareerSimulatorService careerSimulatorService;
    private final EmployeeService employeeService;

    @GetMapping("/my-career-path")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem lộ trình thăng tiến chuẩn từ vị trí hiện tại của bản thân (ESS)")
    public ResponseEntity<ApiResponse<List<CareerPathResponse>>> getMyCareerPath(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        UUID employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(ApiResponse.ok(
                careerPathService.getCareerPathsForEmployee(effectiveCompanyId, employeeId),
                "Lấy danh sách lộ trình thăng tiến cá nhân thành công"));
    }

    @GetMapping("/career-simulator/suggested-paths")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem gợi ý lộ trình thăng tiến và phân tích tương đồng cho bản thân (ESS)")
    public ResponseEntity<ApiResponse<List<CareerSimulatorSuggestionResponse>>> getMySuggestedPaths(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        UUID employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(ApiResponse.ok(
                careerSimulatorService.getSuggestedPaths(effectiveCompanyId, employeeId),
                "Phân tích và gợi ý lộ trình cá nhân thành công"));
    }

    @GetMapping("/career-simulator/similar-profiles")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem danh sách các nhân sự có xuất phát điểm tương đồng với bản thân (ESS)")
    public ResponseEntity<ApiResponse<List<SimilarProfileResponse>>> getMySimilarProfiles(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        UUID employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(ApiResponse.ok(
                careerSimulatorService.getSimilarProfiles(effectiveCompanyId, employeeId),
                "Lấy danh sách hồ sơ tương đồng thành công"));
    }

    @PostMapping("/career-simulator/save-path")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lưu kịch bản lộ trình mô phỏng yêu thích của bản thân (ESS)")
    public ResponseEntity<ApiResponse<CareerSimulationSavedPathResponse>> saveMyPath(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @Valid @RequestBody CareerSimulationSaveRequest request
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        UUID employeeId = resolveCurrentEmployeeId();
        CareerSimulationSavedPathResponse response = careerSimulatorService.savePath(effectiveCompanyId, employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Lưu kịch bản lộ trình cá nhân thành công"));
    }

    @GetMapping("/career-simulator/saved-paths")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem danh sách các kịch bản lộ trình đã lưu của bản thân (ESS)")
    public ResponseEntity<ApiResponse<List<CareerSimulationSavedPathResponse>>> getMySavedPaths(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        UUID employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(ApiResponse.ok(
                careerSimulatorService.getSavedPaths(effectiveCompanyId, employeeId),
                "Lấy danh sách kịch bản lộ trình đã lưu thành công"));
    }

    @DeleteMapping("/career-simulator/saved-paths/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa một kịch bản lộ trình đã lưu của bản thân (ESS)")
    public ResponseEntity<ApiResponse<Void>> deleteMySavedPath(
            @RequestParam(required = false) UUID companyId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID headerCompanyId,
            @PathVariable UUID id
    ) {
        UUID effectiveCompanyId = companyId != null ? companyId : headerCompanyId;
        UUID employeeId = resolveCurrentEmployeeId();
        careerSimulatorService.deleteSavedPath(effectiveCompanyId, employeeId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa kịch bản lộ trình thành công"));
    }

    private UUID resolveCurrentEmployeeId() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(TalentErrorCode.CURRENT_USER_NOT_LINKED_EMPLOYEE));
    }
}
