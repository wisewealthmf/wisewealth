package com.wisewealth.controller;

import com.wisewealth.dto.ResendVerificationRequest;
import com.wisewealth.dto.UserDto;
import com.wisewealth.dto.UserLoginRequest;
import com.wisewealth.dto.UserLoginResponse;
import com.wisewealth.dto.UserRegisterRequest;
import com.wisewealth.service.AuthService;
import com.wisewealth.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("Register endpoint called for email={}", request.getEmail());
        UserDto response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam("token") String token) {
        log.info("Verify email request received for token={}", token);
        emailVerificationService.verifyToken(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
    }

    /**
     * Returns whether the given email exists in the DB and is verified.
     * Used by the frontend to show a green tick without requiring a submit attempt.
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam("email") String email) {
        log.info("Check email verification status for email={}", email);
        boolean verified = emailVerificationService.isEmailVerified(email);
        return ResponseEntity.ok(Map.of("verified", verified));
    }

    /**
     * Resends a verification email for the given email address.
     * If the user does not exist yet, a new unverified account is created.
     * Input is validated via ResendVerificationRequest (@NotBlank @Email @Size).
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        log.info("Resend verification requested for email={}", request.getEmail());
        emailVerificationService.resendVerification(request.getName(), request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Verification email sent. Please check your inbox."));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("Login endpoint called for email={}", request.getEmail());
        UserLoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
