package com.cyclosa.contract.controller;

import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.contract.dto.ContractDto;
import com.cyclosa.contract.dto.ContractSigningDto;
import com.cyclosa.contract.exception.ContractErrorCode;
import com.cyclosa.contract.service.ContractService;
import com.cyclosa.contract.service.ContractSigningService;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Contract Self-Service", description = "Dịch vụ tự phục vụ hợp đồng & ký văn bản cho nhân viên")
public class ContractSelfServiceController {

    private final ContractService contractService;
    private final ContractSigningService signingService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/me/contracts")
    @Operation(summary = "Xem toàn bộ hợp đồng của chính tôi")
    public ResponseEntity<ApiResponse<List<ContractDto.ContractSummaryResponse>>> getMyContracts() {
        Employee employee = getCurrentEmployee();
        if (employee == null) {
            return ResponseEntity.ok(ApiResponse.ok(Collections.emptyList(), "Chưa có hồ sơ nhân sự liên kết"));
        }
        List<ContractDto.ContractSummaryResponse> result = contractService.getContractsByEmployee(employee.getId());
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách hợp đồng của tôi thành công"));
    }

    @GetMapping("/me/contract-signing-tasks")
    @Operation(summary = "Xem các văn bản hợp đồng đang chờ chính tôi ký điện tử")
    public ResponseEntity<ApiResponse<List<ContractSigningDto.ContractSignerResponse>>> getMyPendingSigningTasks() {
        Employee employee = getCurrentEmployee();
        if (employee == null) {
            return ResponseEntity.ok(ApiResponse.ok(Collections.emptyList(), "Chưa có hồ sơ nhân sự liên kết"));
        }
        List<ContractSigningDto.ContractSignerResponse> result = signingService.getMyPendingSigningTasks(employee.getId());
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách văn bản chờ ký thành công"));
    }

    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String principal = auth.getName();
        UUID userId = userRepository.findByEmailAndDeletedFalse(principal)
                .or(() -> userRepository.findByUsernameAndDeletedFalse(principal))
                .map(User::getId)
                .orElse(null);

        if (userId == null) return null;
        return employeeRepository.findByUserId(userId).orElse(null);
    }
}

