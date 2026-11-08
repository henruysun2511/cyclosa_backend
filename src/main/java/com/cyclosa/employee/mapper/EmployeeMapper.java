package com.cyclosa.employee.mapper;

import com.cyclosa.employee.dto.response.*;
import com.cyclosa.employee.entity.*;
import com.cyclosa.employee.entity.enums.EmployeeStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmployeeMapper {

    public EmployeeSummaryResponse toSummaryResponse(
            Employee employee,
            EmployeeWorkAssignment currentAssignment,
            EmployeeStatus currentStatus) {

        EmployeeSummaryResponse.EmployeeSummaryResponseBuilder builder = EmployeeSummaryResponse.builder()
                .id(employee.getId())
                .companyId(employee.getCompanyId())
                .userId(employee.getUserId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .lastName(employee.getLastName())
                .firstName(employee.getFirstName())
                .dateOfBirth(employee.getDateOfBirth())
                .gender(employee.getGender())
                .phone(employee.getPhone())
                .personalEmail(employee.getPersonalEmail())
                .startDate(employee.getStartDate())
                .probationEndDate(employee.getProbationEndDate())
                .portraitUrl(employee.getPortraitUrl())
                .currentStatus(currentStatus)
                .createdAt(employee.getCreatedAt());

        if (currentAssignment != null) {
            if (currentAssignment.getDepartment() != null) {
                builder.currentDepartmentId(currentAssignment.getDepartment().getId());
                if (!currentAssignment.getDepartment().getVersions().isEmpty()) {
                    builder.currentDepartmentName(currentAssignment.getDepartment().getVersions().get(0).getName());
                } else {
                    builder.currentDepartmentName(currentAssignment.getDepartment().getCode());
                }
            }

            if (currentAssignment.getBranch() != null) {
                builder.currentBranchId(currentAssignment.getBranch().getId());
                builder.currentBranchName(currentAssignment.getBranch().getName());
            }

            if (currentAssignment.getJobTitle() != null) {
                builder.currentJobTitleId(currentAssignment.getJobTitle().getId());
                builder.currentJobTitleName(currentAssignment.getJobTitle().getName());
            }

            if (currentAssignment.getJobLevel() != null) {
                builder.currentJobLevelId(currentAssignment.getJobLevel().getId());
                builder.currentJobLevelName(currentAssignment.getJobLevel().getName());
            }

            if (currentAssignment.getManager() != null) {
                builder.currentManagerId(currentAssignment.getManager().getId());
                builder.currentManagerName(currentAssignment.getManager().getFullName());
            }

            builder.currentEmploymentType(currentAssignment.getEmploymentType());
        }

        return builder.build();
    }

    public EmployeeDetailResponse toDetailResponse(
            Employee employee,
            EmployeeWorkAssignment currentAssignment,
            EmployeeStatus currentStatus,
            List<EmployeeIdentityDocument> identities,
            EmployeeBankAccount bankAccount,
            List<EmployeeDependent> dependents,
            List<EmployeeEmergencyContact> emergencyContacts,
            List<EmployeeWorkAssignment> workAssignments,
            List<EmployeeStatusHistory> statusHistory,
            List<ProbationEvaluation> probationEvaluations,
            FaceEnrollment faceEnrollment) {

        return EmployeeDetailResponse.builder()
                .id(employee.getId())
                .companyId(employee.getCompanyId())
                .userId(employee.getUserId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .lastName(employee.getLastName())
                .firstName(employee.getFirstName())
                .dateOfBirth(employee.getDateOfBirth())
                .gender(employee.getGender())
                .phone(employee.getPhone())
                .personalEmail(employee.getPersonalEmail())
                .address(employee.getAddress())
                .startDate(employee.getStartDate())
                .probationEndDate(employee.getProbationEndDate())
                .personalTaxCode(employee.getPersonalTaxCode())
                .socialInsuranceNumber(employee.getSocialInsuranceNumber())
                .portraitUrl(employee.getPortraitUrl())
                .sourceApplicationId(employee.getSourceApplicationId())
                .currentAssignment(currentAssignment != null ? toWorkAssignmentResponse(currentAssignment) : null)
                .currentStatus(currentStatus)
                .bankAccount(bankAccount != null ? toBankAccountResponse(bankAccount) : null)
                .identityDocuments(identities != null ? identities.stream().map(this::toIdentityDocumentResponse).collect(Collectors.toList()) : Collections.emptyList())
                .dependents(dependents != null ? dependents.stream().map(this::toDependentResponse).collect(Collectors.toList()) : Collections.emptyList())
                .emergencyContacts(emergencyContacts != null ? emergencyContacts.stream().map(this::toEmergencyContactResponse).collect(Collectors.toList()) : Collections.emptyList())
                .workAssignments(workAssignments != null ? workAssignments.stream().map(this::toWorkAssignmentResponse).collect(Collectors.toList()) : Collections.emptyList())
                .statusHistory(statusHistory != null ? statusHistory.stream().map(this::toStatusHistoryResponse).collect(Collectors.toList()) : Collections.emptyList())
                .probationEvaluations(probationEvaluations != null ? probationEvaluations.stream().map(this::toProbationEvaluationResponse).collect(Collectors.toList()) : Collections.emptyList())
                .faceEnrollment(faceEnrollment != null ? toFaceEnrollmentResponse(faceEnrollment) : null)
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    public WorkAssignmentResponse toWorkAssignmentResponse(EmployeeWorkAssignment assignment) {
        if (assignment == null) return null;

        String deptName = "";
        String deptCode = "";
        if (assignment.getDepartment() != null) {
            deptCode = assignment.getDepartment().getCode();
            if (!assignment.getDepartment().getVersions().isEmpty()) {
                deptName = assignment.getDepartment().getVersions().get(0).getName();
            } else {
                deptName = deptCode;
            }
        }

        return WorkAssignmentResponse.builder()
                .id(assignment.getId())
                .employeeId(assignment.getEmployee().getId())
                .effectiveFrom(assignment.getEffectiveFrom())
                .departmentId(assignment.getDepartment() != null ? assignment.getDepartment().getId() : null)
                .departmentName(deptName)
                .departmentCode(deptCode)
                .branchId(assignment.getBranch() != null ? assignment.getBranch().getId() : null)
                .branchName(assignment.getBranch() != null ? assignment.getBranch().getName() : null)
                .branchCode(assignment.getBranch() != null ? assignment.getBranch().getCode() : null)
                .jobTitleId(assignment.getJobTitle() != null ? assignment.getJobTitle().getId() : null)
                .jobTitleName(assignment.getJobTitle() != null ? assignment.getJobTitle().getName() : null)
                .jobTitleCode(assignment.getJobTitle() != null ? assignment.getJobTitle().getCode() : null)
                .jobLevelId(assignment.getJobLevel() != null ? assignment.getJobLevel().getId() : null)
                .jobLevelName(assignment.getJobLevel() != null ? assignment.getJobLevel().getName() : null)
                .jobLevelRank(assignment.getJobLevel() != null ? assignment.getJobLevel().getRankOrder() : null)
                .managerEmployeeId(assignment.getManager() != null ? assignment.getManager().getId() : null)
                .managerEmployeeName(assignment.getManager() != null ? assignment.getManager().getFullName() : null)
                .employmentType(assignment.getEmploymentType())
                .changeReason(assignment.getChangeReason())
                .transferRequestId(assignment.getTransferRequestId())
                .createdAt(assignment.getCreatedAt())
                .build();
    }

    public StatusHistoryResponse toStatusHistoryResponse(EmployeeStatusHistory history) {
        if (history == null) return null;
        return StatusHistoryResponse.builder()
                .id(history.getId())
                .employeeId(history.getEmployee().getId())
                .status(history.getStatus())
                .effectiveFrom(history.getEffectiveFrom())
                .note(history.getNote())
                .createdAt(history.getCreatedAt())
                .build();
    }

    public IdentityDocumentResponse toIdentityDocumentResponse(EmployeeIdentityDocument doc) {
        if (doc == null) return null;
        return IdentityDocumentResponse.builder()
                .id(doc.getId())
                .employeeId(doc.getEmployee().getId())
                .documentType(doc.getDocumentType())
                .documentNumber(doc.getDocumentNumber())
                .issueDate(doc.getIssueDate())
                .issuePlace(doc.getIssuePlace())
                .expiryDate(doc.getExpiryDate())
                .build();
    }

    public BankAccountResponse toBankAccountResponse(EmployeeBankAccount account) {
        if (account == null) return null;
        return BankAccountResponse.builder()
                .id(account.getId())
                .employeeId(account.getEmployee().getId())
                .bankName(account.getBankName())
                .bankBranchName(account.getBankBranchName())
                .accountNumber(account.getAccountNumber())
                .build();
    }

    public DependentResponse toDependentResponse(EmployeeDependent dependent) {
        if (dependent == null) return null;
        return DependentResponse.builder()
                .id(dependent.getId())
                .employeeId(dependent.getEmployee().getId())
                .fullName(dependent.getFullName())
                .dateOfBirth(dependent.getDateOfBirth())
                .relationship(dependent.getRelationship())
                .identityNumber(dependent.getIdentityNumber())
                .taxDeductionFrom(dependent.getTaxDeductionFrom())
                .taxDeductionTo(dependent.getTaxDeductionTo())
                .build();
    }

    public EmergencyContactResponse toEmergencyContactResponse(EmployeeEmergencyContact contact) {
        if (contact == null) return null;
        return EmergencyContactResponse.builder()
                .id(contact.getId())
                .employeeId(contact.getEmployee().getId())
                .fullName(contact.getFullName())
                .relationship(contact.getRelationship())
                .phone(contact.getPhone())
                .address(contact.getAddress())
                .build();
    }

    public ProbationEvaluationResponse toProbationEvaluationResponse(ProbationEvaluation eval) {
        if (eval == null) return null;
        return ProbationEvaluationResponse.builder()
                .id(eval.getId())
                .employeeId(eval.getEmployee().getId())
                .result(eval.getResult())
                .evaluationDate(eval.getEvaluationDate())
                .evaluatedByEmployeeId(eval.getEvaluatedBy() != null ? eval.getEvaluatedBy().getId() : null)
                .evaluatedByEmployeeName(eval.getEvaluatedBy() != null ? eval.getEvaluatedBy().getFullName() : null)
                .comment(eval.getComment())
                .createdAt(eval.getCreatedAt())
                .build();
    }

    public FaceEnrollmentResponse toFaceEnrollmentResponse(FaceEnrollment enrollment) {
        if (enrollment == null) return null;
        return FaceEnrollmentResponse.builder()
                .id(enrollment.getId())
                .employeeId(enrollment.getEmployee().getId())
                .templateRef(enrollment.getTemplateRef())
                .qualityScore(enrollment.getQualityScore())
                .livenessVerified(enrollment.getLivenessVerified())
                .consentGivenAt(enrollment.getConsentGivenAt())
                .enrolledAt(enrollment.getEnrolledAt())
                .revokedAt(enrollment.getRevokedAt())
                .revokeReason(enrollment.getRevokeReason())
                .build();
    }
}

