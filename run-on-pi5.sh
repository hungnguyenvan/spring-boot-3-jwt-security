#!/bin/bash

# Script để chạy Spring Boot JWT với PostgreSQL Docker trên Pi 5
# File: run-on-pi5.sh

echo "🚀 Starting Spring Boot JWT Security on Raspberry Pi 5..."

# Kiểm tra các dependency
echo "🔍 Checking dependencies..."

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java không được tìm thấy. Cài đặt Java 17:"
    echo "sudo apt update && sudo apt install openjdk-17-jdk -y"
    exit 1
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven không được tìm thấy. Cài đặt Maven:"
    echo "sudo apt update && sudo apt install maven -y"
    exit 1
fi

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker không được tìm thấy."
    exit 1
fi

echo "✅ All dependencies checked"

# Set Maven options for Pi5 (tối ưu hóa cho ARM64)
export MAVEN_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -Djava.awt.headless=true -Djansi.force=true"
export _JAVA_OPTIONS="-Djava.awt.headless=true"

# Tạo thư mục logs
mkdir -p logs

# Kiểm tra PostgreSQL Docker container
echo "🔍 Checking PostgreSQL Docker container..."
if docker ps | grep -q postgres-jwt-optimized; then
    echo "✅ PostgreSQL container is running"
else
    echo "🔄 Starting PostgreSQL container..."
    
    # Try docker-compose first
    if [ -f "docker-compose-optimized.yml" ]; then
        docker-compose -f docker-compose-optimized.yml up -d postgres
    else
        # Fallback to docker run
        docker run -d \
          --name postgres-jwt-optimized \
          -e POSTGRES_USER=hungcop \
          -e POSTGRES_PASSWORD=hungcop290987 \
          -e POSTGRES_DB=jwt_security \
          -v /opt/docker-data/postgres:/var/lib/postgresql/data \
          -v $(pwd)/database_schema.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro \
          -p 5432:5432 \
          --restart unless-stopped \
          postgres:15-alpine
    fi
    
    echo "⏳ Waiting for PostgreSQL to start..."
    sleep 15
    
    # Verify PostgreSQL is ready
    for i in {1..30}; do
        if docker exec postgres-jwt-optimized pg_isready -U hungcop -d jwt_security; then
            echo "✅ PostgreSQL is ready"
            break
        fi
        echo "⏳ Waiting... ($i/30)"
        sleep 2
    done
fi

# Test database connection
echo "🗄️ Testing database connection..."
if docker exec postgres-jwt-optimized psql -U hungcop -d jwt_security -c "SELECT COUNT(*) FROM _user;" > /dev/null 2>&1; then
    echo "✅ Database connection successful"
else
    echo "❌ Database connection failed"
    echo "Checking logs:"
    docker logs postgres-jwt-optimized --tail 20
    exit 1
fi

# Build application
echo "🔨 Building application..."

# Sử dụng script Maven tối ưu cho Pi5
chmod +x mvn-pi5.sh
./mvn-pi5.sh clean compile

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    echo "📋 Checking build logs..."
    tail -50 logs/maven-pi5.log
    exit 1
fi

echo "✅ Build successful"

# Show system info
echo ""
echo "📊 System Information:"
echo "Host: $(hostname)"
echo "IP: $(hostname -I | awk '{print $1}')"
echo "Java: $(java -version 2>&1 | head -1)"
echo "Maven: $(mvn -version | head -1)"
echo "Docker: $(docker --version)"
echo ""

# Start application with pi5 profile
echo "🌟 Starting Spring Boot with pi5 profile (PostgreSQL Docker)..."
export SPRING_PROFILES_ACTIVE=pi5

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=pi5 &
APP_PID=$!

# Wait a bit and check if app started successfully
sleep 10

if kill -0 $APP_PID 2>/dev/null; then
    echo ""
    echo "🎉 Application started successfully!"
    echo "📱 Access at: http://$(hostname -I | awk '{print $1}'):8080"
    echo "🗄️ Database: PostgreSQL Docker (localhost:5432)"  
    echo "📊 Health Check: http://$(hostname -I | awk '{print $1}'):8080/actuator/health"
    echo "📋 Logs: tail -f logs/application.log"
    echo ""
    echo "🔑 Test Authentication:"
    echo "curl -X POST http://$(hostname -I | awk '{print $1}'):8080/api/v1/auth/register \\"
    echo '  -H "Content-Type: application/json" \'
    echo '  -d '"'"'{"firstname":"Test","lastname":"User","email":"test@pi5.com","username":"testpi5","password":"password"}'"'"
    echo ""
    echo "Press Ctrl+C to stop..."
    
    # Wait for the app process
    wait $APP_PID
else
    echo "❌ Application failed to start"
    echo "Check logs: tail -f logs/application.log"
    exit 1
fi
