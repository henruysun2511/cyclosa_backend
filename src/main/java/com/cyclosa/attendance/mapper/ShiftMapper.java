package com.cyclosa.attendance.mapper;

import com.cyclosa.attendance.dto.request.CreateShiftRequest;
import com.cyclosa.attendance.dto.request.UpdateShiftRequest;
import com.cyclosa.attendance.dto.response.ShiftResponse;
import com.cyclosa.attendance.entity.Shift;
import com.cyclosa.common.dto.summary.ShiftSummary;
import org.mapstruct.*;

import java.time.Duration;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShiftMapper {

    @Mapping(target = "companyId", ignore = true)
    Shift toEntity(CreateShiftRequest request);

    ShiftResponse toResponse(Shift shift);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "activeAssignmentsCount", ignore = true)
    com.cyclosa.attendance.dto.response.ShiftDetailResponse toDetailResponse(Shift shift);

    List<ShiftResponse> toResponseList(List<Shift> shifts);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromRequest(UpdateShiftRequest request, @MappingTarget Shift shift);

    default ShiftSummary toSummary(Shift shift) {
        if (shift == null) {
            return null;
        }
        Integer breakMins = null;
        if (shift.getBreakStartTime() != null && shift.getBreakEndTime() != null) {
            breakMins = (int) Duration.between(shift.getBreakStartTime(), shift.getBreakEndTime()).toMinutes();
        }
        return ShiftSummary.builder()
                .id(shift.getId())
                .code(shift.getCode())
                .name(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .breakMinutes(breakMins)
                .build();
    }
}
