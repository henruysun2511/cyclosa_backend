package com.cyclosa.employee.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.dto.request.CreateWorkAssignmentRequest;
import com.cyclosa.employee.dto.response.WorkAssignmentResponse;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.entity.EmployeeWorkAssignment;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.mapper.EmployeeMapper;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.employee.repository.EmployeeWorkAssignmentRepository;
import com.cyclosa.organization.entity.Branch;
import com.cyclosa.organization.entity.Department;
import com.cyclosa.organization.entity.JobLevel;
import com.cyclosa.organization.entity.JobTitle;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.repository.BranchRepository;
import com.cyclosa.organization.repository.DepartmentRepository;
import com.cyclosa.organization.repository.JobLevelRepository;
import com.cyclosa.organization.repository.JobTitleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeAssignmentService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkAssignmentRepository workAssignmentRepository;
    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final JobTitleRepository jobTitleRepository;
    private final JobLevelRepository jobLevelRepository;
    private final EmployeeMapper employeeMapper;

    public List<WorkAssignmentResponse> getWorkAssignments(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }
        return workAssignmentRepository.findAllByEmployeeIdOrderByEffectiveFromDesc(employeeId)
                .stream()
                .map(employeeMapper::toWorkAssignmentResponse)
                .collect(Collectors.toList());
    }

    public WorkAssignmentResponse getWorkAssignmentAt(UUID employeeId, LocalDate date) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }
        LocalDate targetDate = date != null ? date : LocalDate.now();
        EmployeeWorkAssignment assignment = workAssignmentRepository
                .findFirstByEmployeeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(employeeId, targetDate)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.NO_VALID_WORK_ASSIGNMENT));
        return employeeMapper.toWorkAssignmentResponse(assignment);
    }

    @Transactional
    public WorkAssignmentResponse createWorkAssignment(UUID employeeId, CreateWorkAssignmentRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        // Kiểm tra không được tự quản lý chính mình
        if (request.getManagerEmployeeId() != null && request.getManagerEmployeeId().equals(employeeId)) {
            throw new AppException(EmployeeErrorCode.SELF_MANAGEMENT_NOT_ALLOWED);
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new AppException(OrganizationErrorCode.DEPARTMENT_NOT_FOUND));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new AppException(OrganizationErrorCode.BRANCH_NOT_FOUND));

        JobTitle jobTitle = jobTitleRepository.findById(request.getJobTitleId())
                .orElseThrow(() -> new AppException(OrganizationErrorCode.JOB_TITLE_NOT_FOUND));

        JobLevel jobLevel = jobLevelRepository.findById(request.getJobLevelId())
                .orElseThrow(() -> new AppException(OrganizationErrorCode.JOB_LEVEL_NOT_FOUND));

        Employee manager = null;
        if (request.getManagerEmployeeId() != null) {
            manager = employeeRepository.findById(request.getManagerEmployeeId())
                    .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
        }

        // Kiểm tra ngày hiệu lực mới phải lớn hơn ngày hiệu lực gần nhất
        Optional<EmployeeWorkAssignment> latestOpt = workAssignmentRepository
                .findFirstByEmployeeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(employeeId, LocalDate.now().plusYears(10));
        if (latestOpt.isPresent()) {
            if (request.getEffectiveFrom().isBefore(latestOpt.get().getEffectiveFrom())) {
                throw new AppException(EmployeeErrorCode.EFFECTIVE_DATE_INVALID);
            }
        }

        EmployeeWorkAssignment assignment = EmployeeWorkAssignment.builder()
                .employee(employee)
                .effectiveFrom(request.getEffectiveFrom())
                .department(department)
                .branch(branch)
                .jobTitle(jobTitle)
                .jobLevel(jobLevel)
                .manager(manager)
                .employmentType(request.getEmploymentType())
                .changeReason(request.getChangeReason())
                .transferRequestId(request.getTransferRequestId())
                .build();

        EmployeeWorkAssignment saved = workAssignmentRepository.save(assignment);
        return employeeMapper.toWorkAssignmentResponse(saved);
    }

    public List<WorkAssignmentResponse> getDirectReports(UUID managerId) {
        if (!employeeRepository.existsById(managerId)) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }
        LocalDate today = LocalDate.now();
        return workAssignmentRepository.findCurrentDirectReports(managerId, today)
                .stream()
                .map(employeeMapper::toWorkAssignmentResponse)
                .collect(Collectors.toList());
    }
}

