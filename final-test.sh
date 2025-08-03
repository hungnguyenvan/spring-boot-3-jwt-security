#!/bin/bash

# Final test JWT fix
echo "🚀 Final test with corrected JWT configuration..."

# Test without profile first (should work with base application.yml)
echo "🧪 Test 1: Base configuration (no profile)"
timeout 15s java -jar target/security-0.0.1-SNAPSHOT.jar &
PID1=$!
sleep 10
if kill -0 $PID1 2>/dev/null; then
    echo "✅ Base config works"
    kill $PID1 2>/dev/null
else
    echo "❌ Base config failed"
fi

# Test with pi5 profile
echo "🧪 Test 2: Pi5 profile"
timeout 15s java -jar target/security-0.0.1-SNAPSHOT.jar --spring.profiles.active=pi5 &
PID2=$!
sleep 10
if kill -0 $PID2 2>/dev/null; then
    echo "✅ Pi5 profile works"
    kill $PID2 2>/dev/null
else
    echo "❌ Pi5 profile failed"
fi

echo "📋 Tests completed"
