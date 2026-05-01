package com.ally.blogapp.security;

import com.ally.blogapp.dto.response.ApiResponse;
import com.ally.blogapp.dto.response.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    public OAuth2AuthenticationSuccessHandler(JwtUtils jwtUtils, ObjectMapper objectMapper) {
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String role = authentication.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");

        String token = jwtUtils.generateToken(email, role);

        AuthResponse authResponse = new AuthResponse(token, email, role, jwtUtils.getExpirationMs() / 1000);
        ApiResponse<AuthResponse> body = ApiResponse.success("OAuth2 login successful", authResponse);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
