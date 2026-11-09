package com.cyclosa.workflow.service;

import com.cyclosa.authorization.repository.UserRoleAssignmentRepository;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.entity.EmployeeWorkAssignment;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.organization.entity.Department;
import com.cyclosa.organization.repository.DepartmentRepository;
import com.cyclosa.workflow.entity.WorkflowDelegation;
import com.cyclosa.workflow.enums.ApproverType;
import com.cyclosa.workflow.enums.RequestType;
import com.cyclosa.workflow.repository.WorkflowDelegationRepository;
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
public class ApproverResolverService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final WorkflowDelegationRepository workflowDelegationRepository;

    @Transactional(readOnly = true)
    public UUID resolveApprover(
            ApproverType approverType,
            UUID approverRoleId,
            UUID approverEmployeeId,
            UUID requesterEmployeeId,
            UUID companyId,
            RequestType requestType
    ) {
        UUID initialAssignee = null;

        switch (approverType) {
            case SPECIFIC_EMPLOYEE:
                initialAssignee = approverEmployeeId;
                break;

            case DIRECT_MANAGER:
                if (requesterEmployeeId != null) {
                    Employee requester = employeeRepository.findById(requesterEmployeeId).orElse(null);
                    if (requester != null && requester.getWorkAssignments() != null) {
                        for (EmployeeWorkAssignment wa : requester.getWorkAssignments()) {
                            if (wa.getManager() != null) {
                                initialAssignee = wa.getManager().getId();
                                break;
                            }
                        }
                        if (initialAssignee == null && !requester.getWorkAssignments().isEmpty()) {
                            Department dept = requester.getWorkAssignments().get(0).getDepartment();
                            if (dept != null && dept.getVersions() != null && !dept.getVersions().isEmpty()) {
                                initialAssignee = dept.getVersions().get(0).getManagerEmployeeId();
                            }
                        }
                    }
                }
                break;

            case DEPARTMENT_HEAD:
                if (requesterEmployeeId != null) {
                    Employee requester = employeeRepository.findById(requesterEmployeeId).orElse(null);
                    if (requester != null && requester.getWorkAssignments() != null && !requester.getWorkAssignments().isEmpty()) {
                        Department dept = requester.getWorkAssignments().get(0).getDepartment();
                        if (dept != null && dept.getVersions() != null && !dept.getVersions().isEmpty()) {
                            initialAssignee = dept.getVersions().get(0).getManagerEmployeeId();
                        }
                    }
                }
                break;

            case ROLE:
                if (approverRoleId != null) {
                    List<UUID> userIds = userRoleAssignmentRepository.findUserIdsByRoleId(approverRoleId);
                    for (UUID uid : userIds) {
                        Optional<Employee> emp = employeeRepository.findByUserId(uid);
                        if (emp.isPresent()) {
                            initialAssignee = emp.get().getId();
                            break;
                        }
                    }
                }
                break;
        }

        // Fallback: If still null, pick first employee of the company or requester
        if (initialAssignee == null) {
            if (requesterEmployeeId != null) {
                initialAssignee = requesterEmployeeId;
            } else {
                List<Employee> emps = employeeRepository.findAll();
                if (!emps.isEmpty()) {
                    initialAssignee = emps.get(0).getId();
                }
            }
        }

        // Check active delegation
        if (initialAssignee != null) {
            LocalDate today = LocalDate.now();
            Optional<WorkflowDelegation> delegation = workflowDelegationRepository.findActiveDelegation(initialAssignee, requestType, today);
            if (delegation.isPresent()) {
                log.info("Thẩm quyền duyệt của nhân viên {} được ủy quyền cho {}", initialAssignee, delegation.get().getDelegateeEmployeeId());
                return delegation.get().getDelegateeEmployeeId();
            }
        }

        return initialAssignee;
    }
}

