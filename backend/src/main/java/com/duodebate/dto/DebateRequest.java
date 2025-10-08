package com.duodebate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebateRequest {

    @NotBlank(message = "Prompt is required")
    private String prompt;

    @Min(value = 1, message = "Max iterations must be at least 1")
    @Max(value = 20, message = "Max iterations must not exceed 20")
    private Integer maxIterations = 10;
}
