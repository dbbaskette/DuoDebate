# Cloud Foundry Deployment Guide

This guide covers deploying DuoDebate to Cloud Foundry as a **single application** using Tanzu buildpacks. The frontend is bundled inside the backend JAR as static resources.

This deployment follows **Cloud Foundry best practices** by using declarative configuration with `manifest.yml` and separating secrets from source control.

## Prerequisites

- Cloud Foundry CLI installed (`cf` command)
- Logged into your Cloud Foundry foundation (`cf login`)
- Java 21+ for building the backend
- Node.js 18+ for building the frontend

## Deployment (Best Practice)

### 1. One-Time Setup: Create Your Secrets File

```bash
# Copy the example secrets file
cp secrets.yml.example secrets.yml

# Edit secrets.yml with your actual values
# NEVER commit secrets.yml to git (it's in .gitignore)
```

Your `secrets.yml` should look like:

```yaml
---
OPENAI_API_KEY: "sk-proj-your-actual-key-here"
OPENAI_CHAT_MODEL: "gpt-4-turbo-preview"
GEMINI_API_KEY: "your-actual-gemini-key-here"
SITE_PASSWORD: "your-secure-password"
```

### 2. Build and Deploy

```bash
# Build the application
./build-all.sh

# Deploy with a single declarative command
cf push --vars-file secrets.yml
```

**That's it!** This single command:
- ✅ Pushes your application
- ✅ Sets all environment variables from `secrets.yml`
- ✅ Starts the application
- ✅ Is repeatable and CI/CD friendly

## Why This Approach?

### ✅ Security
- Secrets are in `secrets.yml`, which is **never** committed to git (`.gitignore`)
- No risk of accidentally exposing API keys in source control
- Clear separation between code and configuration

### ✅ Declarative
- `manifest.yml` describes your app's requirements
- All configuration is in YAML files that CF understands natively
- No custom scripts to maintain

### ✅ Portability
- Works identically on any Cloud Foundry platform
- Easy to integrate into CI/CD pipelines
- No dependency on local shell scripts

### ✅ Best Practice
- This is the **recommended** Cloud Foundry deployment pattern
- Follows 12-factor app principles
- Native to the CF platform

## How It Works

### The Manifest (`manifest.yml`)

```yaml
env:
  OPENAI_API_KEY: ${OPENAI_API_KEY}
  GEMINI_API_KEY: ${GEMINI_API_KEY}
  # ... other variables
```

The `${VARIABLE_NAME}` syntax tells CF to substitute values from your `secrets.yml` file.

### Variable Substitution

When you run `cf push --vars-file secrets.yml`, Cloud Foundry:
1. Reads `manifest.yml` for app configuration
2. Reads `secrets.yml` for variable values
3. Substitutes `${VARIABLE}` placeholders with actual values
4. Sets them as environment variables in your app

### Application Architecture

The application is packaged as a single Spring Boot JAR:
1. Frontend built with Vite → `frontend/dist/`
2. Maven copies `dist/` → `backend/target/classes/static/`
3. Spring Boot serves static files from `/`
4. API endpoints served from `/api/*`
5. No CORS issues (same origin)

## Updating the Application

```bash
# Rebuild
./build-all.sh

# Redeploy (uses same secrets.yml)
cf push --vars-file secrets.yml
```

## CI/CD Integration

For automated deployments in CI/CD pipelines:

```yaml
# Example GitHub Actions / GitLab CI
steps:
  - name: Build
    run: ./build-all.sh

  - name: Deploy
    run: |
      echo "$SECRETS_YML" > secrets.yml
      cf push --vars-file secrets.yml
    env:
      SECRETS_YML: ${{ secrets.CF_SECRETS_YML }}
```

Store your `secrets.yml` content as a CI/CD secret variable.

## Alternative: Environment Variables

If you can't use `secrets.yml` (e.g., in some CI/CD systems), set variables directly:

```bash
# Build
./build-all.sh

# Push without starting
cf push --no-start

# Set environment variables
cf set-env duodebate OPENAI_API_KEY "your-key"
cf set-env duodebate GEMINI_API_KEY "your-key"
cf set-env duodebate SITE_PASSWORD "your-password"

# Start the app
cf start duodebate
```

**Note**: The `--vars-file` approach is preferred as it's atomic and declarative.

## Access Your Application

```bash
cf app duodebate
# Visit the URL shown (e.g., https://duodebate.apps.yourplatform.com)
```

## Scaling

```bash
cf scale duodebate -i 2 -m 2G
```

## Logs

```bash
# View recent logs
cf logs duodebate --recent

# Follow logs in real-time
cf logs duodebate
```

## Environment Variables Reference

| Variable | Required | Description | Default |
|----------|----------|-------------|---------|
| `OPENAI_API_KEY` | Yes | Your OpenAI API key | - |
| `OPENAI_CHAT_MODEL` | No | OpenAI model to use | gpt-4-turbo-preview |
| `GEMINI_API_KEY` | Yes | Your Google Gemini API key | - |
| `SITE_PASSWORD` | No | Password to access the site (leave empty to disable) | - |

## Troubleshooting

### Missing variables error during push
- Ensure `secrets.yml` exists and contains all required variables
- Check for typos in variable names (must match `manifest.yml`)

### Application won't start
- Check logs: `cf logs duodebate --recent`
- Verify environment variables: `cf env duodebate`
- Ensure API keys are valid

### Frontend not loading
- Verify frontend built successfully: `ls -la backend/target/classes/static/`
- Spring Boot automatically serves from `static/` directory

## Security Best Practices

1. ✅ **Never commit** `secrets.yml` to version control
2. ✅ **Always verify** `.gitignore` includes `secrets.yml`
3. ✅ **Rotate secrets** regularly
4. ✅ **Use different secrets** for dev/staging/production
5. ✅ **Store CI/CD secrets** in your platform's secure secret management

## Files Overview

- `manifest.yml` - App configuration (checked into git)
- `secrets.yml` - Your actual secrets (NEVER committed)
- `secrets.yml.example` - Template (checked into git)
- `.gitignore` - Ensures secrets.yml is never committed
- `build-all.sh` - Builds frontend + backend
