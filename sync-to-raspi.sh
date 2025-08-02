#!/bin/bash

# Script đồng bộ code từ dev laptop sang Raspberry Pi
# File: sync-to-raspi.sh

# Cấu hình
RASPI_IP="192.168.1.100"  # Thay bằng IP thực của Raspberry Pi
RASPI_USER="pi"
RASPI_PATH="/home/pi/spring-boot-3-jwt-security"
LOCAL_PATH="."

echo "🔄 Syncing code to Raspberry Pi..."

# Kiểm tra kết nối
echo "🔍 Checking connection to Raspberry Pi..."
if ! ping -c 1 $RASPI_IP &> /dev/null; then
    echo "❌ Cannot reach Raspberry Pi at $RASPI_IP"
    echo "📝 Please update RASPI_IP in this script"
    exit 1
fi

# Sync code (loại trừ các file không cần thiết)
echo "📁 Syncing files..."
rsync -avz --progress \
    --exclude 'target/' \
    --exclude '.git/' \
    --exclude '*.log' \
    --exclude '.idea/' \
    --exclude '*.iml' \
    --exclude 'node_modules/' \
    $LOCAL_PATH/ $RASPI_USER@$RASPI_IP:$RASPI_PATH/

if [ $? -eq 0 ]; then
    echo "✅ Sync completed successfully!"
    
    # Restart application trên Raspberry Pi
    echo "🔄 Restarting application on Raspberry Pi..."
    ssh $RASPI_USER@$RASPI_IP "cd $RASPI_PATH && ./start-app.sh"
    
    echo "🎉 Application restarted!"
    echo "📱 Access at: http://$RASPI_IP:8080"
else
    echo "❌ Sync failed!"
    exit 1
fi
