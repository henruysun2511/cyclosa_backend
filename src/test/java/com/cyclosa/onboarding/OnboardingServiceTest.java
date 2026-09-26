package com.cyclosa.onboarding;

import com.cyclosa.asset.service.AssetService;
import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.onboarding.dto.request.*;
import com.cyclosa.onboarding.dto.response.*;
import com.cyclosa.onboarding.entity.*;
import com.cyclosa.onboarding.enums.*;
import com.cyclosa.onboarding.exception.OnboardingErrorCode;
import com.cyclosa.onboarding.mapper.OnboardingMapper;
import com.cyclosa.onboarding.repository.*;
import com.cyclosa.onboarding.service.OnboardingService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.organization.service.PositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private OnboardingChecklistTemplateRepository templateRepository;
    @Mock
    private OnboardingChecklistTemplateItemRepository templateItemRepository;
    @Mock
    private OnboardingProcessRepository processRepository;
    @Mock
    private OnboardingProcessItemRepository processItemRepository;
    @Mock
    private AccountProvisioningRepository accountRepository;
    @Mock
    private OrientationSessionRepository sessionRepository;
    @Mock
    private EmployeeDocumentRepository documentRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private OrganizationalUnitService unitService;
    @Mock
    private PositionService positionService;
    @Mock
    private AssetService assetService;
    @Mock
    private OnboardingMapper onboardingMapper;

    private OnboardingService onboardingService;

    private final UUID companyId = UUID.randomUUID();
    private final UUID employeeId = UUID.randomUUID();
    private final UUID templateId = UUID.randomUUID();
    private final UUID processId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        onboardingService = new OnboardingService(
                templateRepository,
                templateItemRepository,
                processRepository,
                processItemRepository,
                accountRepository,
                sessionRepository,
                documentRepository,
                employeeService,
                unitService,
                positionService,
                assetService,
                onboardingMapper
        );
    }

    @Test
    @DisplayName("Tạo mới Onboarding Checklist Template thành công")
    void createTemplate_success() {
        CreateChecklistTemplateRequest req = CreateChecklistTemplateRequest.builder()
                .name("Kỹ sư phần mềm Onboarding")
                .description("Checklist chào đón lập trình viên mới")
                .items(List.of(
                        CreateTemplateItemRequest.builder()
                                .title("Nộp bản sao CCCD")
                                .category(OnboardingItemCategory.DOCUMENT)
                                .isRequired(true)
                                .build()
                ))
                .build();

        OnboardingChecklistTemplate entity = OnboardingChecklistTemplate.builder()
                .name(req.getName())
                .description(req.getDescription())
                .build();
        entity.setId(templateId);

        OnboardingChecklistTemplateItem itemEntity = OnboardingChecklistTemplateItem.builder()
                .title("Nộp bản sao CCCD")
                .category(OnboardingItemCategory.DOCUMENT)
                .isRequired(true)
                .build();

        when(onboardingMapper.toEntity(any(CreateChecklistTemplateRequest.class))).thenReturn(entity);
        when(onboardingMapper.toEntity(any(CreateTemplateItemRequest.class))).thenReturn(itemEntity);
        when(templateRepository.save(any(OnboardingChecklistTemplate.class))).thenReturn(entity);
        when(onboardingMapper.toDetailResponse(any(OnboardingChecklistTemplate.class))).thenReturn(
                ChecklistTemplateDetailResponse.builder().id(templateId).name(req.getName()).build()
        );

        ChecklistTemplateDetailResponse res = onboardingService.createTemplate(companyId, req);

        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(templateId);
        verify(templateRepository).save(any(OnboardingChecklistTemplate.class));
    }

    @Test
    @DisplayName("Khởi tạo quy trình Onboarding thành công và tự động bung các items từ mẫu template")
    void createProcess_success_clonesTemplateItems() {
        CreateOnboardingProcessRequest req = CreateOnboardingProcessRequest.builder()
                .employeeId(employeeId)
                .checklistTemplateId(templateId)
                .startDate(LocalDate.now())
                .build();

        EmployeeDetailResponse employee = EmployeeDetailResponse.builder()
                .id(employeeId)
                .company(CompanySummary.builder().id(companyId).build())
                .fullName("Nguyễn Văn A")
                .employeeCode("EMP-001")
                .employmentStatus(EmploymentStatus.PROBATION)
                .build();

        OnboardingChecklistTemplate template = OnboardingChecklistTemplate.builder()
                .name("Standard Checklist")
                .build();
        template.setId(templateId);

        OnboardingChecklistTemplateItem item1 = OnboardingChecklistTemplateItem.builder()
                .title("Nộp CCCD")
                .category(OnboardingItemCategory.DOCUMENT)
                .isRequired(true)
                .orderIndex(1)
                .build();
        item1.setId(UUID.randomUUID());

        OnboardingChecklistTemplateItem item2 = OnboardingChecklistTemplateItem.builder()
                .title("Cấp email nội bộ")
                .category(OnboardingItemCategory.ACCOUNT)
                .isRequired(true)
                .orderIndex(2)
                .build();
        item2.setId(UUID.randomUUID());

        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(employee);
        when(processRepository.existsByEmployeeIdAndStatus(employeeId, OnboardingStatus.IN_PROGRESS)).thenReturn(false);
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(templateItemRepository.findByTemplateIdOrderByOrderIndexAsc(templateId)).thenReturn(List.of(item1, item2));

        OnboardingProcess savedProcess = OnboardingProcess.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .checklistTemplateId(templateId)
                .status(OnboardingStatus.IN_PROGRESS)
                .startDate(LocalDate.now())
                .build();
        savedProcess.setId(processId);

        when(processRepository.save(any(OnboardingProcess.class))).thenReturn(savedProcess);
        when(onboardingMapper.toProcessDetailResponse(any(OnboardingProcess.class))).thenReturn(
                OnboardingProcessDetailResponse.builder()
                        .id(processId)
                        .employeeId(employeeId)
                        .status(OnboardingStatus.IN_PROGRESS)
                        .build()
        );
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(processItemRepository.findByOnboardingProcessIdOrderByOrderIndexAsc(processId)).thenReturn(List.of());

        OnboardingProcessDetailResponse res = onboardingService.createProcess(companyId, req);

        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(processId);
        verify(processRepository).save(any(OnboardingProcess.class));
    }

    @Test
    @DisplayName("Khởi tạo quy trình Onboarding thất bại nếu nhân viên đang có tiến trình IN_PROGRESS")
    void createProcess_fails_whenAlreadyInProgress() {
        CreateOnboardingProcessRequest req = CreateOnboardingProcessRequest.builder()
                .employeeId(employeeId)
                .checklistTemplateId(templateId)
                .build();

        EmployeeDetailResponse employee = EmployeeDetailResponse.builder()
                .id(employeeId)
                .company(CompanySummary.builder().id(companyId).build())
                .fullName("Nguyễn Văn A")
                .build();

        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(employee);
        when(processRepository.existsByEmployeeIdAndStatus(employeeId, OnboardingStatus.IN_PROGRESS)).thenReturn(true);

        assertThatThrownBy(() -> onboardingService.createProcess(companyId, req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("đang diễn ra");
    }

    @Test
    @DisplayName("Hoàn thành một Process Item danh mục thường (DOCUMENT) thành công")
    void completeProcessItem_success() {
        UUID itemId = UUID.randomUUID();
        OnboardingProcess process = OnboardingProcess.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .status(OnboardingStatus.IN_PROGRESS)
                .build();
        process.setId(processId);

        OnboardingProcessItem item = OnboardingProcessItem.builder()
                .onboardingProcess(process)
                .category(OnboardingItemCategory.DOCUMENT)
                .status(OnboardingItemStatus.PENDING)
                .title("Nộp giấy tờ")
                .build();
        item.setId(itemId);

        when(processItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(processItemRepository.save(any(OnboardingProcessItem.class))).thenReturn(item);
        when(onboardingMapper.toProcessItemResponse(any(OnboardingProcessItem.class))).thenReturn(
                OnboardingProcessItemResponse.builder()
                        .id(itemId)
                        .status(OnboardingItemStatus.COMPLETED)
                        .build()
        );

        OnboardingProcessItemResponse res = onboardingService.completeProcessItem(companyId, itemId, null, UUID.randomUUID());

        assertThat(res).isNotNull();
        assertThat(item.getStatus()).isEqualTo(OnboardingItemStatus.COMPLETED);
        assertThat(item.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Hoàn thành Process Item loại ACCOUNT thất bại nếu chưa có tài khoản nào được PROVISIONED")
    void completeProcessItem_accountValidation_failsIfNoProvisionedAccount() {
        UUID itemId = UUID.randomUUID();
        OnboardingProcess process = OnboardingProcess.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .status(OnboardingStatus.IN_PROGRESS)
                .build();
        process.setId(processId);

        OnboardingProcessItem item = OnboardingProcessItem.builder()
                .onboardingProcess(process)
                .category(OnboardingItemCategory.ACCOUNT)
                .status(OnboardingItemStatus.PENDING)
                .title("Cấp tài khoản Email")
                .build();
        item.setId(itemId);

        when(processItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(accountRepository.existsByOnboardingProcessIdAndStatus(processId, ProvisioningStatus.PROVISIONED)).thenReturn(false);
        when(accountRepository.existsByEmployeeIdAndStatus(employeeId, ProvisioningStatus.PROVISIONED)).thenReturn(false);

        assertThatThrownBy(() -> onboardingService.completeProcessItem(companyId, itemId, null, UUID.randomUUID()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Cần cấp phát tài khoản");
    }

    @Test
    @DisplayName("Hoàn tất quy trình Onboarding thất bại nếu còn hạng mục bắt buộc chưa hoàn thành")
    void completeProcess_fails_whenRequiredItemsIncomplete() {
        OnboardingProcess process = OnboardingProcess.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .status(OnboardingStatus.IN_PROGRESS)
                .build();
        process.setId(processId);

        when(processRepository.findByIdAndCompanyId(processId, companyId)).thenReturn(Optional.of(process));
        when(processItemRepository.countIncompleteRequiredItems(processId)).thenReturn(2L);

        assertThatThrownBy(() -> onboardingService.completeProcess(companyId, processId, UUID.randomUUID()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Các hạng mục bắt buộc chưa hoàn thành");
    }

    @Test
    @DisplayName("Hoàn tất 100% Onboarding thành công và tự động kích hoạt nhân viên sang ACTIVE")
    void completeProcess_success_activatesEmployee() {
        OnboardingProcess process = OnboardingProcess.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .status(OnboardingStatus.IN_PROGRESS)
                .build();
        process.setId(processId);

        Employee employee = Employee.builder()
                .companyId(companyId)
                .employmentStatus(EmploymentStatus.PROBATION)
                .fullName("Trần Thị B")
                .build();
        employee.setId(employeeId);

        when(processRepository.findByIdAndCompanyId(processId, companyId)).thenReturn(Optional.of(process));
        when(processItemRepository.countIncompleteRequiredItems(processId)).thenReturn(0L);
        when(processRepository.save(any(OnboardingProcess.class))).thenReturn(process);
        when(onboardingMapper.toProcessDetailResponse(any(OnboardingProcess.class))).thenReturn(
                OnboardingProcessDetailResponse.builder()
                        .id(processId)
                        .employeeId(employeeId)
                        .status(OnboardingStatus.COMPLETED)
                        .build()
        );
        when(processItemRepository.findByOnboardingProcessIdOrderByOrderIndexAsc(processId)).thenReturn(List.of());

        OnboardingProcessDetailResponse res = onboardingService.completeProcess(companyId, processId, UUID.randomUUID());

        assertThat(res).isNotNull();
        assertThat(process.getStatus()).isEqualTo(OnboardingStatus.COMPLETED);
        verify(employeeService).updateEmploymentStatus(employeeId, EmploymentStatus.ACTIVE);
    }

    @Test
    @DisplayName("Tải lên và xác thực tài liệu hồ sơ nhân sự (Employee Document)")
    void uploadAndVerifyDocument_success() {
        UUID docId = UUID.randomUUID();
        UploadEmployeeDocumentRequest req = UploadEmployeeDocumentRequest.builder()
                .documentName("CCCD 2 mặt")
                .documentType(EmployeeDocumentType.ID_CARD)
                .fileUrl("https://storage.cyclosa.com/docs/cccd.pdf")
                .fileSize(102400L)
                .build();

        EmployeeDetailResponse employee = EmployeeDetailResponse.builder()
                .id(employeeId)
                .company(CompanySummary.builder().id(companyId).build())
                .fullName("Lê Văn C")
                .build();

        EmployeeDocument document = EmployeeDocument.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .documentName(req.getDocumentName())
                .documentType(req.getDocumentType())
                .fileUrl(req.getFileUrl())
                .isVerified(false)
                .build();
        document.setId(docId);

        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(employee);
        when(documentRepository.save(any(EmployeeDocument.class))).thenReturn(document);
        when(onboardingMapper.toDocumentResponse(any(EmployeeDocument.class))).thenReturn(
                EmployeeDocumentResponse.builder()
                        .id(docId)
                        .documentName(req.getDocumentName())
                        .isVerified(false)
                        .build()
        );

        EmployeeDocumentResponse uploadRes = onboardingService.uploadDocument(companyId, employeeId, req, UUID.randomUUID());
        assertThat(uploadRes).isNotNull();
        assertThat(uploadRes.getId()).isEqualTo(docId);

        // Verify document
        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));
        when(onboardingMapper.toDocumentResponse(document)).thenReturn(
                EmployeeDocumentResponse.builder()
                        .id(docId)
                        .isVerified(true)
                        .build()
        );

        EmployeeDocumentResponse verifyRes = onboardingService.verifyDocument(companyId, docId, UUID.randomUUID());
        assertThat(verifyRes).isNotNull();
        assertThat(document.getIsVerified()).isTrue();
        assertThat(document.getVerifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("Tạo yêu cầu cấp phát tài khoản và cập nhật trạng thái PROVISIONED")
    void accountProvisioning_lifecycle_success() {
        UUID accountId = UUID.randomUUID();
        CreateAccountProvisioningRequest req = CreateAccountProvisioningRequest.builder()
                .systemName("Google Workspace")
                .accountUsername("c.le@cyclosa.com")
                .build();

        OnboardingProcess process = OnboardingProcess.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .status(OnboardingStatus.IN_PROGRESS)
                .build();
        process.setId(processId);

        AccountProvisioning account = AccountProvisioning.builder()
                .companyId(companyId)
                .onboardingProcessId(processId)
                .employeeId(employeeId)
                .systemName(req.getSystemName())
                .accountUsername(req.getAccountUsername())
                .status(ProvisioningStatus.PENDING)
                .build();
        account.setId(accountId);

        when(processRepository.findByIdAndCompanyId(processId, companyId)).thenReturn(Optional.of(process));
        when(accountRepository.save(any(AccountProvisioning.class))).thenReturn(account);
        when(onboardingMapper.toAccountResponse(any(AccountProvisioning.class))).thenReturn(
                AccountProvisioningResponse.builder().id(accountId).status(ProvisioningStatus.PENDING).build()
        );

        AccountProvisioningResponse createRes = onboardingService.createAccountProvisioning(companyId, processId, req, UUID.randomUUID());
        assertThat(createRes).isNotNull();
        assertThat(createRes.getId()).isEqualTo(accountId);

        // Update status to PROVISIONED
        UpdateAccountProvisioningRequest updateReq = UpdateAccountProvisioningRequest.builder()
                .status(ProvisioningStatus.PROVISIONED)
                .notes("Đã tạo hòm thư và cấp mật khẩu tạm")
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(onboardingMapper.toAccountResponse(account)).thenReturn(
                AccountProvisioningResponse.builder().id(accountId).status(ProvisioningStatus.PROVISIONED).build()
        );

        AccountProvisioningResponse updateRes = onboardingService.updateAccountProvisioningStatus(companyId, accountId, updateReq, UUID.randomUUID());
        assertThat(updateRes).isNotNull();
        assertThat(account.getStatus()).isEqualTo(ProvisioningStatus.PROVISIONED);
        assertThat(account.getProvisionedAt()).isNotNull();
    }

    @Test
    @DisplayName("Lên lịch buổi đào tạo hội nhập thành công")
    void createOrientationSession_success() {
        UUID sessionId = UUID.randomUUID();
        CreateOrientationSessionRequest req = CreateOrientationSessionRequest.builder()
                .sessionName("Chào đón nhân viên mới & Văn hóa doanh nghiệp")
                .location("Phòng họp VIP Tầng 3")
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .build();

        OnboardingProcess process = OnboardingProcess.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .status(OnboardingStatus.IN_PROGRESS)
                .build();
        process.setId(processId);

        OrientationSession session = OrientationSession.builder()
                .companyId(companyId)
                .onboardingProcessId(processId)
                .sessionName(req.getSessionName())
                .location(req.getLocation())
                .scheduledAt(req.getScheduledAt())
                .status(SessionStatus.SCHEDULED)
                .build();
        session.setId(sessionId);

        when(processRepository.findByIdAndCompanyId(processId, companyId)).thenReturn(Optional.of(process));
        when(sessionRepository.save(any(OrientationSession.class))).thenReturn(session);
        when(onboardingMapper.toSessionResponse(any(OrientationSession.class))).thenReturn(
                OrientationSessionResponse.builder()
                        .id(sessionId)
                        .sessionName(req.getSessionName())
                        .status(SessionStatus.SCHEDULED)
                        .build()
        );

        OrientationSessionResponse res = onboardingService.createOrientationSession(companyId, processId, req);

        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(sessionId);
        verify(sessionRepository).save(any(OrientationSession.class));
    }
}
