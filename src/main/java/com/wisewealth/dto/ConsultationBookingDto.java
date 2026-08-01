package com.wisewealth.dto;

import com.wisewealth.entity.StatusEnum;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationBookingDto {
    private Long consultationId;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String financialGoal;
    private StatusEnum status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
