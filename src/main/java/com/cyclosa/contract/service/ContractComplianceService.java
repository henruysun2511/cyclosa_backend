package com.cyclosa.contract.service;

import com.cyclosa.contract.dto.ContractDto;
import com.cyclosa.contract.entity.Contract;
import com.cyclosa.contract.entity.enums.ContractType;
import com.cyclosa.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractComplianceService {

    private final ContractRepository contractRepository;

    /**
     * Tự động kiểm tra tuân thủ các quy định của Bộ luật Lao động 2019
     */
    public ContractDto.ComplianceCheckResponse checkCompliance(Contract contract) {
        List<String> violations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 1. Kiểm tra ngày hiệu lực
        if (contract.getContractType() == ContractType.INDEFINITE) {
            if (contract.getEndDate() != null) {
                violations.add("Hợp đồng không xác định thời hạn tuyệt đối không được có ngày kết thúc (Điều 20 BLLĐ 2019).");
            }
        } else {
            if (contract.getEndDate() == null) {
                violations.add("Hợp đồng xác định thời hạn hoặc thử việc bắt buộc phải có ngày kết thúc hợp đồng.");
            } else if (contract.getEndDate().isBefore(contract.getStartDate()) || contract.getEndDate().isEqual(contract.getStartDate())) {
                violations.add("Ngày kết thúc hợp đồng phải sau ngày bắt đầu có hiệu lực.");
            } else {
                long months = ChronoUnit.MONTHS.between(contract.getStartDate(), contract.getEndDate());
                if (contract.getContractType() == ContractType.FIXED_TERM && months > 36) {
                    violations.add("Hợp đồng xác định thời hạn không được vượt quá 36 tháng (Khoản 1 Điều 20 BLLĐ 2019). Thời hạn hiện tại: " + months + " tháng.");
                }
                if (contract.getContractType() == ContractType.PROBATION && months > 6) {
                    warnings.add("Thời gian thử việc không được vượt quá 180 ngày đối với quản trị viên doanh nghiệp hoặc 60 ngày đối với lao động chuyên môn kỹ thuật (Điều 25 BLLĐ 2019).");
                }
            }
        }

        // 2. Kiểm tra giới hạn số lần ký hợp đồng xác định thời hạn (Khoản 2 Điều 20 BLLĐ 2019)
        if (contract.getContractType() == ContractType.FIXED_TERM) {
            long existingFixedTermCount = contractRepository.countFixedTermContractsByEmployee(contract.getEmployeeId());
            if (existingFixedTermCount >= 2 && contract.getPreviousContractId() != null) {
                violations.add("Người lao động đã ký tối đa 02 lần hợp đồng xác định thời hạn. Theo Khoản 2 Điều 20 BLLĐ 2019, nếu tiếp tục làm việc bắt buộc phải ký Hợp đồng không xác định thời hạn (INDEFINITE).");
            } else if (existingFixedTermCount == 1) {
                warnings.add("Đây là lần ký hợp đồng xác định thời hạn thứ 2 (lần cuối cùng). Hợp đồng tiếp theo bắt buộc phải là hợp đồng không xác định thời hạn.");
            }
        }

        // 3. Kiểm tra lương thử việc tối thiểu 85% lương chính thức (Điều 26 BLLĐ 2019)
        if (contract.getContractType() == ContractType.PROBATION) {
            if (contract.getBaseSalary() == null || contract.getBaseSalary().compareTo(BigDecimal.ZERO) <= 0) {
                violations.add("Tiền lương thử việc không được để trống hoặc bằng 0.");
            }
        }

        // 4. Kiểm tra kỳ trả lương
        if (contract.getPayDayOfMonth() < 1 || contract.getPayDayOfMonth() > 31) {
            violations.add("Ngày trả lương cố định hàng tháng phải từ ngày 1 đến ngày 31.");
        }

        return ContractDto.ComplianceCheckResponse.builder()
                .compliant(violations.isEmpty())
                .violations(violations)
                .warnings(warnings)
                .build();
    }
}

