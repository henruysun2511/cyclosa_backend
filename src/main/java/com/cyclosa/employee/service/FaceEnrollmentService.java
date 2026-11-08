package com.cyclosa.employee.service;

import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.dto.request.EnrollFaceRequest;
import com.cyclosa.employee.dto.response.FaceEnrollmentResponse;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.entity.FaceEnrollment;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.mapper.EmployeeMapper;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.employee.repository.FaceEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaceEnrollmentService {

    private final EmployeeRepository employeeRepository;
    private final FaceEnrollmentRepository faceEnrollmentRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;

    public FaceEnrollmentResponse getFaceEnrollment(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }
        FaceEnrollment enrollment = faceEnrollmentRepository.findByEmployeeIdAndRevokedAtIsNull(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.FACE_ENROLLMENT_NOT_FOUND));
        return employeeMapper.toFaceEnrollmentResponse(enrollment);
    }

    @Transactional
    public FaceEnrollmentResponse enrollFace(UUID employeeId, EnrollFaceRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        if (Boolean.FALSE.equals(request.getConsentGiven())) {
            throw new AppException(EmployeeErrorCode.FACE_CONSENT_REQUIRED);
        }

        if (request.getQualityScore() != null && request.getQualityScore().compareTo(new BigDecimal("70.00")) < 0) {
            throw new AppException(EmployeeErrorCode.FACE_QUALITY_TOO_LOW);
        }

        if (Boolean.FALSE.equals(request.getLivenessVerified())) {
            throw new AppException(EmployeeErrorCode.FACE_LIVENESS_FAILED);
        }

        // Thu hồi mẫu cũ nếu có
        Optional<FaceEnrollment> oldOpt = faceEnrollmentRepository.findByEmployeeIdAndRevokedAtIsNull(employeeId);
        oldOpt.ifPresent(old -> {
            old.setRevokedAt(LocalDateTime.now());
            old.setRevokeReason("Cập nhật mẫu sinh trắc mới");
            faceEnrollmentRepository.save(old);
        });

        LocalDateTime now = LocalDateTime.now();
        FaceEnrollment enrollment = FaceEnrollment.builder()
                .employee(employee)
                .templateRef(request.getTemplateRef().trim())
                .qualityScore(request.getQualityScore() != null ? request.getQualityScore() : new BigDecimal("85.00"))
                .livenessVerified(Boolean.TRUE.equals(request.getLivenessVerified()))
                .consentGivenAt(now)
                .enrolledAt(now)
                .build();

        FaceEnrollment saved = faceEnrollmentRepository.save(enrollment);
        return employeeMapper.toFaceEnrollmentResponse(saved);
    }

    @Transactional
    public void revokeFaceEnrollment(UUID enrollmentId, String reason) {
        FaceEnrollment enrollment = faceEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.FACE_ENROLLMENT_NOT_FOUND));

        enrollment.setRevokedAt(LocalDateTime.now());
        enrollment.setRevokeReason(reason != null ? reason : "Thu hồi theo yêu cầu quản trị viên");
        faceEnrollmentRepository.save(enrollment);
    }

    public FaceEnrollmentResponse getMyFaceEnrollment() {
        Employee currentEmployee = getCurrentAuthenticatedEmployee();
        return getFaceEnrollment(currentEmployee.getId());
    }

    @Transactional
    public FaceEnrollmentResponse enrollMyFace(EnrollFaceRequest request) {
        Employee currentEmployee = getCurrentAuthenticatedEmployee();
        return enrollFace(currentEmployee.getId(), request);
    }

    @Transactional
    public void revokeMyFaceEnrollment() {
        Employee currentEmployee = getCurrentAuthenticatedEmployee();
        FaceEnrollment enrollment = faceEnrollmentRepository.findByEmployeeIdAndRevokedAtIsNull(currentEmployee.getId())
                .orElseThrow(() -> new AppException(EmployeeErrorCode.FACE_ENROLLMENT_NOT_FOUND));

        enrollment.setRevokedAt(LocalDateTime.now());
        enrollment.setRevokeReason("EMPLOYEE_REQUEST");
        faceEnrollmentRepository.save(enrollment);
    }

    private Employee getCurrentAuthenticatedEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }
        String principal = auth.getName();
        User user = userRepository.findByEmailAndDeletedFalse(principal)
                .or(() -> userRepository.findByUsernameAndDeletedFalse(principal))
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        return employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
    }
}

