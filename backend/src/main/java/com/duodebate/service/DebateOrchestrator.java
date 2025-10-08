package com.duodebate.service;

import com.duodebate.dto.DebateEvent;
import com.duodebate.dto.DebateMessage;
import com.duodebate.dto.DebateRequest;
import com.duodebate.dto.DebateResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DebateOrchestrator {

    private final ChatClient proposerClient;
    private final ChatClient challengerClient;
    private final String proposerSystemPrompt;
    private final String challengerSystemPrompt;
    private final ObjectMapper objectMapper;

    public DebateOrchestrator(
            OpenAiChatModel openAiChatModel,
            GoogleGenAiChatModel geminiChatModel,
            @Value("classpath:prompts/proposer-system.txt") Resource proposerPrompt,
            @Value("classpath:prompts/challenger-system.txt") Resource challengerPrompt,
            ObjectMapper objectMapper) throws IOException {

        this.proposerSystemPrompt = proposerPrompt.getContentAsString(StandardCharsets.UTF_8);
        this.challengerSystemPrompt = challengerPrompt.getContentAsString(StandardCharsets.UTF_8);
        this.objectMapper = objectMapper;

        // OpenAI for PROPOSER
        this.proposerClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(this.proposerSystemPrompt)
                .build();

        // Google Gemini for CHALLENGER
        this.challengerClient = ChatClient.builder(geminiChatModel)
                .defaultSystem(this.challengerSystemPrompt)
                .build();

        log.info("DebateOrchestrator initialized with OpenAI (PROPOSER) and Google Gemini (CHALLENGER)");
    }

    public DebateResponse conductDebate(DebateRequest request) {
        log.info("Starting debate for prompt: {}", request.getPrompt());

        List<DebateMessage> transcript = new ArrayList<>();
        List<String> sources = new ArrayList<>();
        String currentDraft = "";
        String challengerFeedback = "";
        String status = "ONGOING";
        int maxIterations = request.getMaxIterations();

        for (int i = 0; i < maxIterations; i++) {
            log.info("=== Iteration {} ===", i + 1);

            // PROPOSER's turn
            String proposerPrompt = buildProposerPrompt(request.getPrompt(), challengerFeedback, i);
            var proposerChatResponse = proposerClient.prompt()
                    .user(proposerPrompt)
                    .call()
                    .chatResponse();

            String proposerResponse = proposerChatResponse.getResult().getOutput().getText();

            log.debug("PROPOSER raw response: {}", proposerResponse);

            try {
                JsonNode proposerJson = parseJsonResponse(proposerResponse);
                currentDraft = proposerJson.get("draft").asText();
                String proposerMessage = proposerJson.has("response")
                    ? proposerJson.get("response").asText()
                    : "Initial draft created";
                status = proposerJson.get("status").asText();

                // Extract sources from PROPOSER's JSON response
                if (proposerJson.has("sources") && proposerJson.get("sources").isArray()) {
                    proposerJson.get("sources").forEach(sourceNode -> {
                        String source = sourceNode.asText();
                        if (!source.isEmpty() && !sources.contains(source)) {
                            sources.add(source);
                            log.info("Added source: {}", source);
                        }
                    });
                }

                DebateMessage proposerMsg = DebateMessage.builder()
                        .role("PROPOSER")
                        .content(proposerMessage)
                        .iteration(i + 1)
                        .model("openai")
                        .status(status)
                        .build();

                transcript.add(proposerMsg);
                log.info("PROPOSER (iteration {}): status={}, draft_length={}, sources_count={}",
                    i + 1, status, currentDraft.length(), sources.size());
                log.info("PROPOSER Response: {}", proposerMessage);
                log.info("PROPOSER Current Draft:\n{}", currentDraft);

                if ("READY".equalsIgnoreCase(status)) {
                    log.info("PROPOSER marked draft as READY");
                    break;
                }

            } catch (Exception e) {
                log.error("Error parsing PROPOSER response", e);
                transcript.add(DebateMessage.builder()
                        .role("PROPOSER")
                        .content("Error processing response: " + e.getMessage())
                        .iteration(i + 1)
                        .model("openai")
                        .status("ERROR")
                        .build());
                break;
            }

            // CHALLENGER's turn
            String challengerPrompt = buildChallengerPrompt(request.getPrompt(), currentDraft);
            String challengerResponse = challengerClient.prompt()
                    .user(challengerPrompt)
                    .call()
                    .content();

            log.debug("CHALLENGER raw response: {}", challengerResponse);

            try {
                JsonNode challengerJson = parseJsonResponse(challengerResponse);
                String critique = challengerJson.get("critique").asText();

                StringBuilder challengerMsg = new StringBuilder(critique);

                if (challengerJson.has("questions") && challengerJson.get("questions").isArray()) {
                    challengerMsg.append("\n\nQuestions:");
                    challengerJson.get("questions").forEach(q ->
                        challengerMsg.append("\n- ").append(q.asText()));
                }

                if (challengerJson.has("suggestions") && challengerJson.get("suggestions").isArray()) {
                    challengerMsg.append("\n\nSuggestions:");
                    challengerJson.get("suggestions").forEach(s ->
                        challengerMsg.append("\n- ").append(s.asText()));
                }

                challengerFeedback = challengerMsg.toString();

                DebateMessage challengerMsgObj = DebateMessage.builder()
                        .role("CHALLENGER")
                        .content(challengerFeedback)
                        .iteration(i + 1)
                        .model("gemini")
                        .status("ONGOING")
                        .build();

                transcript.add(challengerMsgObj);
                log.info("CHALLENGER (iteration {}): provided critique and suggestions", i + 1);
                log.info("CHALLENGER Critique:\n{}", challengerFeedback);

            } catch (Exception e) {
                log.error("Error parsing CHALLENGER response", e);
                transcript.add(DebateMessage.builder()
                        .role("CHALLENGER")
                        .content("Error processing response: " + e.getMessage())
                        .iteration(i + 1)
                        .model("gemini")
                        .status("ERROR")
                        .build());
                break;
            }
        }

        String finalStatus = "READY".equalsIgnoreCase(status) ? "READY" : "MAX_ITERATIONS";
        int totalIterations = transcript.size() / 2;

        log.info("========================================");
        log.info("Debate Completed!");
        log.info("========================================");
        log.info("Status: {}", finalStatus);
        log.info("Total Iterations: {}", totalIterations);
        log.info("Final Draft Length: {} characters", currentDraft.length());
        log.info("========================================");
        log.info("FINAL DRAFT:\n{}", currentDraft);
        log.info("========================================");

        return DebateResponse.builder()
                .prompt(request.getPrompt())
                .transcript(transcript)
                .finalStatus(finalStatus)
                .totalIterations(totalIterations)
                .finalDraft(currentDraft)
                .sources(sources)
                .build();
    }

    public void conductDebateStreaming(DebateRequest request, SseEmitter emitter) throws IOException {
        log.info("Starting streaming debate for prompt: {}", request.getPrompt());

        // Send start event
        emitter.send(DebateEvent.builder()
                .type(DebateEvent.EventType.DEBATE_START)
                .build());

        List<DebateMessage> transcript = new ArrayList<>();
        List<String> sources = new ArrayList<>();
        String currentDraft = "";
        String challengerFeedback = "";
        String status = "ONGOING";
        int maxIterations = request.getMaxIterations();

        for (int i = 0; i < maxIterations; i++) {
            log.info("=== Iteration {} ===", i + 1);

            // Send iteration start event
            emitter.send(DebateEvent.builder()
                    .type(DebateEvent.EventType.ITERATION_START)
                    .build());

            // PROPOSER's turn
            String proposerPrompt = buildProposerPrompt(request.getPrompt(), challengerFeedback, i);
            var proposerChatResponse = proposerClient.prompt()
                    .user(proposerPrompt)
                    .call()
                    .chatResponse();

            String proposerResponse = proposerChatResponse.getResult().getOutput().getText();

            log.debug("PROPOSER raw response: {}", proposerResponse);

            try {
                JsonNode proposerJson = parseJsonResponse(proposerResponse);
                currentDraft = proposerJson.get("draft").asText();
                String proposerMessage = proposerJson.has("response")
                    ? proposerJson.get("response").asText()
                    : "Initial draft created";
                status = proposerJson.get("status").asText();

                // Extract sources from PROPOSER's JSON response
                if (proposerJson.has("sources") && proposerJson.get("sources").isArray()) {
                    proposerJson.get("sources").forEach(sourceNode -> {
                        String source = sourceNode.asText();
                        if (!source.isEmpty() && !sources.contains(source)) {
                            sources.add(source);
                            log.info("Added source: {}", source);
                        }
                    });
                }

                // For first iteration, show the draft. For subsequent iterations, show the response message
                String displayContent = (i == 0) ? currentDraft : proposerMessage;

                DebateMessage proposerMsg = DebateMessage.builder()
                        .role("PROPOSER")
                        .content(displayContent)
                        .iteration(i + 1)
                        .model("openai")
                        .status(status)
                        .build();

                transcript.add(proposerMsg);
                log.info("PROPOSER (iteration {}): status={}, draft_length={}, sources_count={}",
                    i + 1, status, currentDraft.length(), sources.size());
                log.info("PROPOSER Response: {}", proposerMessage);
                log.info("PROPOSER Current Draft:\n{}", currentDraft);

                // Send PROPOSER response event
                emitter.send(DebateEvent.builder()
                        .type(DebateEvent.EventType.PROPOSER_RESPONSE)
                        .message(proposerMsg)
                        .build());

                if ("READY".equalsIgnoreCase(status)) {
                    log.info("PROPOSER marked draft as READY");
                    break;
                }

            } catch (Exception e) {
                log.error("Error parsing PROPOSER response", e);
                DebateMessage errorMsg = DebateMessage.builder()
                        .role("PROPOSER")
                        .content("Error processing response: " + e.getMessage())
                        .iteration(i + 1)
                        .model("openai")
                        .status("ERROR")
                        .build();
                transcript.add(errorMsg);
                emitter.send(DebateEvent.builder()
                        .type(DebateEvent.EventType.ERROR)
                        .error(e.getMessage())
                        .message(errorMsg)
                        .build());
                break;
            }

            // CHALLENGER's turn
            String challengerPrompt = buildChallengerPrompt(request.getPrompt(), currentDraft);
            String challengerResponse = challengerClient.prompt()
                    .user(challengerPrompt)
                    .call()
                    .content();

            log.debug("CHALLENGER raw response: {}", challengerResponse);

            try {
                JsonNode challengerJson = parseJsonResponse(challengerResponse);
                String critique = challengerJson.get("critique").asText();

                StringBuilder challengerMsg = new StringBuilder(critique);

                if (challengerJson.has("questions") && challengerJson.get("questions").isArray()) {
                    challengerMsg.append("\n\nQuestions:");
                    challengerJson.get("questions").forEach(q ->
                        challengerMsg.append("\n- ").append(q.asText()));
                }

                if (challengerJson.has("suggestions") && challengerJson.get("suggestions").isArray()) {
                    challengerMsg.append("\n\nSuggestions:");
                    challengerJson.get("suggestions").forEach(s ->
                        challengerMsg.append("\n- ").append(s.asText()));
                }

                challengerFeedback = challengerMsg.toString();

                DebateMessage challengerMsgObj = DebateMessage.builder()
                        .role("CHALLENGER")
                        .content(challengerFeedback)
                        .iteration(i + 1)
                        .model("gemini")
                        .status("ONGOING")
                        .build();

                transcript.add(challengerMsgObj);
                log.info("CHALLENGER (iteration {}): provided critique and suggestions", i + 1);
                log.info("CHALLENGER Critique:\n{}", challengerFeedback);

                // Send CHALLENGER response event
                emitter.send(DebateEvent.builder()
                        .type(DebateEvent.EventType.CHALLENGER_RESPONSE)
                        .message(challengerMsgObj)
                        .build());

            } catch (Exception e) {
                log.error("Error parsing CHALLENGER response", e);
                DebateMessage errorMsg = DebateMessage.builder()
                        .role("CHALLENGER")
                        .content("Error processing response: " + e.getMessage())
                        .iteration(i + 1)
                        .model("gemini")
                        .status("ERROR")
                        .build();
                transcript.add(errorMsg);
                emitter.send(DebateEvent.builder()
                        .type(DebateEvent.EventType.ERROR)
                        .error(e.getMessage())
                        .message(errorMsg)
                        .build());
                break;
            }
        }

        String finalStatus = "READY".equalsIgnoreCase(status) ? "READY" : "MAX_ITERATIONS";
        int totalIterations = transcript.size() / 2;

        log.info("========================================");
        log.info("Debate Completed!");
        log.info("========================================");
        log.info("Status: {}", finalStatus);
        log.info("Total Iterations: {}", totalIterations);
        log.info("Final Draft Length: {} characters", currentDraft.length());
        log.info("========================================");
        log.info("FINAL DRAFT:\n{}", currentDraft);
        log.info("========================================");

        // Send completion event
        DebateResponse finalResponse = DebateResponse.builder()
                .prompt(request.getPrompt())
                .transcript(transcript)
                .finalStatus(finalStatus)
                .totalIterations(totalIterations)
                .finalDraft(currentDraft)
                .sources(sources)
                .build();

        emitter.send(DebateEvent.builder()
                .type(DebateEvent.EventType.DEBATE_COMPLETE)
                .finalResponse(finalResponse)
                .build());
    }

    private String buildProposerPrompt(String originalPrompt, String feedback, int iteration) {
        if (iteration == 0) {
            return String.format(
                "Create an initial draft for the following request:\n\n%s\n\n" +
                "Remember to respond in JSON format with 'draft', 'response', and 'status' fields.",
                originalPrompt
            );
        } else {
            return String.format(
                "Original request: %s\n\n" +
                "The CHALLENGER provided this feedback:\n%s\n\n" +
                "Please refine your draft based on this feedback. " +
                "Respond in JSON format with 'draft', 'response', and 'status' fields.",
                originalPrompt, feedback
            );
        }
    }

    private String buildChallengerPrompt(String originalPrompt, String currentDraft) {
        return String.format(
            "Original request: %s\n\n" +
            "Current draft:\n%s\n\n" +
            "Please provide constructive criticism and suggestions for improvement. " +
            "Respond in JSON format with 'critique', 'questions', and 'suggestions' fields.",
            originalPrompt, currentDraft
        );
    }

    private JsonNode parseJsonResponse(String response) throws IOException {
        // Try to extract JSON from markdown code blocks if present
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        try {
            return objectMapper.readTree(cleaned);
        } catch (Exception e) {
            // If JSON parsing fails, try to extract JSON from the response
            log.warn("Failed to parse JSON directly, attempting to extract JSON block");

            // Try to find JSON between curly braces
            int start = cleaned.indexOf('{');
            int lastEnd = cleaned.lastIndexOf('}');

            if (start >= 0 && lastEnd > start) {
                String extracted = cleaned.substring(start, lastEnd + 1);
                try {
                    return objectMapper.readTree(extracted);
                } catch (Exception e2) {
                    log.error("Failed to parse extracted JSON: {}", extracted);
                    throw e; // Throw original exception
                }
            }

            throw e; // No JSON found, throw original exception
        }
    }
}
