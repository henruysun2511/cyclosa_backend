package com.cyclosa.role;

import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.permission.entity.Permission;
import com.cyclosa.role.dto.request.AssignUserRolesRequest;
import com.cyclosa.role.dto.response.EffectivePermissionResponse;
import com.cyclosa.role.entity.Role;
import com.cyclosa.role.entity.RolePermission;
import com.cyclosa.role.entity.UserRoleMapping;
import com.cyclosa.role.repository.RolePermissionRepository;
import com.cyclosa.role.repository.RoleRepository;
import com.cyclosa.role.repository.UserRoleMappingRepository;
import com.cyclosa.role.service.UserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @Mock UserRoleMappingRepository userRoleMappingRepository;
    @Mock RoleRepository roleRepository;
    @Mock RolePermissionRepository rolePermissionRepository;
    @Mock UserService userService; // Calling service of auth module

    @InjectMocks UserRoleService userRoleService;

    private User testUser;
    private UUID userId;
    private Role employeeRole;
    private UUID employeeRoleId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .email("test@cyclosa.com")
                .userRoles(new ArrayList<>())
                .build();
        testUser.setId(userId);

        employeeRoleId = UUID.randomUUID();
        employeeRole = Role.builder()
                .name("Nhân viên")
                .code("EMPLOYEE")
                .isSystemRole(true)
                .build();
        employeeRole.setId(employeeRoleId);
    }

    @Nested
    @DisplayName("assignRolesToUser()")
    class AssignRolesToUserTests {

        @Test
        @DisplayName("Thành công: gán danh sách vai trò cho người dùng")
        void success() {
            AssignUserRolesRequest req = AssignUserRolesRequest.builder()
                    .roles(List.of(
                            AssignUserRolesRequest.UserRoleItem.builder()
                                    .roleId(employeeRoleId)
                                    .companyId(null)
                                    .build()
                    ))
                    .build();

            given(userService.findById(userId)).willReturn(testUser);
            given(roleRepository.findAllById(List.of(employeeRoleId))).willReturn(List.of(employeeRole));

            userRoleService.assignRolesToUser(userId, req);

            verify(userService).findById(userId);
            verify(userRoleMappingRepository).deleteByUserId(userId);
            verify(userRoleMappingRepository).saveAll(any());
            assertThat(testUser.getUserRoles()).hasSize(1);
        }

        @Test
        @DisplayName("Lỗi: vai trò không tồn tại")
        void roleNotFound() {
            UUID unknownRoleId = UUID.randomUUID();
            AssignUserRolesRequest req = AssignUserRolesRequest.builder()
                    .roles(List.of(
                            AssignUserRolesRequest.UserRoleItem.builder()
                                    .roleId(unknownRoleId)
                                    .build()
                    ))
                    .build();

            given(userService.findById(userId)).willReturn(testUser);
            given(roleRepository.findAllById(List.of(unknownRoleId))).willReturn(List.of());

            assertThatThrownBy(() -> userRoleService.assignRolesToUser(userId, req))
                    .isInstanceOf(AppException.class);
        }
    }

    @Nested
    @DisplayName("assignDefaultRoleToUser()")
    class AssignDefaultRoleTests {

        @Test
        @DisplayName("Thành công: gán vai trò mặc định")
        void success() {
            given(userService.findById(userId)).willReturn(testUser);
            given(roleRepository.findByCodeAndCompanyIdIsNull("EMPLOYEE")).willReturn(Optional.of(employeeRole));

            userRoleService.assignDefaultRoleToUser(userId, "EMPLOYEE", null);

            verify(userService).findById(userId);
            verify(userRoleMappingRepository).save(any(UserRoleMapping.class));
            assertThat(testUser.getUserRoles()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getEffectivePermissions()")
    class GetEffectivePermissionsTests {

        @Test
        @DisplayName("Thành công: gộp quyền và lấy DataScope cao nhất")
        void mergesWithHighestDataScope() {
            Role role1 = Role.builder().code("EMPLOYEE").build();
            role1.setId(UUID.randomUUID());
            Role role2 = Role.builder().code("HR_MANAGER").build();
            role2.setId(UUID.randomUUID());

            UserRoleMapping urm1 = UserRoleMapping.builder().user(testUser).role(role1).build();
            UserRoleMapping urm2 = UserRoleMapping.builder().user(testUser).role(role2).build();

            Permission perm = Permission.builder()
                    .code("leave.view")
                    .module("leave")
                    .action("view")
                    .description("Xem đơn nghỉ phép")
                    .build();
            perm.setId(UUID.randomUUID());

            // Role1 grants leave.view with OWN scope
            RolePermission rp1 = RolePermission.builder()
                    .role(role1)
                    .permission(perm)
                    .dataScope(DataScope.OWN)
                    .build();

            // Role2 grants leave.view with COMPANY scope (higher scope)
            RolePermission rp2 = RolePermission.builder()
                    .role(role2)
                    .permission(perm)
                    .dataScope(DataScope.COMPANY)
                    .build();

            given(userRoleMappingRepository.findByUserIdWithRole(userId)).willReturn(List.of(urm1, urm2));
            given(rolePermissionRepository.findByRoleIdInWithPermission(any())).willReturn(List.of(rp1, rp2));

            List<EffectivePermissionResponse> result = userRoleService.getEffectivePermissions(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPermissionCode()).isEqualTo("leave.view");
            assertThat(result.get(0).getDataScope()).isEqualTo(DataScope.COMPANY);
        }

        @Test
        @DisplayName("Trả về danh sách rỗng khi người dùng chưa có vai trò nào")
        void returnsEmptyWhenNoRoles() {
            given(userRoleMappingRepository.findByUserIdWithRole(userId)).willReturn(List.of());

            List<EffectivePermissionResponse> result = userRoleService.getEffectivePermissions(userId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserRoleCodes()")
    class GetUserRoleCodesTests {

        @Test
        @DisplayName("Thành công: lấy danh sách mã vai trò duy nhất")
        void returnsDistinctRoleCodes() {
            UserRoleMapping urm = UserRoleMapping.builder().user(testUser).role(employeeRole).build();
            given(userRoleMappingRepository.findByUserIdWithRole(userId)).willReturn(List.of(urm));

            List<String> codes = userRoleService.getUserRoleCodes(userId);

            assertThat(codes).containsExactly("EMPLOYEE");
        }
    }
}
