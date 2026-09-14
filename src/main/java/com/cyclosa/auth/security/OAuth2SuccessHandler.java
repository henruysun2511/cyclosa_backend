package com.cyclosa.auth.security;

import com.cyclosa.auth.dto.response.TokenResponse;
import com.cyclosa.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.oauth2.frontend-redirect-uri:http://localhost:5173}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oauth2User.getAttributes();

        String sub     = String.valueOf(attrs.get("sub"));
        String email   = attrs.get("email") != null ? String.valueOf(attrs.get("email")) : null;
        String name    = attrs.get("name")  != null ? String.valueOf(attrs.get("name"))  : email;
        String picture = attrs.get("picture") != null ? String.valueOf(attrs.get("picture")) : null;

        if (email == null) {
            response.sendRedirect(frontendRedirectUri + "?error=google_login_failed");
            return;
        }

        TokenResponse token = authService.oauth2Login(sub, email, name, picture);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("access_token", token.getAccessToken())
                .queryParam("refresh_token", token.getRefreshToken())
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
