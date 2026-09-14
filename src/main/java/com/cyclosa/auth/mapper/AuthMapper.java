package com.cyclosa.auth.mapper;

import com.cyclosa.auth.dto.response.UserInfo;
import com.cyclosa.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthMapper {

    @Mapping(target = "roles", expression = "java(user.getRoleCodes())")
    UserInfo toUserInfo(User user);
}
