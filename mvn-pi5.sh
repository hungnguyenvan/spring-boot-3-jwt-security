#!/bin/bash

# Maven wrapper script tối ưu cho Raspberry Pi 5
# File: mvn-pi5.sh

echo "🔧 Running Maven on Raspberry Pi 5..."

# Set optimal JVM options for Pi5
export MAVEN_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -Djava.awt.headless=true -Djansi.force=true -Djansi.passthrough=true"
export _JAVA_OPTIONS="-Djava.awt.headless=true"

# Disable Maven color output để tránh broken pipe
MAVEN_ARGS="--batch-mode --no-transfer-progress"

# Function để chạy Maven với retry
run_maven_with_retry() {
    local cmd="$1"
    local max_attempts=3
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        echo "🔄 Attempt $attempt/$max_attempts: $cmd"
        
        # Chạy Maven với timeout và redirect output
        timeout 600 mvn $MAVEN_ARGS $cmd 2>&1 | tee logs/maven-pi5.log
        local exit_code=${PIPESTATUS[0]}
        
        if [ $exit_code -eq 0 ]; then
            echo "✅ Maven command succeeded on attempt $attempt"
            return 0
        elif [ $exit_code -eq 124 ]; then
            echo "⏰ Maven command timed out on attempt $attempt"
        else
            echo "❌ Maven command failed with exit code $exit_code on attempt $attempt"
        fi
        
        attempt=$((attempt + 1))
        if [ $attempt -le $max_attempts ]; then
            echo "⏳ Waiting 10 seconds before retry..."
            sleep 10
        fi
    done
    
    echo "❌ Maven command failed after $max_attempts attempts"
    return 1
}

# Tạo thư mục logs
mkdir -p logs

# Parse command line arguments
if [ $# -eq 0 ]; then
    echo "Usage: $0 <maven-goals>"
    echo "Example: $0 clean compile"
    echo "Example: $0 spring-boot:run"
    exit 1
fi

# Chạy Maven command
run_maven_with_retry "$*"
