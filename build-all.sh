#!/bin/bash
set -e

echo "======================================="
echo "  Building DuoDebate"
echo "======================================="

# Build frontend first
echo ""
echo "📦 Building frontend..."
cd frontend

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "   Installing frontend dependencies..."
    npm install
else
    echo "   Dependencies already installed"
fi

echo "   Building production bundle..."
npm run build
cd ..

# Verify frontend build
if [ ! -d "frontend/dist" ]; then
    echo "❌ Frontend build failed - dist directory not found"
    exit 1
fi

echo "   ✓ Frontend build complete"

# Build backend (which will copy frontend dist into static resources)
echo ""
echo "📦 Building backend..."
cd backend
echo "   Compiling and packaging with Maven..."
./mvnw clean package -DskipTests
cd ..

# Verify backend build
if [ ! -f "backend/target/duodebate-backend-1.0.0-SNAPSHOT.jar" ]; then
    echo "❌ Backend build failed - JAR not found"
    exit 1
fi

# Verify static resources were copied
if [ ! -d "backend/target/classes/static" ]; then
    echo "⚠️  Warning: Static resources not found in JAR"
fi

echo "   ✓ Backend build complete"

echo ""
echo "✅ Build successful!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 Artifact: backend/target/duodebate-backend-1.0.0-SNAPSHOT.jar"
echo "📁 Static files: backend/target/classes/static/"
echo ""
echo "🚀 Deploy to Cloud Foundry:"
echo "   1. Ensure secrets.yml exists:"
echo "      cp secrets.yml.example secrets.yml"
echo "      # Edit secrets.yml with your API keys"
echo ""
echo "   2. Deploy:"
echo "      cf push --vars-file secrets.yml"
echo ""
echo "📝 For local testing:"
echo "   ./run.sh"
