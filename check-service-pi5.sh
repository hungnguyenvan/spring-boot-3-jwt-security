#!/bin/bash

# Script để kiểm tra và sửa service Spring Boot trên Pi5
# File: check-service-pi5.sh

echo "🔍 Checking Spring Boot service on Pi5..."

# Stop existing service
echo "🛑 Stopping existing service..."
sudo systemctl stop spring-boot-jwt 2>/dev/null || true

# Get current user and home directory
CURRENT_USER=$(whoami)
CURRENT_HOME=$(eval echo ~$CURRENT_USER)

echo "👤 Current user: $CURRENT_USER"
echo "🏠 Home directory: $CURRENT_HOME"

# Create corrected service file
echo "📝 Creating corrected service file..."
cat > spring-boot-jwt.service << EOF
[Unit]
Description=Spring Boot JWT Security Application
After=docker.service network.target
Wants=docker.service

[Service]
Type=simple
User=$CURRENT_USER
Group=$CURRENT_USER
WorkingDirectory=$CURRENT_HOME/spring-boot-3-jwt-security
Environment=JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64
Environment=JAVA_OPTS="-Xmx1536m -Xms512m -XX:+UseG1GC"
Environment=SPRING_PROFILES_ACTIVE=pi5
ExecStart=/usr/bin/java \$JAVA_OPTS -jar target/security-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=spring-boot-jwt

[Install]
WantedBy=multi-user.target
EOF

# Install service
echo "🔧 Installing service..."
sudo cp spring-boot-jwt.service /etc/systemd/system/
sudo systemctl daemon-reload

# Check if jar file exists
JAR_FILE="$CURRENT_HOME/spring-boot-3-jwt-security/target/security-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "🔨 Building application first..."
    
    cd "$CURRENT_HOME/spring-boot-3-jwt-security"
    ./maven-safe.sh clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
fi

# Check file permissions
echo "🔒 Checking file permissions..."
ls -la "$JAR_FILE"
chmod +r "$JAR_FILE"

# Test Java execution
echo "☕ Testing Java execution..."
java -version
/usr/bin/java -version

# Start service
echo "🚀 Starting service..."
sudo systemctl enable spring-boot-jwt
sudo systemctl start spring-boot-jwt

# Wait a bit and check status
sleep 5

echo "📊 Service status:"
sudo systemctl status spring-boot-jwt --no-pager

echo "📋 Recent logs:"
sudo journalctl -u spring-boot-jwt --no-pager -n 20

echo "🔍 Process check:"
ps aux | grep java || echo "No Java processes found"

echo "🌐 Port check:"
netstat -tlnp | grep :8080 || echo "Port 8080 not listening"
