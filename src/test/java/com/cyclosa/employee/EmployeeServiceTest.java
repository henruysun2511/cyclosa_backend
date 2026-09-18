package com.cyclosa.employee;

import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.enums.UserStatus;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.employee.dto.request.*;
import com.cyclosa.employee.dto.response.EmployeeDependentResponse;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.entity.*;
import com.cyclosa.employee.enums.*;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.mapper.EmployeeMapper;
import com.cyclosa.employee.mapper.EmployeeMapperImpl;
import com.cyclosa.employee.repository.*;
import com.cyclosa.employee.service.impl.EmployeeServiceImpl;
import com.cyclosa.organization.entity.Branch;
import com.cyclosa.organization.entity.JobLevel;
import com.cyclosa.organization.entity.OrganizationalUnit;
import com.cyclosa.organization.entity.Position;
import com.cyclosa.organization.repository.BranchRepository;
import com.cyclosa.organization.repository.JobLevelRepository;
import com.cyclosa.organization.repository.OrganizationalUnitRepository;
import com.cyclosa.organization.repository.PositionRepository;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock EmployeeRepository employeeRepository;
    @Mock EmployeePersonalInfoRepository personalInfoRepository;
    @Mock EmployeeEmploymentInfoRepository employmentInfoRepository;
    @Mock EmployeeDependentRepository dependentRepository;
    @Mock EmployeeEmergencyContactRepository emergencyContactRepository;
    @Mock EmployeeHistoryRepository historyRepository;

    @Mock OrganizationalUnitRepository orgUnitRepository;
    @Mock BranchRepository branchRepository;
    @Mock PositionRepository positionRepository;
    @Mock JobLevelRepository jobLevelRepository;
    @Mock OrganizationalUnitService orgUnitService;
    @Mock CompanyService companyService;

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock SecurityPermissionEvaluator permEvaluator;

    @Spy EmployeeMapper employeeMapper = new EmployeeMapperImpl();

    @InjectMocks EmployeeServiceImpl employeeService;

    private UUID companyId;
    private UUID employeeId;
    private UUID unitId;
    private UUID branchId;
    private UUID positionId;
    private UUID jobLevelId;
    private UUID userId;

    private Employee employee;
    private EmployeePersonalInfo personalInfo;
    private EmployeeEmploymentInfo employmentInfo;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        unitId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        positionId = UUID.randomUUID();
        jobLevelId = UUID.randomUUID();
        userId = UUID.randomUUID();

        employee = Employee.builder()
                .employeeCode("EMP-2026-0001")
                .companyId(companyId)
                .userId(userId)
                .fullName("Nguyễn Văn A")
                .hireDate(LocalDate.of(2026, 1, 15))
                .employmentStatus(EmploymentStatus.PROBATION)
                .build();
        employee.setId(employeeId);

        personalInfo = EmployeePersonalInfo.builder()
                .employee(employee)
                .nationalIdNumber("001200000001")
                .phone("0987654321")
                .personalEmail("vana@gmail.com")
                .bankAccountNumber("19030000000001")
                .bankName("Techcombank")
                .build();
        personalInfo.setId(UUID.randomUUID());
        employee.setPersonalInfo(personalInfo);

        employmentInfo = EmployeeEmploymentInfo.builder()
                .employee(employee)
                .organizationalUnitId(unitId)
                .branchId(branchId)
                .positionId(positionId)
                .jobLevelId(jobLevelId)
                .employmentType(EmploymentType.FULL_TIME)
                .companyEmail("vana@cyclosa.com")
                .build();
        employmentInfo.setId(UUID.randomUUID());
        employee.setEmploymentInfo(employmentInfo);
    }

    @Test
    @DisplayName("Tiếp nhận nhân sự mới thành công (gắn 3 chiều tổ chức, tự sinh mã NV)")
    void createEmployee_Success() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setCompanyId(companyId);
        request.setFullName("Trần Thị B");
        request.setHireDate(LocalDate.now());
        request.setNationalIdNumber("001200000002");
        request.setOrganizationalUnitId(unitId);
        request.setBranchId(branchId);
        request.setPositionId(positionId);
        request.setJobLevelId(jobLevelId);

        given(personalInfoRepository.existsByNationalIdNumber("001200000002")).willReturn(false);
        given(orgUnitRepository.existsById(unitId)).willReturn(true);
        given(branchRepository.existsById(branchId)).willReturn(true);
        given(positionRepository.existsById(positionId)).willReturn(true);
        given(jobLevelRepository.existsById(jobLevelId)).willReturn(true);
        given(employeeRepository.countByCompanyId(companyId)).willReturn(5L);

        given(employeeRepository.save(any(Employee.class))).willAnswer(inv -> {
            Employee e = inv.getArgument(0);
            if (e.getId() == null) e.setId(UUID.randomUUID());
            return e;
        });
        given(personalInfoRepository.save(any(EmployeePersonalInfo.class))).willAnswer(inv -> inv.getArgument(0));
        given(employmentInfoRepository.save(any(EmployeeEmploymentInfo.class))).willAnswer(inv -> inv.getArgument(0));

        EmployeeDetailResponse response = employeeService.createEmployee(companyId, request);

        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("Trần Thị B");
        assertThat(response.getEmployeeCode()).isEqualTo("EMP-2026-0006");
        assertThat(response.getEmploymentStatus()).isEqualTo(EmploymentStatus.PROBATION);
        verify(historyRepository).save(any(EmployeeHistory.class));
    }

    @Test
    @DisplayName("Tiếp nhận nhân viên thất bại khi trùng số CCCD")
    void createEmployee_Throws_WhenDuplicateNationalId() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setCompanyId(companyId);
        request.setFullName("Lê Văn C");
        request.setHireDate(LocalDate.now());
        request.setNationalIdNumber("001200000001"); // đã tồn tại

        given(personalInfoRepository.existsByNationalIdNumber("001200000001")).willReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(companyId, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(EmployeeErrorCode.NATIONAL_ID_EXISTS));
    }

    @Test
    @DisplayName("Điều chuyển công tác thành công và ghi nhận lịch sử biến động")
    void updateEmploymentInfo_RecordsHistory() {
        UUID newUnitId = UUID.randomUUID();
        UUID newPosId = UUID.randomUUID();

        UpdateEmploymentInfoRequest request = new UpdateEmploymentInfoRequest();
        request.setOrganizationalUnitId(newUnitId);
        request.setBranchId(branchId);
        request.setPositionId(newPosId);
        request.setJobLevelId(jobLevelId);
        request.setChangeReason("Bổ nhiệm Trưởng nhóm mới");

        given(employeeRepository.findByIdAndCompanyId(employeeId, companyId)).willReturn(Optional.of(employee));
        given(permEvaluator.getDataScope("employee.view")).willReturn(Optional.of(DataScope.COMPANY));
        given(orgUnitRepository.existsById(newUnitId)).willReturn(true);
        given(branchRepository.existsById(branchId)).willReturn(true);
        given(positionRepository.existsById(newPosId)).willReturn(true);
        given(jobLevelRepository.existsById(jobLevelId)).willReturn(true);

        EmployeeDetailResponse response = employeeService.updateEmploymentInfo(companyId, employeeId, request);

        assertThat(response).isNotNull();
        assertThat(employmentInfo.getOrganizationalUnitId()).isEqualTo(newUnitId);
        assertThat(employmentInfo.getPositionId()).isEqualTo(newPosId);
        // Kiểm tra đã lưu ít nhất 2 biến động (Department + Position)
        verify(historyRepository, org.mockito.Mockito.atLeast(2)).save(any(EmployeeHistory.class));
    }

    @Test
    @DisplayName("Chuyển trạng thái sang TERMINATED tự động khóa tài khoản User liên kết")
    void changeStatus_TerminatesEmployee_AndLocksUser() {
        User linkedUser = User.builder()
                .email("vana@cyclosa.com")
                .status(UserStatus.ACTIVE)
                .build();
        linkedUser.setId(userId);

        ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest();
        request.setStatus(EmploymentStatus.TERMINATED);
        request.setReason("Hết hạn HĐLĐ và không tái ký");

        given(employeeRepository.findByIdAndCompanyId(employeeId, companyId)).willReturn(Optional.of(employee));
        given(permEvaluator.getDataScope("employee.view")).willReturn(Optional.of(DataScope.COMPANY));
        given(employeeRepository.save(any(Employee.class))).willAnswer(inv -> inv.getArgument(0));
        given(userRepository.findById(userId)).willReturn(Optional.of(linkedUser));

        EmployeeDetailResponse response = employeeService.changeStatus(companyId, employeeId, request);

        assertThat(response).isNotNull();
        assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.TERMINATED);
        assertThat(linkedUser.getStatus()).isEqualTo(UserStatus.LOCKED);
        verify(userRepository).save(linkedUser);
        verify(historyRepository).save(any(EmployeeHistory.class));
    }

    @Test
    @DisplayName("Thêm người phụ thuộc giảm trừ gia cảnh thành công")
    void createDependent_Success() {
        CreateDependentRequest request = new CreateDependentRequest();
        request.setFullName("Nguyễn Văn Con");
        request.setRelationship(FamilyRelationship.CHILD);
        request.setDateOfBirth(LocalDate.of(2020, 5, 1));
        request.setTaxDeductionRegistered(true);

        given(employeeRepository.findByIdAndCompanyId(employeeId, companyId)).willReturn(Optional.of(employee));
        given(permEvaluator.getDataScope("employee.view")).willReturn(Optional.of(DataScope.COMPANY));
        given(dependentRepository.save(any(EmployeeDependent.class))).willAnswer(inv -> {
            EmployeeDependent dep = inv.getArgument(0);
            dep.setId(UUID.randomUUID());
            return dep;
        });

        EmployeeDependentResponse response = employeeService.createDependent(companyId, employeeId, request);

        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("Nguyễn Văn Con");
        assertThat(response.getRelationship()).isEqualTo(FamilyRelationship.CHILD);
        assertThat(response.isTaxDeductionRegistered()).isTrue();
    }
}
