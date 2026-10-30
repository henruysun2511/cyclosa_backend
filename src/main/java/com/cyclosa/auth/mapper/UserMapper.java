package com.cyclosa.auth.mapper;

import com.cyclosa.auth.dto.response.UserDetailResponse;
import com.cyclosa.auth.dto.response.UserProfileResponse;
import com.cyclosa.auth.dto.response.UserResponse;
import com.cyclosa.auth.entity.OAuthAccount;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.enums.OAuthProvider;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "linkedProviders", source = "oauthAccounts", qualifiedByName = "mapOAuthProviders")
    UserDetailResponse toDetailResponse(User user);

    @Mapping(target = "roles", ignore = true)
    UserProfileResponse toProfileResponse(User user);

    @Named("mapOAuthProviders")
    default List<OAuthProvider> mapOAuthProviders(List<OAuthAccount> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return Collections.emptyList();
        }
        return accounts.stream().map(OAuthAccount::getProvider).toList();
    }
}

