#!/bin/bash

# Script để pull latest changes trên Pi5
# File: pull-latest.sh

echo "📥 Pulling latest changes from GitHub..."

# Backup current changes if any
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "⚠️ You have uncommitted changes. Creating backup..."
    git stash push -m "Backup before pull - $(date '+%Y-%m-%d %H:%M')"
    STASHED=true
fi

# Pull latest changes
echo "Pulling from main branch..."
git pull origin main

if [ $? -eq 0 ]; then
    echo "✅ Successfully pulled latest changes!"
    
    # Make scripts executable
    echo "Making scripts executable..."
    chmod +x *.sh
    
    # Show what changed
    echo ""
    echo "Recent commits:"
    git log --oneline -5
    
    if [ "$STASHED" = true ]; then
        echo ""
        echo "⚠️ Your previous changes were stashed."
        echo "To restore them: git stash pop"
    fi
    
    echo ""
    echo "🚀 Ready to deploy on Pi5!"
    echo "Run: ./complete-pi5-deployment.sh"
    
else
    echo "❌ Pull failed. Check your network connection."
    
    if [ "$STASHED" = true ]; then
        echo "Restoring your changes..."
        git stash pop
    fi
fi
