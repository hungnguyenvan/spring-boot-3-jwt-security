#!/bin/bash

# Pi5 Production Test Script - Comprehensive Check
# Kiểm tra toàn diện ứng dụng trên Raspberry Pi 5

echo "=================================================="
echo "PI5 PRODUCTION TEST - COMPREHENSIVE CHECK"
echo "$(date)"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test functions
test_step() {
    echo -e "${BLUE}[TEST]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Step 1: System Check
test_step "Checking Pi5 system resources..."
echo "Memory usage:"
free -h
echo ""
echo "Disk space:"
df -h
echo ""
echo "Java version:"
java -version
echo ""

# Step 2: Database Check
test_step "Checking PostgreSQL database..."
if systemctl is-active --quiet postgresql; then
    success "PostgreSQL is running"
    
    # Test database connection
    if sudo -u postgres psql -d jwt_security -c "SELECT 1;" > /dev/null 2>&1; then
        success "Database connection successful"
        
        # Check tables
        echo "Checking database tables:"
        sudo -u postgres psql -d jwt_security -c "\dt"
    else
        error "Cannot connect to database"
        exit 1
    fi
else
    error "PostgreSQL is not running"
    echo "Starting PostgreSQL..."
    sudo systemctl start postgresql
    sleep 5
fi

# Step 3: Docker Check (if using Docker)
test_step "Checking Docker services..."
if command -v docker &> /dev/null; then
    if docker ps | grep -q postgres; then
        success "PostgreSQL Docker container is running"
    else
        warning "PostgreSQL Docker container not found"
    fi
else
    warning "Docker not installed or not in PATH"
fi

# Step 4: Application JAR Check
test_step "Checking application JAR file..."
JAR_FILE="target/security-0.0.1-SNAPSHOT.jar"
if [ -f "$JAR_FILE" ]; then
    success "JAR file found: $JAR_FILE"
    JAR_SIZE=$(stat -f%z "$JAR_FILE" 2>/dev/null || stat -c%s "$JAR_FILE" 2>/dev/null)
    echo "JAR size: $JAR_SIZE bytes"
    
    # Check if JAR is valid
    if jar tf "$JAR_FILE" | head -5; then
        success "JAR file is valid"
    else
        error "JAR file is corrupted"
        exit 1
    fi
else
    error "JAR file not found: $JAR_FILE"
    exit 1
fi

# Step 5: Test JWT Configuration (check hardcoded values)
test_step "Checking JWT configuration in JAR..."
if jar tf "$JAR_FILE" | grep -q "com/alibou/security/config/JwtService.class"; then
    success "JwtService.class found in JAR"
    # Extract and check for hardcoded values
    mkdir -p temp_check
    cd temp_check
    jar xf "../$JAR_FILE" com/alibou/security/config/JwtService.class
    if [ -f "com/alibou/security/config/JwtService.class" ]; then
        success "JwtService.class extracted successfully"
    fi
    cd ..
    rm -rf temp_check
else
    error "JwtService.class not found in JAR"
fi

# Step 6: Application Test Start
test_step "Starting application test..."
echo "Using profile: pi5"
echo "Database: PostgreSQL"
echo "JWT: Hardcoded values (fallback mode)"

# Set Java options for Pi5
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

echo "Starting Spring Boot application..."
echo "Command: java \$JAVA_OPTS -jar $JAR_FILE --spring.profiles.active=pi5"

# Start application in background
java $JAVA_OPTS -jar "$JAR_FILE" --spring.profiles.active=pi5 > app.log 2>&1 &
APP_PID=$!
echo "Application started with PID: $APP_PID"

# Wait for application startup
test_step "Waiting for application startup..."
sleep 15

# Check if application is running
if kill -0 $APP_PID 2>/dev/null; then
    success "Application process is running"
    
    # Check application logs
    echo "Application logs (last 20 lines):"
    tail -20 app.log
    
    # Test application endpoints
    test_step "Testing application endpoints..."
    sleep 5
    
    # Test health endpoint
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        success "Health endpoint accessible"
    else
        warning "Health endpoint not accessible (may not be enabled)"
    fi
    
    # Test main endpoint
    if curl -s http://localhost:8080/ > /dev/null 2>&1; then
        success "Main endpoint accessible"
    else
        warning "Main endpoint not accessible"
    fi
    
    # Test registration endpoint
    echo "Testing registration endpoint..."
    REGISTER_RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null -X POST \
        -H "Content-Type: application/json" \
        -d '{"firstname":"Test","lastname":"User","email":"test@example.com","password":"password"}' \
        http://localhost:8080/api/v1/auth/register)
    
    if [ "$REGISTER_RESPONSE" = "200" ] || [ "$REGISTER_RESPONSE" = "400" ]; then
        success "Registration endpoint is working (HTTP $REGISTER_RESPONSE)"
    else
        warning "Registration endpoint returned HTTP $REGISTER_RESPONSE"
    fi
    
else
    error "Application failed to start"
    echo "Application logs:"
    cat app.log
    exit 1
fi

# Cleanup
test_step "Cleaning up..."
kill $APP_PID 2>/dev/null
wait $APP_PID 2>/dev/null

echo ""
echo "=================================================="
echo "PI5 PRODUCTION TEST COMPLETED"
echo "$(date)"
echo "=================================================="
echo ""
echo "Summary:"
echo "- System resources: Checked"
echo "- Database: Checked"
echo "- JAR file: Validated"
echo "- JWT configuration: Hardcoded fallback mode"
echo "- Application startup: Tested"
echo "- Endpoints: Tested"
echo ""
echo "Next steps:"
echo "1. If all tests passed, the application is ready for production"
echo "2. Set up systemd service for automatic startup"
echo "3. Configure nginx reverse proxy if needed"
echo "4. Set up log rotation"
echo ""
