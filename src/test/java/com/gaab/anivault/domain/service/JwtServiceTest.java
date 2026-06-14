package com.gaab.anivault.domain.service;

import com.gaab.anivault.domain.enums.Role;
import com.gaab.anivault.web.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Secret de al menos 32 caracteres para HMAC-SHA256
        String secret = "test-secret-key-that-is-at-least-32-characters-long";
        long expiration = 86400000L; // 24 horas
        jwtService = new JwtService(secret, expiration);
    }

    private CustomUserDetails createUserDetails(Long userId, String username) {
        return new CustomUserDetails(userId, username, "password", Role.USER);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        UserDetails userDetails = createUserDetails(1L, "testuser");

        String token = jwtService.generateToken(userDetails, 1L);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        UserDetails userDetails = createUserDetails(1L, "testuser");
        String token = jwtService.generateToken(userDetails, 1L);

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals("testuser", extractedUsername);
    }

    @Test
    void extractUserId_returnsCorrectUserId() {
        UserDetails userDetails = createUserDetails(42L, "testuser");
        String token = jwtService.generateToken(userDetails, 42L);

        Long extractedUserId = jwtService.extractUserId(token);

        assertEquals(42L, extractedUserId);
    }

    @Test
    void isTokenValid_returnsTrueForValidToken() {
        UserDetails userDetails = createUserDetails(1L, "testuser");
        String token = jwtService.generateToken(userDetails, 1L);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        // Crear JwtService con expiración de 0ms (token expira inmediatamente)
        JwtService expiredJwtService = new JwtService(
                "test-secret-key-that-is-at-least-32-characters-long", 0L
        );
        UserDetails userDetails = createUserDetails(1L, "testuser");
        String token = expiredJwtService.generateToken(userDetails, 1L);

        assertThrows(Exception.class, () -> expiredJwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_returnsFalseForWrongUsername() {
        UserDetails userA = createUserDetails(1L, "userA");
        UserDetails userB = createUserDetails(2L, "userB");
        String token = jwtService.generateToken(userA, 1L);

        boolean isValid = jwtService.isTokenValid(token, userB);

        assertFalse(isValid);
    }

    @Test
    void extractUsername_throwsForMalformedToken() {
        assertThrows(Exception.class, () -> jwtService.extractUsername("invalid.token.here"));
    }
}
