package com.cyclosa.talent;

import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.organization.service.PositionService;
import com.cyclosa.talent.dto.request.CareerSimulationSaveRequest;
import com.cyclosa.talent.dto.response.CareerSimulationSavedPathResponse;
import com.cyclosa.talent.dto.response.CareerSimulatorSuggestionResponse;
import com.cyclosa.talent.entity.CareerPath;
import com.cyclosa.talent.entity.CareerSimulationSavedPath;
import com.cyclosa.talent.exception.TalentErrorCode;
import com.cyclosa.talent.mapper.TalentMapper;
import com.cyclosa.talent.repository.CareerPathRepository;
import com.cyclosa.talent.repository.CareerSimulationSavedPathRepository;
import com.cyclosa.talent.service.CareerSimulatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareerSimulatorServiceTest {

    @Mock
    private CareerPathRepository careerPathRepository;

    @Mock
    private CareerSimulationSavedPathRepository savedPathRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private PositionService positionService;

    @Mock
    private OrganizationalUnitService orgUnitService;

    @Spy
    private TalentMapper talentMapper = Mappers.getMapper(TalentMapper.class);

    @InjectMocks
    private CareerSimulatorService careerSimulatorService;

    private UUID companyId;
    private UUID employeeId;
    private UUID currentPosId;
    private UUID targetPosId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        currentPosId = UUID.randomUUID();
        targetPosId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Gợi ý lộ trình thăng tiến cho nhân viên thành công")
    void getSuggestedPaths_Success() {
        EmployeeDetailResponse emp = EmployeeDetailResponse.builder()
                .id(employeeId)
                .position(PositionSummary.builder().id(currentPosId).name("Mid Developer").build())
                .hireDate(LocalDate.now().minusYears(2))
                .build();

        CareerPath path = CareerPath.builder()
                .companyId(companyId)
                .fromPositionId(currentPosId)
                .toPositionId(targetPosId)
                .minYearsRequired(BigDecimal.valueOf(2.0))
                .description("Lên Senior Developer")
                .build();
        path.setId(UUID.randomUUID());

        when(employeeService.getEmployeeById(companyId, employeeId)).thenReturn(emp);
        when(careerPathRepository.findByCompanyIdAndFromPositionId(companyId, currentPosId)).thenReturn(List.of(path));
        when(positionService.getPositionSummaries(Set.of(targetPosId))).thenReturn(Map.of(
                targetPosId, PositionSummary.builder().id(targetPosId).name("Senior Developer").build()
        ));
        when(positionService.getPositionSummary(currentPosId)).thenReturn(
                PositionSummary.builder().id(currentPosId).name("Mid Developer").build()
        );

        List<CareerSimulatorSuggestionResponse> suggestions = careerSimulatorService.getSuggestedPaths(companyId, employeeId);

        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions.get(0).getTargetPosition().getName()).isEqualTo("Senior Developer");
        assertThat(suggestions.get(0).getMatchPercentage()).isGreaterThan(70.0);
    }

    @Test
    @DisplayName("Lưu kịch bản mô phỏng lộ trình thành công")
    void savePath_Success() {
        CareerSimulationSaveRequest request = CareerSimulationSaveRequest.builder()
                .targetPositionId(targetPosId)
                .suggestedPath("{\"targetPosition\":\"Senior Dev\", \"years\": 2}")
                .build();

        CareerSimulationSavedPath savedEntity = CareerSimulationSavedPath.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .targetPositionId(targetPosId)
                .suggestedPath(request.getSuggestedPath())
                .savedAt(LocalDateTime.now())
                .build();
        savedEntity.setId(UUID.randomUUID());

        when(employeeService.getEmployeeById(companyId, employeeId)).thenReturn(EmployeeDetailResponse.builder().id(employeeId).build());
        when(savedPathRepository.save(any(CareerSimulationSavedPath.class))).thenReturn(savedEntity);
        when(positionService.getPositionSummary(targetPosId)).thenReturn(PositionSummary.builder().id(targetPosId).name("Senior Dev").build());

        CareerSimulationSavedPathResponse response = careerSimulatorService.savePath(companyId, employeeId, request);

        assertThat(response).isNotNull();
        assertThat(response.getEmployeeId()).isEqualTo(employeeId);
        assertThat(response.getTargetPosition().getName()).isEqualTo("Senior Dev");
    }

    @Test
    @DisplayName("Xóa kịch bản lộ trình thành công")
    void deleteSavedPath_Success() {
        UUID savedPathId = UUID.randomUUID();
        CareerSimulationSavedPath saved = CareerSimulationSavedPath.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .build();
        saved.setId(savedPathId);

        when(savedPathRepository.findByIdAndCompanyId(savedPathId, companyId)).thenReturn(Optional.of(saved));

        careerSimulatorService.deleteSavedPath(companyId, employeeId, savedPathId);

        verify(savedPathRepository, times(1)).delete(saved);
    }

    @Test
    @DisplayName("Xóa kịch bản thất bại nếu không phải chủ sở hữu (403 Forbidden)")
    void deleteSavedPath_Forbidden_ThrowsException() {
        UUID savedPathId = UUID.randomUUID();
        UUID otherEmpId = UUID.randomUUID();

        CareerSimulationSavedPath saved = CareerSimulationSavedPath.builder()
                .companyId(companyId)
                .employeeId(otherEmpId) // Khác với employeeId gọi
                .build();
        saved.setId(savedPathId);

        when(savedPathRepository.findByIdAndCompanyId(savedPathId, companyId)).thenReturn(Optional.of(saved));

        assertThatThrownBy(() -> careerSimulatorService.deleteSavedPath(companyId, employeeId, savedPathId))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(TalentErrorCode.FORBIDDEN_SIMULATION_ACCESS));
    }
}
