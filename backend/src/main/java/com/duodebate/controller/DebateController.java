package com.duodebate.controller;

import com.duodebate.dto.DebateEvent;
import com.duodebate.dto.DebateRequest;
import com.duodebate.dto.DebateResponse;
import com.duodebate.service.DebateOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DebateController {

    private final DebateOrchestrator debateOrchestrator;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostMapping("/debate")
    public ResponseEntity<DebateResponse> conductDebate(@Valid @RequestBody DebateRequest request) {
        log.info("Received debate request: {}", request);

        try {
            DebateResponse response = debateOrchestrator.conductDebate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error conducting debate", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/debate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter conductDebateStream(@Valid @RequestBody DebateRequest request) {
        log.info("Received streaming debate request: {}", request);

        SseEmitter emitter = new SseEmitter(600000L); // 10 minute timeout

        executorService.execute(() -> {
            try {
                debateOrchestrator.conductDebateStreaming(request, emitter);
                emitter.complete();
            } catch (Exception e) {
                log.error("Error in streaming debate", e);
                try {
                    DebateEvent errorEvent = DebateEvent.builder()
                            .type(DebateEvent.EventType.ERROR)
                            .error(e.getMessage())
                            .build();
                    emitter.send(errorEvent);
                } catch (Exception ex) {
                    log.error("Failed to send error event", ex);
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("DuoDebate API is running");
    }
}
