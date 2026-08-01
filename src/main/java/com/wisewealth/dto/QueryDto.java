package com.wisewealth.dto;

import com.wisewealth.entity.StatusEnum;
import com.wisewealth.entity.CategoryEnum;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryDto {
    private Long queryId;
    private Long userId;
    // consultationId removed — queries are independent
    private String name;
    private String email;
    private String phone;
    private String queryText;
    private CategoryEnum category;
    private StatusEnum status;
    private String reply;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
