package com.cyclosa.workflow.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.entity.EmployeeEmploymentInfo;
import com.cyclosa.employee.repository.EmployeeEmploymentInfoRepository;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.organization.entity.OrganizationalUnit;
import com.cyclosa.organization.repository.OrganizationalUnitRepository;
import com.cyclosa.role.repository.UserRoleMappingRepository;
import com.cyclosa.workflow.entity.WorkflowDelegate;
import com.cyclosa.workflow.entity.WorkflowStep;
import com.cyclosa.workflow.exception.WorkflowErrorCode;
import com.cyclosa.workflow.repository.WorkflowDelegateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApproverResolverService {

    private final EmployeeEmploymentInfoRepository employmentInfoRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final WorkflowDelegateRepository delegateRepository;
    public UUID resolveAssignedApprover(WorkflowStep step, UUID requesterEmployeeId, UUID companyId) {
        if (step == null) {
            throw new AppException(WorkflowErrorCode.WORKFLOW_STEP_NOT_FOUND);
        }

        switch (step.getApproverType()) {
            case SPECIFIC_EMPLOYEE -> {
                if (step.getSpecificApproverEmployeeId() == null) {
                    throw new AppException(WorkflowErrorCode.APPROVER_NOT_RESOLVED,
                            "Bước quy trình yêu cầu nhân viên cụ thể nhưng chưa cấu hình");
                }
                return step.getSpecificApproverEmployeeId();
            }

            case DIRECT_MANAGER -> {
                Optional<EmployeeEmploymentInfo> empInfoOpt = employmentInfoRepository.findByEmployeeId(requesterEmployeeId);
                if (empInfoOpt.isPresent() && empInfoOpt.get().getManagerEmployeeId() != null) {
                    return empInfoOpt.get().getManagerEmployeeId();
                }

                // Fallback: Tìm trưởng đơn vị / phòng ban
                log.warn("Nhân viên {} chưa có quản lý trực tiếp, đang fallback lên Trưởng đơn vị", requesterEmployeeId);
                if (empInfoOpt.isPresent() && empInfoOpt.get().getOrganizationalUnitId() != null) {
                    Optional<OrganizationalUnit> unitOpt = organizationalUnitRepository.findById(empInfoOpt.get().getOrganizationalUnitId());
                    if (unitOpt.isPresent() && unitOpt.get().getManagerEmployeeId() != null) {
                        return unitOpt.get().getManagerEmployeeId();
                    }
                }
                throw new AppException(WorkflowErrorCode.APPROVER_NOT_RESOLVED,
                        "Không thể xác định quản lý trực tiếp hoặc trưởng đơn vị của nhân viên");
            }

            case ORGANIZATIONAL_UNIT_HEAD -> {
                Optional<EmployeeEmploymentInfo> empInfoOpt = employmentInfoRepository.findByEmployeeId(requesterEmployeeId);
                if (empInfoOpt.isEmpty() || empInfoOpt.get().getOrganizationalUnitId() == null) {
                    throw new AppException(WorkflowErrorCode.APPROVER_NOT_RESOLVED,
                            "Nhân viên chưa được gán đơn vị phòng ban nào");
                }

                Optional<OrganizationalUnit> unitOpt = organizationalUnitRepository.findById(empInfoOpt.get().getOrganizationalUnitId());
                if (unitOpt.isEmpty() || unitOpt.get().getManagerEmployeeId() == null) {
                    throw new AppException(WorkflowErrorCode.APPROVER_NOT_RESOLVED,
                            "Đơn vị phòng ban của nhân viên hiện chưa có Trưởng đơn vị");
                }
                return unitOpt.get().getManagerEmployeeId();
            }

            case SPECIFIC_ROLE -> {
                if (step.getSpecificRoleId() == null) {
                    throw new AppException(WorkflowErrorCode.APPROVER_NOT_RESOLVED,
                            "Bước quy trình chỉ định duyệt theo Role nhưng chưa cấu hình Role");
                }
                List<UUID> userIds = userRoleMappingRepository.findUserIdsByRoleIdAndCompanyId(step.getSpecificRoleId(), companyId);
                for (UUID userId : userIds) {
                    Optional<Employee> empOpt = employeeRepository.findByUserId(userId);
                    if (empOpt.isPresent()) {
                        return empOpt.get().getId();
                    }
                }
                throw new AppException(WorkflowErrorCode.APPROVER_NOT_RESOLVED,
                        "Không tìm thấy nhân viên nào trong công ty sở hữu vai trò yêu cầu");
            }

            default -> throw new AppException(WorkflowErrorCode.APPROVER_NOT_RESOLVED);
        }
    }
    public boolean isAuthorizedApprover(UUID assignedApproverId, UUID currentEmployeeId, LocalDate date) {
        if (assignedApproverId == null || currentEmployeeId == null) {
            return false;
        }

        // Chính chủ duyệt
        if (assignedApproverId.equals(currentEmployeeId)) {
            return true;
        }

        // Kiểm tra ủy quyền hợp lệ
        Optional<WorkflowDelegate> activeDelegation = delegateRepository.findActiveDelegation(assignedApproverId, date);
        return activeDelegation.isPresent() && currentEmployeeId.equals(activeDelegation.get().getDelegateEmployeeId());
    }
}
