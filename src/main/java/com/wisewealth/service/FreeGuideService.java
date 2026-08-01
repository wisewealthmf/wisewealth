package com.wisewealth.service;

import com.wisewealth.entity.UserEmail;
import com.wisewealth.repository.UserEmailRepository;
import com.wisewealth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FreeGuideService {

    private final UserEmailRepository userEmailRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.free-guide.pdf-url:}")
    private String pdfUrl;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public void requestFreeGuide(String rawName, String rawEmail) {
        String name  = rawName.trim();
        String email = rawEmail.trim().toLowerCase();

        // Check whether this email already belongs to a registered user
        boolean isUser = userRepository.existsByEmail(email);

        userEmailRepository.findByEmail(email).ifPresentOrElse(existing -> {
            // Already captured — append FREE_GUIDE to purpose if not already present,
            // then re-send the guide without duplicating the row
            String merged = WealthCheckService.addPurpose(existing.getPurpose(), "FREE_GUIDE");
            if (!merged.equals(existing.getPurpose())) {
                existing.setPurpose(merged);
                userEmailRepository.save(existing);
                log.info("Appended FREE_GUIDE to existing lead id={} email={} purpose={}", existing.getId(), email, merged);
            } else {
                log.info("Re-sending free guide to existing lead email={}", email);
            }
            sendGuideEmail(name, email);
        }, () -> {
            UserEmail entry = UserEmail.builder()
                    .name(name)
                    .email(email)
                    .purpose("FREE_GUIDE")
                    .isUser(isUser)
                    .hasFollowedUp(false)
                    .build();
            userEmailRepository.save(entry);
            log.info("Saved new lead name={} email={} isUser={}", name, email, isUser);
            sendGuideEmail(name, email);
        });
    }

    private void sendGuideEmail(String name, String email) {
        String downloadLink = pdfUrl != null && !pdfUrl.isBlank()
                ? pdfUrl
                : frontendUrl + "/free-guide.pdf";

        String subject = "Your Free WiseWealth Financial Guide";
        String body = String.format(
                "<p>Hello %s,</p>" +
                "<p>Thank you for your interest in WiseWealth!</p>" +
                "<p>You can download your free financial guide using the link below:</p>" +
                "<p><a href=\"%s\" style=\"background:#2e7d32;color:white;padding:12px 24px;" +
                "border-radius:8px;text-decoration:none;font-weight:bold;\">Download Free Guide</a></p>" +
                "<p>If you have any questions, feel free to reach out to us.</p>" +
                "<p>Warm regards,<br/>The WiseWealth Team</p>",
                name, downloadLink);

        emailService.sendHtmlEmail(email, subject, body);
        log.info("Free guide email sent to {}", email);
    }
}
