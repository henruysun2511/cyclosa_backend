package com.cyclosa.workplace;

import com.cyclosa.common.dto.summary.BranchSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.organization.service.GeographyService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.workplace.dto.request.CreateFloorRequest;
import com.cyclosa.workplace.dto.request.CreateSiteRequest;
import com.cyclosa.workplace.dto.request.CreateZoneRequest;
import com.cyclosa.workplace.dto.response.WorkplaceFloorResponse;
import com.cyclosa.workplace.dto.response.WorkplaceSiteResponse;
import com.cyclosa.workplace.dto.response.WorkplaceZoneResponse;
import com.cyclosa.workplace.entity.WorkplaceFloor;
import com.cyclosa.workplace.entity.WorkplaceSite;
import com.cyclosa.workplace.entity.WorkplaceZone;
import com.cyclosa.workplace.enums.ZoneShapeType;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.mapper.WorkplaceMapper;
import com.cyclosa.workplace.repository.WorkplaceFloorRepository;
import com.cyclosa.workplace.repository.WorkplaceRowRepository;
import com.cyclosa.workplace.repository.WorkplaceSiteRepository;
import com.cyclosa.workplace.repository.WorkplaceZoneRepository;
import com.cyclosa.workplace.service.WorkplaceHierarchyService;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkplaceHierarchyServiceTest {

    @Mock
    private WorkplaceSiteRepository siteRepository;
    @Mock
    private WorkplaceFloorRepository floorRepository;
    @Mock
    private WorkplaceZoneRepository zoneRepository;
    @Mock
    private WorkplaceRowRepository rowRepository;
    @Mock
    private GeographyService geographyService;
    @Mock
    private OrganizationalUnitService organizationalUnitService;

    @Spy
    private WorkplaceMapper workplaceMapper = Mappers.getMapper(WorkplaceMapper.class);

    @InjectMocks
    private WorkplaceHierarchyService hierarchyService;

    private UUID companyId;
    private UUID siteId;
    private UUID floorId;
    private UUID branchId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        siteId = UUID.randomUUID();
        floorId = UUID.randomUUID();
        branchId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Tạo Site thành công khi mã hợp lệ, chưa tồn tại và branch hợp lệ")
    void createSite_success() {
        CreateSiteRequest request = CreateSiteRequest.builder()
                .code("SITE-HN")
                .name("Tòa nhà Keangnam Hà Nội")
                .address("Phạm Hùng, Nam Từ Liêm, Hà Nội")
                .branchId(branchId)
                .build();

        when(siteRepository.existsByCompanyIdAndCode(companyId, "SITE-HN")).thenReturn(false);

        BranchSummary branch = BranchSummary.builder()
                .id(branchId)
                .name("Chi nhánh Hà Nội")
                .build();
        when(geographyService.getBranchSummary(branchId)).thenReturn(branch);

        when(siteRepository.save(any(WorkplaceSite.class))).thenAnswer(invocation -> {
            WorkplaceSite saved = invocation.getArgument(0);
            saved.setId(siteId);
            return saved;
        });

        WorkplaceSiteResponse response = hierarchyService.createSite(companyId, request);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("SITE-HN");
        assertThat(response.getName()).isEqualTo("Tòa nhà Keangnam Hà Nội");
        verify(siteRepository, times(1)).save(any(WorkplaceSite.class));
    }

    @Test
    @DisplayName("Ném lỗi SITE_CODE_ALREADY_EXISTS khi mã tòa nhà trùng")
    void createSite_duplicateCode_throwsException() {
        CreateSiteRequest request = CreateSiteRequest.builder()
                .code("SITE-HN")
                .name("Tòa nhà Keangnam")
                .branchId(branchId)
                .build();

        when(siteRepository.existsByCompanyIdAndCode(companyId, "SITE-HN")).thenReturn(true);

        assertThatThrownBy(() -> hierarchyService.createSite(companyId, request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(WorkplaceErrorCode.SITE_CODE_ALREADY_EXISTS.getMessage());

        verify(siteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tạo Tầng (Floor) thành công liên kết đúng với Site")
    void createFloor_success() {
        WorkplaceSite site = WorkplaceSite.builder()
                .code("SITE-HN")
                .name("Keangnam")
                .companyId(companyId)
                .build();
        site.setId(siteId);

        // Service calls findByIdAndCompanyId (not findById)
        when(siteRepository.findByIdAndCompanyId(siteId, companyId)).thenReturn(Optional.of(site));
        when(floorRepository.existsByCompanyIdAndSiteIdAndFloorNumber(companyId, siteId, "Tầng 12")).thenReturn(false);
        when(floorRepository.save(any(WorkplaceFloor.class))).thenAnswer(invocation -> {
            WorkplaceFloor saved = invocation.getArgument(0);
            saved.setId(floorId);
            return saved;
        });

        CreateFloorRequest request = CreateFloorRequest.builder()
                .siteId(siteId)
                .floorNumber("Tầng 12")
                .name("Khối Kỹ Thuật & Sản Phẩm")
                .build();

        WorkplaceFloorResponse response = hierarchyService.createFloor(companyId, request);

        assertThat(response).isNotNull();
        assertThat(response.getFloorNumber()).isEqualTo("Tầng 12");
        assertThat(response.getName()).isEqualTo("Khối Kỹ Thuật & Sản Phẩm");
        verify(floorRepository, times(1)).save(any(WorkplaceFloor.class));
    }

    @Test
    @DisplayName("Tạo Khu Vực (Zone) thành công với enrichment OrgUnit")
    void createZone_successWithOrgUnitEnrichment() {
        WorkplaceSite site = WorkplaceSite.builder().code("S1").name("Site 1").build();
        site.setId(UUID.randomUUID());

        WorkplaceFloor floor = WorkplaceFloor.builder().site(site).floorNumber("Tầng 12").build();
        floor.setId(floorId);

        UUID departmentId = UUID.randomUUID();

        // Service calls findByIdAndCompanyIdWithSite
        when(floorRepository.findByIdAndCompanyIdWithSite(floorId, companyId)).thenReturn(Optional.of(floor));
        when(zoneRepository.save(any(WorkplaceZone.class))).thenAnswer(invocation -> {
            WorkplaceZone saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        OrgUnitSummary orgSummary = OrgUnitSummary.builder()
                .id(departmentId)
                .name("Trung tâm Phát triển Phần mềm")
                .code("SWD")
                .build();
        when(organizationalUnitService.getUnitSummaries(Set.of(departmentId)))
                .thenReturn(Map.of(departmentId, orgSummary));

        CreateZoneRequest request = CreateZoneRequest.builder()
                .floorId(floorId)
                .name("Khu vực Backend Engineering")
                .shapeType(ZoneShapeType.POLYGON)
                .organizationalUnitId(departmentId)
                .build();

        WorkplaceZoneResponse response = hierarchyService.createZone(companyId, request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Khu vực Backend Engineering");
        assertThat(response.getOrganizationalUnit()).isNotNull();
        assertThat(response.getOrganizationalUnit().getName()).isEqualTo("Trung tâm Phát triển Phần mềm");
    }
}
