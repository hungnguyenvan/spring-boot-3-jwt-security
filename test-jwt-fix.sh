#!/bin/bash

# Quick test JWT config fix
echo "🧪 Testing JWT configuration fix..."

echo "📦 JAR info:"
ls -lh target/security-0.0.1-SNAPSHOT.jar

echo "🚀 Starting app for 20 seconds..."
java -jar target/security-0.0.1-SNAPSHOT.jar --spring.profiles.active=pi5 &
PID=$!

echo "🆔 App PID: $PID"
sleep 20

if kill -0 $PID 2>/dev/null; then
    echo "✅ App started successfully!"
    
    # Check endpoint
    if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
        echo "✅ Health endpoint accessible"
    else
        echo "⚠️ Health endpoint not ready yet"
    fi
    
    kill $PID 2>/dev/null
    echo "🛑 App stopped"
else
    echo "❌ App failed during startup"
fi
