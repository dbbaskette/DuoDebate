#!/bin/bash
echo "Stopping DuoDebate..."
pkill -f "spring-boot:run" || true
pkill -f "vite" || true
echo "✓ All processes stopped"
