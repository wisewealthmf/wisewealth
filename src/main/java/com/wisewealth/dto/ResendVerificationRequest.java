package com.wisewealth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for POST /auth/resend-verification.
 * Validates email format and caps field lengths before reaching the service layer.
 */
@Data
public class ResendVerificationRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name = "";

    @NotBlank(message = "Email is required")
    @Email(message = "A valid email address is required")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
}
