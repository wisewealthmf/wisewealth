package com.wisewealth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryReplyRequest {

    @NotBlank(message = "Reply text is required")
    @Size(max = 5000)
    @JsonProperty("reply_text")
    private String replyText;
}
