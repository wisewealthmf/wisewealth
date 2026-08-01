package com.wisewealth.controller;

import com.wisewealth.dto.ConsultationBookingDto;
import com.wisewealth.dto.ConsultationBookingRequest;
import com.wisewealth.dto.StatusUpdateRequest;
import com.wisewealth.entity.StatusEnum;
import com.wisewealth.service.ConsultationBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/consultations")
@RequiredArgsConstructor
public class AdminConsultationController {
    private final ConsultationBookingService consultationBookingService;

    @GetMapping
    public ResponseEntity<Page<ConsultationBookingDto>> getAllConsultations(
            @RequestParam(value = "status", required = false) String status,
            Pageable pageable) {
        Page<ConsultationBookingDto> response;
        if (status != null && !status.isEmpty()) {
            StatusEnum statusEnum = parseStatus(status);
            response = consultationBookingService.getAllConsultationsByStatus(statusEnum, pageable);
        } else {
            response = consultationBookingService.getAllConsultations(pageable);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{consultationId}")
    public ResponseEntity<ConsultationBookingDto> getConsultationById(@PathVariable Long consultationId) {
        ConsultationBookingDto response = consultationBookingService.getConsultationByIdAdmin(consultationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{consultationId}")
    public ResponseEntity<ConsultationBookingDto> createConsultationForUser(
            @PathVariable Long consultationId,
            @Valid @RequestBody ConsultationBookingRequest request) {
        ConsultationBookingDto response = consultationBookingService.createConsultation(request, consultationId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{consultationId}/status")
    public ResponseEntity<ConsultationBookingDto> updateConsultationStatus(
            @PathVariable Long consultationId,
            @Valid @RequestBody StatusUpdateRequest request) {
        ConsultationBookingDto response = consultationBookingService.updateConsultationStatus(consultationId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{consultationId}")
    public ResponseEntity<Void> deleteConsultation(@PathVariable Long consultationId) {
        consultationBookingService.deleteConsultation(consultationId);
        return ResponseEntity.noContent().build();
    }

    private StatusEnum parseStatus(String status) {
        try {
            return StatusEnum.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }
}
