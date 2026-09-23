package com.cyclosa.asset;

import com.cyclosa.asset.dto.request.*;
import com.cyclosa.asset.dto.response.AssetAllocationResponse;
import com.cyclosa.asset.dto.response.AssetResponse;
import com.cyclosa.asset.entity.Asset;
import com.cyclosa.asset.entity.AssetAllocation;
import com.cyclosa.asset.enums.AssetCategory;
import com.cyclosa.asset.enums.AssetCondition;
import com.cyclosa.asset.enums.AssetStatus;
import com.cyclosa.asset.exception.AssetErrorCode;
import com.cyclosa.asset.mapper.AssetMapper;
import com.cyclosa.asset.repository.AssetAllocationRepository;
import com.cyclosa.asset.repository.AssetRepository;
import com.cyclosa.asset.service.AssetServiceImpl;
import com.cyclosa.common.exception.AppException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetAllocationRepository assetAllocationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Spy
    private AssetMapper assetMapper = Mappers.getMapper(AssetMapper.class);

    @InjectMocks
    private AssetServiceImpl assetService;

    private UUID companyId;
    private UUID employeeId;
    private UUID assetId;
    private Asset mockAsset;
    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        assetId = UUID.randomUUID();

        mockAsset = Asset.builder()
                .assetCode("LAP-001")
                .name("MacBook Pro M3")
                .category(AssetCategory.LAPTOP)
                .purchaseDate(LocalDate.of(2024, 1, 15))
                .purchaseCost(BigDecimal.valueOf(45000000))
                .status(AssetStatus.IN_STOCK)
                .companyId(companyId)
                .serialNumber("MBP2024M3001")
                .build();
        mockAsset.setId(assetId);

        mockEmployee = Employee.builder()
                .employeeCode("EMP001")
                .fullName("Nguyen Van A")
                .companyId(companyId)
                .hireDate(LocalDate.of(2023, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
        mockEmployee.setId(employeeId);
    }

    @Test
    @DisplayName("Tạo tài sản mới thành công với trạng thái IN_STOCK")
    void createAsset_success() {
        CreateAssetRequest request = CreateAssetRequest.builder()
                .assetCode("LAP-001")
                .name("MacBook Pro M3")
                .category(AssetCategory.LAPTOP)
                .companyId(companyId)
                .purchaseCost(BigDecimal.valueOf(45000000))
                .build();

        when(assetRepository.existsByAssetCodeAndCompanyId("LAP-001", companyId)).thenReturn(false);
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> {
            Asset a = invocation.getArgument(0);
            a.setId(assetId);
            return a;
        });

        AssetResponse response = assetService.createAsset(request);

        assertThat(response).isNotNull();
        assertThat(response.getAssetCode()).isEqualTo("LAP-001");
        assertThat(response.getStatus()).isEqualTo(AssetStatus.IN_STOCK);
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    @DisplayName("Tạo tài sản thất bại khi trùng mã trong cùng công ty")
    void createAsset_duplicateCode_throwsException() {
        CreateAssetRequest request = CreateAssetRequest.builder()
                .assetCode("LAP-001")
                .companyId(companyId)
                .build();

        when(assetRepository.existsByAssetCodeAndCompanyId("LAP-001", companyId)).thenReturn(true);

        assertThatThrownBy(() -> assetService.createAsset(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AssetErrorCode.ASSET_CODE_ALREADY_EXISTS);

        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cấp phát tài sản thành công -> chuyển trạng thái ALLOCATED")
    void allocateAsset_success() {
        AllocateAssetRequest request = AllocateAssetRequest.builder()
                .employeeId(employeeId)
                .allocatedDate(LocalDate.now())
                .conditionOnAllocation(AssetCondition.NEW)
                .note("Cấp phát máy mới cho nhân viên")
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetAllocationRepository.existsByAssetIdAndReturnedDateIsNull(assetId)).thenReturn(false);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(assetAllocationRepository.save(any(AssetAllocation.class))).thenAnswer(i -> {
            AssetAllocation alloc = i.getArgument(0);
            alloc.setId(UUID.randomUUID());
            return alloc;
        });

        AssetAllocationResponse response = assetService.allocateAsset(assetId, request);

        assertThat(response).isNotNull();
        assertThat(response.getAssetCode()).isEqualTo("LAP-001");
        assertThat(response.getEmployeeName()).isEqualTo("Nguyen Van A");
        assertThat(mockAsset.getStatus()).isEqualTo(AssetStatus.ALLOCATED);
        verify(assetRepository).save(mockAsset);
    }

    @Test
    @DisplayName("Cấp phát thất bại khi tài sản không ở trạng thái IN_STOCK")
    void allocateAsset_notInStock_throwsException() {
        mockAsset.setStatus(AssetStatus.UNDER_REPAIR);
        AllocateAssetRequest request = AllocateAssetRequest.builder()
                .employeeId(employeeId)
                .allocatedDate(LocalDate.now())
                .conditionOnAllocation(AssetCondition.GOOD)
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));

        assertThatThrownBy(() -> assetService.allocateAsset(assetId, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AssetErrorCode.ASSET_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("Cấp phát thất bại khi tài sản đang có lượt cấp phát chưa trả")
    void allocateAsset_alreadyAllocated_throwsException() {
        AllocateAssetRequest request = AllocateAssetRequest.builder()
                .employeeId(employeeId)
                .allocatedDate(LocalDate.now())
                .conditionOnAllocation(AssetCondition.GOOD)
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetAllocationRepository.existsByAssetIdAndReturnedDateIsNull(assetId)).thenReturn(true);

        assertThatThrownBy(() -> assetService.allocateAsset(assetId, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AssetErrorCode.ASSET_ALREADY_ALLOCATED);
    }

    @Test
    @DisplayName("Cấp phát thất bại khi nhân viên đã thôi việc")
    void allocateAsset_employeeResigned_throwsException() {
        mockEmployee.setEmploymentStatus(EmploymentStatus.RESIGNED);
        AllocateAssetRequest request = AllocateAssetRequest.builder()
                .employeeId(employeeId)
                .allocatedDate(LocalDate.now())
                .conditionOnAllocation(AssetCondition.GOOD)
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetAllocationRepository.existsByAssetIdAndReturnedDateIsNull(assetId)).thenReturn(false);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));

        assertThatThrownBy(() -> assetService.allocateAsset(assetId, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AssetErrorCode.EMPLOYEE_NOT_ACTIVE);
    }

    @Test
    @DisplayName("Thu hồi tài sản bình thường -> chuyển trạng thái IN_STOCK")
    void returnAsset_success() {
        mockAsset.setStatus(AssetStatus.ALLOCATED);
        AssetAllocation openAlloc = AssetAllocation.builder()
                .asset(mockAsset)
                .employeeId(employeeId)
                .allocatedDate(LocalDate.now().minusMonths(6))
                .conditionOnAllocation(AssetCondition.NEW)
                .build();
        openAlloc.setId(UUID.randomUUID());

        ReturnAssetRequest request = ReturnAssetRequest.builder()
                .returnedDate(LocalDate.now())
                .conditionOnReturn(AssetCondition.GOOD)
                .note("Máy hoạt động tốt")
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetAllocationRepository.findByAssetIdAndReturnedDateIsNull(assetId)).thenReturn(Optional.of(openAlloc));
        when(assetAllocationRepository.save(any(AssetAllocation.class))).thenAnswer(i -> i.getArgument(0));

        AssetAllocationResponse response = assetService.returnAsset(assetId, request);

        assertThat(response).isNotNull();
        assertThat(mockAsset.getStatus()).isEqualTo(AssetStatus.IN_STOCK);
        assertThat(openAlloc.getReturnedDate()).isEqualTo(LocalDate.now());
        assertThat(openAlloc.getConditionOnReturn()).isEqualTo(AssetCondition.GOOD);
    }

    @Test
    @DisplayName("Thu hồi tài sản bị hỏng -> tự động chuyển UNDER_REPAIR")
    void returnAsset_damaged_setsStatusUnderRepair() {
        mockAsset.setStatus(AssetStatus.ALLOCATED);
        AssetAllocation openAlloc = AssetAllocation.builder()
                .asset(mockAsset)
                .employeeId(employeeId)
                .allocatedDate(LocalDate.now().minusMonths(6))
                .conditionOnAllocation(AssetCondition.NEW)
                .build();

        ReturnAssetRequest request = ReturnAssetRequest.builder()
                .returnedDate(LocalDate.now())
                .conditionOnReturn(AssetCondition.DAMAGED)
                .note("Bị vỡ màn hình")
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetAllocationRepository.findByAssetIdAndReturnedDateIsNull(assetId)).thenReturn(Optional.of(openAlloc));
        when(assetAllocationRepository.save(any(AssetAllocation.class))).thenAnswer(i -> i.getArgument(0));

        assetService.returnAsset(assetId, request);

        assertThat(mockAsset.getStatus()).isEqualTo(AssetStatus.UNDER_REPAIR);
    }

    @Test
    @DisplayName("Thu hồi thất bại khi tài sản không có allocation đang mở")
    void returnAsset_noOpenAllocation_throwsException() {
        ReturnAssetRequest request = ReturnAssetRequest.builder()
                .returnedDate(LocalDate.now())
                .conditionOnReturn(AssetCondition.GOOD)
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetAllocationRepository.findByAssetIdAndReturnedDateIsNull(assetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.returnAsset(assetId, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AssetErrorCode.NO_OPEN_ALLOCATION_FOUND);
    }

    @Test
    @DisplayName("Thu hồi thất bại khi ngày trả trước ngày cấp phát")
    void returnAsset_returnedBeforeAllocatedDate_throwsException() {
        AssetAllocation openAlloc = AssetAllocation.builder()
                .asset(mockAsset)
                .employeeId(employeeId)
                .allocatedDate(LocalDate.now())
                .build();

        ReturnAssetRequest request = ReturnAssetRequest.builder()
                .returnedDate(LocalDate.now().minusDays(1))
                .conditionOnReturn(AssetCondition.GOOD)
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetAllocationRepository.findByAssetIdAndReturnedDateIsNull(assetId)).thenReturn(Optional.of(openAlloc));

        assertThatThrownBy(() -> assetService.returnAsset(assetId, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AssetErrorCode.RETURN_DATE_BEFORE_ALLOCATED_DATE);
    }
}
