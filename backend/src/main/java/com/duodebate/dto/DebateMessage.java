package com.duodebate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebateMessage {

    private String role; // "PROPOSER" or "CHALLENGER"
    private String content;
    private Integer iteration;
    private String model; // "openai" or "gemini"
    private String status; // "ONGOING", "READY", "MAX_ITERATIONS"
}
