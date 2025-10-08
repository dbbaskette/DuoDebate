package com.duodebate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebateEvent {

    private EventType type;
    private DebateMessage message;
    private String error;
    private DebateResponse finalResponse;

    public enum EventType {
        DEBATE_START,
        ITERATION_START,
        PROPOSER_RESPONSE,
        CHALLENGER_RESPONSE,
        DEBATE_COMPLETE,
        ERROR
    }
}
