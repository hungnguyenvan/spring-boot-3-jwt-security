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

# 6. Setup app-control.sh script
echo "Setting up app-control.sh script..."
if [ -f "app-control.sh" ]; then
    chmod +x app-control.sh
    echo "app-control.sh permissions set"
else
    echo "Warning: app-control.sh not found in current directory"
fi

# 7. Setup systemd service (optional)
echo "Setting up systemd service..."
if [ -f "spring-boot-jwt.service" ]; then
    echo "To install systemd service, run:"
    echo "  sudo cp spring-boot-jwt.service /etc/systemd/system/"
    echo "  sudo systemctl daemon-reload"
    echo "  sudo systemctl enable spring-boot-jwt"
else
    echo "Warning: spring-boot-jwt.service not found"
fi

# 8. Verify setup
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
echo "3. Use app-control.sh to manage the application:"
echo "   ./app-control.sh start    # Start in background"
echo "   ./app-control.sh stop     # Stop application"
echo "   ./app-control.sh status   # Check status"
echo "   ./app-control.sh logs     # View logs"
echo ""
echo "Systemd service commands (if installed):"
echo "   sudo systemctl start spring-boot-jwt"
echo "   sudo systemctl stop spring-boot-jwt"
echo "   sudo systemctl status spring-boot-jwt"
echo "3. Run: docker-compose up -d"

# Show disk space
echo ""
echo "Available disk space:"
df -h /opt/docker-data/
