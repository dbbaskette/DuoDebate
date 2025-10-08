# DuoDebate Logging Guide

Complete guide to logging and debugging in DuoDebate.

## 📋 Log Files Overview

### Runtime Logs (Root Directory)

Created by `./run.sh`:

| File | Purpose | Content |
|------|---------|---------|
| `backend.log` | Backend runtime output | Maven, Spring Boot startup, console output |
| `frontend.log` | Frontend runtime output | Vite dev server, HMR, console output |

### Application Logs (backend/logs/)

Created by Spring Boot application:

| File | Purpose | Rotation | Retention |
|------|---------|----------|-----------|
| `duodebate.log` | General application logs | 10MB/day | 7 days |
| `debates.log` | Debate transcripts & details | 10MB/day | 30 days |
| `spring-ai.log` | AI API interactions | 10MB/day | 7 days |

## 🔍 Viewing Logs

### Using logs.sh Script

```bash
# Combined runtime logs (backend + frontend)
./logs.sh

# Runtime logs only
./logs.sh backend      # Backend startup, errors
./logs.sh frontend     # Vite dev server

# Application logs
./logs.sh app          # General application logs
./logs.sh debates      # Debate-specific logs
./logs.sh ai           # Spring AI interactions
./logs.sh all          # All backend logs together

# Help
./logs.sh help
```

### Manual Viewing

```bash
# Real-time monitoring
tail -f backend/logs/debates.log
tail -f backend/logs/spring-ai.log

# Last 100 lines
tail -n 100 backend/logs/duodebate.log

# Search logs
grep "ERROR" backend/logs/duodebate.log
grep "PROPOSER" backend/logs/debates.log
grep "OpenAI" backend/logs/spring-ai.log
```

## 📊 What's Logged Where

### duodebate.log
- Application startup/shutdown
- HTTP request mappings
- Configuration loading
- General errors and warnings
- Database operations (if added)

**Example:**
```
2025-10-07 12:30:15.123 [main] INFO  c.d.DuoDebateApplication - Starting DuoDebateApplication
2025-10-07 12:30:16.456 [main] INFO  o.s.b.w.e.t.TomcatWebServer - Tomcat started on port 8080
```

### debates.log
- Debate requests with full prompt
- Each iteration's details
- Model responses (PROPOSER/CHALLENGER)
- Draft evolution
- Final status and results
- Error details for failed debates

**Example:**
```
2025-10-07 12:35:20.123 [http-nio-8080-exec-1] INFO  c.d.c.DebateController - Received debate request: DebateRequest(prompt=Outline a blog on AI, maxIterations=10)
2025-10-07 12:35:20.456 [http-nio-8080-exec-1] INFO  c.d.s.DebateOrchestrator - Starting debate for prompt: Outline a blog on AI
2025-10-07 12:35:20.789 [http-nio-8080-exec-1] INFO  c.d.s.DebateOrchestrator - === Iteration 1 ===
2025-10-07 12:35:25.123 [http-nio-8080-exec-1] INFO  c.d.s.DebateOrchestrator - PROPOSER (iteration 1): status=ONGOING, draft_length=1234
2025-10-07 12:35:30.456 [http-nio-8080-exec-1] INFO  c.d.s.DebateOrchestrator - CHALLENGER (iteration 1): provided critique and suggestions
```

### spring-ai.log
- OpenAI API calls and responses
- Gemini/Vertex AI interactions
- Token usage
- Model selection
- API errors and retries
- Rate limiting issues

**Example:**
```
2025-10-07 12:35:21.000 [http-nio-8080-exec-1] DEBUG o.s.a.o.OpenAiChatModel - Calling OpenAI API with model: gpt-4-turbo-preview
2025-10-07 12:35:24.500 [http-nio-8080-exec-1] DEBUG o.s.a.o.OpenAiChatModel - OpenAI response received, tokens: 1234
```

## ⚙️ Configuration

### Log Levels

Edit `backend/src/main/resources/application.properties`:

```properties
# Application logs (DEBUG for more detail)
logging.level.com.duodebate=INFO

# Spring AI (DEBUG to see API calls)
logging.level.org.springframework.ai=DEBUG

# Spring Web (DEBUG for HTTP details)
logging.level.org.springframework.web=INFO

# HTTP request mapping
logging.level.org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping=INFO
```

### Log File Settings

Edit `backend/src/main/resources/logback-spring.xml`:

```xml
<!-- Change file size limit -->
<maxFileSize>20MB</maxFileSize>

<!-- Change retention period -->
<maxHistory>30</maxHistory>

<!-- Change file location -->
<file>logs/custom-name.log</file>
```

## 🐛 Debugging Scenarios

### Debate Not Starting
```bash
# Check application logs for startup errors
./logs.sh app

# Look for API key issues
grep "api-key" backend/logs/duodebate.log
grep "authentication" backend/logs/spring-ai.log
```

### Model Not Responding
```bash
# Check Spring AI logs for API errors
./logs.sh ai

# Look for rate limiting
grep "rate" backend/logs/spring-ai.log

# Check timeout issues
grep "timeout" backend/logs/spring-ai.log
```

### Invalid JSON Responses
```bash
# Check debate logs for parsing errors
./logs.sh debates

# Look for JSON parse errors
grep "JsonParseException" backend/logs/debates.log
grep "Error parsing" backend/logs/debates.log
```

### Frontend Not Connecting
```bash
# Check CORS errors
./logs.sh backend | grep CORS

# Check backend health
curl http://localhost:8080/api/health
```

## 📈 Log Levels Explained

| Level | When to Use | Example |
|-------|-------------|---------|
| TRACE | Very detailed debugging | Variable values at each step |
| DEBUG | Development debugging | Method calls, API requests |
| INFO | General information | App startup, debate started |
| WARN | Potential issues | Deprecated API, slow response |
| ERROR | Actual errors | API failures, parsing errors |

## 🔧 Advanced Configuration

### Add Custom Logger

In `logback-spring.xml`:

```xml
<logger name="com.duodebate.controller" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
</logger>
```

### Add SQL Logging (if using database)

In `application.properties`:

```properties
logging.level.org.springframework.jdbc=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### JSON Log Format

For production, change to JSON in `logback-spring.xml`:

```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
```

## 🧹 Maintenance

### Clean Old Logs

Automatic (configured in logback-spring.xml):
- Logs auto-rotate daily
- Old logs auto-deleted after retention period

Manual:
```bash
# Delete logs older than 7 days
find backend/logs -name "*.log" -mtime +7 -delete

# Delete all logs
rm -f backend/logs/*.log
```

### Monitor Disk Space

```bash
# Check log directory size
du -sh backend/logs

# Check individual log sizes
ls -lh backend/logs
```

## 💡 Tips

1. **Development**: Use `DEBUG` level for `com.duodebate`
2. **Production**: Use `INFO` level, `ERROR` for Spring AI
3. **Troubleshooting**: Enable `DEBUG` for specific packages only
4. **Performance**: Lower log levels in production (INFO/WARN)
5. **Privacy**: Be careful logging sensitive data (API keys, user data)

## 📚 Common Log Patterns

### Successful Debate
```
INFO  c.d.controller.DebateController - Received debate request
INFO  c.d.service.DebateOrchestrator - Starting debate
INFO  c.d.service.DebateOrchestrator - === Iteration 1 ===
DEBUG o.s.ai.openai.OpenAiChatModel - Calling OpenAI API
INFO  c.d.service.DebateOrchestrator - PROPOSER: status=ONGOING
DEBUG o.s.ai.vertexai.VertexAiGeminiChatModel - Calling Gemini API
INFO  c.d.service.DebateOrchestrator - CHALLENGER: provided critique
INFO  c.d.service.DebateOrchestrator - Debate completed: status=READY
```

### API Error
```
ERROR o.s.ai.openai.OpenAiChatModel - OpenAI API error: 401 Unauthorized
ERROR c.d.service.DebateOrchestrator - Error conducting debate
com.openai.ApiException: Invalid API key
```

### JSON Parse Error
```
ERROR c.d.service.DebateOrchestrator - Error parsing PROPOSER response
com.fasterxml.jackson.core.JsonParseException: Unexpected character
```

## 🆘 Troubleshooting

**No logs created?**
- Check `backend/logs` directory exists
- Verify `logback-spring.xml` in classpath
- Check file permissions

**Logs too large?**
- Reduce `maxFileSize` in logback-spring.xml
- Lower log level (INFO instead of DEBUG)
- Reduce `maxHistory`

**Need more detail?**
- Increase log level to DEBUG or TRACE
- Add specific loggers in application.properties
- Enable Spring AI DEBUG logging

**Logs missing information?**
- Check log level configuration
- Verify logger is enabled for package
- Check pattern includes required fields
