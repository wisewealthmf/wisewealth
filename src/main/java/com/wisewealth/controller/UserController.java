package com.wisewealth.controller;

import com.wisewealth.dto.UserDto;
import com.wisewealth.dto.UserUpdateRequest;
import com.wisewealth.exception.UnauthorizedException;
import com.wisewealth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserUpdateRequest request) {
        Long userId = getCurrentUserId(authentication);
        UserDto user = userService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        userService.deactivateUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (!(authentication instanceof UsernamePasswordAuthenticationToken auth)
                || !(auth.getPrincipal() instanceof Long userId)) {
            throw new UnauthorizedException("Authorization header is required");
        }
        return userId;
    }
}
