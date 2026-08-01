package com.wisewealth.service;

import com.wisewealth.dto.UserEmailDto;
import com.wisewealth.dto.UserEmailUpdateRequest;
import com.wisewealth.entity.UserEmail;
import com.wisewealth.exception.ResourceNotFoundException;
import com.wisewealth.repository.UserEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEmailService {

    private final UserEmailRepository userEmailRepository;

    public Page<UserEmailDto> getAllLeads(Pageable pageable) {
        log.info("Admin fetching all user_email leads");
        return userEmailRepository.findAll(pageable).map(this::toDto);
    }

    public UserEmailDto getLeadById(Long id) {
        UserEmail lead = userEmailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        return toDto(lead);
    }

    @Transactional
    public UserEmailDto updateLead(Long id, UserEmailUpdateRequest request) {
        log.info("Admin updating user_email lead id={}", id);
        UserEmail lead = userEmailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        if (request.getIsUser() != null) {
            lead.setIsUser(request.getIsUser());
        }
        if (request.getHasFollowedUp() != null) {
            lead.setHasFollowedUp(request.getHasFollowedUp());
        }
        if (request.getPurpose() != null) {
            lead.setPurpose(request.getPurpose());
        }

        UserEmail saved = userEmailRepository.save(lead);
        log.info("user_email lead id={} updated: isUser={} hasFollowedUp={} purpose={}", id, saved.getIsUser(), saved.getHasFollowedUp(), saved.getPurpose());
        return toDto(saved);
    }

    @Transactional
    public void deleteLead(Long id) {
        log.info("Admin deleting user_email lead id={}", id);
        if (!userEmailRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lead not found with id: " + id);
        }
        userEmailRepository.deleteById(id);
        log.info("user_email lead id={} deleted", id);
    }

    private UserEmailDto toDto(UserEmail e) {
        return UserEmailDto.builder()
                .id(e.getId())
                .name(e.getName())
                .email(e.getEmail())
                .purpose(e.getPurpose())
                .isUser(e.getIsUser())
                .hasFollowedUp(e.getHasFollowedUp())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
