package com.wisewealth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserEmailDto {
    private Long id;
    private String name;
    private String email;
    private String purpose;
    private Boolean isUser;
    private Boolean hasFollowedUp;
    private LocalDateTime createdAt;
}
