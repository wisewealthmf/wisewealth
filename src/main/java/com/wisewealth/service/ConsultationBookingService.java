package com.wisewealth.service;

import com.wisewealth.dto.ConsultationBookingDto;
import com.wisewealth.dto.ConsultationBookingRequest;
import com.wisewealth.dto.StatusUpdateRequest;
import com.wisewealth.entity.ConsultationBooking;
import com.wisewealth.entity.StatusEnum;
import com.wisewealth.entity.User;
import com.wisewealth.exception.ResourceNotFoundException;
import com.wisewealth.repository.ConsultationBookingRepository;
import com.wisewealth.repository.UserRepository;
import com.wisewealth.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationBookingService {
    private final ConsultationBookingRepository consultationBookingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public ConsultationBookingDto createConsultation(ConsultationBookingRequest request, Long userId) {
        log.info("Creating consultation for user id: {}", userId);

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required to book a consultation.");
        }

        User user = emailVerificationService.requireVerifiedOrCreateUser(request.getName(), request.getEmail(), request.getPhone());

        ConsultationBooking consultation = ConsultationBooking.builder()
                .user(user)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .financialGoal(request.getFinancialGoal())
                .notes(request.getNotes())
                .status(StatusEnum.NEW)
                .build();

        ConsultationBooking saved = consultationBookingRepository.save(consultation);
        log.info("Consultation created with id: {}", saved.getConsultationId());
        ConsultationBookingDto dto = mapToDto(saved);
        // send confirmation email if email provided
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String subject = "Your consultation booking";
            String body = String.format("<p>Hello %s,</p><p>Thank you for booking a consultation with WiseWealth. We received your request and will contact you soon.</p><p><b>Details</b></p><p>Goal: %s</p>", dto.getName(), dto.getFinancialGoal() != null ? dto.getFinancialGoal() : "-");
            try {
                emailService.sendHtmlEmail(dto.getEmail(), subject, body);
            } catch (Exception ex) {
                log.error("Failed to send consultation confirmation email for consultationId={}", saved.getConsultationId(), ex);
            }
        }
        return dto;
    }

    public ConsultationBookingDto getConsultationById(Long consultationId, Long userId) {
        log.info("Fetching consultation id: {} for user: {}", consultationId, userId);

        ConsultationBooking consultation = consultationBookingRepository
                .findByConsultationIdAndUserUserId(consultationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found"));

        return mapToDto(consultation);
    }

    public ConsultationBookingDto getConsultationByIdAdmin(Long consultationId) {
        log.info("Admin fetching consultation id: {}", consultationId);

        ConsultationBooking consultation = consultationBookingRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found"));

        return mapToDto(consultation);
    }

    public Page<ConsultationBookingDto> getMyConsultations(Long userId, Pageable pageable) {
        log.info("Fetching consultations for user id: {}", userId);
        return consultationBookingRepository.findByUserUserId(userId, pageable)
                .map(this::mapToDto);
    }

    public Page<ConsultationBookingDto> getMyConsultationsByStatus(Long userId, StatusEnum status, Pageable pageable) {
        log.info("Fetching consultations for user id: {} with status: {}", userId, status);
        return consultationBookingRepository.findByUserUserIdAndStatus(userId, status, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public ConsultationBookingDto updateConsultation(Long consultationId, ConsultationBookingRequest request, Long userId) {
        log.info("Updating consultation id: {} for user: {}", consultationId, userId);

        ConsultationBooking consultation = consultationBookingRepository
                .findByConsultationIdAndUserUserId(consultationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found"));

        if (request.getName() != null) consultation.setName(request.getName());
        if (request.getEmail() != null) consultation.setEmail(request.getEmail());
        if (request.getPhone() != null) consultation.setPhone(request.getPhone());
        if (request.getFinancialGoal() != null) consultation.setFinancialGoal(request.getFinancialGoal());
        if (request.getNotes() != null) consultation.setNotes(request.getNotes());

        ConsultationBooking updated = consultationBookingRepository.save(consultation);
        log.info("Consultation updated: {}", consultationId);
        return mapToDto(updated);
    }

    @Transactional
    public ConsultationBookingDto updateConsultationById(Long consultationId, ConsultationBookingRequest request) {
        log.info("Updating consultation id: {} (no user scope)", consultationId);

        ConsultationBooking consultation = consultationBookingRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found"));

        if (request.getName() != null) consultation.setName(request.getName());
        if (request.getEmail() != null) consultation.setEmail(request.getEmail());
        if (request.getPhone() != null) consultation.setPhone(request.getPhone());
        if (request.getFinancialGoal() != null) consultation.setFinancialGoal(request.getFinancialGoal());
        if (request.getNotes() != null) consultation.setNotes(request.getNotes());

        ConsultationBooking updated = consultationBookingRepository.save(consultation);
        log.info("Consultation updated by id: {}", consultationId);
        return mapToDto(updated);
    }

    @Transactional
    public ConsultationBookingDto updateConsultationStatus(Long consultationId, StatusUpdateRequest request) {
        log.info("Updating status for consultation id: {} to: {}", consultationId, request.getStatus());

        ConsultationBooking consultation = consultationBookingRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found"));

        consultation.setStatus(request.getStatus());
        ConsultationBooking updated = consultationBookingRepository.save(consultation);
        log.info("Consultation status updated: {}", consultationId);
        ConsultationBookingDto dto = mapToDto(updated);
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String subject = "Consultation status updated";
            String body = String.format("<p>Hello %s,</p><p>Your consultation status has been updated to <b>%s</b>.</p>", dto.getName(), dto.getStatus());
            try {
                emailService.sendHtmlEmail(dto.getEmail(), subject, body);
            } catch (Exception ex) {
                log.error("Failed to send consultation status email for consultationId={}", consultationId, ex);
            }
        }
        return dto;
    }

    @Transactional
    public void deleteConsultation(Long consultationId) {
        log.info("Deleting consultation id: {}", consultationId);
        if (!consultationBookingRepository.existsById(consultationId)) {
            throw new ResourceNotFoundException("Consultation not found with id: " + consultationId);
        }
        consultationBookingRepository.deleteById(consultationId);
        log.info("Consultation deleted: {}", consultationId);
    }

    public Page<ConsultationBookingDto> getAllConsultations(Pageable pageable) {
        log.info("Admin fetching all consultations");
        return consultationBookingRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    public Page<ConsultationBookingDto> getAllConsultationsByStatus(StatusEnum status, Pageable pageable) {
        log.info("Admin fetching consultations with status: {}", status);
        return consultationBookingRepository.findByStatus(status, pageable)
                .map(this::mapToDto);
    }

    private ConsultationBookingDto mapToDto(ConsultationBooking consultation) {
        return ConsultationBookingDto.builder()
                .consultationId(consultation.getConsultationId())
                .userId(consultation.getUser() != null ? consultation.getUser().getUserId() : null)
                .name(consultation.getName())
                .email(consultation.getEmail())
                .phone(consultation.getPhone())
                .financialGoal(consultation.getFinancialGoal())
                .status(consultation.getStatus())
                .notes(consultation.getNotes())
                .createdAt(consultation.getCreatedAt())
                .updatedAt(consultation.getUpdatedAt())
                .build();
    }
}
