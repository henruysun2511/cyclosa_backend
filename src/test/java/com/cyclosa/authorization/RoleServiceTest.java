package com.cyclosa.authorization;

import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.authorization.dto.request.AssignRolePermissionsRequest;
import com.cyclosa.authorization.dto.request.CreateRoleRequest;
import com.cyclosa.authorization.dto.request.UpdateRoleRequest;
import com.cyclosa.authorization.dto.response.RoleResponse;
import com.cyclosa.authorization.entity.Permission;
import com.cyclosa.authorization.entity.Role;
import com.cyclosa.authorization.entity.RolePermission;
import com.cyclosa.authorization.exception.RoleErrorCode;
import com.cyclosa.authorization.mapper.RoleMapper;
import com.cyclosa.authorization.repository.PermissionRepository;
import com.cyclosa.authorization.repository.RoleDocumentTypePermissionRepository;
import com.cyclosa.authorization.repository.RolePermissionRepository;
import com.cyclosa.authorization.repository.RoleRepository;
import com.cyclosa.authorization.repository.UserRoleAssignmentRepository;
import com.cyclosa.authorization.security.RoleCacheService;
import com.cyclosa.authorization.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private RoleDocumentTypePermissionRepository roleDocumentTypePermissionRepository;

    @Mock
    private RoleCacheService roleCacheService;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        testRole = Role.builder()
                .code("HR_STAFF")
                .name("Nhân viên Nhân sự")
                .description("Quản lý hồ sơ nhân sự")
                .isSystemRole(false)
                .deleted(false)
                .build();
        testRole.setId(roleId);
    }

    @Test
    @DisplayName("Tạo vai trò thành công khi mã chưa tồn tại")
    void createRole_Success() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setCode("hr_staff");
        request.setName("Nhân viên Nhân sự");

        when(roleRepository.existsByCodeAndCompanyIdIsNullAndDeletedFalse("HR_STAFF")).thenReturn(false);
        when(roleMapper.toEntity(request)).thenReturn(testRole);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        when(roleMapper.toResponse(testRole)).thenReturn(RoleResponse.builder()
                .id(roleId)
                .code("HR_STAFF")
                .name("Nhân viên Nhân sự")
                .build());

        RoleResponse response = roleService.createRole(request);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("HR_STAFF");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi tạo vai trò với mã đã tồn tại")
    void createRole_ThrowsException_WhenCodeExists() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setCode("HR_STAFF");

        when(roleRepository.existsByCodeAndCompanyIdIsNullAndDeletedFalse("HR_STAFF")).thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", RoleErrorCode.ROLE_CODE_EXISTS);

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cập nhật vai trò thành công")
    void updateRole_Success() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("Tên mới");
        request.setDescription("Mô tả mới");

        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(testRole)).thenReturn(testRole);
        when(roleMapper.toResponse(testRole)).thenReturn(RoleResponse.builder()
                .id(roleId)
                .name("Tên mới")
                .build());

        RoleResponse response = roleService.updateRole(roleId, request);

        assertThat(response.getName()).isEqualTo("Tên mới");
        verify(roleMapper).updateEntityFromDto(request, testRole);
    }

    @Test
    @DisplayName("Cập nhật tên và mô tả vai trò hệ thống thành công")
    void updateRole_Success_WhenSystemRole() {
        testRole.setIsSystemRole(true);
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(testRole)).thenReturn(testRole);
        when(roleMapper.toResponse(testRole)).thenReturn(RoleResponse.builder()
                .id(roleId)
                .name("Tên hiển thị mới")
                .build());

        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("Tên hiển thị mới");

        RoleResponse response = roleService.updateRole(roleId, request);
        assertThat(response.getName()).isEqualTo("Tên hiển thị mới");
        verify(roleRepository).save(testRole);
    }

    @Test
    @DisplayName("Xóa mềm vai trò thành công khi chưa có user nắm giữ")
    void deleteRole_Success() {
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(testRole));
        when(userRoleAssignmentRepository.countByRoleId(roleId)).thenReturn(0L);

        roleService.deleteRole(roleId);

        verify(roleRepository).delete(testRole);
        verify(roleCacheService).evictRolePermissions(roleId);
    }

    @Test
    @DisplayName("Không được xóa vai trò khi đang có người dùng nắm giữ")
    void deleteRole_ThrowsException_WhenRoleInUse() {
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(testRole));
        when(userRoleAssignmentRepository.countByRoleId(roleId)).thenReturn(3L);

        assertThatThrownBy(() -> roleService.deleteRole(roleId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", RoleErrorCode.ROLE_IN_USE);

        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    @DisplayName("Gán quyền cho vai trò thành công và xóa cache")
    void assignPermissions_Success() {
        UUID permId = UUID.randomUUID();
        Permission permission = Permission.builder().code("EMPLOYEE_VIEW").build();
        permission.setId(permId);

        AssignRolePermissionsRequest.PermissionScopeItem item = new AssignRolePermissionsRequest.PermissionScopeItem();
        item.setPermissionId(permId);
        item.setDataScope(DataScope.COMPANY);

        AssignRolePermissionsRequest request = new AssignRolePermissionsRequest();
        request.setPermissions(List.of(item));

        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByIdIn(Set.of(permId))).thenReturn(List.of(permission));
        when(rolePermissionRepository.saveAll(anyList())).thenReturn(List.of());
        when(userRoleAssignmentRepository.findUserIdsByRoleId(roleId)).thenReturn(List.of(UUID.randomUUID()));

        roleService.assignPermissions(roleId, request);

        verify(rolePermissionRepository).deleteByRoleId(roleId);
        verify(rolePermissionRepository).saveAll(anyList());
        verify(roleCacheService).evictRolePermissions(roleId);
        verify(roleCacheService).evictUsersPermissions(anyList());
    }
}
