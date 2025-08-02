#!/bin/bash

# Spring Boot JWT Security Application Control Script
# Usage: ./app-control.sh {start|stop|restart|status|logs}

APP_NAME="spring-boot-jwt-security"
APP_DIR="/home/hungcop/spring-boot-3-jwt-security"
JAVA_OPTS="-Xms512m -Xmx1024m -Dspring.profiles.active=pi5"
PID_FILE="$APP_DIR/app.pid"
LOG_FILE="$APP_DIR/logs/application.log"
JAR_FILE="$APP_DIR/target/security-0.0.1-SNAPSHOT.jar"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Create logs directory if it doesn't exist
mkdir -p "$APP_DIR/logs"

# Function to get process ID
get_pid() {
    if [ -f "$PID_FILE" ]; then
        cat "$PID_FILE"
    else
        echo ""
    fi
}

# Function to check if application is running
is_running() {
    local pid=$(get_pid)
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
        return 0
    else
        return 1
    fi
}

# Function to start the application
start_app() {
    echo -e "${BLUE}Starting $APP_NAME...${NC}"
    
    if is_running; then
        echo -e "${YELLOW}Application is already running with PID $(get_pid)${NC}"
        return 1
    fi
    
    # Check if JAR file exists
    if [ ! -f "$JAR_FILE" ]; then
        echo -e "${RED}JAR file not found: $JAR_FILE${NC}"
        echo -e "${YELLOW}Building application...${NC}"
        cd "$APP_DIR"
        mvn clean package -DskipTests
        if [ $? -ne 0 ]; then
            echo -e "${RED}Failed to build application${NC}"
            return 1
        fi
    fi
    
    cd "$APP_DIR"
    
    # Start application in background
    nohup java $JAVA_OPTS -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &
    local pid=$!
    
    # Save PID to file
    echo $pid > "$PID_FILE"
    
    # Wait a moment and check if process is still running
    sleep 3
    if is_running; then
        echo -e "${GREEN}Application started successfully with PID $pid${NC}"
        echo -e "${BLUE}Log file: $LOG_FILE${NC}"
        echo -e "${BLUE}Application URL: http://192.168.102.10:8080${NC}"
        return 0
    else
        echo -e "${RED}Failed to start application${NC}"
        rm -f "$PID_FILE"
        return 1
    fi
}

# Function to stop the application
stop_app() {
    echo -e "${BLUE}Stopping $APP_NAME...${NC}"
    
    if ! is_running; then
        echo -e "${YELLOW}Application is not running${NC}"
        rm -f "$PID_FILE"
        return 1
    fi
    
    local pid=$(get_pid)
    echo -e "${YELLOW}Stopping process $pid...${NC}"
    
    # Try graceful shutdown first
    kill "$pid"
    
    # Wait for process to stop
    for i in {1..30}; do
        if ! is_running; then
            echo -e "${GREEN}Application stopped successfully${NC}"
            rm -f "$PID_FILE"
            return 0
        fi
        sleep 1
    done
    
    # Force kill if graceful shutdown failed
    echo -e "${YELLOW}Graceful shutdown failed, forcing termination...${NC}"
    kill -9 "$pid" 2>/dev/null
    rm -f "$PID_FILE"
    echo -e "${GREEN}Application forcefully stopped${NC}"
}

# Function to restart the application
restart_app() {
    echo -e "${BLUE}Restarting $APP_NAME...${NC}"
    stop_app
    sleep 2
    start_app
}

# Function to show application status
show_status() {
    if is_running; then
        local pid=$(get_pid)
        echo -e "${GREEN}Application is running with PID $pid${NC}"
        
        # Show memory usage
        local memory=$(ps -p "$pid" -o rss= 2>/dev/null)
        if [ -n "$memory" ]; then
            echo -e "${BLUE}Memory usage: ${memory}KB${NC}"
        fi
        
        # Show uptime
        local start_time=$(ps -p "$pid" -o lstart= 2>/dev/null)
        if [ -n "$start_time" ]; then
            echo -e "${BLUE}Started: $start_time${NC}"
        fi
        
        # Test application endpoint
        echo -e "${BLUE}Testing application endpoint...${NC}"
        if curl -s -f http://localhost:8080/actuator/health > /dev/null; then
            echo -e "${GREEN}Application is responding to health checks${NC}"
        else
            echo -e "${YELLOW}Application is not responding to health checks${NC}"
        fi
    else
        echo -e "${RED}Application is not running${NC}"
        if [ -f "$PID_FILE" ]; then
            echo -e "${YELLOW}Removing stale PID file${NC}"
            rm -f "$PID_FILE"
        fi
    fi
}

# Function to show application logs
show_logs() {
    if [ -f "$LOG_FILE" ]; then
        echo -e "${BLUE}Showing last 50 lines of log file:${NC}"
        tail -n 50 "$LOG_FILE"
    else
        echo -e "${RED}Log file not found: $LOG_FILE${NC}"
    fi
}

# Function to follow logs in real-time
follow_logs() {
    if [ -f "$LOG_FILE" ]; then
        echo -e "${BLUE}Following log file (Ctrl+C to exit):${NC}"
        tail -f "$LOG_FILE"
    else
        echo -e "${RED}Log file not found: $LOG_FILE${NC}"
    fi
}

# Function to show usage information
show_usage() {
    echo "Usage: $0 {start|stop|restart|status|logs|follow-logs|build}"
    echo ""
    echo "Commands:"
    echo "  start       Start the application in background"
    echo "  stop        Stop the application"
    echo "  restart     Restart the application"
    echo "  status      Show application status"
    echo "  logs        Show last 50 lines of logs"
    echo "  follow-logs Follow logs in real-time"
    echo "  build       Build the application"
    echo ""
    echo "Configuration:"
    echo "  App Directory: $APP_DIR"
    echo "  Log File:      $LOG_FILE"
    echo "  PID File:      $PID_FILE"
    echo "  Java Options:  $JAVA_OPTS"
}

# Function to build the application
build_app() {
    echo -e "${BLUE}Building $APP_NAME...${NC}"
    cd "$APP_DIR"
    
    echo -e "${YELLOW}Running Maven build...${NC}"
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Build completed successfully${NC}"
        echo -e "${BLUE}JAR file: $JAR_FILE${NC}"
    else
        echo -e "${RED}Build failed${NC}"
        return 1
    fi
}

# Main script logic
case "$1" in
    start)
        start_app
        ;;
    stop)
        stop_app
        ;;
    restart)
        restart_app
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    follow-logs)
        follow_logs
        ;;
    build)
        build_app
        ;;
    *)
        show_usage
        exit 1
        ;;
esac

exit $?
