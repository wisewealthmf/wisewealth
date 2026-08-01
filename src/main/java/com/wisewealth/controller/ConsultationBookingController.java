package com.wisewealth.controller;

import com.wisewealth.dto.ConsultationBookingDto;
import com.wisewealth.dto.ConsultationBookingRequest;
import com.wisewealth.entity.StatusEnum;
import com.wisewealth.exception.UnauthorizedException;
import com.wisewealth.service.ConsultationBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/consultations")
@RequiredArgsConstructor
public class ConsultationBookingController {
    private final ConsultationBookingService consultationBookingService;

    /** Public — unauthenticated users may book a consultation (lead capture). */
    @PostMapping
    public ResponseEntity<ConsultationBookingDto> createConsultation(
            @Valid @RequestBody ConsultationBookingRequest request) {
        log.info("Create consultation request received: email={} name={}", request.getEmail(), request.getName());
        ConsultationBookingDto response = consultationBookingService.createConsultation(request, null);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /** Authenticated — returns only the calling user's consultations. */
    @GetMapping
    public ResponseEntity<Page<ConsultationBookingDto>> getMyConsultations(
            Authentication authentication,
            @RequestParam(value = "status", required = false) String status,
            Pageable pageable) {
        Long userId = requireUserId(authentication);
        log.info("GetMyConsultations called for userId={} status={}", userId, status);
        Page<ConsultationBookingDto> response;
        if (status != null && !status.isEmpty()) {
            StatusEnum statusEnum = parseStatus(status);
            response = consultationBookingService.getMyConsultationsByStatus(userId, statusEnum, pageable);
        } else {
            response = consultationBookingService.getMyConsultations(userId, pageable);
        }
        return ResponseEntity.ok(response);
    }

    /** Authenticated — returns only the calling user's specific consultation. */
    @GetMapping("/{consultationId}")
    public ResponseEntity<ConsultationBookingDto> getConsultationById(
            Authentication authentication,
            @PathVariable Long consultationId) {
        Long userId = requireUserId(authentication);
        log.info("GetConsultationById called for userId={} consultationId={}", userId, consultationId);
        ConsultationBookingDto response = consultationBookingService.getConsultationById(consultationId, userId);
        return ResponseEntity.ok(response);
    }

    /** Authenticated — only the owning user may update their consultation. */
    @PutMapping("/{consultationId}")
    public ResponseEntity<ConsultationBookingDto> updateConsultation(
            Authentication authentication,
            @PathVariable Long consultationId,
            @Valid @RequestBody ConsultationBookingRequest request) {
        Long userId = requireUserId(authentication);
        log.info("UpdateConsultation called for userId={} consultationId={}", userId, consultationId);
        ConsultationBookingDto response = consultationBookingService.updateConsultation(consultationId, request, userId);
        return ResponseEntity.ok(response);
    }

    private Long requireUserId(Authentication authentication) {
        if (!(authentication instanceof UsernamePasswordAuthenticationToken auth)
                || !(auth.getPrincipal() instanceof Long userId)) {
            throw new UnauthorizedException("Authorization header is required");
        }
        return userId;
    }

    private StatusEnum parseStatus(String status) {
        try {
            return StatusEnum.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }
}
