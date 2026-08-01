package com.wisewealth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationBookingRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Email(message = "Email must be valid")
    @Size(max = 255)
    private String email;

    @Size(max = 30)
    private String phone;

    @NotBlank(message = "Financial goal is required")
    @Size(max = 1000)
    @JsonProperty("financial_goal")
    private String financialGoal;

    @Size(max = 2000)
    private String notes;
}
