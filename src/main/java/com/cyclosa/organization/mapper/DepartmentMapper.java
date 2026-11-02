package com.cyclosa.organization.mapper;

import com.cyclosa.organization.dto.response.DepartmentResponse;
import com.cyclosa.organization.dto.response.DepartmentVersionResponse;
import com.cyclosa.organization.entity.Department;
import com.cyclosa.organization.entity.DepartmentVersion;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DepartmentMapper {

    @Mapping(target = "id", source = "department.id")
    @Mapping(target = "companyId", source = "department.companyId")
    @Mapping(target = "code", source = "department.code")
    @Mapping(target = "name", source = "version.name")
    @Mapping(target = "parentDepartmentId", source = "version.parentDepartmentId")
    @Mapping(target = "parentDepartmentName", source = "parentDepartmentName")
    @Mapping(target = "costCenterId", source = "version.costCenterId")
    @Mapping(target = "costCenterName", source = "costCenterName")
    @Mapping(target = "managerEmployeeId", source = "version.managerEmployeeId")
    @Mapping(target = "isPermissionInherited", source = "version.isPermissionInherited")
    @Mapping(target = "effectiveFrom", source = "version.effectiveFrom")
    @Mapping(target = "dissolvedDate", source = "department.dissolvedDate")
    @Mapping(target = "isActive", expression = "java(department.getDissolvedDate() == null || department.getDissolvedDate().isAfter(java.time.LocalDate.now()))")
    @Mapping(target = "createdAt", source = "department.createdAt")
    @Mapping(target = "updatedAt", source = "department.updatedAt")
    DepartmentResponse toResponse(Department department, DepartmentVersion version, String parentDepartmentName, String costCenterName);

    @Mapping(target = "id", source = "version.id")
    @Mapping(target = "departmentId", source = "version.department.id")
    @Mapping(target = "parentDepartmentName", source = "parentDepartmentName")
    @Mapping(target = "costCenterName", source = "costCenterName")
    DepartmentVersionResponse toVersionResponse(DepartmentVersion version, String parentDepartmentName, String costCenterName);
}

