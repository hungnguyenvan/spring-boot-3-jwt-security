#!/bin/bash

# Quick test với quoted JWT secret
echo "🧪 Testing with quoted JWT secret..."

echo "📦 JAR timestamp:"
ls -lh target/security-0.0.1-SNAPSHOT.jar

echo "🚀 Quick test run (10 seconds)..."
timeout 10s java -jar target/security-0.0.1-SNAPSHOT.jar --spring.profiles.active=pi5 || echo "App failed to start"

echo "✅ Test completed"
