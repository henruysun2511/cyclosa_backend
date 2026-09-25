package com.cyclosa.notification.mapper;

import com.cyclosa.notification.dto.request.CreateNotificationRequest;
import com.cyclosa.notification.dto.response.NotificationResponse;
import com.cyclosa.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {

    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "readAt", ignore = true)
    Notification toEntity(CreateNotificationRequest request);

    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}
