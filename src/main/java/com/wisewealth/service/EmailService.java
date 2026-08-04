package com.wisewealth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class EmailService {

    private static final String BREVO_SEND_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api-key:}")
    private String brevoApiKey;

    @Value("${mail.from:wisewealth.mf@gmail.com}")
    private String mailFrom;

    @Async("taskExecutor")
    public void sendReplyEmail(
            String to,
            String customerName,
            String originalQuery,
            String reply) {

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

        sendHtmlEmail(to, "Reply to your query", html);
    }

    @Async("taskExecutor")
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            String payload = String.format("""
                    {
                      "sender":  { "email": "%s" },
                      "to":      [{ "email": "%s" }],
                      "subject": "%s",
                      "htmlContent": %s
                    }
                    """,
                    escape(mailFrom),
                    escape(to),
                    escape(subject),
                    toJsonString(htmlBody));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            restTemplate.postForObject(BREVO_SEND_URL, request, String.class);

            log.info("Sent email '{}' to {}", subject, to);
        } catch (Exception ex) {
            log.error("Failed to send email '{}' to {}", subject, to, ex);
        }
    }

    /** Escapes a plain string for safe embedding inside a JSON string value. */
    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Wraps arbitrary HTML in a JSON string, escaping as needed. */
    private static String toJsonString(String html) {
        return "\"" + escape(html) + "\"";
    }
}
