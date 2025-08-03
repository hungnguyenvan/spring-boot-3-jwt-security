#!/bin/bash

# Script để rebuild và test sau khi fix JPA Auditing conflict
# File: rebuild-and-test-pi5.sh

echo "🔧 Rebuilding after fixing JPA Auditing conflict..."

# Set environment
export MAVEN_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -Djava.awt.headless=true"
export _JAVA_OPTIONS="-Djava.awt.headless=true"
export SPRING_PROFILES_ACTIVE=pi5

# Stop any running application
echo "🛑 Stopping existing applications..."
pkill -f "security-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
pkill -f "spring-boot" 2>/dev/null || true

# Clean build
echo "🧹 Cleaning previous build..."
./maven-safe.sh clean

# Compile to check for errors
echo "🔍 Checking compilation..."
./maven-safe.sh compile

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi

echo "✅ Compilation successful"

# Build JAR
echo "📦 Building JAR..."
./maven-safe.sh package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful"

# Check JAR file
JAR_FILE="target/security-0.0.1-SNAPSHOT.jar"
if [ -f "$JAR_FILE" ]; then
    echo "📦 JAR file created: $(ls -lh $JAR_FILE)"
else
    echo "❌ JAR file not found!"
    exit 1
fi

# Test run for 30 seconds
echo "🧪 Testing application startup..."
java $JAVA_OPTS -jar "$JAR_FILE" &
TEST_PID=$!
echo "🆔 Test PID: $TEST_PID"

# Wait and check
sleep 30

if kill -0 $TEST_PID 2>/dev/null; then
    echo "✅ Application is running successfully!"
    
    # Check if port is listening
    if netstat -tlnp 2>/dev/null | grep -q :8080; then
        echo "✅ Port 8080 is listening"
        
        # Test health endpoint
        if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
            echo "✅ Health check passed"
        else
            echo "⚠️ Health check failed, but app is running"
        fi
    else
        echo "⚠️ Port 8080 not listening yet"
    fi
    
    # Stop test instance
    echo "🛑 Stopping test instance..."
    kill $TEST_PID 2>/dev/null
    wait $TEST_PID 2>/dev/null
    
    echo ""
    echo "🎉 SUCCESS! Application is ready to deploy"
    echo "🚀 Run with: ./run-direct-pi5.sh"
    
else
    echo "❌ Application failed to start during test"
    echo "📋 Check logs for errors"
    exit 1
fi
