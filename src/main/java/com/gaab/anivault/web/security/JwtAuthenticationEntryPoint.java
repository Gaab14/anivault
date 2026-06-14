package com.gaab.anivault.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaab.anivault.web.exception.Error;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Error error = new Error("unauthorized", "Authentication is required to access this resource.");
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}
