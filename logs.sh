#!/bin/bash

# DuoDebate Logs Viewer

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

show_help() {
    echo "DuoDebate Logs Viewer"
    echo ""
    echo "Usage: ./logs.sh [option]"
    echo ""
    echo "Options:"
    echo "  backend       Show backend runtime logs (from run.sh)"
    echo "  frontend      Show frontend runtime logs (from run.sh)"
    echo "  app           Show application logs (backend/logs/duodebate.log)"
    echo "  debates       Show debate-specific logs (backend/logs/debates.log)"
    echo "  ai            Show Spring AI logs (backend/logs/spring-ai.log)"
    echo "  all           Show all backend log files"
    echo "  help          Show this help message"
    echo ""
    echo "No option:      Show combined runtime logs (backend + frontend)"
    echo ""
    echo "Examples:"
    echo "  ./logs.sh              # Runtime logs from both services"
    echo "  ./logs.sh debates      # Debate transcripts only"
    echo "  ./logs.sh ai           # AI model interactions only"
}

case "$1" in
    backend)
        if [ -f backend.log ]; then
            echo "Showing backend runtime logs (Ctrl+C to exit)..."
            tail -f backend.log
        else
            echo "Error: backend.log not found. Is the backend running?"
            exit 1
        fi
        ;;
    frontend)
        if [ -f frontend.log ]; then
            echo "Showing frontend runtime logs (Ctrl+C to exit)..."
            tail -f frontend.log
        else
            echo "Error: frontend.log not found. Is the frontend running?"
            exit 1
        fi
        ;;
    app)
        if [ -f backend/logs/duodebate.log ]; then
            echo "Showing application logs (Ctrl+C to exit)..."
            tail -f backend/logs/duodebate.log
        else
            echo "Error: backend/logs/duodebate.log not found. Has the backend been started?"
            exit 1
        fi
        ;;
    debates)
        if [ -f backend/logs/debates.log ]; then
            echo "Showing debate logs (Ctrl+C to exit)..."
            tail -f backend/logs/debates.log
        else
            echo "Error: backend/logs/debates.log not found. Has any debate been run?"
            exit 1
        fi
        ;;
    ai)
        if [ -f backend/logs/spring-ai.log ]; then
            echo "Showing Spring AI logs (Ctrl+C to exit)..."
            tail -f backend/logs/spring-ai.log
        else
            echo "Error: backend/logs/spring-ai.log not found. Has the backend been started?"
            exit 1
        fi
        ;;
    all)
        if [ -d backend/logs ] && [ "$(ls -A backend/logs/*.log 2>/dev/null)" ]; then
            echo "Showing all backend logs (Ctrl+C to exit)..."
            tail -f backend/logs/*.log
        else
            echo "Error: No log files found in backend/logs/"
            exit 1
        fi
        ;;
    help|--help|-h)
        show_help
        ;;
    "")
        # No argument - show combined runtime logs
        if [ -f backend.log ] && [ -f frontend.log ]; then
            echo "Showing combined runtime logs (Ctrl+C to exit)..."
            tail -f backend.log frontend.log
        elif [ -f backend.log ]; then
            echo "Showing backend runtime logs (frontend not started)..."
            tail -f backend.log
        elif [ -f frontend.log ]; then
            echo "Showing frontend runtime logs (backend not started)..."
            tail -f frontend.log
        else
            echo "Error: No runtime logs found. Have you started the application with ./run.sh?"
            exit 1
        fi
        ;;
    *)
        echo "Unknown option: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
