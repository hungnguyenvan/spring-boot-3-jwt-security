#!/bin/bash

# Debug profile loading issue
echo "🔍 Debugging profile loading..."

echo "📋 Testing different profile activation methods:"

echo "🧪 Method 1: --spring.profiles.active=pi5"
timeout 10s java -jar target/security-0.0.1-SNAPSHOT.jar --spring.profiles.active=pi5 || echo "Failed method 1"

echo ""
echo "🧪 Method 2: -Dspring.profiles.active=pi5"
timeout 10s java -Dspring.profiles.active=pi5 -jar target/security-0.0.1-SNAPSHOT.jar || echo "Failed method 2"

echo ""
echo "🧪 Method 3: SPRING_PROFILES_ACTIVE environment variable"
SPRING_PROFILES_ACTIVE=pi5 timeout 10s java -jar target/security-0.0.1-SNAPSHOT.jar || echo "Failed method 3"

echo ""
echo "📋 All tests completed"
