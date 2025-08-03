#!/bin/bash

# Quick test without JPA auditing
echo "🧪 Testing app without JPA auditing..."

# Build
mvn clean package -DskipTests -q

# Test run for 30 seconds
java -jar target/security-0.0.1-SNAPSHOT.jar --spring.profiles.active=pi5 &
PID=$!

echo "🆔 App PID: $PID"
sleep 15

if kill -0 $PID 2>/dev/null; then
    echo "✅ App started successfully without JPA auditing!"
    
    # Check if port is listening
    if netstat -tlnp 2>/dev/null | grep -q :8080; then
        echo "✅ Port 8080 is listening"
    fi
    
    # Stop
    kill $PID 2>/dev/null
    echo "🛑 App stopped"
else
    echo "❌ App failed to start"
fi
