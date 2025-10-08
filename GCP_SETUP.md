# Google Cloud Platform (GCP) Setup for Gemini

DuoDebate uses Google's Vertex AI Gemini API, which requires GCP authentication. Follow one of the methods below.

## Quick Setup (Recommended for Development)

### Option 1: gcloud CLI Authentication

The easiest way to get started:

```bash
# Install gcloud CLI if you haven't already
# https://cloud.google.com/sdk/docs/install

# Authenticate your gcloud CLI
gcloud auth application-default login

# Set your project
gcloud config set project YOUR_PROJECT_ID
```

This creates credentials at `~/.config/gcloud/application_default_credentials.json` that Spring AI will automatically use.

### Option 2: Service Account JSON

For production or when you need more control:

1. **Create a Service Account:**
   ```bash
   gcloud iam service-accounts create duodebate-sa \
       --display-name="DuoDebate Service Account"
   ```

2. **Grant Vertex AI User role:**
   ```bash
   gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
       --member="serviceAccount:duodebate-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
       --role="roles/aiplatform.user"
   ```

3. **Create and download key:**
   ```bash
   gcloud iam service-accounts keys create ~/duodebate-key.json \
       --iam-account=duodebate-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com
   ```

4. **Add to .env:**
   ```env
   GOOGLE_APPLICATION_CREDENTIALS=/Users/yourname/duodebate-key.json
   GEMINI_PROJECT_ID=YOUR_PROJECT_ID
   ```

## Required GCP APIs

Enable the Vertex AI API for your project:

```bash
# Enable Vertex AI API
gcloud services enable aiplatform.googleapis.com

# Verify it's enabled
gcloud services list --enabled | grep aiplatform
```

## Configuration in DuoDebate

### .env File

```env
# Your GCP Project ID
GEMINI_PROJECT_ID=your-project-id

# Region (us-central1 recommended for Gemini)
GEMINI_LOCATION=us-central1

# Authentication (ONE of these):
# Option 1: Service Account JSON path
GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json

# Option 2: Leave blank if using gcloud auth
```

### Verify Setup

Test your configuration:

```bash
# Check if credentials are available
gcloud auth application-default print-access-token

# If this works, you're all set!
```

## Troubleshooting

### "Default credentials not found"

**Problem:** Application can't find GCP credentials

**Solutions:**
1. Run `gcloud auth application-default login`
2. Or set `GOOGLE_APPLICATION_CREDENTIALS` in `.env`
3. Verify credentials file exists and path is correct

### "Permission denied" or "403 Forbidden"

**Problem:** Service account lacks permissions

**Solution:**
```bash
# Grant Vertex AI User role
gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
    --member="serviceAccount:YOUR_SA@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/aiplatform.user"
```

### "API not enabled"

**Problem:** Vertex AI API not enabled for your project

**Solution:**
```bash
gcloud services enable aiplatform.googleapis.com
```

### "Project not found"

**Problem:** Invalid project ID in `.env`

**Solution:**
```bash
# List your projects
gcloud projects list

# Copy the PROJECT_ID and update .env
```

## Cost Information

Vertex AI Gemini pricing (as of 2025):
- Gemini 2.0 Flash: $0.075 per million input tokens, $0.30 per million output tokens
- Free tier: Check https://cloud.google.com/vertex-ai/pricing

Typical debate (10 rounds):
- Estimated tokens: ~50,000 tokens total
- Cost: ~$0.01-0.05 per debate

## Security Best Practices

1. **Never commit credentials:**
   - `.gitignore` already excludes `.env` and `*.json`
   - Keep service account keys secure

2. **Use least privilege:**
   - Only grant `roles/aiplatform.user` (not `roles/owner`)

3. **Rotate keys regularly:**
   ```bash
   # Delete old key
   gcloud iam service-accounts keys delete KEY_ID \
       --iam-account=SERVICE_ACCOUNT_EMAIL

   # Create new key
   gcloud iam service-accounts keys create new-key.json \
       --iam-account=SERVICE_ACCOUNT_EMAIL
   ```

4. **For production:**
   - Use Workload Identity if running on GKE
   - Use service account impersonation
   - Enable audit logging

## Alternative: OpenAI Only

If you want to skip GCP setup entirely, you can modify `DebateOrchestrator.java` to use OpenAI for both PROPOSER and CHALLENGER roles. See the code comments for details.

## Resources

- [Vertex AI Documentation](https://cloud.google.com/vertex-ai/docs)
- [Gemini API Pricing](https://cloud.google.com/vertex-ai/pricing)
- [Service Account Best Practices](https://cloud.google.com/iam/docs/best-practices-service-accounts)
- [Spring AI Vertex AI Docs](https://docs.spring.io/spring-ai/reference/1.1-SNAPSHOT/api/chat/vertexai-gemini-chat.html)
