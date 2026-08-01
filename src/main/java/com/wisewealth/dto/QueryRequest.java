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
public class QueryRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Email(message = "Email must be valid")
    @Size(max = 255)
    private String email;

    @Size(max = 30)
    private String phone;

    @NotBlank(message = "Query text is required")
    @Size(max = 5000, message = "Query text must not exceed 5000 characters")
    @JsonProperty("query_text")
    private String queryText;

    @Size(max = 100)
    private String category;
}
