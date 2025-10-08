# Phase 2: Local Model Support with Ollama

## Overview

Phase 2 will add support for local LLM models via Ollama, enabling scenarios like:
- Two local models debating
- One remote + one local model
- Custom base URLs for OpenAI-compatible APIs

## Current State (Phase 1)

✅ **Implemented:**
- Role-based configuration (PROPOSER/CHALLENGER)
- Generic variable names instead of provider-specific
- Support for BASE_URL override (prepared for local models)
- Spring AI auto-configuration for OpenAI and Gemini

**Configuration Variables:**
```yaml
PROPOSER_API_KEY: "..."
PROPOSER_MODEL: "gpt-4-turbo"
PROPOSER_BASE_URL: ""  # empty = use default

CHALLENGER_API_KEY: "..."
CHALLENGER_MODEL: "gemini-2.0-flash-exp"
CHALLENGER_BASE_URL: ""  # empty = use default
```

## Phase 2 Implementation Plan

### 1. Add Ollama Dependency

Already prepared in research:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-ollama</artifactId>
</dependency>
```

### 2. Dynamic ChatModel Builder

Create a factory method in `DebateOrchestrator` to build ChatModel based on configuration:

```java
private ChatModel buildChatModel(String apiKey, String model, String baseUrl) {
    if (!baseUrl.isEmpty()) {
        // Local or custom OpenAI-compatible endpoint
        if (baseUrl.contains("localhost") || baseUrl.contains("11434")) {
            return buildOllamaModel(model, baseUrl);
        } else {
            return buildOpenAiModel(model, apiKey, baseUrl);
        }
    } else {
        // Default: determine by API key format or model name
        if (model.startsWith("gemini")) {
            return buildGeminiModel(model, apiKey);
        } else {
            return buildOpenAiModel(model, apiKey, "");
        }
    }
}

private ChatModel buildOllamaModel(String model, String baseUrl) {
    String ollamaUrl = baseUrl.isEmpty() ? "http://localhost:11434" : baseUrl;
    OllamaApi ollamaApi = new OllamaApi(ollamaUrl);
    OllamaOptions options = OllamaOptions.create()
            .withModel(model)
            .withTemperature(0.7f);
    return new OllamaChatModel(ollamaApi, options);
}

private ChatModel buildOpenAiModel(String model, String apiKey, String baseUrl) {
    OpenAiApi openAiApi = baseUrl.isEmpty()
        ? new OpenAiApi(apiKey)
        : new OpenAiApi(baseUrl, apiKey);

    OpenAiChatOptions options = OpenAiChatOptions.builder()
            .withModel(model)
            .withTemperature(1.0)
            .build();

    return new OpenAiChatModel(openAiApi, options);
}

private ChatModel buildGeminiModel(String model, String apiKey) {
    GoogleGenAiApi geminiApi = new GoogleGenAiApi(apiKey);
    GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
            .withModel(model)
            .withTemperature(0.7f)
            .build();
    return new GoogleGenAiChatModel(geminiApi, options);
}
```

### 3. Update Constructor

Replace auto-configured ChatModel injection with manual building:

```java
public DebateOrchestrator(
        @Value("classpath:prompts/proposer-system.txt") Resource proposerPrompt,
        @Value("classpath:prompts/challenger-system.txt") Resource challengerPrompt,
        @Value("${PROPOSER_API_KEY:}") String proposerApiKey,
        @Value("${PROPOSER_MODEL}") String proposerModel,
        @Value("${PROPOSER_BASE_URL:}") String proposerBaseUrl,
        @Value("${CHALLENGER_API_KEY:}") String challengerApiKey,
        @Value("${CHALLENGER_MODEL}") String challengerModel,
        @Value("${CHALLENGER_BASE_URL:}") String challengerBaseUrl,
        ObjectMapper objectMapper) throws IOException {

    // Build chat models dynamically
    ChatModel proposerChatModel = buildChatModel(proposerApiKey, proposerModel, proposerBaseUrl);
    ChatModel challengerChatModel = buildChatModel(challengerApiKey, challengerModel, challengerBaseUrl);

    this.proposerClient = ChatClient.builder(proposerChatModel)
            .defaultSystem(this.proposerSystemPrompt)
            .build();

    this.challengerClient = ChatClient.builder(challengerChatModel)
            .defaultSystem(this.challengerSystemPrompt)
            .build();
}
```

### 4. Example Configurations

**Two Remote (Current - Phase 1):**
```yaml
PROPOSER_API_KEY: "sk-proj-..."
PROPOSER_MODEL: "gpt-4o"
PROPOSER_BASE_URL: ""

CHALLENGER_API_KEY: "..."
CHALLENGER_MODEL: "gemini-2.0-flash-exp"
CHALLENGER_BASE_URL: ""
```

**One Local (Ollama) + One Remote:**
```yaml
PROPOSER_API_KEY: ""  # not needed for Ollama
PROPOSER_MODEL: "llama3:70b"
PROPOSER_BASE_URL: "http://localhost:11434"

CHALLENGER_API_KEY: "sk-proj-..."
CHALLENGER_MODEL: "gpt-4o"
CHALLENGER_BASE_URL: ""
```

**Two Local (Ollama):**
```yaml
PROPOSER_API_KEY: ""
PROPOSER_MODEL: "llama3:70b"
PROPOSER_BASE_URL: "http://localhost:11434"

CHALLENGER_API_KEY: ""
CHALLENGER_MODEL: "mistral:latest"
CHALLENGER_BASE_URL: "http://localhost:11434"
```

**Custom OpenAI-Compatible API:**
```yaml
PROPOSER_API_KEY: "custom-key"
PROPOSER_MODEL: "custom-model"
PROPOSER_BASE_URL: "https://api.custom.com/v1"

CHALLENGER_API_KEY: "..."
CHALLENGER_MODEL: "gemini-2.0-flash-exp"
CHALLENGER_BASE_URL: ""
```

## Testing Checklist

- [ ] Test OpenAI + Gemini (existing functionality)
- [ ] Test Ollama locally (llama3 + mistral)
- [ ] Test OpenAI + Ollama hybrid
- [ ] Test custom OpenAI-compatible endpoint
- [ ] Verify provider names display correctly in UI
- [ ] Test BASE_URL validation and error handling

## Potential Issues

1. **Ollama JSON Format**: Ollama models may not follow the JSON format as strictly as OpenAI/Gemini
   - **Solution**: Enhanced JSON parsing with fallback extraction (already implemented)

2. **Ollama Model Availability**: Models need to be pulled first with `ollama pull`
   - **Solution**: Add health check endpoint that validates model availability

3. **Temperature Ranges**: Different providers have different temperature ranges
   - **Solution**: Normalize or configure per-provider

4. **API Key Validation**: Empty API keys for Ollama should not fail validation
   - **Solution**: Make API_KEY optional, validate based on BASE_URL

## Documentation Updates Needed

- [ ] Update README with Ollama setup instructions
- [ ] Add OLLAMA_SETUP.md with model pulling and configuration
- [ ] Update secrets.yml.example with Ollama examples
- [ ] Add troubleshooting section for local models

## Benefits

1. **Cost Savings**: Run debates locally without API costs
2. **Privacy**: Sensitive data stays local
3. **Customization**: Use fine-tuned or specialized models
4. **Offline**: Works without internet connectivity
5. **Speed**: Reduced latency for local models on powerful hardware

## Timeline Estimate

- Implementation: 2-3 hours
- Testing: 1-2 hours
- Documentation: 1 hour
- **Total: 4-6 hours**

## Status

🔄 **Phase 1 Complete** - Role-based configuration with OpenAI + Gemini
⏳ **Phase 2 Pending** - Awaiting user confirmation to implement Ollama support
