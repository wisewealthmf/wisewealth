package com.wisewealth.controller;

import com.wisewealth.dto.UserEmailDto;
import com.wisewealth.dto.UserEmailUpdateRequest;
import com.wisewealth.service.UserEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/email-leads")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminUserEmailController {

    private final UserEmailService userEmailService;

    /** List all email leads, paginated. */
    @GetMapping
    public ResponseEntity<Page<UserEmailDto>> getAllLeads(Pageable pageable) {
        log.info("Admin listing email leads");
        return ResponseEntity.ok(userEmailService.getAllLeads(pageable));
    }

    /** Fetch a single lead. */
    @GetMapping("/{id}")
    public ResponseEntity<UserEmailDto> getLeadById(@PathVariable Long id) {
        return ResponseEntity.ok(userEmailService.getLeadById(id));
    }

    /**
     * Update is_user and/or has_followed_up for a lead.
     * Only the fields present in the request body are changed.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserEmailDto> updateLead(
            @PathVariable Long id,
            @RequestBody UserEmailUpdateRequest request) {
        log.info("Admin PATCH email-lead id={} request={}", id, request);
        return ResponseEntity.ok(userEmailService.updateLead(id, request));
    }

    /** Permanently delete a lead. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLead(@PathVariable Long id) {
        userEmailService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }
}
