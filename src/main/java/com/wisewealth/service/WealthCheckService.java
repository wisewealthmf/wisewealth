package com.wisewealth.service;

import com.wisewealth.entity.UserEmail;
import com.wisewealth.exception.ResourceNotFoundException;
import com.wisewealth.repository.UserEmailRepository;
import com.wisewealth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Slf4j
public class WealthCheckService {

    private static final long MAX_PDF_BYTES = 30 * 1024 * 1024L; // 30 MB
    private static final byte[] PDF_MAGIC   = {'%', 'P', 'D', 'F'};

    private final UserEmailRepository userEmailRepository;
    private final UserRepository userRepository;

    @Value("${app.wealth-check.reports-dir:./wealth-check-reports}")
    private String reportsDir;

    /**
     * Appends {@code newPurpose} to the existing comma-separated purpose string
     * if it is not already present, then returns the merged value.
     * e.g. addPurpose("FREE_GUIDE", "WEALTH_CHECK") → "FREE_GUIDE,WEALTH_CHECK"
     *      addPurpose("WEALTH_CHECK", "WEALTH_CHECK") → "WEALTH_CHECK"  (no duplicate)
     */
    static String addPurpose(String existing, String newPurpose) {
        if (existing == null || existing.isBlank()) return newPurpose;
        for (String part : existing.split(",")) {
            if (part.trim().equalsIgnoreCase(newPurpose)) return existing; // already present
        }
        return existing + "," + newPurpose;
    }

    /**
     * Captures a lead for the Wealth Check tool.
     * If the email already exists, appends WEALTH_CHECK to the purpose field
     * (comma-separated) rather than replacing it, so a FREE_GUIDE lead that
     * later uses the tool retains both purposes.
     *
     * @return the saved/existing lead id so the frontend can use it for the PDF filename
     */
    @Transactional
    public long captureLead(String rawName, String rawEmail) {
        String name  = rawName.trim();
        String email = rawEmail.trim().toLowerCase();

        return userEmailRepository.findByEmail(email).map(existing -> {
            // Update name and append WEALTH_CHECK to purpose (preserve FREE_GUIDE if present)
            existing.setName(name);
            existing.setPurpose(addPurpose(existing.getPurpose(), "WEALTH_CHECK"));
            userEmailRepository.save(existing);
            log.info("Updated existing lead id={} email={} purpose={}", existing.getId(), email, existing.getPurpose());
            return existing.getId();
        }).orElseGet(() -> {
            boolean isUser = userRepository.existsByEmail(email);
            UserEmail entry = UserEmail.builder()
                    .name(name)
                    .email(email)
                    .purpose("WEALTH_CHECK")
                    .isUser(isUser)
                    .hasFollowedUp(false)
                    .build();
            UserEmail saved = userEmailRepository.save(entry);
            log.info("Saved new wealth-check lead id={} name={} email={}", saved.getId(), name, email);
            return saved.getId();
        });
    }

    /**
     * Saves the uploaded PDF report to disk as {leadId}_{email}.pdf.
     *
     * Security checks performed before writing:
     *   1. leadId must exist in the DB and its stored email must match the supplied email.
     *   2. File must not exceed MAX_PDF_BYTES.
     *   3. First 4 bytes must be the PDF magic number (%PDF).
     *   4. Resolved destination path must remain inside reportsDir (path-traversal guard).
     */
    public String saveReport(long leadId, String suppliedEmail, MultipartFile file) throws IOException {
        // 1. Verify leadId exists and email matches the captured lead — prevents arbitrary upload
        String normalizedEmail = suppliedEmail.trim().toLowerCase();
        UserEmail lead = userEmailRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found: " + leadId));
        if (!lead.getEmail().equalsIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email does not match the captured lead");
        }

        // 2. File size guard (defence-in-depth beyond Spring multipart config)
        if (file.getSize() > MAX_PDF_BYTES) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of 20 MB");
        }

        // 3. Validate PDF magic bytes (%PDF)
        byte[] header = file.getBytes();
        if (header.length < 4
                || header[0] != PDF_MAGIC[0] || header[1] != PDF_MAGIC[1]
                || header[2] != PDF_MAGIC[2] || header[3] != PDF_MAGIC[3]) {
            throw new IllegalArgumentException("Uploaded file is not a valid PDF");
        }

        // 4. Build path and assert it stays inside reportsDir (path-traversal guard)
        Path dir  = Paths.get(reportsDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String safeEmail = normalizedEmail.replaceAll("[^a-z0-9@._-]", "_");
        String filename  = leadId + "_" + safeEmail + ".pdf";
        Path dest = dir.resolve(filename).normalize();

        if (!dest.startsWith(dir)) {
            throw new IllegalArgumentException("Invalid report path — possible path traversal attempt");
        }

        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        log.info("Saved wealth-check report: {}", dest);
        return filename;
    }

    /**
     * Loads a stored wealth-check PDF report for admin viewing.
     * Finds the file by scanning for {leadId}_*.pdf inside reportsDir.
     * Throws ResourceNotFoundException if no matching file exists.
     */
    public Resource loadReport(long leadId) {
        Path dir = Paths.get(reportsDir).toAbsolutePath().normalize();
        // Find the file whose name starts with "{leadId}_"
        try (var stream = Files.list(dir)) {
            Path match = stream
                    .filter(p -> p.getFileName().toString().startsWith(leadId + "_")
                            && p.getFileName().toString().endsWith(".pdf"))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No wealth-check report found for leadId: " + leadId));

            // Path-traversal guard
            if (!match.normalize().startsWith(dir)) {
                throw new ResourceNotFoundException("Invalid report path for leadId: " + leadId);
            }

            try {
                Resource resource = new UrlResource(match.toUri());
                if (!resource.exists() || !resource.isReadable()) {
                    throw new ResourceNotFoundException(
                            "Report file is not readable for leadId: " + leadId);
                }
                return resource;
            } catch (MalformedURLException e) {
                throw new ResourceNotFoundException(
                        "Could not read report for leadId: " + leadId);
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new ResourceNotFoundException(
                    "Could not access reports directory for leadId: " + leadId);
        }
    }
}
