package com.cyclosa.workplace.mapper;

import com.cyclosa.workplace.dto.response.*;
import com.cyclosa.workplace.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkplaceMapper {

    @Mapping(target = "branch", ignore = true)
    WorkplaceSiteResponse toSiteResponse(WorkplaceSite site);

    List<WorkplaceSiteResponse> toSiteResponseList(List<WorkplaceSite> sites);

    @Mapping(target = "siteId", source = "site.id")
    @Mapping(target = "siteName", source = "site.name")
    @Mapping(target = "siteCode", source = "site.code")
    WorkplaceFloorResponse toFloorResponse(WorkplaceFloor floor);

    List<WorkplaceFloorResponse> toFloorResponseList(List<WorkplaceFloor> floors);

    @Mapping(target = "floorId", source = "floor.id")
    @Mapping(target = "floorNumber", source = "floor.floorNumber")
    @Mapping(target = "organizationalUnit", ignore = true)
    WorkplaceZoneResponse toZoneResponse(WorkplaceZone zone);

    List<WorkplaceZoneResponse> toZoneResponseList(List<WorkplaceZone> zones);

    @Mapping(target = "zoneId", source = "zone.id")
    @Mapping(target = "zoneName", source = "zone.name")
    WorkplaceRowResponse toRowResponse(WorkplaceRow row);

    List<WorkplaceRowResponse> toRowResponseList(List<WorkplaceRow> rows);

    @Mapping(target = "rowId", source = "row.id")
    @Mapping(target = "rowName", source = "row.name")
    @Mapping(target = "zoneId", source = "row.zone.id")
    @Mapping(target = "zoneName", source = "row.zone.name")
    @Mapping(target = "floorId", source = "row.zone.floor.id")
    @Mapping(target = "floorNumber", source = "row.zone.floor.floorNumber")
    @Mapping(target = "siteId", source = "row.zone.floor.site.id")
    @Mapping(target = "siteName", source = "row.zone.floor.site.name")
    @Mapping(target = "assignedEmployee", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "linkedAssetCount", ignore = true)
    WorkplaceSeatResponse toSeatResponse(WorkplaceSeat seat);

    List<WorkplaceSeatResponse> toSeatResponseList(List<WorkplaceSeat> seats);

    @Mapping(target = "zoneId", source = "zone.id")
    @Mapping(target = "zoneName", source = "zone.name")
    @Mapping(target = "floorId", source = "zone.floor.id")
    @Mapping(target = "floorNumber", source = "zone.floor.floorNumber")
    WorkplaceNonDeskObjectResponse toNonDeskObjectResponse(WorkplaceNonDeskObject object);

    List<WorkplaceNonDeskObjectResponse> toNonDeskObjectResponseList(List<WorkplaceNonDeskObject> objects);

    @Mapping(target = "floorId", source = "floor.id")
    @Mapping(target = "floorNumber", source = "floor.floorNumber")
    WorkplaceFloorPlanResponse toFloorPlanResponse(WorkplaceFloorPlan plan);

    List<WorkplaceFloorPlanResponse> toFloorPlanResponseList(List<WorkplaceFloorPlan> plans);

    @Mapping(target = "seatId", source = "seat.id")
    @Mapping(target = "seatCode", source = "seat.seatCode")
    @Mapping(target = "floorNumber", source = "seat.row.zone.floor.floorNumber")
    @Mapping(target = "zoneName", source = "seat.row.zone.name")
    @Mapping(target = "employee", ignore = true)
    HotdeskBookingResponse toHotdeskBookingResponse(WorkplaceHotdeskBooking booking);

    List<HotdeskBookingResponse> toHotdeskBookingResponseList(List<WorkplaceHotdeskBooking> bookings);

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomName", source = "room.name")
    @Mapping(target = "roomCapacity", source = "room.meetingRoomCapacity")
    @Mapping(target = "floorNumber", source = "room.zone.floor.floorNumber")
    @Mapping(target = "zoneName", source = "room.zone.name")
    @Mapping(target = "bookedByEmployee", ignore = true)
    RoomBookingResponse toRoomBookingResponse(WorkplaceRoomBooking booking);

    List<RoomBookingResponse> toRoomBookingResponseList(List<WorkplaceRoomBooking> bookings);

    @Mapping(target = "seatId", source = "seat.id")
    @Mapping(target = "seatCode", source = "seat.seatCode")
    @Mapping(target = "nonDeskObjectId", source = "nonDeskObject.id")
    @Mapping(target = "nonDeskObjectName", source = "nonDeskObject.name")
    @Mapping(target = "floorId", source = "floor.id")
    @Mapping(target = "floorNumber", source = "floor.floorNumber")
    @Mapping(target = "reportedByEmployee", ignore = true)
    IssueTicketResponse toIssueTicketResponse(WorkplaceIssueTicket ticket);

    List<IssueTicketResponse> toIssueTicketResponseList(List<WorkplaceIssueTicket> tickets);

    default java.time.Instant map(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();
    }

    default java.time.LocalDateTime map(java.time.Instant instant) {
        if (instant == null) return null;
        return java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
    }
}
