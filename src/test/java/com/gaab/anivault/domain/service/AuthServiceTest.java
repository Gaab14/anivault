package com.gaab.anivault.domain.service;

import com.gaab.anivault.domain.dto.AuthResponseDto;
import com.gaab.anivault.domain.dto.LoginRequestDto;
import com.gaab.anivault.domain.dto.RegisterRequestDto;
import com.gaab.anivault.domain.enums.Role;
import com.gaab.anivault.domain.exception.UserAlreadyExistsException;
import com.gaab.anivault.persistence.crud.CrudUserEntity;
import com.gaab.anivault.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CrudUserEntity crudUserEntity;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private UserEntity createUser() {
        return UserEntity.builder()
                .id(1L)
                .username("testuser")
                .email("test@email.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_success_returnsAuthResponse() {
        RegisterRequestDto request = new RegisterRequestDto("testuser", "test@email.com", "password123");
        UserEntity savedUser = createUser();

        when(crudUserEntity.existsByUsername("testuser")).thenReturn(false);
        when(crudUserEntity.existsByEmail("test@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(crudUserEntity.save(any(UserEntity.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(), eq(1L))).thenReturn("jwt-token");

        AuthResponseDto response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("testuser", response.username());
    }

    @Test
    void register_duplicateUsername_throwsUserAlreadyExistsException() {
        RegisterRequestDto request = new RegisterRequestDto("testuser", "test@email.com", "password123");
        when(crudUserEntity.existsByUsername("testuser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(crudUserEntity, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsUserAlreadyExistsException() {
        RegisterRequestDto request = new RegisterRequestDto("testuser", "test@email.com", "password123");
        when(crudUserEntity.existsByUsername("testuser")).thenReturn(false);
        when(crudUserEntity.existsByEmail("test@email.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(crudUserEntity, never()).save(any());
    }

    @Test
    void register_hashesPassword() {
        RegisterRequestDto request = new RegisterRequestDto("testuser", "test@email.com", "rawPassword");
        UserEntity savedUser = createUser();

        when(crudUserEntity.existsByUsername(anyString())).thenReturn(false);
        when(crudUserEntity.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");
        when(crudUserEntity.save(any(UserEntity.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(), any())).thenReturn("token");

        authService.register(request);

        verify(passwordEncoder).encode("rawPassword");
    }

    @Test
    void login_success_returnsAuthResponse() {
        LoginRequestDto request = new LoginRequestDto("testuser", "password123");
        UserEntity user = createUser();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", "password123"));
        when(crudUserEntity.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), eq(1L))).thenReturn("jwt-token");

        AuthResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("testuser", response.username());
    }

    @Test
    void login_badCredentials_throwsBadCredentialsException() {
        LoginRequestDto request = new LoginRequestDto("testuser", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
