package com.wisewealth.controller;

import com.wisewealth.dto.WealthCheckLeadRequest;
import com.wisewealth.service.WealthCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WealthCheckController {

    private final WealthCheckService wealthCheckService;

    /**
     * Public endpoint — captures name + email when the user starts the tool.
     * Returns the generated lead id so the frontend can reference it for the PDF upload.
     */
    @PostMapping("/wealth-check/leads")
    public ResponseEntity<Map<String, Object>> captureLead(
            @Valid @RequestBody WealthCheckLeadRequest request) {
        log.info("Wealth-check lead capture: name={} email={}", request.getName(), request.getEmail());
        long leadId = wealthCheckService.captureLead(request.getName(), request.getEmail());
        return ResponseEntity.ok(Map.of(
                "id", leadId,
                "message", "Lead captured"
        ));
    }

    /**
     * Public endpoint — receives the generated PDF and stores it as {id}_{email}.pdf.
     * The client must send: multipart/form-data with fields "leadId", "email", "file".
     *
     * Security: leadId + email are verified against the DB before writing.
     * File-type and path-traversal checks are enforced in WealthCheckService.
     */
    @PostMapping(value = "/wealth-check/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadReport(
            @RequestParam("leadId") long leadId,
            @RequestParam("email") String email,
            @RequestParam("file") MultipartFile file) {
        log.info("Wealth-check report upload: leadId={} size={}", leadId, file.getSize());
        try {
            String filename = wealthCheckService.saveReport(leadId, email, file);
            return ResponseEntity.ok(Map.of("filename", filename, "message", "Report saved"));
        } catch (IllegalArgumentException ex) {
            log.warn("Rejected wealth-check report upload for leadId={}: {}", leadId, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Failed to save wealth-check report for leadId={}", leadId, ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Failed to save report. Please try again."));
        }
    }

    /**
     * Admin-only endpoint — streams the stored PDF report for a given lead.
     * Requires ROLE_ADMIN. Returns 404 if no report file exists for the lead.
     */
    @GetMapping("/admin/wealth-check/reports/{leadId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Resource> getReport(@PathVariable long leadId) {
        log.info("Admin fetching wealth-check report for leadId={}", leadId);
        Resource resource = wealthCheckService.loadReport(leadId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
