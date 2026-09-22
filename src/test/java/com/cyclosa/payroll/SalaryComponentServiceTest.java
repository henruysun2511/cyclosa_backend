package com.cyclosa.payroll;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.payroll.dto.request.CreateSalaryComponentRequest;
import com.cyclosa.payroll.dto.request.UpdateSalaryComponentRequest;
import com.cyclosa.payroll.dto.response.SalaryComponentResponse;
import com.cyclosa.payroll.entity.SalaryComponent;
import com.cyclosa.payroll.enums.ComponentType;
import com.cyclosa.payroll.mapper.SalaryComponentMapper;
import com.cyclosa.payroll.repository.SalaryComponentRepository;
import com.cyclosa.payroll.service.SalaryComponentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryComponentServiceTest {

    @Mock
    private SalaryComponentRepository repository;
    @Mock
    private CompanyService companyService;

    private SalaryComponentMapper mapper = Mappers.getMapper(SalaryComponentMapper.class);

    private SalaryComponentService service;

    private UUID companyId;

    @BeforeEach
    void setUp() {
        service = new SalaryComponentService(repository, companyService, mapper);
        companyId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should create salary component successfully")
    void testCreateSalaryComponentSuccess() {
        CreateSalaryComponentRequest req = CreateSalaryComponentRequest.builder()
                .code("MEAL")
                .name("Phụ cấp ăn trưa")
                .componentType(ComponentType.ALLOWANCE)
                .isTaxable(false)
                .isInsuranceBase(false)
                .isRecurring(true)
                .defaultAmount(new BigDecimal("1000000"))
                .build();

        when(repository.existsByCodeAndCompanyId("MEAL", companyId)).thenReturn(false);
        when(repository.save(any(SalaryComponent.class))).thenAnswer(inv -> {
            SalaryComponent sc = inv.getArgument(0);
            ReflectionTestUtils.setField(sc, "id", UUID.randomUUID());
            return sc;
        });

        SalaryComponentResponse resp = service.createComponent(companyId, req);

        assertThat(resp).isNotNull();
        assertThat(resp.getCode()).isEqualTo("MEAL");
        assertThat(resp.getDefaultAmount()).isEqualByComparingTo(new BigDecimal("1000000"));
        verify(repository).save(any(SalaryComponent.class));
    }

    @Test
    @DisplayName("Should throw exception when creating component with duplicate code")
    void testCreateSalaryComponentDuplicateCode() {
        CreateSalaryComponentRequest req = CreateSalaryComponentRequest.builder()
                .code("MEAL")
                .name("Phụ cấp ăn trưa")
                .componentType(ComponentType.ALLOWANCE)
                .build();

        when(repository.existsByCodeAndCompanyId("MEAL", companyId)).thenReturn(true);

        assertThatThrownBy(() -> service.createComponent(companyId, req))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("Should update salary component successfully")
    void testUpdateSalaryComponent() {
        UUID componentId = UUID.randomUUID();
        SalaryComponent existing = SalaryComponent.builder()
                .companyId(companyId)
                .code("MEAL")
                .name("Phụ cấp ăn trưa")
                .componentType(ComponentType.ALLOWANCE)
                .defaultAmount(new BigDecimal("1000000"))
                .isTaxable(false)
                .isInsuranceBase(false)
                .build();
        ReflectionTestUtils.setField(existing, "id", componentId);

        UpdateSalaryComponentRequest req = UpdateSalaryComponentRequest.builder()
                .name("Phụ cấp ăn trưa đặc biệt")
                .componentType(ComponentType.ALLOWANCE)
                .defaultAmount(new BigDecimal("1500000"))
                .isTaxable(false)
                .isInsuranceBase(false)
                .isRecurring(true)
                .build();

        when(repository.findByIdAndCompanyId(componentId, companyId)).thenReturn(Optional.of(existing));
        when(repository.save(any(SalaryComponent.class))).thenAnswer(inv -> inv.getArgument(0));

        SalaryComponentResponse resp = service.updateComponent(companyId, componentId, req);

        assertThat(resp.getName()).isEqualTo("Phụ cấp ăn trưa đặc biệt");
        assertThat(resp.getDefaultAmount()).isEqualByComparingTo(new BigDecimal("1500000"));
    }

    @Test
    @DisplayName("Should delete salary component successfully")
    void testDeleteSalaryComponent() {
        UUID componentId = UUID.randomUUID();
        SalaryComponent existing = SalaryComponent.builder()
                .companyId(companyId)
                .code("MEAL")
                .build();
        ReflectionTestUtils.setField(existing, "id", componentId);

        when(repository.findByIdAndCompanyId(componentId, companyId)).thenReturn(Optional.of(existing));

        service.deleteComponent(companyId, componentId);

        verify(repository).delete(existing);
    }
}
