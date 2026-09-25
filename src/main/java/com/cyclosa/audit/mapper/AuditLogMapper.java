package com.cyclosa.audit.mapper;

import com.cyclosa.audit.dto.request.AuditLogCommand;
import com.cyclosa.audit.dto.response.AuditLogDetailResponse;
import com.cyclosa.audit.dto.response.AuditLogResponse;
import com.cyclosa.audit.entity.AuditLog;
import com.cyclosa.audit.event.AuditLogEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    AuditLog toEntity(AuditLogCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    AuditLog toEntity(AuditLogEvent event);

    @Mapping(target = "user", ignore = true)
    AuditLogResponse toResponse(AuditLog auditLog);

    @Mapping(target = "user", ignore = true)
    AuditLogDetailResponse toDetailResponse(AuditLog auditLog);
}
