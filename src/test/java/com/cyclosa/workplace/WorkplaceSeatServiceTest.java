package com.cyclosa.workplace;

import com.cyclosa.asset.dto.response.AssetDetailResponse;
import com.cyclosa.asset.service.AssetService;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.workplace.dto.request.AssignSeatRequest;
import com.cyclosa.workplace.dto.request.BulkGenerateSeatsRequest;
import com.cyclosa.workplace.dto.request.CreateSeatRequest;
import com.cyclosa.workplace.dto.response.WorkplaceSeatResponse;
import com.cyclosa.workplace.entity.*;
import com.cyclosa.workplace.enums.ClusterType;
import com.cyclosa.workplace.enums.DeskShape;
import com.cyclosa.workplace.enums.SeatStatus;
import com.cyclosa.workplace.enums.SeatType;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.mapper.WorkplaceMapper;
import com.cyclosa.workplace.repository.WorkplaceRowRepository;
import com.cyclosa.workplace.repository.WorkplaceSeatLinkedAssetRepository;
import com.cyclosa.workplace.repository.WorkplaceSeatRepository;
import com.cyclosa.workplace.service.WorkplaceQrService;
import com.cyclosa.workplace.service.WorkplaceSeatService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkplaceSeatServiceTest {

    @Mock
    private WorkplaceSeatRepository seatRepository;
    @Mock
    private WorkplaceRowRepository rowRepository;
    @Mock
    private WorkplaceSeatLinkedAssetRepository linkedAssetRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private AssetService assetService;
    @Mock
    private WorkplaceQrService qrService;

    @Spy
    private WorkplaceMapper workplaceMapper = Mappers.getMapper(WorkplaceMapper.class);

    @InjectMocks
    private WorkplaceSeatService seatService;

    private UUID companyId;
    private UUID rowId;
    private UUID seatId;
    private UUID employeeId;
    private WorkplaceRow testRow;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        rowId = UUID.randomUUID();
        seatId = UUID.randomUUID();
        employeeId = UUID.randomUUID();

        WorkplaceSite site = WorkplaceSite.builder().code("SITE-1").name("Tòa A").companyId(companyId).build();
        site.setId(UUID.randomUUID());
        WorkplaceFloor floor = WorkplaceFloor.builder().site(site).floorNumber("T5").name("Tầng 5").companyId(companyId).build();
        floor.setId(UUID.randomUUID());
        WorkplaceZone zone = WorkplaceZone.builder().floor(floor).name("Zone K").companyId(companyId).build();
        zone.setId(UUID.randomUUID());

        testRow = WorkplaceRow.builder()
                .zone(zone)
                .name("Dãy A1")
                .clusterType(ClusterType.POD_4_FACING)
                .companyId(companyId)
                .build();
        testRow.setId(rowId);

        lenient().when(qrService.generateSeatQrCode(any())).thenReturn("cyclosa://workplace/seat/mock");
    }

    @Test
    @DisplayName("Tạo chỗ ngồi đơn lẻ thành công")
    void createSeat_success() {
        CreateSeatRequest request = CreateSeatRequest.builder()
                .rowId(rowId)
                .seatCode("A1-01")
                .seatType(SeatType.DEDICATED)
                .deskShape(DeskShape.RECTANGLE)
                .positionX(BigDecimal.valueOf(100))
                .positionY(BigDecimal.valueOf(150))
                .build();

        // Service calls existsByCompanyIdAndSeatCode with uppercase
        when(seatRepository.existsByCompanyIdAndSeatCode(companyId, "A1-01")).thenReturn(false);
        // Service calls findByIdAndCompanyIdWithHierarchy for row
        when(rowRepository.findByIdAndCompanyIdWithHierarchy(rowId, companyId)).thenReturn(Optional.of(testRow));
        when(seatRepository.save(any(WorkplaceSeat.class))).thenAnswer(invocation -> {
            WorkplaceSeat seat = invocation.getArgument(0);
            seat.setId(seatId);
            return seat;
        });

        WorkplaceSeatResponse response = seatService.createSeat(companyId, request);

        assertThat(response).isNotNull();
        assertThat(response.getSeatCode()).isEqualTo("A1-01");
        assertThat(response.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        verify(seatRepository, times(1)).save(any(WorkplaceSeat.class));
    }

    @Test
    @DisplayName("Sinh hàng loạt chỗ ngồi theo cụm Pod 4 thành công")
    void bulkGenerateSeats_pod4_success() {
        // Service calls findByIdAndCompanyIdWithHierarchy for row
        when(rowRepository.findByIdAndCompanyIdWithHierarchy(rowId, companyId)).thenReturn(Optional.of(testRow));
        when(seatRepository.existsByCompanyIdAndSeatCode(eq(companyId), any())).thenReturn(false);
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<WorkplaceSeat> list = invocation.getArgument(0);
            list.forEach(s -> s.setId(UUID.randomUUID()));
            return list;
        });

        BulkGenerateSeatsRequest request = BulkGenerateSeatsRequest.builder()
                .codePrefix("P")
                .count(4)
                .clusterType(ClusterType.POD_4_FACING)
                .seatType(SeatType.DEDICATED)
                .originX(BigDecimal.valueOf(200))
                .originY(BigDecimal.valueOf(300))
                .build();

        List<WorkplaceSeatResponse> responses = seatService.bulkGenerateSeats(companyId, rowId, request);

        assertThat(responses).hasSize(4);
        verify(seatRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Gán ghế cố định cho nhân viên thành công")
    void assignSeat_success() {
        WorkplaceSeat seat = WorkplaceSeat.builder()
                .seatCode("A1-01")
                .seatType(SeatType.DEDICATED)
                .status(SeatStatus.AVAILABLE)
                .companyId(companyId)
                .row(testRow)
                .build();
        seat.setId(seatId);

        // Service calls findByIdAndCompanyIdWithHierarchy for seat
        when(seatRepository.findByIdAndCompanyIdWithHierarchy(seatId, companyId)).thenReturn(Optional.of(seat));
        // validateEmployeeAssignment calls findByCompanyIdAndAssignedEmployeeId
        when(seatRepository.findByCompanyIdAndAssignedEmployeeId(companyId, employeeId)).thenReturn(Optional.empty());
        when(seatRepository.save(any(WorkplaceSeat.class))).thenReturn(seat);
        lenient().when(employeeService.getEmployeeSummaries(any())).thenReturn(java.util.Map.of());

        AssignSeatRequest request = AssignSeatRequest.builder()
                .employeeId(employeeId)
                .notes("Cấp ghế cho lập trình viên mới")
                .build();

        WorkplaceSeatResponse response = seatService.assignSeat(companyId, seatId, request);

        assertThat(response).isNotNull();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.OCCUPIED);
        assertThat(seat.getAssignedEmployeeId()).isEqualTo(employeeId);
        verify(seatRepository, times(1)).save(seat);
    }

    @Test
    @DisplayName("Tự động giải phóng ghế khi nhân viên Offboarding")
    void releaseSeatForOffboarding_success() {
        WorkplaceSeat seat = WorkplaceSeat.builder()
                .seatCode("A1-01")
                .seatType(SeatType.DEDICATED)
                .status(SeatStatus.OCCUPIED)
                .assignedEmployeeId(employeeId)
                .companyId(companyId)
                .row(testRow)
                .build();
        seat.setId(seatId);

        when(seatRepository.findByCompanyIdAndAssignedEmployeeId(companyId, employeeId)).thenReturn(Optional.of(seat));

        seatService.releaseSeatByEmployeeId(companyId, employeeId);

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seat.getAssignedEmployeeId()).isNull();
        verify(seatRepository, times(1)).save(seat);
    }

    @Test
    @DisplayName("Liên kết tài sản phần cứng (Module 13) vào bàn làm việc thành công")
    void linkAssetToSeat_success() {
        WorkplaceSeat seat = WorkplaceSeat.builder()
                .seatCode("A1-01")
                .companyId(companyId)
                .row(testRow)
                .build();
        seat.setId(seatId);

        UUID assetId = UUID.randomUUID();

        // Service calls findByIdAndCompanyId (not findById) for linkAsset
        when(seatRepository.findByIdAndCompanyId(seatId, companyId)).thenReturn(Optional.of(seat));
        when(assetService.getAssetById(assetId)).thenReturn(
                AssetDetailResponse.builder().id(assetId).assetCode("MON-001").name("Dell U2723QE").build());
        when(linkedAssetRepository.existsByCompanyIdAndSeatIdAndAssetId(companyId, seatId, assetId)).thenReturn(false);

        seatService.linkAsset(companyId, seatId, assetId);

        verify(linkedAssetRepository, times(1)).save(any(WorkplaceSeatLinkedAsset.class));
    }
}
