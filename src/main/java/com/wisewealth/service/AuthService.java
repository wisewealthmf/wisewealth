package com.wisewealth.service;

import com.wisewealth.dto.*;
import com.wisewealth.entity.User;
import com.wisewealth.exception.DuplicateEmailException;
import com.wisewealth.repository.UserRepository;
import com.wisewealth.security.JwtTokenProvider;
import com.wisewealth.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;

    public UserDto register(UserRegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + request.getEmail());
        }

        User savedUser = emailVerificationService.createUnverifiedUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone());

        log.info("User registered successfully with id: {}", savedUser.getUserId());
        return mapToUserDto(savedUser);
    }

    public UserLoginResponse login(UserLoginRequest request) {
        log.info("User login attempt");

        // Use a constant-time path for both "not found" and "wrong password"
        // to prevent user enumeration via timing differences.
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            // Perform a dummy BCrypt check so timing is indistinguishable
            passwordEncoder.matches(request.getPassword(), "$2a$10$dummyhashfortimingxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Admins are not required to have a verified email — they are created directly in the DB.
        if (!Boolean.TRUE.equals(user.getIsAdmin()) && !Boolean.TRUE.equals(user.getIsEmailVerified())) {
            throw new IllegalArgumentException("Email has not been verified.");
        }

        String accessToken = jwtTokenProvider.generateToken(user);

        log.info("User logged in successfully: {}", user.getUserId());

        return UserLoginResponse.builder()
                .accessToken(accessToken)
                .expiresIn(3600L)
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .isAdmin(user.getIsAdmin())
                .build();
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
