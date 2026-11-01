package com.cyclosa.authorization.mapper;

import com.cyclosa.authorization.dto.request.CreateRoleRequest;
import com.cyclosa.authorization.dto.request.UpdateRoleRequest;
import com.cyclosa.authorization.dto.response.RolePermissionResponse;
import com.cyclosa.authorization.dto.response.RoleResponse;
import com.cyclosa.authorization.entity.Role;
import com.cyclosa.authorization.entity.RolePermission;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isSystemRole", constant = "false")
    @Mapping(target = "permissions", ignore = true)
    Role toEntity(CreateRoleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "isSystemRole", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    void updateEntityFromDto(UpdateRoleRequest request, @MappingTarget Role role);

    RoleResponse toResponse(Role role);

    List<RoleResponse> toResponseList(List<Role> roles);

    @Mapping(target = "permissionId", source = "permission.id")
    @Mapping(target = "permissionCode", source = "permission.code")
    @Mapping(target = "resource", source = "permission.resource")
    @Mapping(target = "action", source = "permission.action")
    @Mapping(target = "description", source = "permission.description")
    RolePermissionResponse toRolePermissionResponse(RolePermission rolePermission);

    List<RolePermissionResponse> toRolePermissionResponseList(List<RolePermission> rolePermissions);
}
