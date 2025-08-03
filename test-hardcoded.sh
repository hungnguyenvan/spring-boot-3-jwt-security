#!/bin/bash

# Test with hardcoded JWT values
echo "🧪 Testing with hardcoded JWT values..."

echo "📦 JAR info:"
ls -lh target/security-0.0.1-SNAPSHOT.jar

echo "🚀 Starting test (15 seconds)..."
timeout 15s java -jar target/security-0.0.1-SNAPSHOT.jar --spring.profiles.active=pi5 &
PID=$!

echo "🆔 PID: $PID"
sleep 12

if kill -0 $PID 2>/dev/null; then
    echo "✅ SUCCESS! App is running"
    
    # Check health endpoint
    if curl -f http://localhost:8080/actuator/health 2>/dev/null; then
        echo "✅ Health endpoint OK"
    else
        echo "⚠️ Health endpoint not ready"
    fi
    
    kill $PID
    echo "🛑 Stopped"
else
    echo "❌ App failed"
fi
