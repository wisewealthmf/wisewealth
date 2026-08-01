package com.wisewealth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 30)
    private String phone;

    @JsonProperty("is_active")
    private Boolean isActive;
}
