package com.cyclosa.authorization;

import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.authorization.dto.request.AssignUserRoleRequest;
import com.cyclosa.authorization.dto.response.UserRoleAssignmentResponse;
import com.cyclosa.authorization.entity.Permission;
import com.cyclosa.authorization.entity.Role;
import com.cyclosa.authorization.entity.RolePermission;
import com.cyclosa.authorization.entity.UserRoleAssignment;
import com.cyclosa.authorization.exception.RoleErrorCode;
import com.cyclosa.authorization.repository.RolePermissionRepository;
import com.cyclosa.authorization.repository.RoleRepository;
import com.cyclosa.authorization.repository.UserRoleAssignmentRepository;
import com.cyclosa.authorization.security.RoleCacheService;
import com.cyclosa.authorization.service.UserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private RoleCacheService roleCacheService;

    @InjectMocks
    private UserRoleService userRoleService;

    private UUID userId;
    private UUID companyId;
    private UUID roleId;
    private User sampleUser;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        sampleUser = User.builder().email("nhanvien@cyclosa.com").build();
        sampleUser.setId(userId);

        sampleRole = Role.builder().code("MANAGER").name("Cán bộ Quản lý").build();
        sampleRole.setId(roleId);
    }

    @Test
    @DisplayName("Gán vai trò cho người dùng thành công và xóa Cache Redis")
    void assignRoleToUser_Success() {
        AssignUserRoleRequest request = new AssignUserRoleRequest();
        request.setRoleId(roleId);
        request.setCompanyId(companyId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(sampleRole));
        when(userRoleAssignmentRepository.existsByUserIdAndRoleIdAndCompanyId(userId, roleId, companyId)).thenReturn(false);

        UserRoleAssignment saved = UserRoleAssignment.builder()
                .userId(userId)
                .role(sampleRole)
                .companyId(companyId)
                .build();
        saved.setId(UUID.randomUUID());
        when(userRoleAssignmentRepository.save(any(UserRoleAssignment.class))).thenReturn(saved);

        UserRoleAssignmentResponse response = userRoleService.assignRoleToUser(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.getRoleCode()).isEqualTo("MANAGER");
        verify(userRoleAssignmentRepository).save(any(UserRoleAssignment.class));
        verify(roleCacheService).evictUserPermissions(userId, companyId);
    }

    @Test
    @DisplayName("Gán vai trò thất bại ném lỗi 409 khi đã được gán trước đó")
    void assignRoleToUser_ThrowsConflict_WhenAlreadyAssigned() {
        AssignUserRoleRequest request = new AssignUserRoleRequest();
        request.setRoleId(roleId);
        request.setCompanyId(companyId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(sampleRole));
        when(userRoleAssignmentRepository.existsByUserIdAndRoleIdAndCompanyId(userId, roleId, companyId)).thenReturn(true);

        assertThatThrownBy(() -> userRoleService.assignRoleToUser(userId, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", RoleErrorCode.USER_ROLE_ASSIGNMENT_EXISTS);

        verify(userRoleAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tính toán quyền hiệu lực đúng theo nguyên tắc max(DataScope) khi kiêm nhiệm vai trò")
    void calculateEffectivePermissions_ResolvesMaxDataScope() {
        // Giả lập Cache Miss
        when(roleCacheService.getEffectivePermissions(userId, companyId)).thenReturn(Optional.empty());

        // Người dùng kiêm nhiệm 2 vai trò: EMPLOYEE và TEAM_LEAD
        Role roleEmployee = Role.builder().code("EMPLOYEE").build();
        roleEmployee.setId(UUID.randomUUID());

        Role roleTeamLead = Role.builder().code("TEAM_LEAD").build();
        roleTeamLead.setId(UUID.randomUUID());

        UserRoleAssignment a1 = UserRoleAssignment.builder().userId(userId).role(roleEmployee).companyId(companyId).build();
        UserRoleAssignment a2 = UserRoleAssignment.builder().userId(userId).role(roleTeamLead).companyId(companyId).build();
        when(userRoleAssignmentRepository.findAllWithRoleByUserIdAndCompanyId(userId, companyId)).thenReturn(List.of(a1, a2));

        // Cả 2 role đều có quyền 'leave.view', nhưng roleEmployee có scope OWN (1), roleTeamLead có scope TEAM (2)
        Permission permLeaveView = Permission.builder().code("leave.view").build();
        RolePermission rp1 = RolePermission.builder().role(roleEmployee).permission(permLeaveView).dataScope(DataScope.OWN).build();
        RolePermission rp2 = RolePermission.builder().role(roleTeamLead).permission(permLeaveView).dataScope(DataScope.TEAM).build();

        // Thêm quyền riêng của TeamLead: 'leave.approve' với scope TEAM
        Permission permLeaveApprove = Permission.builder().code("leave.approve").build();
        RolePermission rp3 = RolePermission.builder().role(roleTeamLead).permission(permLeaveApprove).dataScope(DataScope.TEAM).build();

        when(rolePermissionRepository.findAllWithPermissionByRoleIdIn(anyList()))
                .thenReturn(List.of(rp1, rp2, rp3));

        Map<String, DataScope> effectivePerms = userRoleService.calculateEffectivePermissions(userId, companyId);

        assertThat(effectivePerms).isNotNull();
        assertThat(effectivePerms).hasSize(2);
        // leave.view phải là TEAM (max giữa OWN và TEAM)
        assertThat(effectivePerms.get("leave.view")).isEqualTo(DataScope.TEAM);
        // leave.approve phải là TEAM
        assertThat(effectivePerms.get("leave.approve")).isEqualTo(DataScope.TEAM);

        // Đảm bảo kết quả được lưu vào cache Redis
        verify(roleCacheService).cacheEffectivePermissions(eq(userId), eq(companyId), anyMap());
    }
}

