#!/bin/bash

# Quick deployment script for Pi 5
# This script will copy necessary files and setup the application control

echo "=== Deploying Spring Boot JWT Security to Pi 5 ==="

# Configuration
PI_HOST="192.168.102.10"
PI_USER="hungcop"
PROJECT_DIR="/home/hungcop/spring-boot-3-jwt-security"

echo "Deploying to: $PI_USER@$PI_HOST:$PROJECT_DIR"

# Copy application control files
echo "Copying application control files..."
scp app-control.sh $PI_USER@$PI_HOST:$PROJECT_DIR/
scp spring-boot-jwt.service $PI_USER@$PI_HOST:$PROJECT_DIR/

# Set permissions
echo "Setting permissions..."
ssh $PI_USER@$PI_HOST "chmod +x $PROJECT_DIR/app-control.sh"

echo "Deployment complete!"
echo ""
echo "To use the application control on Pi 5:"
echo ""
echo "ssh $PI_USER@$PI_HOST"
echo "cd $PROJECT_DIR"
echo ""
echo "# Application management commands:"
echo "./app-control.sh start      # Start application in background"
echo "./app-control.sh stop       # Stop application"
echo "./app-control.sh restart    # Restart application"
echo "./app-control.sh status     # Check application status"
echo "./app-control.sh logs       # View last 50 lines of logs"
echo "./app-control.sh follow-logs # Follow logs in real-time"
echo "./app-control.sh build      # Build the application"
echo ""
echo "# To install as systemd service:"
echo "sudo cp spring-boot-jwt.service /etc/systemd/system/"
echo "sudo systemctl daemon-reload"
echo "sudo systemctl enable spring-boot-jwt"
echo "sudo systemctl start spring-boot-jwt"
