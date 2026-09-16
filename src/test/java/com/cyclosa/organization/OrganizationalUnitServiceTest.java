package com.cyclosa.organization;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.organization.dto.request.CreateOrgUnitRequest;
import com.cyclosa.organization.dto.request.MoveOrgUnitRequest;
import com.cyclosa.organization.dto.response.OrgUnitImpactPreviewResponse;
import com.cyclosa.organization.dto.response.OrgUnitResponse;
import com.cyclosa.organization.dto.response.OrgUnitTreeResponse;
import com.cyclosa.organization.entity.OrganizationalUnit;
import com.cyclosa.organization.enums.UnitType;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.OrganizationalUnitMapper;
import com.cyclosa.organization.mapper.OrganizationalUnitMapperImpl;
import com.cyclosa.organization.repository.CostCenterRepository;
import com.cyclosa.organization.repository.OrganizationalUnitRepository;
import com.cyclosa.organization.service.impl.OrganizationalUnitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrganizationalUnitServiceTest {

    @Mock OrganizationalUnitRepository orgUnitRepository;
    @Mock CostCenterRepository costCenterRepository;
    @Spy OrganizationalUnitMapper orgUnitMapper = new OrganizationalUnitMapperImpl();

    @InjectMocks OrganizationalUnitServiceImpl orgUnitService;

    private UUID companyId;
    private UUID rootId;
    private UUID deptId;
    private UUID teamId;
    private OrganizationalUnit rootUnit;
    private OrganizationalUnit deptUnit;
    private OrganizationalUnit teamUnit;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        rootId = UUID.randomUUID();
        deptId = UUID.randomUUID();
        teamId = UUID.randomUUID();

        rootUnit = OrganizationalUnit.builder()
                .code("DIV_TECH")
                .name("Khối Công nghệ")
                .unitType(UnitType.DIVISION)
                .companyId(companyId)
                .build();
        rootUnit.setId(rootId);

        deptUnit = OrganizationalUnit.builder()
                .code("DEPT_DEV")
                .name("Phòng Phát triển Phần mềm")
                .unitType(UnitType.DEPARTMENT)
                .companyId(companyId)
                .parentUnit(rootUnit)
                .build();
        deptUnit.setId(deptId);

        teamUnit = OrganizationalUnit.builder()
                .code("TEAM_BACKEND")
                .name("Nhóm Backend")
                .unitType(UnitType.TEAM)
                .companyId(companyId)
                .parentUnit(deptUnit)
                .build();
        teamUnit.setId(teamId);
    }

    @Test
    @DisplayName("Tạo đơn vị tổ chức thành công")
    void createUnit_Success() {
        CreateOrgUnitRequest request = new CreateOrgUnitRequest();
        request.setCode("DEPT_HR");
        request.setName("Phòng Nhân sự");
        request.setUnitType(UnitType.DEPARTMENT);
        request.setCompanyId(companyId);

        given(orgUnitRepository.existsByCodeAndCompanyId("DEPT_HR", companyId)).willReturn(false);
        given(orgUnitRepository.save(any(OrganizationalUnit.class))).willAnswer(inv -> {
            OrganizationalUnit u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        OrgUnitResponse response = orgUnitService.createUnit(companyId, request);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("DEPT_HR");
        assertThat(response.getName()).isEqualTo("Phòng Nhân sự");
    }

    @Test
    @DisplayName("Tạo đơn vị thất bại khi trùng mã code")
    void createUnit_Throws_WhenCodeExists() {
        CreateOrgUnitRequest request = new CreateOrgUnitRequest();
        request.setCode("DIV_TECH");
        request.setName("Khối Công nghệ");
        request.setUnitType(UnitType.DIVISION);
        request.setCompanyId(companyId);

        given(orgUnitRepository.existsByCodeAndCompanyId("DIV_TECH", companyId)).willReturn(true);

        assertThatThrownBy(() -> orgUnitService.createUnit(companyId, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(OrganizationErrorCode.ORG_UNIT_CODE_EXISTS));
    }

    @Test
    @DisplayName("Dựng cây phân cấp phòng ban chính xác (Root -> Dept -> Team)")
    void getUnitTree_BuildsHierarchyCorrectly() {
        given(orgUnitRepository.findAllByCompanyIdWithDetails(companyId))
                .willReturn(List.of(rootUnit, deptUnit, teamUnit));

        List<OrgUnitTreeResponse> tree = orgUnitService.getUnitTree(companyId);

        assertThat(tree).hasSize(1); // 1 Root node
        OrgUnitTreeResponse rootNode = tree.get(0);
        assertThat(rootNode.getId()).isEqualTo(rootId);
        assertThat(rootNode.getChildren()).hasSize(1); // Dept

        OrgUnitTreeResponse deptNode = rootNode.getChildren().get(0);
        assertThat(deptNode.getId()).isEqualTo(deptId);
        assertThat(deptNode.getChildren()).hasSize(1); // Team

        OrgUnitTreeResponse teamNode = deptNode.getChildren().get(0);
        assertThat(teamNode.getId()).isEqualTo(teamId);
        assertThat(teamNode.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("Dự báo tác động phát hiện vòng lặp khi chuyển đơn vị cha vào con của nó")
    void getImpactPreview_DetectsCircularLoop() {
        given(orgUnitRepository.findById(rootId)).willReturn(Optional.of(rootUnit));
        given(orgUnitRepository.findById(teamId)).willReturn(Optional.of(teamUnit));

        // Cố gắng chuyển Root (Khối Công nghệ) vào làm con của Team (Nhóm Backend)
        OrgUnitImpactPreviewResponse preview = orgUnitService.getImpactPreview(companyId, rootId, teamId);

        assertThat(preview.isCircularLoop()).isTrue();
        assertThat(preview.getMessage()).contains("vòng lặp phân cấp");
    }

    @Test
    @DisplayName("Điều chuyển đơn vị ném ngoại lệ khi có vòng lặp phân cấp")
    void moveUnit_Throws_WhenCircularLoop() {
        given(orgUnitRepository.findById(rootId)).willReturn(Optional.of(rootUnit));
        given(orgUnitRepository.findById(teamId)).willReturn(Optional.of(teamUnit));

        MoveOrgUnitRequest request = new MoveOrgUnitRequest();
        request.setTargetParentId(teamId);

        assertThatThrownBy(() -> orgUnitService.moveUnit(companyId, rootId, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(OrganizationErrorCode.CIRCULAR_PARENT_DEPENDENCY));
    }

    @Test
    @DisplayName("Xóa đơn vị thất bại khi đang có đơn vị con trực thuộc")
    void deleteUnit_Throws_WhenHasChildren() {
        given(orgUnitRepository.findById(rootId)).willReturn(Optional.of(rootUnit));
        given(orgUnitRepository.countByParentUnitId(rootId)).willReturn(1L);

        assertThatThrownBy(() -> orgUnitService.deleteUnit(companyId, rootId))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(OrganizationErrorCode.CANNOT_DELETE_UNIT_WITH_CHILDREN));
    }

    @Test
    @DisplayName("Xóa đơn vị thành công khi không có đơn vị con")
    void deleteUnit_Success() {
        given(orgUnitRepository.findById(teamId)).willReturn(Optional.of(teamUnit));
        given(orgUnitRepository.countByParentUnitId(teamId)).willReturn(0L);

        orgUnitService.deleteUnit(companyId, teamId);

        verify(orgUnitRepository).delete(teamUnit);
    }
}
