package com.wisewealth.dto;

import com.wisewealth.entity.StatusEnum;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateRequest {
    private StatusEnum status;
}
