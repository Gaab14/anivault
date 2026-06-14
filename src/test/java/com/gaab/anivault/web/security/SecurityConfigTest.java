package com.gaab.anivault.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaab.anivault.domain.dto.AuthResponseDto;
import com.gaab.anivault.domain.dto.RegisterRequestDto;
import com.gaab.anivault.domain.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Test
    void authEndpoints_arePublic() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "publicUser" + System.nanoTime(), "public" + System.nanoTime() + "@email.com", "password123"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void animeEndpoints_requireAuth() throws Exception {
        mockMvc.perform(get("/animes"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/animes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwt_returns401() throws Exception {
        mockMvc.perform(get("/animes")
                        .header("Authorization", "Bearer totally.invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredJwt_returns401() throws Exception {
        // Crear un servicio con expiración 0 para generar un token expirado
        JwtService expiredService = new JwtService(
                "anivault-test-secret-key-that-is-at-least-32-characters-long", 0L
        );
        com.gaab.anivault.web.security.CustomUserDetails userDetails =
                new CustomUserDetails(1L, "testuser", "pass",
                        com.gaab.anivault.domain.enums.Role.USER);
        String expiredToken = expiredService.generateToken(userDetails, 1L);

        mockMvc.perform(get("/animes")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validJwt_canAccessProtectedEndpoints() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "secUser" + System.nanoTime(), "sec" + System.nanoTime() + "@email.com", "password123"
        );
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponseDto.class);

        mockMvc.perform(get("/animes")
                        .header("Authorization", "Bearer " + response.token()))
                .andExpect(status().isOk());
    }

    @Test
    void unauthorizedResponse_hasJsonBody() throws Exception {
        mockMvc.perform(get("/animes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("unauthorized"));
    }
}
