#!/bin/bash

# Script để xử lý Maven broken pipe issues trên Pi5
# File: fix-maven-pi5.sh

echo "🔧 Fixing Maven broken pipe issues on Pi5..."

# 1. Tạo Maven wrapper với error handling
cat > maven-safe.sh << 'EOF'
#!/bin/bash
# Safe Maven wrapper for Pi5

# Set environment variables
export MAVEN_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -Djava.awt.headless=true"
export _JAVA_OPTIONS="-Djava.awt.headless=true"

# Function to run Maven safely
safe_maven() {
    local cmd="$@"
    echo "Running: mvn $cmd"
    
    # Disable colored output and use batch mode
    mvn --batch-mode --no-transfer-progress --quiet "$@" 2>&1 || {
        echo "Maven command failed, retrying..."
        sleep 5
        mvn --batch-mode --no-transfer-progress "$@"
    }
}

# Run the command
safe_maven "$@"
EOF

chmod +x maven-safe.sh

# 2. Tạo systemd service file cho Spring Boot
cat > spring-boot-jwt.service << 'EOF'
[Unit]
Description=Spring Boot JWT Security Application
After=docker.service
Requires=docker.service

[Service]
Type=simple
User=pi
WorkingDirectory=/home/pi/spring-boot-3-jwt-security
Environment=JAVA_OPTS="-Xmx1536m -Xms512m -XX:+UseG1GC"
Environment=SPRING_PROFILES_ACTIVE=pi5
ExecStart=/usr/bin/java -jar target/security-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# 3. Tạo script khởi động tối ưu
cat > start-app-pi5.sh << 'EOF'
#!/bin/bash

echo "🚀 Starting Spring Boot JWT on Pi5..."

# Stop any existing service
sudo systemctl stop spring-boot-jwt 2>/dev/null || true

# Set environment
export JAVA_OPTS="-Xmx1536m -Xms512m -XX:+UseG1GC -Djava.awt.headless=true"
export SPRING_PROFILES_ACTIVE=pi5

# Build with safe Maven
echo "🔨 Building application..."
./maven-safe.sh clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build successful"
    
    # Install and start service
    sudo cp spring-boot-jwt.service /etc/systemd/system/
    sudo systemctl daemon-reload
    sudo systemctl enable spring-boot-jwt
    sudo systemctl start spring-boot-jwt
    
    echo "✅ Service started"
    echo "📊 Service status:"
    sudo systemctl status spring-boot-jwt --no-pager
else
    echo "❌ Build failed"
    exit 1
fi
EOF

chmod +x start-app-pi5.sh

echo "✅ Created Pi5 optimization scripts:"
echo "   - maven-safe.sh: Safe Maven wrapper"
echo "   - spring-boot-jwt.service: Systemd service"
echo "   - start-app-pi5.sh: Optimized startup script"
echo ""
echo "📋 Usage on Pi5:"
echo "   ./start-app-pi5.sh    # Build and start as service"
echo "   ./maven-safe.sh clean compile  # Safe Maven commands"
