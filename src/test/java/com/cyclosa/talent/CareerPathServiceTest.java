package com.cyclosa.talent;

import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.dto.response.PositionDetailResponse;
import com.cyclosa.organization.service.PositionService;
import com.cyclosa.talent.dto.filter.CareerPathFilter;
import com.cyclosa.talent.dto.request.CreateCareerPathRequest;
import com.cyclosa.talent.dto.request.UpdateCareerPathRequest;
import com.cyclosa.talent.dto.response.CareerPathResponse;
import com.cyclosa.talent.entity.CareerPath;
import com.cyclosa.talent.exception.TalentErrorCode;
import com.cyclosa.talent.mapper.TalentMapper;
import com.cyclosa.talent.repository.CareerPathRepository;
import com.cyclosa.talent.service.CareerPathService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareerPathServiceTest {

    @Mock
    private CareerPathRepository careerPathRepository;

    @Mock
    private PositionService positionService;

    @Mock
    private EmployeeService employeeService;

    @Spy
    private TalentMapper talentMapper = Mappers.getMapper(TalentMapper.class);

    @InjectMocks
    private CareerPathService careerPathService;

    private UUID companyId;
    private UUID fromPosId;
    private UUID toPosId;
    private CareerPath samplePath;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        fromPosId = UUID.randomUUID();
        toPosId = UUID.randomUUID();

        samplePath = CareerPath.builder()
                .companyId(companyId)
                .fromPositionId(fromPosId)
                .toPositionId(toPosId)
                .description("Junior lên Senior Developer")
                .minYearsRequired(BigDecimal.valueOf(2.5))
                .build();
        samplePath.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Tạo lộ trình thăng tiến thành công")
    void createCareerPath_Success() {
        CreateCareerPathRequest request = CreateCareerPathRequest.builder()
                .fromPositionId(fromPosId)
                .toPositionId(toPosId)
                .description("Junior lên Senior Developer")
                .minYearsRequired(BigDecimal.valueOf(2.5))
                .build();

        when(positionService.getPositionById(companyId, fromPosId)).thenReturn(PositionDetailResponse.builder().id(fromPosId).build());
        when(positionService.getPositionById(companyId, toPosId)).thenReturn(PositionDetailResponse.builder().id(toPosId).build());
        when(careerPathRepository.existsByCompanyIdAndFromPositionIdAndToPositionId(companyId, fromPosId, toPosId)).thenReturn(false);
        when(careerPathRepository.save(any(CareerPath.class))).thenReturn(samplePath);
        when(positionService.getPositionSummaries(anySet())).thenReturn(Map.of(
                fromPosId, PositionSummary.builder().id(fromPosId).name("Junior Dev").build(),
                toPosId, PositionSummary.builder().id(toPosId).name("Senior Dev").build()
        ));

        CareerPathResponse response = careerPathService.createCareerPath(companyId, request);

        assertThat(response).isNotNull();
        assertThat(response.getFromPositionId()).isEqualTo(fromPosId);
        assertThat(response.getToPositionId()).isEqualTo(toPosId);
        assertThat(response.getFromPosition().getName()).isEqualTo("Junior Dev");
        assertThat(response.getToPosition().getName()).isEqualTo("Senior Dev");
    }

    @Test
    @DisplayName("Tạo lộ trình thất bại nếu vị trí xuất phát trùng vị trí đích")
    void createCareerPath_SameFromTo_ThrowsException() {
        CreateCareerPathRequest request = CreateCareerPathRequest.builder()
                .fromPositionId(fromPosId)
                .toPositionId(fromPosId)
                .build();

        assertThatThrownBy(() -> careerPathService.createCareerPath(companyId, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(TalentErrorCode.SAME_FROM_TO_POSITION));
    }

    @Test
    @DisplayName("Tạo lộ trình thất bại nếu lộ trình đã tồn tại")
    void createCareerPath_AlreadyExists_ThrowsException() {
        CreateCareerPathRequest request = CreateCareerPathRequest.builder()
                .fromPositionId(fromPosId)
                .toPositionId(toPosId)
                .build();

        when(positionService.getPositionById(companyId, fromPosId)).thenReturn(PositionDetailResponse.builder().id(fromPosId).build());
        when(positionService.getPositionById(companyId, toPosId)).thenReturn(PositionDetailResponse.builder().id(toPosId).build());
        when(careerPathRepository.existsByCompanyIdAndFromPositionIdAndToPositionId(companyId, fromPosId, toPosId)).thenReturn(true);

        assertThatThrownBy(() -> careerPathService.createCareerPath(companyId, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(TalentErrorCode.CAREER_PATH_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("Lấy danh sách lộ trình thăng tiến phân trang thành công")
    void getCareerPaths_Success() {
        CareerPathFilter filter = new CareerPathFilter();
        when(careerPathRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(samplePath)));
        when(positionService.getPositionSummaries(anySet())).thenReturn(Map.of(
                fromPosId, PositionSummary.builder().id(fromPosId).name("Junior Dev").build(),
                toPosId, PositionSummary.builder().id(toPosId).name("Senior Dev").build()
        ));

        PageData<CareerPathResponse> page = careerPathService.getCareerPaths(companyId, filter);

        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getItems().get(0).getFromPosition().getName()).isEqualTo("Junior Dev");
    }

    @Test
    @DisplayName("Tra cứu lộ trình cho nhân viên theo vị trí hiện tại")
    void getCareerPathsForEmployee_Success() {
        UUID employeeId = UUID.randomUUID();
        when(employeeService.getEmployeeById(companyId, employeeId)).thenReturn(
                EmployeeDetailResponse.builder()
                        .id(employeeId)
                        .position(PositionSummary.builder().id(fromPosId).name("Junior Dev").build())
                        .build()
        );
        when(careerPathRepository.findByCompanyIdAndFromPositionId(companyId, fromPosId))
                .thenReturn(List.of(samplePath));
        when(positionService.getPositionSummaries(anySet())).thenReturn(Map.of(
                fromPosId, PositionSummary.builder().id(fromPosId).name("Junior Dev").build(),
                toPosId, PositionSummary.builder().id(toPosId).name("Senior Dev").build()
        ));

        List<CareerPathResponse> responses = careerPathService.getCareerPathsForEmployee(companyId, employeeId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getToPositionId()).isEqualTo(toPosId);
    }
}
