package com.cyclosa.talent;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.talent.dto.request.AddTalentPoolRequest;
import com.cyclosa.talent.dto.response.TalentPoolResponse;
import com.cyclosa.talent.entity.InternalTalentPool;
import com.cyclosa.talent.exception.TalentErrorCode;
import com.cyclosa.talent.mapper.TalentMapper;
import com.cyclosa.talent.repository.InternalTalentPoolRepository;
import com.cyclosa.talent.service.InternalTalentPoolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalTalentPoolServiceTest {

    @Mock
    private InternalTalentPoolRepository talentPoolRepository;

    @Mock
    private EmployeeService employeeService;

    @Spy
    private TalentMapper talentMapper = Mappers.getMapper(TalentMapper.class);

    @InjectMocks
    private InternalTalentPoolService talentPoolService;

    private UUID companyId;
    private UUID employeeId;
    private UUID addedByEmpId;
    private InternalTalentPool sampleTalent;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        addedByEmpId = UUID.randomUUID();

        sampleTalent = InternalTalentPool.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .tag("HiPo")
                .note("Nhân sự tiềm năng cao")
                .addedByEmployeeId(addedByEmpId)
                .build();
        sampleTalent.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Đưa nhân sự vào kho nhân tài thành công")
    void addTalent_Success() {
        AddTalentPoolRequest request = AddTalentPoolRequest.builder()
                .employeeId(employeeId)
                .tag("HiPo")
                .note("Nhân sự tiềm năng cao")
                .build();

        when(employeeService.getEmployeeById(companyId, employeeId)).thenReturn(EmployeeDetailResponse.builder().id(employeeId).build());
        when(talentPoolRepository.existsByCompanyIdAndEmployeeIdAndTag(companyId, employeeId, "HiPo")).thenReturn(false);
        when(talentPoolRepository.save(any(InternalTalentPool.class))).thenReturn(sampleTalent);
        when(employeeService.getEmployeeSummaries(anySet())).thenReturn(Map.of(
                employeeId, EmployeeSummary.builder().id(employeeId).fullName("Trần Thị C").build(),
                addedByEmpId, EmployeeSummary.builder().id(addedByEmpId).fullName("Lê Quản Lý").build()
        ));

        TalentPoolResponse response = talentPoolService.addTalent(companyId, request, addedByEmpId);

        assertThat(response).isNotNull();
        assertThat(response.getEmployeeId()).isEqualTo(employeeId);
        assertThat(response.getTag()).isEqualTo("HiPo");
        assertThat(response.getEmployee().getFullName()).isEqualTo("Trần Thị C");
        assertThat(response.getAddedByEmployee().getFullName()).isEqualTo("Lê Quản Lý");
    }

    @Test
    @DisplayName("Thêm nhân sự thất bại nếu đã có trong kho với cùng thẻ tag")
    void addTalent_Duplicate_ThrowsException() {
        AddTalentPoolRequest request = AddTalentPoolRequest.builder()
                .employeeId(employeeId)
                .tag("HiPo")
                .build();

        when(employeeService.getEmployeeById(companyId, employeeId)).thenReturn(EmployeeDetailResponse.builder().id(employeeId).build());
        when(talentPoolRepository.existsByCompanyIdAndEmployeeIdAndTag(companyId, employeeId, "HiPo")).thenReturn(true);

        assertThatThrownBy(() -> talentPoolService.addTalent(companyId, request, addedByEmpId))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(TalentErrorCode.EMPLOYEE_ALREADY_IN_TALENT_POOL));
    }
}
