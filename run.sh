#!/bin/bash

# DuoDebate Runner Script
# Usage: ./run.sh [--build]

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   DuoDebate Launcher${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if .env exists
if [ ! -f .env ]; then
    echo -e "${RED}ERROR: .env file not found!${NC}"
    echo -e "${YELLOW}Please copy .env.example to .env and add your API keys:${NC}"
    echo -e "  cp .env.example .env"
    echo -e "  # Then edit .env with your keys"
    exit 1
fi

# Source environment variables
echo -e "${GREEN}✓${NC} Loading environment variables from .env..."
set -a  # automatically export all variables
source .env
set +a
echo ""

# Check required environment variables
REQUIRED_VARS=("OPENAI_API_KEY" "GEMINI_API_KEY")
MISSING_VARS=()

for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ] || [[ "${!var}" == *"your-"* ]] || [[ "${!var}" == *"-here"* ]]; then
        MISSING_VARS+=("$var")
    fi
done

if [ ${#MISSING_VARS[@]} -ne 0 ]; then
    echo -e "${RED}ERROR: Missing or placeholder values for required environment variables:${NC}"
    for var in "${MISSING_VARS[@]}"; do
        echo -e "  - ${YELLOW}$var${NC}"
    done
    echo ""
    echo -e "${YELLOW}Please edit .env and add your actual values.${NC}"
    exit 1
fi

# Check GCP Authentication
if [ -z "$GOOGLE_APPLICATION_CREDENTIALS" ] && [ ! -f "$HOME/.config/gcloud/application_default_credentials.json" ]; then
    echo -e "${YELLOW}WARNING: No GCP credentials found!${NC}"
    echo -e "${YELLOW}Gemini/Vertex AI requires authentication. Please use ONE of:${NC}"
    echo -e "  1. Set GOOGLE_APPLICATION_CREDENTIALS in .env to your service account JSON path"
    echo -e "  2. Run: ${GREEN}gcloud auth application-default login${NC}"
    echo ""
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo -e "${GREEN}✓${NC} All required environment variables are set"
echo ""

# Check for --build flag
BUILD_FLAG=false
if [ "$1" == "--build" ]; then
    BUILD_FLAG=true
    echo -e "${YELLOW}Build flag detected. Will rebuild both backend and frontend.${NC}"
    echo ""
fi

# Backend section
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Backend (Spring Boot)${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

cd backend

if [ "$BUILD_FLAG" = true ]; then
    echo -e "${YELLOW}Building backend...${NC}"
    ./mvnw clean package -DskipTests
    echo -e "${GREEN}✓${NC} Backend built successfully"
    echo ""
fi

echo -e "${YELLOW}Starting backend server...${NC}"
echo -e "  Port: ${SERVER_PORT:-8080}"
echo -e "  OpenAI Model: ${OPENAI_CHAT_MODEL:-gpt-4-turbo-preview}"
echo ""

# Start backend in background
./mvnw spring-boot:run > ../backend.log 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}✓${NC} Backend started with PID: $BACKEND_PID"
echo -e "  Logs: backend.log"
echo ""

# Wait for backend to be ready
echo -e "${YELLOW}Waiting for backend to be ready...${NC}"
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -s http://localhost:${SERVER_PORT:-8080}/api/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} Backend is ready!"
        break
    fi
    ATTEMPT=$((ATTEMPT + 1))
    echo -n "."
    sleep 2
done
echo ""

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    echo -e "${RED}ERROR: Backend failed to start after 60 seconds${NC}"
    echo -e "${YELLOW}Check backend.log for details${NC}"
    kill $BACKEND_PID 2>/dev/null || true
    exit 1
fi

# Frontend section
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Frontend (React + Vite)${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

cd ../frontend

if [ "$BUILD_FLAG" = true ]; then
    echo -e "${YELLOW}Installing frontend dependencies...${NC}"
    npm install
    echo -e "${GREEN}✓${NC} Frontend dependencies installed"
    echo ""
fi

echo -e "${YELLOW}Starting frontend dev server...${NC}"
echo -e "  URL: http://localhost:5173"
echo ""

# Start frontend in background
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo -e "${GREEN}✓${NC} Frontend started with PID: $FRONTEND_PID"
echo -e "  Logs: frontend.log"
echo ""

# Create cleanup script
cd ..
cat > stop.sh << 'EOF'
#!/bin/bash
echo "Stopping DuoDebate..."
pkill -f "spring-boot:run" || true
pkill -f "vite" || true
echo "✓ All processes stopped"
EOF
chmod +x stop.sh

# Summary
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}   DuoDebate is Running!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Frontend:     ${GREEN}http://localhost:5173${NC}"
echo -e "Backend API:  ${GREEN}http://localhost:${SERVER_PORT:-8080}${NC}"
echo ""
echo -e "Process IDs:"
echo -e "  Backend:  $BACKEND_PID"
echo -e "  Frontend: $FRONTEND_PID"
echo ""
echo -e "Logs:"
echo -e "  Backend:  ${YELLOW}tail -f backend.log${NC}"
echo -e "  Frontend: ${YELLOW}tail -f frontend.log${NC}"
echo ""
echo -e "To stop all services:"
echo -e "  ${YELLOW}./stop.sh${NC}"
echo ""
echo -e "${GREEN}Happy Debating! 🎉${NC}"
echo ""

# Keep script running and show logs
trap "./stop.sh; exit" INT TERM

echo -e "${YELLOW}Showing combined logs (Ctrl+C to stop):${NC}"
echo -e "${BLUE}========================================${NC}"
tail -f backend.log frontend.log
