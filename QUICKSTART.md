# DuoDebate - Quick Start Guide

## 🚀 Fast Setup (5 minutes)

### Step 1: Configure API Keys

```bash
cp .env.example .env
```

Edit `.env` and add your API keys:
- `OPENAI_API_KEY`: Get from https://platform.openai.com/api-keys
- `GEMINI_API_KEY`: Get from https://aistudio.google.com/apikey
- `SITE_PASSWORD`: (Optional) Password protect your site

### Step 2: Start Backend (Terminal 1)

```bash
cd backend
./mvnw spring-boot:run
```

Wait for: `Started DuoDebateApplication in X seconds`

### Step 3: Start Frontend (Terminal 2)

```bash
cd frontend
npm install
npm run dev
```

Open: http://localhost:5173

### Step 4: Test It!

1. Enter prompt: **"Outline a blog post on AI in technical marketing"**
2. Click **"Start Debate 🚀"**
3. Watch the debate unfold!

---

## 🎯 Example Prompts

- "Create a product launch strategy for an AI coding assistant"
- "Design a database schema for a social media platform"
- "Write an executive summary for adopting Kubernetes"
- "Outline a technical presentation on microservices"

## 🔧 Troubleshooting

**Backend won't start?**
- Check your API keys in `.env`
- Ensure Java 21+ is installed: `java -version`

**Frontend won't connect?**
- Verify backend is running on port 8080
- Check CORS settings in `application.properties`

**Models not responding?**
- Verify API keys are valid
- Check console logs for detailed errors

## 📊 Architecture at a Glance

```
User Input → Frontend (React)
              ↓
          POST /api/debate (SSE)
              ↓
    DebateOrchestrator (Spring AI)
      ↓                    ↓
  OpenAI GPT-4      Google Gemini
  (PROPOSER)        (CHALLENGER)
      ↓                    ↓
    Iterate until READY or max rounds
              ↓
    Final Draft → Stream to User
```

## 🎨 Customization Ideas

### Change AI Models

Edit `backend/src/main/resources/application.properties`:

```properties
# Use GPT-3.5 for faster/cheaper responses
spring.ai.openai.chat.options.model=gpt-3.5-turbo

# Use different Gemini model
spring.ai.google.genai.chat.options.model=gemini-1.5-pro
```

### Adjust Temperature

Lower = more focused, Higher = more creative

```properties
spring.ai.openai.chat.options.temperature=0.3  # More deterministic
spring.ai.google.genai.chat.options.temperature=0.9  # More creative
```

### Swap Roles

Want Gemini as PROPOSER and GPT as CHALLENGER?

Edit `DebateOrchestrator.java` and swap the chat clients.

### Add More Models

Spring AI supports:
- Anthropic Claude
- Azure OpenAI
- Ollama (local models)
- Hugging Face
- AWS Bedrock

Check: https://docs.spring.io/spring-ai/reference/

## 📝 Next Steps

- Deploy to Cloud Foundry (see [CF_DEPLOYMENT.md](CF_DEPLOYMENT.md))
- Add authentication for multi-user support
- Store debate history in a database
- Export debates as PDF/Markdown
- Create debate templates for common use cases

## 🤝 Need Help?

- Check the main [README.md](README.md) for detailed docs
- Review Spring AI docs: https://docs.spring.io/spring-ai/reference/

---

**Happy Debating! 🎉**
