package com.cyclosa.workplace;

import com.cyclosa.workplace.dto.request.NavigationRequest;
import com.cyclosa.workplace.dto.response.NavigationRouteResponse;
import com.cyclosa.workplace.entity.*;
import com.cyclosa.workplace.enums.DeskShape;
import com.cyclosa.workplace.enums.NavigationType;
import com.cyclosa.workplace.enums.NonDeskObjectType;
import com.cyclosa.workplace.repository.*;
import com.cyclosa.workplace.service.AStarPathfinder;
import com.cyclosa.workplace.service.WorkplaceNavigationService;
import com.cyclosa.workplace.service.WorkplaceSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkplaceNavigationServiceTest {

    @Mock
    private WorkplaceSeatRepository seatRepository;
    @Mock
    private WorkplaceNonDeskObjectRepository nonDeskObjectRepository;
    @Mock
    private WorkplaceFloorPlanRepository floorPlanRepository;
    @Mock
    private WorkplaceSecurityContext securityContext;

    @Spy
    private AStarPathfinder aStarPathfinder = new AStarPathfinder();

    @InjectMocks
    private WorkplaceNavigationService navigationService;

    private UUID companyId;
    private WorkplaceSite siteA;
    private WorkplaceSite siteB;
    private WorkplaceFloor floor1;
    private WorkplaceFloor floor2;
    private WorkplaceSeat seatStart;
    private WorkplaceSeat seatEnd;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        lenient().when(securityContext.getCurrentCompanyId()).thenReturn(companyId);
        lenient().when(securityContext.getRequiredEmployeeId()).thenReturn(null);

        siteA = WorkplaceSite.builder().name("Tòa Keangnam").code("SITE-KN").build();
        siteA.setId(UUID.randomUUID());

        siteB = WorkplaceSite.builder().name("Tòa Lotte").code("SITE-LT").build();
        siteB.setId(UUID.randomUUID());

        floor1 = WorkplaceFloor.builder().site(siteA).floorNumber("Tầng 5").name("Tầng 5 Kỹ Thuật").build();
        floor1.setId(UUID.randomUUID());

        floor2 = WorkplaceFloor.builder().site(siteA).floorNumber("Tầng 8").name("Tầng 8 Marketing").build();
        floor2.setId(UUID.randomUUID());

        WorkplaceZone zone1 = WorkplaceZone.builder().floor(floor1).name("Zone A").build();
        zone1.setId(UUID.randomUUID());

        WorkplaceRow row1 = WorkplaceRow.builder().zone(zone1).name("Row 1").build();
        row1.setId(UUID.randomUUID());

        seatStart = WorkplaceSeat.builder()
                .seatCode("SEAT-01")
                .deskShape(DeskShape.RECTANGLE)
                .positionX(BigDecimal.valueOf(100))
                .positionY(BigDecimal.valueOf(100))
                .width(BigDecimal.valueOf(60))
                .height(BigDecimal.valueOf(40))
                .row(row1)
                .companyId(companyId)
                .build();
        seatStart.setId(UUID.randomUUID());

        seatEnd = WorkplaceSeat.builder()
                .seatCode("SEAT-02")
                .deskShape(DeskShape.RECTANGLE)
                .positionX(BigDecimal.valueOf(400))
                .positionY(BigDecimal.valueOf(100))
                .width(BigDecimal.valueOf(60))
                .height(BigDecimal.valueOf(40))
                .row(row1)
                .companyId(companyId)
                .build();
        seatEnd.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Tìm đường cùng tầng (SAME_FLOOR) thành công qua thuật toán A*")
    void navigate_sameFloor_success() {
        NavigationRequest request = NavigationRequest.builder()
                .originSeatId(seatStart.getId())
                .destinationSeatId(seatEnd.getId())
                .build();

        // Service calls seatRepository.findById (JpaRepository base method)
        when(seatRepository.findById(seatStart.getId())).thenReturn(Optional.of(seatStart));
        when(seatRepository.findById(seatEnd.getId())).thenReturn(Optional.of(seatEnd));

        // collectObstacles calls findByFloorIdWithHierarchy (not findByFloorIdAndCompanyId)
        when(seatRepository.findByFloorIdWithHierarchy(floor1.getId()))
                .thenReturn(List.of(seatStart, seatEnd));
        when(nonDeskObjectRepository.findByFloorId(floor1.getId()))
                .thenReturn(Collections.emptyList());
        when(floorPlanRepository.findByFloorId(floor1.getId()))
                .thenReturn(Optional.empty());

        NavigationRouteResponse response = navigationService.calculateRoute(request);

        assertThat(response).isNotNull();
        assertThat(response.getNavigationType()).isEqualTo(NavigationType.SAME_FLOOR);
        assertThat(response.getPathPoints()).isNotEmpty();
        assertThat(response.getTotalDistanceMeters()).isGreaterThan(0.0);
        assertThat(response.getEstimatedWalkSeconds()).isGreaterThan(0);
        assertThat(response.getTurnByTurnInstructions()).isNotEmpty();
    }

    @Test
    @DisplayName("Tìm đường khác tầng cùng tòa nhà (CROSS_FLOOR) chỉ dẫn qua thang máy")
    void navigate_crossFloor_success() {
        WorkplaceZone zone2 = WorkplaceZone.builder().floor(floor2).name("Zone B").build();
        zone2.setId(UUID.randomUUID());
        WorkplaceRow row2 = WorkplaceRow.builder().zone(zone2).name("Row 2").build();
        row2.setId(UUID.randomUUID());

        WorkplaceSeat seatFloor2 = WorkplaceSeat.builder()
                .seatCode("SEAT-801")
                .positionX(BigDecimal.valueOf(300))
                .positionY(BigDecimal.valueOf(300))
                .row(row2)
                .companyId(companyId)
                .build();
        seatFloor2.setId(UUID.randomUUID());

        NavigationRequest request = NavigationRequest.builder()
                .originSeatId(seatStart.getId())
                .destinationSeatId(seatFloor2.getId())
                .build();

        when(seatRepository.findById(seatStart.getId())).thenReturn(Optional.of(seatStart));
        when(seatRepository.findById(seatFloor2.getId())).thenReturn(Optional.of(seatFloor2));

        // buildCrossFloorResponse calls nonDeskObjectRepository.findByFloorIdAndObjectTypeIn on origin floor
        WorkplaceNonDeskObject elevator = WorkplaceNonDeskObject.builder()
                .name("Thang máy sảnh trung tâm")
                .objectType(NonDeskObjectType.ELEVATOR)
                .positionX(BigDecimal.valueOf(200))
                .positionY(BigDecimal.valueOf(200))
                .zone(seatStart.getRow().getZone())
                .companyId(companyId)
                .build();
        elevator.setId(UUID.randomUUID());

        when(nonDeskObjectRepository.findByFloorIdAndObjectTypeIn(eq(floor1.getId()), anyCollection()))
                .thenReturn(List.of(elevator));
        when(floorPlanRepository.findByFloorId(floor1.getId())).thenReturn(Optional.empty());

        // collectObstacles for the cross-floor path
        when(seatRepository.findByFloorIdWithHierarchy(floor1.getId())).thenReturn(List.of(seatStart));
        when(nonDeskObjectRepository.findByFloorId(floor1.getId())).thenReturn(List.of(elevator));

        NavigationRouteResponse response = navigationService.calculateRoute(request);

        assertThat(response).isNotNull();
        assertThat(response.getNavigationType()).isEqualTo(NavigationType.CROSS_FLOOR);
        assertThat(response.getTurnByTurnInstructions())
                .anyMatch(step -> step.contains("Thang máy") || step.contains("Tầng"));
    }

    @Test
    @DisplayName("Tìm đường giữa hai tòa nhà khác nhau (CROSS_SITE) xuất chỉ dẫn di chuyển tòa nhà")
    void navigate_crossSite_success() {
        WorkplaceFloor floorB = WorkplaceFloor.builder().site(siteB).floorNumber("Tầng 10").name("Tầng 10").build();
        floorB.setId(UUID.randomUUID());
        WorkplaceZone zoneB = WorkplaceZone.builder().floor(floorB).name("Zone Lotte").build();
        zoneB.setId(UUID.randomUUID());
        WorkplaceRow rowB = WorkplaceRow.builder().zone(zoneB).name("Row Lotte").build();
        rowB.setId(UUID.randomUUID());

        WorkplaceSeat seatSiteB = WorkplaceSeat.builder()
                .seatCode("LT-01")
                .positionX(BigDecimal.valueOf(100))
                .positionY(BigDecimal.valueOf(100))
                .row(rowB)
                .companyId(companyId)
                .build();
        seatSiteB.setId(UUID.randomUUID());

        NavigationRequest request = NavigationRequest.builder()
                .originSeatId(seatStart.getId())
                .destinationSeatId(seatSiteB.getId())
                .build();

        // Service calls seatRepository.findById for both seats
        when(seatRepository.findById(seatStart.getId())).thenReturn(Optional.of(seatStart));
        when(seatRepository.findById(seatSiteB.getId())).thenReturn(Optional.of(seatSiteB));

        // Cross-site: just builds instruction list, no further calls
        NavigationRouteResponse response = navigationService.calculateRoute(request);

        assertThat(response).isNotNull();
        assertThat(response.getNavigationType()).isEqualTo(NavigationType.CROSS_SITE);
        assertThat(response.getTurnByTurnInstructions())
                .anyMatch(step -> step.contains("Tòa") || step.contains("cơ sở"));
    }
}
