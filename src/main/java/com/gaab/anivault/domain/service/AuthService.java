package com.gaab.anivault.domain.service;

import com.gaab.anivault.domain.dto.AuthResponseDto;
import com.gaab.anivault.domain.dto.LoginRequestDto;
import com.gaab.anivault.domain.dto.RegisterRequestDto;
import com.gaab.anivault.domain.exception.UserAlreadyExistsException;
import com.gaab.anivault.persistence.crud.CrudUserEntity;
import com.gaab.anivault.persistence.entity.UserEntity;
import com.gaab.anivault.web.security.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final CrudUserEntity crudUserEntity;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(CrudUserEntity crudUserEntity, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.crudUserEntity = crudUserEntity;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDto register(RegisterRequestDto request) {
        if (crudUserEntity.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("username", request.username());
        }
        if (crudUserEntity.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("email", request.email());
        }

        UserEntity user = UserEntity.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        UserEntity savedUser = crudUserEntity.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(
                savedUser.getId(), savedUser.getUsername(),
                savedUser.getPassword(), savedUser.getRole()
        );
        String token = jwtService.generateToken(userDetails, savedUser.getId());
        return new AuthResponseDto(token, savedUser.getUsername());
    }

    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserEntity user = crudUserEntity.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(
                user.getId(), user.getUsername(),
                user.getPassword(), user.getRole()
        );
        String token = jwtService.generateToken(userDetails, user.getId());
        return new AuthResponseDto(token, user.getUsername());
    }
}
