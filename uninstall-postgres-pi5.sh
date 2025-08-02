#!/bin/bash

# Script to uninstall PostgreSQL from Raspberry Pi 5
# This will remove native PostgreSQL installation to use Docker version instead

echo "=== PostgreSQL Uninstallation Script for Pi 5 ==="
echo "This script will remove native PostgreSQL to use Docker version"
echo ""

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

# Check if PostgreSQL is installed
echo "Checking PostgreSQL installation..."
if ! dpkg -l | grep -q postgresql; then
    echo "PostgreSQL is not installed via apt. Nothing to remove."
    exit 0
fi

echo "Found PostgreSQL installation:"
dpkg -l | grep postgres

echo ""
if confirm "Do you want to proceed with PostgreSQL removal?"; then
    echo "Proceeding with PostgreSQL removal..."
else
    echo "Aborted."
    exit 0
fi

# Stop PostgreSQL service
echo "Stopping PostgreSQL service..."
sudo systemctl stop postgresql 2>/dev/null || echo "PostgreSQL service not running"
sudo systemctl disable postgresql 2>/dev/null || echo "PostgreSQL service not enabled"

# Check for running processes
echo "Checking for running PostgreSQL processes..."
POSTGRES_PROCESSES=$(ps aux | grep postgres | grep -v grep | wc -l)
if [ $POSTGRES_PROCESSES -gt 0 ]; then
    echo "Found running PostgreSQL processes:"
    ps aux | grep postgres | grep -v grep
    
    if confirm "Kill running PostgreSQL processes?"; then
        sudo pkill -f postgres
        echo "PostgreSQL processes killed"
    fi
fi

# Check port usage
echo "Checking port 5432 usage..."
PORT_USAGE=$(sudo netstat -tlnp | grep 5432 || true)
if [ ! -z "$PORT_USAGE" ]; then
    echo "Port 5432 is in use:"
    echo "$PORT_USAGE"
else
    echo "Port 5432 is free"
fi

# Remove PostgreSQL packages
echo "Removing PostgreSQL packages..."
if confirm "Remove all PostgreSQL packages and configurations?"; then
    sudo apt remove --purge postgresql postgresql-* -y
    echo "PostgreSQL packages removed"
else
    echo "Keeping PostgreSQL packages"
fi

# Remove data directories
echo "Checking PostgreSQL data directories..."
POSTGRES_DIRS="/var/lib/postgresql/ /etc/postgresql/ /var/log/postgresql/"
for dir in $POSTGRES_DIRS; do
    if [ -d "$dir" ]; then
        echo "Found directory: $dir"
        if confirm "Remove directory $dir? (This will delete all data)"; then
            sudo rm -rf "$dir"
            echo "Removed $dir"
        fi
    fi
done

# Remove postgres user and group
echo "Checking postgres user and group..."
if id "postgres" &>/dev/null; then
    if confirm "Remove postgres user and group?"; then
        sudo deluser postgres 2>/dev/null || echo "Could not remove postgres user"
        sudo delgroup postgres 2>/dev/null || echo "Could not remove postgres group"
        echo "Postgres user and group removed"
    fi
fi

# Cleanup
echo "Cleaning up package cache..."
sudo apt autoremove -y
sudo apt autoclean

# Final verification
echo ""
echo "=== Verification ==="
echo "Checking for remaining PostgreSQL packages:"
REMAINING_PACKAGES=$(dpkg -l | grep postgres || true)
if [ -z "$REMAINING_PACKAGES" ]; then
    echo "✅ No PostgreSQL packages found"
else
    echo "⚠️ Remaining packages:"
    echo "$REMAINING_PACKAGES"
fi

echo ""
echo "Checking port 5432:"
PORT_CHECK=$(sudo netstat -tlnp | grep 5432 || true)
if [ -z "$PORT_CHECK" ]; then
    echo "✅ Port 5432 is free"
else
    echo "⚠️ Port 5432 still in use:"
    echo "$PORT_CHECK"
fi

echo ""
echo "=== PostgreSQL Removal Complete ==="
echo "You can now use PostgreSQL in Docker without conflicts"
echo ""
echo "Next steps:"
echo "1. Ensure Docker is working: docker info"
echo "2. Start PostgreSQL container: docker-compose -f docker-compose-pi5.yml up -d postgres"
echo "3. Check container status: docker ps"
