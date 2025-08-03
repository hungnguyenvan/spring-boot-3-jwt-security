#!/bin/bash

# Test app with detailed debug info
echo "🧪 Testing application startup with debug info..."

# Set Java options for detailed logging
export JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -Djava.awt.headless=true -Ddebug=true"

# Run with debug to see condition evaluation report
java $JAVA_OPTS -jar target/security-0.0.1-SNAPSHOT.jar \
    --spring.profiles.active=pi5 \
    --debug \
    --logging.level.org.springframework.boot.autoconfigure=DEBUG \
    --logging.level.org.springframework.data.jpa=DEBUG
