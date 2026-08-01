package com.wisewealth.controller;

import com.wisewealth.dto.ConsultationBookingDto;
import com.wisewealth.dto.ConsultationBookingRequest;
import com.wisewealth.dto.UserDto;
import com.wisewealth.dto.UserStatusUpdateRequest;
import com.wisewealth.dto.UserUpdateRequest;
import com.wisewealth.service.ConsultationBookingService;
import com.wisewealth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;
    private final ConsultationBookingService consultationBookingService;

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(value = "active", required = false) Boolean active,
            Pageable pageable) {
        Page<UserDto> response;
        if (active != null) {
            response = userService.getAllUsers(pageable);
        } else {
            response = userService.getAllUsers(pageable);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserDto response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateUserStatus(userId, request.getIsActive());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/consultations")
    public ResponseEntity<ConsultationBookingDto> createConsultationForUser(
            @PathVariable Long userId,
            @Valid @RequestBody ConsultationBookingRequest request) {
        ConsultationBookingDto response = consultationBookingService.createConsultation(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/consultations")
    public ResponseEntity<Page<ConsultationBookingDto>> getUserConsultations(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<ConsultationBookingDto> response = consultationBookingService.getMyConsultations(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
