<div align="center">

# 🎭 DuoDebate

### *Where AI Minds Meet to Refine Ideas*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.1.1-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Vite](https://img.shields.io/badge/Vite-7.1.2-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)
[![OpenAI](https://img.shields.io/badge/OpenAI-GPT--4-412991?style=for-the-badge&logo=openai&logoColor=white)](https://openai.com/)
[![Gemini](https://img.shields.io/badge/Google-Gemini-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/)

<p align="center">
  <strong>A full-stack AI-powered debate platform where two LLMs collaborate in real-time to refine your ideas</strong>
</p>

[Quick Start](#-quick-start) •
[Features](#-features) •
[Architecture](#-architecture) •
[Tech Stack](#-tech-stack) •
[Deployment](#-deployment)

---

</div>

## 🎯 What is DuoDebate?

DuoDebate orchestrates **intelligent debates** between two AI models that work together to refine your ideas:

<div align="center">

| 🤖 **PROPOSER** | 🎯 **CHALLENGER** |
|:---:|:---:|
| **OpenAI** | **Google Gemini** |
| Creates and refines drafts | Provides constructive criticism |
| Iterates based on feedback | Asks probing questions |
| Marks completion when ready | Ensures quality and completeness |

</div>

Watch as they iterate back and forth, each response building on the last, until they reach consensus or hit the maximum iteration limit!

## ✨ Features

<table>
<tr>
<td width="50%">

### 🎨 **Beautiful UI**
- **Glassmorphic Design** with translucent cards
- **Liquid Metal Theme** with animated gradients
- **Markdown Rendering** with table support
- **Copy & Download** final drafts and sources
- **Responsive Layout** that works everywhere

</td>
<td width="50%">

### ⚡ **Powerful Backend**
- **Spring AI 1.1.0** for seamless model integration
- **Server-Sent Events (SSE)** for real-time streaming
- **Source Citation Tracking** for verifiable claims
- **Spring Security** with configurable auth
- **Comprehensive Logging** for debugging

</td>
</tr>
<tr>
<td width="50%">

### 🤖 **Dual AI Models**
- **OpenAI** as the creative proposer with citations
- **Google Gemini** as the critical challenger
- **Configurable Models** via environment variables
- **Configurable Iterations** (1-20 rounds)
- **Automatic Consensus** detection

</td>
<td width="50%">

### 🚀 **Developer Experience**
- **Hot Module Replacement** for instant updates
- **Automated Scripts** for easy setup
- **Cloud Foundry Ready** with single-instance packaging
- **Source Verification** for all factual claims
- **Detailed Documentation** for everything

</td>
</tr>
</table>

## 🏗️ Architecture

```
┌─────────────┐                                    ┌──────────────┐
│   Browser   │◄───────── SSE Stream ─────────────►│ Spring Boot  │
│   (React)   │                                    │   Backend    │
└─────────────┘                                    └──────┬───────┘
                                                          │
                                            ┌─────────────┴─────────────┐
                                            │                           │
                                    ┌───────▼──────┐           ┌───────▼──────┐
                                    │  OpenAI API  │           │  Gemini API  │
                                    │  PROPOSER    │           │  CHALLENGER  │
                                    └──────────────┘           └──────────────┘
                                            │                           │
                                            └──────────┬────────────────┘
                                                       │
                                                  Iterative
                                                   Debate
```

## 🚀 Quick Start

### Prerequisites

- ☕ **Java 21+** ([Download](https://adoptium.net/))
- 📦 **Node.js 18+** ([Download](https://nodejs.org/))
- 🔑 **OpenAI API Key** ([Get Key](https://platform.openai.com/api-keys))
- 🔑 **Gemini API Key** ([Get Key](https://aistudio.google.com/apikey))

### 1️⃣ Clone & Configure

```bash
# Clone the repository
git clone <your-repo-url>
cd DuoDebate

# Copy secrets template
cp secrets.yml.example secrets.yml

# Edit secrets.yml with your API keys
```

Your `secrets.yml` should look like:
```yaml
OPENAI_API_KEY: "sk-proj-your-key-here"
OPENAI_CHAT_MODEL: "gpt-5-nano"  # or your preferred OpenAI model
GEMINI_API_KEY: "your-gemini-key-here"
GEMINI_CHAT_MODEL: "gemini-2.5-flash"  # or your preferred Gemini model
SECURITY_ENABLED: "false"  # Set to "true" to enable password protection
SECURITY_USERNAME: "admin"
SECURITY_PASSWORD: "your-password-here"
```

### 2️⃣ Run DuoDebate

**Easy Mode** (Recommended):
```bash
# First time: build everything
./run.sh --build

# Subsequent runs
./run.sh
```

**Manual Mode**:
```bash
# Terminal 1: Backend
cd backend
./mvnw spring-boot:run

# Terminal 2: Frontend
cd frontend
npm install && npm run dev
```

### 3️⃣ Start Debating!

Open [http://localhost:5173](http://localhost:5173) and try:
- *"Outline a blog post on AI in technical marketing"*
- *"Design a database schema for a social media platform"*
- *"Create a product launch strategy for an AI coding assistant"*

## 💻 Tech Stack

### 🚀 Backend Technologies

<table>
<tr>
<th width="30%">Technology</th>
<th width="70%">Purpose</th>
</tr>

<tr>
<td>**Spring Boot 3.4.1**</td>
<td>Modern Java framework with auto-configuration</td>
</tr>

<tr>
<td>**Spring AI 1.1.0**</td>
<td>Unified API for multiple LLM providers</td>
</tr>

<tr>
<td>**Java 21**</td>
<td>Latest LTS with virtual threads and pattern matching</td>
</tr>

<tr>
<td>**Maven**</td>
<td>Dependency management and build automation</td>
</tr>

<tr>
<td>**Logback**</td>
<td>Comprehensive logging with file rotation</td>
</tr>

<tr>
<td>**Jackson**</td>
<td>JSON serialization/deserialization</td>
</tr>

</table>

## 📖 API Reference

### POST `/api/debate`

Start a debate between AI models with Server-Sent Events streaming.

**Request:**
```json
{
  "prompt": "Your debate topic",
  "maxIterations": 10
}
```

**Response:** SSE Stream with events:
- `DEBATE_START` - Debate initiated
- `ITERATION_START` - New round beginning
- `PROPOSER_RESPONSE` - Proposer's message
- `CHALLENGER_RESPONSE` - Challenger's message
- `DEBATE_COMPLETE` - Final result with complete transcript
- `ERROR` - Error occurred

### GET `/api/health`

Health check endpoint returning `{"status": "UP"}`.

## 🎨 UI Design & Libraries

DuoDebate features a modern, glassmorphic UI built with zero external CSS frameworks - just React, Vite, and pure CSS3.

### 🎭 Design Philosophy

**Glassmorphism** - A modern design trend featuring translucent, frosted-glass effects that create depth and visual hierarchy while maintaining readability.

**Liquid Metal Background** - Animated gradient backgrounds that shift smoothly, creating a dynamic, engaging experience.

**Role-Based Visual Identity** - Each AI model has distinct colors and styling to make debates easy to follow.

### 📚 Frontend Libraries

<table>
<tr>
<th width="25%">Library</th>
<th width="15%">Version</th>
<th width="60%">Purpose</th>
</tr>

<tr>
<td><strong>React</strong></td>
<td>19.1.1</td>
<td>Core UI framework with hooks for state management and side effects</td>
</tr>

<tr>
<td><strong>Vite</strong></td>
<td>7.1.2</td>
<td>Lightning-fast dev server with HMR and optimized production builds</td>
</tr>

<tr>
<td><strong>react-markdown</strong></td>
<td>Latest</td>
<td>GitHub-flavored markdown rendering for debate outputs</td>
</tr>

<tr>
<td><strong>remark-gfm</strong></td>
<td>Latest</td>
<td>Markdown tables, strikethrough, task lists, and other GFM features</td>
</tr>

<tr>
<td><strong>Pure CSS3</strong></td>
<td>-</td>
<td>Zero external CSS frameworks - custom glassmorphism and animations</td>
</tr>

<tr>
<td><strong>Fetch API + SSE</strong></td>
<td>Native</td>
<td>Browser-native Server-Sent Events for real-time streaming</td>
</tr>

</table>

### 🎨 Visual Design System

#### Color Palette

```css
/* PROPOSER - Blue/Purple Gradient */
.proposer {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
}

/* CHALLENGER - Pink/Orange Gradient */
.challenger {
  background: linear-gradient(135deg, #ec4899 0%, #f97316 100%);
}

/* Primary Gradient - App Background */
.background {
  background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
  animation: gradientShift 15s ease infinite;
}
```

#### Glassmorphism Effects

```css
/* Glass Card Component */
.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 20px;
}

/* Chat Bubbles */
.chat-bubble {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
```

#### Typography & Spacing

- **Headings**: System font stack with gradient text effects
- **Body Text**: `rgba(255, 255, 255, 0.9)` for high contrast on dark backgrounds
- **Spacing**: Consistent 1rem base with 1.5-2rem gaps between sections
- **Line Height**: 1.6-1.8 for optimal readability

### 🧩 Component Architecture

```
App.jsx (Main Container)
├── State Management (React hooks)
├── SSE Event Handling
└── Auto-scroll Logic

├─ GlassCard (Reusable Container)
│  ├── Glassmorphic backdrop-filter
│  ├── Border gradients
│  └── Responsive padding

├─ ChatBubble (Message Display)
│  ├── MarkdownRenderer (Content)
│  ├── Role badges (PROPOSER/CHALLENGER)
│  ├── Model labels (OpenAI/Gemini)
│  └── Iteration counter

├─ MarkdownRenderer (Content Formatter)
│  ├── react-markdown (Parser)
│  ├── remark-gfm (Tables support)
│  └── Custom table styling

└─ Toolbar (Input Interface)
   ├── Prompt textarea with validation
   ├── Max iterations selector
   ├── Submit button with loading state
   └── Copy/Download actions
```

### ✨ Interactive Features

- **📋 Copy to Clipboard** - One-click copy of final drafts with visual feedback
- **⬇️ Download as Markdown** - Export debates as `.md` files with timestamp
- **📚 Source Downloads** - Separate download for citation bibliography
- **🎯 Auto-scroll** - Automatically follows the debate as it progresses
- **💫 Loading States** - Animated typing indicators during AI responses
- **🎨 Hover Effects** - Smooth transitions on buttons and cards

### 📱 Responsive Design

- **Desktop**: Full-width layout with optimal 1200px max-width
- **Tablet**: Flexible grid system adapts to smaller screens
- **Mobile**: Single-column layout with horizontal scroll for tables
- **Touch-friendly**: Adequate button sizes and spacing for mobile interaction

## 📁 Project Structure

```
DuoDebate/
├── 📂 backend/
│   ├── src/main/java/com/duodebate/
│   │   ├── DuoDebateApplication.java       # Spring Boot entry point
│   │   ├── controller/
│   │   │   └── DebateController.java       # REST API + SSE
│   │   ├── service/
│   │   │   └── DebateOrchestrator.java     # AI orchestration logic
│   │   └── dto/
│   │       ├── DebateRequest.java          # API request model
│   │       ├── DebateResponse.java         # API response model
│   │       ├── DebateMessage.java          # Chat message model
│   │       └── DebateEvent.java            # SSE event model
│   ├── src/main/resources/
│   │   ├── application.properties          # Spring configuration
│   │   ├── logback-spring.xml              # Logging config
│   │   └── prompts/
│   │       ├── proposer-system.txt         # PROPOSER system prompt
│   │       └── challenger-system.txt       # CHALLENGER system prompt
│   └── pom.xml                             # Maven dependencies
│
├── 📂 frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── ChatBubble.jsx              # Message display component
│   │   │   ├── GlassCard.jsx               # Reusable glass container
│   │   │   ├── Toolbar.jsx                 # Input/submit interface
│   │   │   └── PasswordPrompt.jsx          # Optional auth
│   │   ├── styles/
│   │   │   ├── ChatBubble.css              # Message styling
│   │   │   ├── GlassCard.css               # Glass effect styles
│   │   │   ├── Toolbar.css                 # Toolbar styling
│   │   │   └── PasswordPrompt.css          # Auth modal styles
│   │   ├── api/
│   │   │   └── debateApi.js                # API client with SSE
│   │   ├── App.jsx                         # Main application
│   │   ├── App.css                         # App-level styles
│   │   ├── index.css                       # Global styles
│   │   └── main.jsx                        # React entry point
│   ├── package.json                        # NPM dependencies
│   └── vite.config.js                      # Vite configuration
│
├── 📂 Scripts & Configuration
│   ├── run.sh                              # Start everything
│   ├── stop.sh                             # Stop all services
│   ├── build-all.sh                        # Build for production
│   ├── logs.sh                             # View logs
│   ├── manifest.yml                        # Cloud Foundry manifest
│   └── secrets.yml.example                 # CF secrets template
│
└── 📄 Documentation
    ├── README.md                           # This file
    ├── QUICKSTART.md                       # 5-minute setup guide
    ├── CF_DEPLOYMENT.md                    # Cloud Foundry deployment
    ├── GCP_SETUP.md                        # Google Cloud setup
    └── LOGGING.md                          # Logging documentation
```

## 🚀 Deployment

### Local Development

```bash
./run.sh --build
```

### Cloud Foundry

```bash
# Create secrets
cp secrets.yml.example secrets.yml
# Edit secrets.yml with your API keys

# Build and deploy
./build-all.sh
cf push --vars-file secrets.yml
```

See [CF_DEPLOYMENT.md](CF_DEPLOYMENT.md) for detailed instructions.

## 🔧 Configuration

### Adjust AI Models

Edit `secrets.yml` (or environment variables):

```yaml
# OpenAI Model Configuration
OPENAI_CHAT_MODEL: "gpt-5-nano"  # Use any available OpenAI model

# Gemini Model Configuration
GEMINI_CHAT_MODEL: "gemini-2.5-flash"  # Use any available Gemini model

# Temperature (in application.properties)
spring.ai.openai.chat.options.temperature=0.7
spring.ai.google.genai.chat.options.temperature=0.8
```

### Swap Roles

Want Gemini as PROPOSER and GPT as CHALLENGER? Edit `DebateOrchestrator.java` and swap the chat clients.

## 📊 Logging & Debugging

DuoDebate includes comprehensive logging:

```bash
./logs.sh           # View all logs
./logs.sh debates   # Debate transcripts only
./logs.sh ai        # AI API interactions
./logs.sh app       # Application logs
```

**Log Files:**
- `backend/logs/duodebate.log` - General application logs
- `backend/logs/debates.log` - Debate transcripts
- `backend/logs/spring-ai.log` - AI model interactions

See [LOGGING.md](LOGGING.md) for detailed logging documentation.

## 🤝 Contributing

Contributions welcome! Feel free to:
- 🐛 Report bugs
- 💡 Suggest features
- 🔧 Submit pull requests
- 📖 Improve documentation

## 📝 License

This project is open source and available under the MIT License.

## 🙏 Acknowledgments

- **[Spring AI](https://docs.spring.io/spring-ai/reference/)** - Unified AI integration framework
- **[OpenAI](https://openai.com/)** - GPT-4 API
- **[Google AI](https://ai.google.dev/)** - Gemini API
- **[React](https://react.dev/)** - UI framework
- **[Vite](https://vitejs.dev/)** - Build tooling

---

<div align="center">

**Built with ❤️ using Spring AI, React, and the power of collaborative AI**

[⭐ Star this repo](https://github.com/yourusername/duodebate) • [🐛 Report Bug](https://github.com/yourusername/duodebate/issues) • [💡 Request Feature](https://github.com/yourusername/duodebate/issues)

</div>
