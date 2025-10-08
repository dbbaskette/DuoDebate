package com.duodebate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebateResponse {

    private String prompt;
    private List<DebateMessage> transcript;
    private String finalStatus; // "READY" or "MAX_ITERATIONS"
    private Integer totalIterations;
    private String finalDraft;
    private List<String> sources; // List of source document URLs/references
}
