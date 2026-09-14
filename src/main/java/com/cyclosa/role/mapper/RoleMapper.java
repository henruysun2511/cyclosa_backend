package com.cyclosa.role.mapper;

import com.cyclosa.role.dto.request.RoleRequest;
import com.cyclosa.role.dto.response.RoleDetailResponse;
import com.cyclosa.role.dto.response.RoleResponse;
import com.cyclosa.role.entity.Role;
import com.cyclosa.role.entity.RolePermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleMapper {

    @Mapping(target = "isSystemRole", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    Role toEntity(RoleRequest request);

    @Mapping(target = "isSystemRole", source = "systemRole")
    RoleResponse toResponse(Role role);

    List<RoleResponse> toResponseList(List<Role> roles);

    @Mapping(target = "permissions", source = "rolePermissions")
    @Mapping(target = "isSystemRole", source = "systemRole")
    RoleDetailResponse toDetailResponse(Role role);

    @Mapping(target = "permissionId", source = "permission.id")
    @Mapping(target = "permissionCode", source = "permission.code")
    @Mapping(target = "module", source = "permission.module")
    @Mapping(target = "action", source = "permission.action")
    @Mapping(target = "description", source = "permission.description")
    @Mapping(target = "dataScope", source = "dataScope")
    RoleDetailResponse.RolePermissionItem toRolePermissionItem(RolePermission rolePermission);
}
