package com.cyclosa.asset;

import com.cyclosa.asset.dto.request.CreateInventoryCheckRequest;
import com.cyclosa.asset.dto.request.InventoryCheckItemRequest;
import com.cyclosa.asset.dto.response.InventoryCheckReportResponse;
import com.cyclosa.asset.dto.response.InventoryCheckResponse;
import com.cyclosa.asset.entity.Asset;
import com.cyclosa.asset.entity.AssetInventoryCheck;
import com.cyclosa.asset.entity.AssetInventoryCheckItem;
import com.cyclosa.asset.enums.AssetCategory;
import com.cyclosa.asset.enums.AssetStatus;
import com.cyclosa.asset.enums.InventoryResult;
import com.cyclosa.asset.mapper.AssetMapper;
import com.cyclosa.asset.repository.AssetInventoryCheckItemRepository;
import com.cyclosa.asset.repository.AssetInventoryCheckRepository;
import com.cyclosa.asset.repository.AssetRepository;
import com.cyclosa.asset.service.AssetInventoryServiceImpl;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetInventoryServiceTest {

    @Mock
    private AssetInventoryCheckRepository inventoryCheckRepository;

    @Mock
    private AssetInventoryCheckItemRepository inventoryCheckItemRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Spy
    private AssetMapper assetMapper = Mappers.getMapper(AssetMapper.class);

    @InjectMocks
    private AssetInventoryServiceImpl inventoryService;

    private UUID companyId;
    private UUID employeeId;
    private Employee mockEmployee;
    private Asset asset1;
    private Asset asset2;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();

        mockEmployee = Employee.builder()
                .employeeCode("IT01")
                .fullName("IT Admin")
                .companyId(companyId)
                .hireDate(LocalDate.of(2022, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
        mockEmployee.setId(employeeId);

        asset1 = Asset.builder()
                .assetCode("LAP-001")
                .name("MacBook Pro")
                .category(AssetCategory.LAPTOP)
                .status(AssetStatus.ALLOCATED)
                .companyId(companyId)
                .build();
        asset1.setId(UUID.randomUUID());

        asset2 = Asset.builder()
                .assetCode("MON-001")
                .name("Dell UltraSharp")
                .category(AssetCategory.OTHER)
                .status(AssetStatus.IN_STOCK)
                .companyId(companyId)
                .build();
        asset2.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Tạo đợt kiểm kê tài sản tính đúng số lượng matched/missing/damaged")
    void createInventoryCheck_success() {
        CreateInventoryCheckRequest request = CreateInventoryCheckRequest.builder()
                .companyId(companyId)
                .title("Kiểm kê thiết bị IT Quý 3")
                .checkDate(LocalDate.now())
                .performedByEmployeeId(employeeId)
                .items(List.of(
                        InventoryCheckItemRequest.builder().assetId(asset1.getId()).actualResult(InventoryResult.MATCHED).build(),
                        InventoryCheckItemRequest.builder().assetId(asset2.getId()).actualResult(InventoryResult.DAMAGED).note("Hỏng cổng HDMI").build()
                ))
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(assetRepository.findAllById(any())).thenReturn(List.of(asset1, asset2));
        when(inventoryCheckRepository.save(any(AssetInventoryCheck.class))).thenAnswer(i -> {
            AssetInventoryCheck c = i.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        InventoryCheckResponse response = inventoryService.createInventoryCheck(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Kiểm kê thiết bị IT Quý 3");
        assertThat(response.getTotalItems()).isEqualTo(2);
        assertThat(response.getMatchedCount()).isEqualTo(1);
        assertThat(response.getDamagedCount()).isEqualTo(1);
        assertThat(response.getMissingCount()).isEqualTo(0);
        assertThat(response.getPerformedByEmployeeName()).isEqualTo("IT Admin");
    }

    @Test
    @DisplayName("Xem báo cáo kiểm kê tính đúng tỷ lệ khớp (matchPercentage)")
    void getInventoryCheckReport_calculatesMatchPercentage() {
        UUID checkId = UUID.randomUUID();
        AssetInventoryCheck check = AssetInventoryCheck.builder()
                .companyId(companyId)
                .title("Kiểm kê kho năm 2026")
                .checkDate(LocalDate.now())
                .performedByEmployeeId(employeeId)
                .build();
        check.setId(checkId);

        AssetInventoryCheckItem item1 = AssetInventoryCheckItem.builder()
                .inventoryCheck(check)
                .asset(asset1)
                .actualResult(InventoryResult.MATCHED)
                .build();
        AssetInventoryCheckItem item2 = AssetInventoryCheckItem.builder()
                .inventoryCheck(check)
                .asset(asset2)
                .actualResult(InventoryResult.MISSING)
                .build();
        check.setItems(List.of(item1, item2));

        when(inventoryCheckRepository.findById(checkId)).thenReturn(Optional.of(check));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));

        InventoryCheckReportResponse report = inventoryService.getInventoryCheckReport(checkId);

        assertThat(report).isNotNull();
        assertThat(report.getTotalItems()).isEqualTo(2);
        assertThat(report.getMatchedCount()).isEqualTo(1);
        assertThat(report.getMissingCount()).isEqualTo(1);
        assertThat(report.getMatchPercentage()).isEqualTo(50.0);
        assertThat(report.getItems()).hasSize(2);
    }
}
