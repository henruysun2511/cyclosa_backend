package com.cyclosa.auth.dto.response;

import com.cyclosa.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private UUID id;
    private String email;
    private String fullName;
    private List<String> roles;
    private String avatarUrl;
    private UserStatus status;
    private UUID employeeId;
}
