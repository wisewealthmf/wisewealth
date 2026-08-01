package com.wisewealth.dto;

import lombok.Data;

@Data
public class UserEmailUpdateRequest {
    /** Nullable — only update the field when the client sends it. */
    private Boolean isUser;
    private Boolean hasFollowedUp;
    private String purpose;
}
