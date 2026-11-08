package com.cyclosa.employee.dto.response;

import com.cyclosa.employee.entity.enums.EmployeeStatus;
import com.cyclosa.employee.entity.enums.EmploymentType;
import com.cyclosa.employee.entity.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailResponse {

    private UUID id;
    private UUID companyId;
    private UUID userId;
    private String employeeCode;
    private String fullName;
    private String lastName;
    private String firstName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phone;
    private String personalEmail;
    private String address;
    private LocalDate startDate;
    private LocalDate probationEndDate;
    private String personalTaxCode;
    private String socialInsuranceNumber;
    private String portraitUrl;
    private UUID sourceApplicationId;

    // Phân công công tác hiện hành
    private WorkAssignmentResponse currentAssignment;

    // Trạng thái hiện hành
    private EmployeeStatus currentStatus;

    // Tài khoản ngân hàng
    private BankAccountResponse bankAccount;

    // Danh sách giấy tờ định danh
    @Builder.Default
    private List<IdentityDocumentResponse> identityDocuments = new ArrayList<>();

    // Người phụ thuộc
    @Builder.Default
    private List<DependentResponse> dependents = new ArrayList<>();

    // Người liên hệ khẩn cấp
    @Builder.Default
    private List<EmergencyContactResponse> emergencyContacts = new ArrayList<>();

    // Lịch sử công tác
    @Builder.Default
    private List<WorkAssignmentResponse> workAssignments = new ArrayList<>();

    // Lịch sử trạng thái
    @Builder.Default
    private List<StatusHistoryResponse> statusHistory = new ArrayList<>();

    // Đánh giá thử việc
    @Builder.Default
    private List<ProbationEvaluationResponse> probationEvaluations = new ArrayList<>();

    // Sinh trắc khuôn mặt
    private FaceEnrollmentResponse faceEnrollment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

