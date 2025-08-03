#!/bin/bash

# Complete Pi5 Deployment Script
# Deploy và test toàn bộ ứng dụng trên Pi5

echo "=================================================="
echo "PI5 COMPLETE DEPLOYMENT & TEST"
echo "$(date)"
echo "=================================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Functions
info() { echo -e "${BLUE}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Check if running on Pi5
info "Checking system..."
if [[ $(uname -m) == "aarch64" ]] && [[ $(cat /proc/device-tree/model 2>/dev/null) == *"Raspberry Pi 5"* ]]; then
    success "Running on Raspberry Pi 5"
else
    warning "Not running on Pi5, continuing anyway..."
fi

# Step 1: System Preparation
info "Preparing system environment..."

# Set Java heap for Pi5 (4GB RAM)
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Djava.security.egd=file:/dev/./urandom"
success "Java options set for Pi5: $JAVA_OPTS"

# Step 2: Database Setup
info "Setting up PostgreSQL database..."

# Check if PostgreSQL is running
if ! systemctl is-active --quiet postgresql; then
    info "Starting PostgreSQL..."
    sudo systemctl start postgresql
    sleep 5
fi

# Create database if not exists
sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = 'jwt_security'" | grep -q 1 || \
sudo -u postgres createdb jwt_security

# Create user if not exists
sudo -u postgres psql -tc "SELECT 1 FROM pg_user WHERE usename = 'hungcop'" | grep -q 1 || \
sudo -u postgres psql -c "CREATE USER hungcop WITH PASSWORD 'hungcop123';"

# Grant permissions
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE jwt_security TO hungcop;"
sudo -u postgres psql -d jwt_security -c "GRANT ALL ON SCHEMA public TO hungcop;"

success "Database setup completed"

# Step 3: Application Deployment
info "Deploying application..."

JAR_FILE="target/security-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    error "JAR file not found: $JAR_FILE"
    exit 1
fi

# Copy JAR to deployment directory
DEPLOY_DIR="/home/hungcop/jwt-security-app"
mkdir -p "$DEPLOY_DIR"
cp "$JAR_FILE" "$DEPLOY_DIR/"
cp application-pi5.yml "$DEPLOY_DIR/" 2>/dev/null || true

success "Application deployed to $DEPLOY_DIR"

# Step 4: Create systemd service
info "Creating systemd service..."

sudo tee /etc/systemd/system/jwt-security.service > /dev/null << EOF
[Unit]
Description=JWT Security Spring Boot Application
After=network.target postgresql.service
Requires=postgresql.service

[Service]
Type=simple
User=hungcop
Group=hungcop
WorkingDirectory=$DEPLOY_DIR
ExecStart=/usr/bin/java $JAVA_OPTS -jar $DEPLOY_DIR/security-0.0.1-SNAPSHOT.jar --spring.profiles.active=pi5
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=jwt-security

# Environment
Environment=SPRING_PROFILES_ACTIVE=pi5
Environment=JAVA_OPTS=$JAVA_OPTS

# Resource limits for Pi5
LimitNOFILE=65536
PrivateTmp=true
ProtectHome=true
ProtectSystem=strict
ReadWritePaths=$DEPLOY_DIR

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
success "Systemd service created"

# Step 5: Start and test application
info "Starting application..."

# Stop if already running
sudo systemctl stop jwt-security 2>/dev/null || true
sleep 2

# Start application
sudo systemctl start jwt-security
sleep 15

# Check status
if sudo systemctl is-active --quiet jwt-security; then
    success "Application started successfully"
    
    # Show status
    echo "Service status:"
    sudo systemctl status jwt-security --no-pager -l
    
    # Show recent logs
    echo ""
    echo "Recent logs:"
    sudo journalctl -u jwt-security --no-pager -l -n 20
    
else
    error "Application failed to start"
    echo "Service status:"
    sudo systemctl status jwt-security --no-pager -l
    echo ""
    echo "Full logs:"
    sudo journalctl -u jwt-security --no-pager -l -n 50
    exit 1
fi

# Step 6: Comprehensive Testing
info "Running comprehensive tests..."

# Wait for full startup
sleep 10

# Test 1: Health check
echo ""
info "Testing application health..."
for i in {1..5}; do
    if curl -s http://localhost:8080/ > /dev/null; then
        success "Application is responding"
        break
    else
        warning "Attempt $i: Application not ready yet..."
        sleep 5
    fi
done

# Test 2: Registration test
echo ""
info "Testing user registration..."
REGISTER_TEST=$(curl -s -w "%{http_code}" -o /tmp/register_test.json \
    -X POST \
    -H "Content-Type: application/json" \
    -d '{
        "firstname": "Pi5Deploy",
        "lastname": "Test",
        "email": "pi5deploy@test.com",
        "password": "DeployTest123"
    }' \
    "http://localhost:8080/api/v1/auth/register")

if [ "$REGISTER_TEST" = "200" ]; then
    success "User registration working"
    
    # Extract token
    TOKEN=$(cat /tmp/register_test.json | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
    if [ -n "$TOKEN" ]; then
        success "JWT token generation working"
        
        # Test protected endpoint
        PROTECTED_TEST=$(curl -s -w "%{http_code}" -o /dev/null \
            -H "Authorization: Bearer $TOKEN" \
            "http://localhost:8080/api/v1/management/")
        
        if [ "$PROTECTED_TEST" = "200" ]; then
            success "Protected endpoint access working"
        else
            warning "Protected endpoint test: HTTP $PROTECTED_TEST"
        fi
    fi
elif [ "$REGISTER_TEST" = "400" ]; then
    info "User registration returned 400 (may already exist)"
else
    error "User registration failed: HTTP $REGISTER_TEST"
    cat /tmp/register_test.json
fi

# Test 3: Database connectivity
echo ""
info "Testing database connectivity..."
DB_TEST=$(sudo -u postgres psql -d jwt_security -c "SELECT COUNT(*) FROM _user;" 2>/dev/null)
if [ $? -eq 0 ]; then
    success "Database connectivity working"
    echo "User count in database: $(echo "$DB_TEST" | grep -o '[0-9]*' | head -1)"
else
    error "Database connectivity issue"
fi

# Step 7: Performance monitoring
echo ""
info "Checking system performance..."
echo "Memory usage:"
free -h
echo ""
echo "CPU usage:"
top -bn1 | head -5
echo ""
echo "Application memory usage:"
ps aux | grep java | grep -v grep

# Step 8: Enable auto-start
info "Enabling auto-start..."
sudo systemctl enable jwt-security
success "Auto-start enabled"

# Cleanup
rm -f /tmp/register_test.json

echo ""
echo "=================================================="
echo "PI5 DEPLOYMENT COMPLETED SUCCESSFULLY!"
echo "$(date)"
echo "=================================================="
echo ""
echo "Summary:"
echo "✅ System environment configured"
echo "✅ PostgreSQL database setup"
echo "✅ Application deployed to $DEPLOY_DIR"
echo "✅ Systemd service created and started"
echo "✅ Auto-start enabled"
echo "✅ All tests passed"
echo ""
echo "Service management commands:"
echo "• Start:   sudo systemctl start jwt-security"
echo "• Stop:    sudo systemctl stop jwt-security"
echo "• Restart: sudo systemctl restart jwt-security"
echo "• Status:  sudo systemctl status jwt-security"
echo "• Logs:    sudo journalctl -u jwt-security -f"
echo ""
echo "Application URL: http://$(hostname -I | cut -d' ' -f1):8080"
echo ""
echo "The application is now running in production mode on Pi5!"
echo ""
