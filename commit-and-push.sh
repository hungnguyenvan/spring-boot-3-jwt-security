#!/bin/bash

# Git commit và push changes cho Pi5
# File: commit-and-push.sh

echo "📤 Committing and pushing changes to GitHub..."

# Kiểm tra git status
echo "Current git status:"
git status --short

echo ""
read -p "Enter commit message (or press Enter for default): " COMMIT_MSG

if [ -z "$COMMIT_MSG" ]; then
    COMMIT_MSG="Update project files - $(date '+%Y-%m-%d %H:%M')"
fi

echo ""
echo "Committing with message: $COMMIT_MSG"

# Add all changes
git add .

# Commit
git commit -m "$COMMIT_MSG"

# Push to main branch
echo "Pushing to GitHub..."
git push origin main

if [ $? -eq 0 ]; then
    echo "✅ Successfully pushed to GitHub!"
    echo ""
    echo "You can now pull on Pi5 with:"
    echo "git pull origin main"
else
    echo "❌ Push failed. Check your GitHub credentials and network connection."
fi
