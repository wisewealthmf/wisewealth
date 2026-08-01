package com.wisewealth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Address;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from:${spring.mail.username:}}")
    private String mailFrom;

    @org.springframework.scheduling.annotation.Async("taskExecutor")
    public void sendReplyEmail(
            String to,
            String customerName,
            String originalQuery,
            String reply) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String html = String.format("""
                    <html>
                    <body>

                    <h2>Hello %s</h2>

                    <p>Thank you for connecting with WiseWealth.</p>

                    <p><b>Your Query</b></p>

                    <p>%s</p>

                    <p><b>Our Reply</b></p>

                    <p>%s</p>

                    </body>
                    </html>
                    """, customerName, originalQuery, reply);

            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(mailFrom);
            }
            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(mailFrom);
            }
            helper.setTo(to);
            helper.setSubject("Reply to your query");
            helper.setText(html, true);

            // Log prepared message and mail sender details for debugging
            try {
                mimeMessage.saveChanges();
                String fromStr = mimeMessage.getFrom() == null ? "" : Arrays.stream(mimeMessage.getFrom()).map(Address::toString).collect(Collectors.joining(","));
                String recipStr = mimeMessage.getAllRecipients() == null ? "" : Arrays.stream(mimeMessage.getAllRecipients()).map(Address::toString).collect(Collectors.joining(","));
                log.debug("Prepared MimeMessage - from={}, recipients={}, subject={}", fromStr, recipStr, mimeMessage.getSubject());
            } catch (Exception e) {
                log.debug("Unable to read MimeMessage headers", e);
            }

            if (mailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
                log.debug("MailSenderImpl configured host={} port={} username={}", impl.getHost(), impl.getPort(), impl.getUsername());
            } else {
                log.debug("MailSender implementation: {}", mailSender.getClass().getName());
            }

            mailSender.send(mimeMessage);
            log.info("Sent reply email to {}", to);
        } catch (Exception ex) {
            log.error("Failed to send reply email to {}", to, ex);
        }
    }

    @org.springframework.scheduling.annotation.Async("taskExecutor")
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(mailFrom);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            try {
                mimeMessage.saveChanges();
                String fromStr = mimeMessage.getFrom() == null ? "" : Arrays.stream(mimeMessage.getFrom()).map(Address::toString).collect(Collectors.joining(","));
                String recipStr = mimeMessage.getAllRecipients() == null ? "" : Arrays.stream(mimeMessage.getAllRecipients()).map(Address::toString).collect(Collectors.joining(","));
                log.debug("Prepared MimeMessage - from={}, recipients={}, subject={}", fromStr, recipStr, mimeMessage.getSubject());
            } catch (Exception e) {
                log.debug("Unable to read MimeMessage headers", e);
            }

            if (mailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
                log.debug("MailSenderImpl configured host={} port={} username={}", impl.getHost(), impl.getPort(), impl.getUsername());
            } else {
                log.debug("MailSender implementation: {}", mailSender.getClass().getName());
            }

            mailSender.send(mimeMessage);
            log.info("Sent email '{}' to {}", subject, to);
        } catch (Exception ex) {
            log.error("Failed to send email '{}' to {}", subject, to, ex);
        }
    }
}
