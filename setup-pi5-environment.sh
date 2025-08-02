#!/bin/bash

# Setup script for Spring Boot JWT project on Raspberry Pi 5
# Run this script on Pi 5 to prepare environment

echo "=== Setting up Spring Boot JWT Project Environment ==="

# 1. Create project directory
PROJECT_DIR="/home/hungcop/spring-boot-jwt"
echo "Creating project directory: $PROJECT_DIR"
mkdir -p "$PROJECT_DIR"
cd "$PROJECT_DIR"

# 2. Create Docker data directories
echo "Creating Docker data directories..."
sudo mkdir -p /opt/docker-data/postgres
sudo mkdir -p /opt/docker-data/redis
sudo mkdir -p /opt/docker-data/logs

# 3. Set proper ownership
echo "Setting proper ownership..."
sudo chown -R hungcop:hungcop /opt/docker-data/

# 4. Create local directories
echo "Creating local directories..."
mkdir -p logs
mkdir -p config
mkdir -p backups

# 5. Set permissions
echo "Setting permissions..."
chmod 755 logs config backups
chmod 755 /opt/docker-data/postgres
chmod 755 /opt/docker-data/redis

# 6. Verify setup
echo ""
echo "=== Verification ==="
echo "Project directory: $(pwd)"
echo "Docker data directories:"
ls -la /opt/docker-data/
echo ""
echo "Local directories:"
ls -la

echo ""
echo "=== Setup Complete ==="
echo "Next steps:"
echo "1. Copy Docker Compose files to $PROJECT_DIR"
echo "2. Copy database schema to $PROJECT_DIR"
echo "3. Run: docker-compose up -d"

# Show disk space
echo ""
echo "Available disk space:"
df -h /opt/docker-data/
