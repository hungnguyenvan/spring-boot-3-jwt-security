#!/bin/bash

# Script chạy Spring Boot trực tiếp trên Pi5 (không qua systemd)
# File: run-direct-pi5.sh

echo "🚀 Running Spring Boot JWT directly on Pi5..."

# Set environment
export JAVA_OPTS="-Xmx1536m -Xms512m -XX:+UseG1GC -Djava.awt.headless=true"
export SPRING_PROFILES_ACTIVE=pi5
export _JAVA_OPTIONS="-Djava.awt.headless=true"

# Get current directory
CURRENT_DIR=$(pwd)
echo "📁 Working directory: $CURRENT_DIR"

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Not in Spring Boot project directory!"
    echo "💡 Please run from spring-boot-3-jwt-security directory"
    exit 1
fi

# Stop any existing Spring Boot processes
echo "🛑 Stopping existing Spring Boot processes..."
pkill -f "security-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
pkill -f "spring-boot" 2>/dev/null || true

# Check Docker PostgreSQL
echo "🗄️ Checking PostgreSQL Docker container..."
if ! docker ps | grep -q postgres; then
    echo "🔄 Starting PostgreSQL container..."
    docker-compose -f docker-compose-pi5.yml up -d postgres
    sleep 10
fi

# Build if jar doesn't exist
JAR_FILE="target/security-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "🔨 Building application..."
    ./maven-safe.sh clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
fi

# Check jar file
echo "📦 JAR file info:"
ls -la "$JAR_FILE"

# Test database connection
echo "🗄️ Testing database connection..."
timeout 10 docker exec $(docker ps -q -f name=postgres) psql -U hungcop -d jwt_security -c "SELECT 1;" > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Database connection successful"
else
    echo "❌ Database connection failed"
    echo "📋 PostgreSQL logs:"
    docker logs $(docker ps -q -f name=postgres) --tail 10
fi

# Show system info
echo "📊 System Information:"
echo "Host: $(hostname)"
echo "User: $(whoami)"
echo "Java: $(java -version 2>&1 | head -1)"
echo "Memory: $(free -h | grep Mem)"
echo "Disk: $(df -h . | tail -1)"

# Create logs directory
mkdir -p logs

# Run Spring Boot application
echo "🚀 Starting Spring Boot application..."
echo "📋 Application will be available at: http://$(hostname -I | awk '{print $1}'):8080"
echo "📋 Logs will be saved to: logs/spring-boot-pi5.log"
echo "📋 Press Ctrl+C to stop"

# Run with output redirect
nohup java $JAVA_OPTS -jar "$JAR_FILE" > logs/spring-boot-pi5.log 2>&1 &
APP_PID=$!

echo "🆔 Application PID: $APP_PID"
echo "📝 PID saved to: spring-boot.pid"
echo $APP_PID > spring-boot.pid

# Wait a bit and check if it started
sleep 10

if kill -0 $APP_PID 2>/dev/null; then
    echo "✅ Application started successfully!"
    echo "📊 Process info:"
    ps aux | grep $APP_PID | grep -v grep
    
    echo "🌐 Checking port 8080..."
    timeout 30 bash -c 'until nc -z localhost 8080; do sleep 1; done'
    if [ $? -eq 0 ]; then
        echo "✅ Application is listening on port 8080"
        echo "🔗 Try: curl http://localhost:8080/actuator/health"
    else
        echo "⚠️ Port 8080 not responding yet, check logs"
    fi
    
    echo "📋 Recent logs:"
    tail -20 logs/spring-boot-pi5.log
    
    echo ""
    echo "📋 Useful commands:"
    echo "  tail -f logs/spring-boot-pi5.log  # Follow logs"
    echo "  kill $APP_PID                    # Stop application"
    echo "  ps aux | grep java               # Check Java processes"
    echo "  netstat -tlnp | grep :8080       # Check port"
    
else
    echo "❌ Application failed to start!"
    echo "📋 Error logs:"
    tail -50 logs/spring-boot-pi5.log
    exit 1
fi
