package com.wisewealth.service;

import com.wisewealth.entity.User;
import com.wisewealth.exception.EmailNotVerifiedException;
import com.wisewealth.exception.ResourceNotFoundException;
import com.wisewealth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email.token-lifetime-hours:24}")
    private long tokenLifetimeHours;

    @Transactional
    public User createUnverifiedUser(String name, String email, String rawPassword) {
        return createUnverifiedUser(name, email, rawPassword, null);
    }

    @Transactional
    public User createUnverifiedUser(String name, String email, String rawPassword, String phone) {
        String normalizedEmail = normalizeEmail(email);

        User user = User.builder()
                .name(name != null && !name.isBlank() ? name : "Anonymous")
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .phone(phone != null && !phone.isBlank() ? phone : null)
                .isActive(true)
                .isAdmin(false)
                .isEmailVerified(false)
                .build();

        generateVerificationToken(user);
        User savedUser = userRepository.save(user);
        sendVerificationEmail(savedUser);
        log.info("Created unverified user id={} email={}", savedUser.getUserId(), normalizedEmail);
        return savedUser;
    }

    @Transactional
    public User requireVerifiedOrCreateUser(String name, String email, String phone) {
        String normalizedEmail = normalizeEmail(email);

        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (optionalUser.isEmpty()) {
            User user = createUnverifiedUser(name, normalizedEmail, UUID.randomUUID().toString(), phone);
            throw new EmailNotVerifiedException("Email is not verified. A verification link has been sent to " + normalizedEmail + ".");
        }

        User user = optionalUser.get();
        if (Boolean.TRUE.equals(user.getIsEmailVerified())) {
            // Update name and phone if new values provided
            boolean changed = false;
            if (name != null && !name.isBlank() && !name.equals(user.getName())) {
                user.setName(name);
                changed = true;
            }
            if (phone != null && !phone.isBlank() && !phone.equals(user.getPhone())) {
                user.setPhone(phone);
                changed = true;
            }
            if (changed) userRepository.save(user);
            return user;
        }

        // Update phone on unverified user if not yet stored
        if (phone != null && !phone.isBlank() && (user.getPhone() == null || user.getPhone().isBlank())) {
            user.setPhone(phone);
        }

        if (isVerificationTokenExpired(user)) {
            generateVerificationToken(user);
        }
        userRepository.save(user);

        sendVerificationEmail(user);
        throw new EmailNotVerifiedException("Email is not verified. A verification link has been sent to " + normalizedEmail + ".");
    }

    /** Backwards-compatible overload — used by resendVerification which has no phone. */
    @Transactional
    public User requireVerifiedOrCreateUser(String name, String email) {
        return requireVerifiedOrCreateUser(name, email, null);
    }

    /**
     * Returns true if the email exists in the DB and is verified.
     */
    public boolean isEmailVerified(String email) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(u -> Boolean.TRUE.equals(u.getIsEmailVerified()))
                .orElse(false);
    }

    /**
     * Resends (or sends a first) verification email for the given email.
     * If the user does not exist, a new unverified user is created.
     * If already verified, this is a no-op.
     */
    @Transactional
    public void resendVerification(String name, String email) {
        String normalizedEmail = normalizeEmail(email);
        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (optionalUser.isEmpty()) {
            createUnverifiedUser(name, normalizedEmail, UUID.randomUUID().toString());
            return;
        }
        User user = optionalUser.get();
        if (Boolean.TRUE.equals(user.getIsEmailVerified())) {
            // Already verified — nothing to do
            return;
        }
        if (isVerificationTokenExpired(user)) {
            generateVerificationToken(user);
            userRepository.save(user);
        }
        sendVerificationEmail(user);
    }

    @Transactional
    public void verifyToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Verification token is required.");
        }

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token."));

        if (isVerificationTokenExpired(user)) {
            throw new IllegalArgumentException("Verification token has expired.");
        }

        user.setIsEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
        log.info("Email verified for user id={}", user.getUserId());
    }

    private void sendVerificationEmail(User user) {
        ensureVerificationToken(user);
        if (user.getVerificationToken() == null) {
            throw new IllegalStateException("Verification token must be present before sending email.");
        }

        String verificationLink = String.format("%s/verify-email?token=%s", frontendUrl, user.getVerificationToken());

        // Always log the link so it can be used manually if email delivery fails (e.g. SMTP not configured locally)
        log.info("Email verification link for {} → {}", user.getEmail(), verificationLink);

        String subject = "Verify your WiseWealth email";
        String body = String.format(
                "<p>Hello %s,</p>" +
                "<p>Thanks for contacting WiseWealth. Please verify your email to submit your form.</p>" +
                "<p><a href=\"%s\">Verify your email</a></p>" +
                "<p>If you did not request this, please ignore this email.</p>",
                user.getName(), verificationLink);

        emailService.sendHtmlEmail(user.getEmail(), subject, body);
    }

    private void generateVerificationToken(User user) {
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(tokenLifetimeHours));
    }

    private boolean isVerificationTokenExpired(User user) {
        return user.getVerificationTokenExpiry() == null || user.getVerificationTokenExpiry().isBefore(LocalDateTime.now());
    }

    private void ensureVerificationToken(User user) {
        if (user.getVerificationToken() == null || isVerificationTokenExpired(user)) {
            generateVerificationToken(user);
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email is required.");
        }
        return email.trim().toLowerCase();
    }
}
