package com.cyclosa.auth.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nhà cung cấp danh tính liên kết OAuth2")
public enum OAuthProvider {
    @Schema(description = "Google OAuth2 SSO")
    GOOGLE,

    @Schema(description = "Microsoft Azure AD SSO")
    MICROSOFT
}

