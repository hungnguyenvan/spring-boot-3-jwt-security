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

# Copy database scripts
echo "Copying database management scripts..."
scp check-database.sh $PI_USER@$PI_HOST:$PROJECT_DIR/
scp quick-db-check.sh $PI_USER@$PI_HOST:$PROJECT_DIR/
scp monitor-database.sh $PI_USER@$PI_HOST:$PROJECT_DIR/
scp detect-postgres.sh $PI_USER@$PI_HOST:$PROJECT_DIR/
scp database-doctor.sh $PI_USER@$PI_HOST:$PROJECT_DIR/

# Set permissions
echo "Setting permissions..."
ssh $PI_USER@$PI_HOST "chmod +x $PROJECT_DIR/app-control.sh"
ssh $PI_USER@$PI_HOST "chmod +x $PROJECT_DIR/check-database.sh"
ssh $PI_USER@$PI_HOST "chmod +x $PROJECT_DIR/quick-db-check.sh"
ssh $PI_USER@$PI_HOST "chmod +x $PROJECT_DIR/monitor-database.sh"
ssh $PI_USER@$PI_HOST "chmod +x $PROJECT_DIR/detect-postgres.sh"
ssh $PI_USER@$PI_HOST "chmod +x $PROJECT_DIR/database-doctor.sh"

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
echo "# Database management commands:"
echo "./detect-postgres.sh         # Auto-detect PostgreSQL container"
echo "./database-doctor.sh         # Database health check and repair"
echo "./quick-db-check.sh         # Quick database overview"
echo "./check-database.sh all     # Complete database analysis"
echo "./check-database.sh users   # Show user data"
echo "./check-database.sh books   # Show book data"
echo "./check-database.sh query   # Interactive SQL mode"
echo "./monitor-database.sh       # Real-time database monitoring"
echo ""
echo "# If container name is wrong, run:"
echo "./detect-postgres.sh update # Auto-fix all database scripts"
echo ""
echo "# If database has issues, run:"
echo "./database-doctor.sh repair # Diagnose and fix database problems"
echo ""
echo "# To install as systemd service:"
echo "sudo cp spring-boot-jwt.service /etc/systemd/system/"
echo "sudo systemctl daemon-reload"
echo "sudo systemctl enable spring-boot-jwt"
echo "sudo systemctl start spring-boot-jwt"
