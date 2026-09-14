package com.cyclosa.role;

import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.exception.ErrorCode;
import com.cyclosa.permission.entity.Permission;
import com.cyclosa.permission.service.PermissionService;
import com.cyclosa.role.dto.request.AssignRolePermissionsRequest;
import com.cyclosa.role.dto.request.RoleRequest;
import com.cyclosa.role.dto.response.RoleDetailResponse;
import com.cyclosa.role.dto.response.RoleResponse;
import com.cyclosa.role.entity.Role;
import com.cyclosa.role.mapper.RoleMapper;
import com.cyclosa.role.mapper.RoleMapperImpl;
import com.cyclosa.role.repository.RolePermissionRepository;
import com.cyclosa.role.repository.RoleRepository;
import com.cyclosa.role.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
class RoleServiceTest {

    @Mock RoleRepository              roleRepository;
    @Mock RolePermissionRepository    rolePermissionRepository;
    @Mock PermissionService           permissionService; // Calling service of permission module

    @Spy RoleMapper roleMapper = new RoleMapperImpl();

    @InjectMocks RoleService roleService;

    private Role testRole;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        testRole = Role.builder()
                .name("Line Manager")
                .code("LINE_MANAGER")
                .description("Trưởng nhóm nghiệp vụ")
                .isSystemRole(false)
                .build();
        testRole.setId(roleId);
    }

    @Nested
    @DisplayName("createRole()")
    class CreateRoleTests {

        @Test
        @DisplayName("Thành công: tạo vai trò mới hợp lệ")
        void success() {
            RoleRequest req = RoleRequest.builder()
                    .name("Line Manager")
                    .code("LINE_MANAGER")
                    .description("Trưởng nhóm nghiệp vụ")
                    .build();

            given(roleRepository.existsByCodeAndCompanyIdIsNull("LINE_MANAGER")).willReturn(false);
            given(roleRepository.save(any(Role.class))).willAnswer(inv -> {
                Role r = inv.getArgument(0);
                r.setId(roleId);
                return r;
            });

            RoleResponse res = roleService.createRole(req);

            assertThat(res.getId()).isEqualTo(roleId);
            assertThat(res.getCode()).isEqualTo("LINE_MANAGER");
            verify(roleRepository).save(any(Role.class));
        }

        @Test
        @DisplayName("Lỗi: mã vai trò đã tồn tại")
        void duplicateCode() {
            RoleRequest req = RoleRequest.builder()
                    .name("Line Manager")
                    .code("LINE_MANAGER")
                    .build();

            given(roleRepository.existsByCodeAndCompanyIdIsNull("LINE_MANAGER")).willReturn(true);

            assertThatThrownBy(() -> roleService.createRole(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT);
        }
    }

    @Nested
    @DisplayName("deleteRole()")
    class DeleteRoleTests {

        @Test
        @DisplayName("Lỗi: không cho phép xóa vai trò hệ thống")
        void cannotDeleteSystemRole() {
            testRole.setSystemRole(true);
            given(roleRepository.findById(roleId)).willReturn(Optional.of(testRole));

            assertThatThrownBy(() -> roleService.deleteRole(roleId))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("assignPermissions()")
    class AssignPermissionsTests {

        @Test
        @DisplayName("Thành công: gán quyền kèm phạm vi dữ liệu qua PermissionService")
        void success() {
            UUID permId = UUID.randomUUID();
            Permission perm = Permission.builder().code("leave.view").module("leave").action("view").build();
            perm.setId(permId);

            AssignRolePermissionsRequest.PermissionAssignment item =
                    AssignRolePermissionsRequest.PermissionAssignment.builder()
                            .permissionId(permId)
                            .dataScope(DataScope.DEPARTMENT)
                            .build();

            AssignRolePermissionsRequest req = AssignRolePermissionsRequest.builder()
                    .permissions(List.of(item))
                    .build();

            given(roleRepository.findById(roleId)).willReturn(Optional.of(testRole));
            given(permissionService.findAllById(List.of(permId))).willReturn(List.of(perm));

            RoleDetailResponse res = roleService.assignPermissions(roleId, req);

            assertThat(res.getId()).isEqualTo(roleId);
            verify(permissionService).findAllById(List.of(permId));
            verify(rolePermissionRepository).deleteByRoleId(roleId);
            verify(rolePermissionRepository).saveAll(any());
        }
    }
}
