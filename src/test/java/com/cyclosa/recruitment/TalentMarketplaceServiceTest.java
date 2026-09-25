package com.cyclosa.recruitment;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.notification.service.NotificationService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.recruitment.dto.request.*;
import com.cyclosa.recruitment.dto.response.*;
import com.cyclosa.recruitment.entity.*;
import com.cyclosa.recruitment.enums.*;
import com.cyclosa.recruitment.exception.RecruitmentErrorCode;
import com.cyclosa.recruitment.repository.*;
import com.cyclosa.recruitment.service.TalentMarketplaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TalentMarketplaceServiceTest {

    @Mock
    private InternalOpportunityRepository opportunityRepository;
    @Mock
    private InternalApplicationRepository applicationRepository;
    @Mock
    private InternalAssignmentRepository assignmentRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private OrganizationalUnitService orgUnitService;
    @Mock
    private NotificationService notificationService;

    private TalentMarketplaceService marketplaceService;

    private final UUID companyId = UUID.randomUUID();
    private final UUID employeeId = UUID.randomUUID();
    private final UUID opportunityId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        marketplaceService = new TalentMarketplaceService(
                opportunityRepository,
                applicationRepository,
                assignmentRepository,
                employeeService,
                orgUnitService,
                notificationService
        );
    }

    @Test
    @DisplayName("Tạo cơ hội nội bộ mới thành công")
    void testCreateOpportunity_Success() {
        CreateOpportunityRequest request = CreateOpportunityRequest.builder()
                .title("Tech Lead cho dự án Fintech")
                .type(OpportunityType.PROJECT)
                .commitmentPercentage(50)
                .startDate(LocalDate.now().plusDays(7))
                .endDate(LocalDate.now().plusMonths(6))
                .description("Cần Lead dẫn dắt team 5 dev phát triển cổng thanh toán")
                .requiredSkills("Java, Spring Boot, Microservices, Kubernetes")
                .build();

        InternalOpportunity saved = InternalOpportunity.builder()
                .companyId(companyId)
                .title(request.getTitle())
                .type(request.getType())
                .commitmentPercentage(request.getCommitmentPercentage())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .requiredSkills(request.getRequiredSkills())
                .status(OpportunityStatus.OPEN)
                .build();
        saved.setId(opportunityId);

        when(opportunityRepository.save(any(InternalOpportunity.class))).thenReturn(saved);

        InternalOpportunityResponse res = marketplaceService.createOpportunity(companyId, request);

        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(opportunityId);
        assertThat(res.getTitle()).isEqualTo("Tech Lead cho dự án Fintech");
        assertThat(res.getStatus()).isEqualTo(OpportunityStatus.OPEN);
        assertThat(res.getCommitmentPercentage()).isEqualTo(50);
        verify(opportunityRepository).save(any(InternalOpportunity.class));
    }

    @Test
    @DisplayName("Nhân viên bày tỏ quan tâm vào cơ hội nội bộ thành công")
    void testExpressInterest_Success() {
        InternalOpportunity opportunity = InternalOpportunity.builder()
                .companyId(companyId)
                .title("Senior Cloud Architect")
                .type(OpportunityType.FULL_TIME_POSITION)
                .status(OpportunityStatus.OPEN)
                .build();
        opportunity.setId(opportunityId);

        when(opportunityRepository.findByIdAndCompanyId(opportunityId, companyId)).thenReturn(Optional.of(opportunity));
        when(applicationRepository.findByOpportunityIdAndEmployeeId(opportunityId, employeeId)).thenReturn(Optional.empty());

        InternalApplication savedApp = InternalApplication.builder()
                .companyId(companyId)
                .opportunityId(opportunityId)
                .employeeId(employeeId)
                .note("Tôi có 5 năm kinh nghiệm AWS và Terraform")
                .status(InternalApplicationStatus.APPLIED)
                .build();
        savedApp.setId(UUID.randomUUID());

        when(applicationRepository.save(any(InternalApplication.class))).thenReturn(savedApp);

        ExpressInterestRequest req = new ExpressInterestRequest();
        req.setNote("Tôi có 5 năm kinh nghiệm AWS và Terraform");

        InternalApplicationResponse res = marketplaceService.expressInterest(companyId, opportunityId, employeeId, req);

        assertThat(res).isNotNull();
        assertThat(res.getOpportunityTitle()).isEqualTo("Senior Cloud Architect");
        assertThat(res.getNote()).isEqualTo("Tôi có 5 năm kinh nghiệm AWS và Terraform");
        verify(applicationRepository).save(any(InternalApplication.class));
    }

    @Test
    @DisplayName("Bày tỏ quan tâm thất bại khi đã nộp đơn trước đó")
    void testExpressInterest_AlreadyApplied() {
        InternalOpportunity opportunity = InternalOpportunity.builder()
                .companyId(companyId)
                .title("Senior Cloud Architect")
                .type(OpportunityType.FULL_TIME_POSITION)
                .status(OpportunityStatus.OPEN)
                .build();
        opportunity.setId(opportunityId);

        InternalApplication existingApp = InternalApplication.builder()
                .companyId(companyId)
                .opportunityId(opportunityId)
                .employeeId(employeeId)
                .build();

        when(opportunityRepository.findByIdAndCompanyId(opportunityId, companyId)).thenReturn(Optional.of(opportunity));
        when(applicationRepository.findByOpportunityIdAndEmployeeId(opportunityId, employeeId))
                .thenReturn(Optional.of(existingApp));

        ExpressInterestRequest req = new ExpressInterestRequest();

        assertThatThrownBy(() -> marketplaceService.expressInterest(companyId, opportunityId, employeeId, req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(RecruitmentErrorCode.INTERNAL_APPLICATION_EXISTS.getMessage());
    }

    @Test
    @DisplayName("Tạo phân công nhiệm vụ biệt phái (Assignment) thành công")
    void testCreateAssignment_Success() {
        InternalOpportunity opportunity = InternalOpportunity.builder()
                .companyId(companyId)
                .title("Dự án AI Chatbot")
                .type(OpportunityType.PROJECT)
                .status(OpportunityStatus.OPEN)
                .build();
        opportunity.setId(opportunityId);

        CreateAssignmentRequest req = CreateAssignmentRequest.builder()
                .opportunityId(opportunityId)
                .employeeId(employeeId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .build();

        when(opportunityRepository.findByIdAndCompanyId(opportunityId, companyId)).thenReturn(Optional.of(opportunity));

        InternalAssignment savedAssignment = InternalAssignment.builder()
                .companyId(companyId)
                .opportunityId(opportunityId)
                .employeeId(employeeId)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(AssignmentStatus.ACTIVE)
                .build();
        savedAssignment.setId(UUID.randomUUID());

        when(assignmentRepository.save(any(InternalAssignment.class))).thenReturn(savedAssignment);
        when(assignmentRepository.countByCompanyIdAndStatus(companyId, AssignmentStatus.ACTIVE)).thenReturn(1L);

        InternalAssignmentResponse res = marketplaceService.createAssignment(companyId, req);

        assertThat(res).isNotNull();
        assertThat(res.getOpportunityId()).isEqualTo(opportunityId);
        assertThat(res.getEmployeeId()).isEqualTo(employeeId);
        assertThat(res.getStatus()).isEqualTo(AssignmentStatus.ACTIVE);
        verify(opportunityRepository).save(opportunity);
        verify(assignmentRepository).save(any(InternalAssignment.class));
    }

    @Test
    @DisplayName("Đánh giá và hoàn thành nhiệm vụ phân công nội bộ")
    void testCompleteAssignment_Success() {
        UUID assignmentId = UUID.randomUUID();
        InternalAssignment assignment = InternalAssignment.builder()
                .companyId(companyId)
                .opportunityId(opportunityId)
                .employeeId(employeeId)
                .status(AssignmentStatus.ACTIVE)
                .build();
        assignment.setId(assignmentId);

        CompleteAssignmentRequest req = new CompleteAssignmentRequest();
        req.setPerformanceRating(new BigDecimal("4.8"));
        req.setEvaluationNote("Hoàn thành xuất sắc nhiệm vụ được giao");

        when(assignmentRepository.findByIdAndCompanyId(assignmentId, companyId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(InternalAssignment.class))).thenReturn(assignment);
        when(opportunityRepository.findById(opportunityId)).thenReturn(Optional.of(
                InternalOpportunity.builder().companyId(companyId).title("Dự án AI Chatbot").build()
        ));

        InternalAssignmentResponse res = marketplaceService.completeAssignment(companyId, assignmentId, req);

        assertThat(res).isNotNull();
        assertThat(res.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
        assertThat(res.getPerformanceRating()).isEqualTo(new BigDecimal("4.8"));
        assertThat(res.getEvaluationNote()).isEqualTo("Hoàn thành xuất sắc nhiệm vụ được giao");
        verify(assignmentRepository).save(assignment);
    }
}
