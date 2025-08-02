#!/bin/bash

# Script khởi chạy Spring Boot App trên Raspberry Pi 5
# File: start-app.sh

echo "🚀 Starting Spring Boot JWT Security Application..."

# Kiểm tra Java
if ! command -v java &> /dev/null; then
    echo "❌ Java không được tìm thấy. Vui lòng cài đặt Java 17"
    exit 1
fi

# Kiểm tra Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven không được tìm thấy. Vui lòng cài đặt Maven"
    exit 1
fi

# Tạo thư mục logs
mkdir -p logs

# Kiểm tra PostgreSQL Docker container
echo "🔍 Kiểm tra PostgreSQL Docker container..."
if docker ps | grep -q postgres-jwt-optimized; then
    echo "✅ PostgreSQL container đang chạy"
else
    echo "🔄 Khởi động PostgreSQL container..."
    docker start postgres-jwt-optimized || docker-compose -f docker-compose-optimized.yml up -d postgres
    echo "⏳ Đợi PostgreSQL khởi động..."
    sleep 10
fi

# Build ứng dụng
echo "🔨 Building application..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

# Chạy với pi5 profile cho PostgreSQL Docker
echo "🌟 Starting application with pi5 profile (PostgreSQL Docker)..."
export SPRING_PROFILES_ACTIVE=pi5

# Chạy ứng dụng
mvn spring-boot:run -Dspring-boot.run.profiles=pi5

echo "🎉 Application started successfully!"
echo "📱 Access at: http://$(hostname -I | awk '{print $1}'):8080"
echo "�️ Database: PostgreSQL Docker (localhost:5432)"
echo "📊 Health Check: http://$(hostname -I | awk '{print $1}'):8080/actuator/health"
