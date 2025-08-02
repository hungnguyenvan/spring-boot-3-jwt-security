#!/bin/bash

# Complete setup script for Spring Boot JWT project on Raspberry Pi 5
# This script will:
# 1. Clone code from GitHub
# 2. Setup Docker data directories  
# 3. Configure environment
# 4. Start services

echo "=== Complete Spring Boot JWT Setup for Pi 5 ==="

# Configuration
GITHUB_REPO="https://github.com/hungnguyenvan/spring-boot-3-jwt-security.git"
PROJECT_BASE="/home/hungcop/projects"
PROJECT_DIR="$PROJECT_BASE/spring-boot-3-jwt-security"
DOCKER_DATA_DIR="/opt/docker-data"

# Function to ask for confirmation
confirm() {
    read -p "$1 [y/N]: " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        return 0
    else
        return 1
    fi
}

echo "Project will be installed to: $PROJECT_DIR"
echo "Docker data will be stored in: $DOCKER_DATA_DIR"
echo ""

if ! confirm "Proceed with installation?"; then
    echo "Installation cancelled."
    exit 0
fi

# Step 1: Create base directories
echo "Creating base directories..."
mkdir -p "$PROJECT_BASE"
sudo mkdir -p "$DOCKER_DATA_DIR"/{postgres,redis,logs}

# Step 2: Clone or update repository
if [ -d "$PROJECT_DIR" ]; then
    echo "Project directory exists. Updating..."
    cd "$PROJECT_DIR"
    git pull origin main
else
    echo "Cloning repository..."
    cd "$PROJECT_BASE"
    git clone "$GITHUB_REPO"
fi

# Step 3: Set proper ownership
echo "Setting ownership and permissions..."
sudo chown -R hungcop:hungcop "$DOCKER_DATA_DIR"
chmod -R 755 "$DOCKER_DATA_DIR"

# Step 4: Navigate to project directory
cd "$PROJECT_DIR"
echo "Current directory: $(pwd)"

# Step 5: Check required files
echo "Checking required files..."
REQUIRED_FILES=("docker-compose-optimized.yml" "database_schema.sql" "pom.xml")
MISSING_FILES=()

for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        MISSING_FILES+=("$file")
    fi
done

if [ ${#MISSING_FILES[@]} -gt 0 ]; then
    echo "⚠️ Missing required files:"
    printf '%s\n' "${MISSING_FILES[@]}"
    echo "Please ensure all files are in the repository."
    exit 1
fi

# Step 6: Check Docker
echo "Checking Docker..."
if ! docker info >/dev/null 2>&1; then
    echo "❌ Docker is not running or not accessible."
    echo "Please ensure Docker is installed and your user is in the docker group."
    echo "Run: sudo usermod -aG docker \$USER && newgrp docker"
    exit 1
fi

echo "✅ Docker is working"

# Step 7: Create .env file for configuration
echo "Creating environment configuration..."
cat > .env << EOF
# Environment configuration for Pi 5
POSTGRES_USER=hungcop
POSTGRES_PASSWORD=hungcop290987
POSTGRES_DB=jwt_security
POSTGRES_DATA_DIR=$DOCKER_DATA_DIR/postgres
REDIS_DATA_DIR=$DOCKER_DATA_DIR/redis
LOGS_DIR=$DOCKER_DATA_DIR/logs

# Pi 5 specific settings
PI5_MEMORY_LIMIT=512M
PI5_CPU_LIMIT=1.0
EOF

# Step 8: Show directory structure
echo ""
echo "=== Setup Complete ==="
echo "Project structure:"
echo "Code location: $PROJECT_DIR"
echo "Data location: $DOCKER_DATA_DIR"
echo ""
echo "Directory contents:"
ls -la "$PROJECT_DIR"
echo ""
echo "Docker data directories:"
ls -la "$DOCKER_DATA_DIR"

# Step 9: Next steps
echo ""
echo "=== Next Steps ==="
echo "1. Start PostgreSQL: docker-compose -f docker-compose-optimized.yml up -d postgres"
echo "2. Check logs: docker logs postgres-jwt-optimized"
echo "3. Connect to database: docker exec -it postgres-jwt-optimized psql -U hungcop -d jwt_security"
echo "4. Build Spring Boot app: mvn clean package"
echo "5. Run Spring Boot app: mvn spring-boot:run"
echo ""

if confirm "Start PostgreSQL container now?"; then
    echo "Starting PostgreSQL container..."
    docker-compose -f docker-compose-optimized.yml up -d postgres
    echo ""
    echo "Waiting for PostgreSQL to start..."
    sleep 10
    echo "Container status:"
    docker ps
    echo ""
    echo "PostgreSQL logs:"
    docker logs postgres-jwt-optimized --tail 20
fi

echo ""
echo "Setup completed successfully! 🚀"
