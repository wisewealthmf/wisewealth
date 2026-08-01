package com.wisewealth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusUpdateRequest {
    private Boolean isActive;
}
