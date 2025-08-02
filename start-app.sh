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

# Kiểm tra PostgreSQL
echo "🔍 Kiểm tra PostgreSQL..."
if systemctl is-active --quiet postgresql; then
    echo "✅ PostgreSQL đang chạy"
else
    echo "🔄 Khởi động PostgreSQL..."
    sudo systemctl start postgresql
fi

# Build ứng dụng
echo "🔨 Building application..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

# Chạy với production profile
echo "🌟 Starting application with production profile..."
export SPRING_PROFILES_ACTIVE=prod

# Chạy ứng dụng
mvn spring-boot:run -Dspring-boot.run.profiles=prod

echo "🎉 Application started successfully!"
echo "📱 Access at: http://$(hostname -I | awk '{print $1}'):8080"
echo "🔍 H2 Console: http://$(hostname -I | awk '{print $1}'):8080/h2-console"
echo "📊 Health Check: http://$(hostname -I | awk '{print $1}'):8080/actuator/health"
