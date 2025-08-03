#!/bin/bash

# Database Connection Checker for Pi5
# File: check-database.sh
# Usage: ./check-database.sh [--detailed] [--summary]

echo "🔍 Checking database connectivity..."

# Parse command line arguments
DETAILED_MODE=false
SUMMARY_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --detailed|-d)
            DETAILED_MODE=true
            shift
            ;;
        --summary|-s)
            SUMMARY_ONLY=true
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [options]"
            echo "Options:"
            echo "  --detailed, -d    Show detailed table structure and content"
            echo "  --summary, -s     Show only summary statistics"
            echo "  --help, -h        Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Default to summary if no mode specified
if [ "$DETAILED_MODE" = false ] && [ "$SUMMARY_ONLY" = false ]; then
    SUMMARY_ONLY=true
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

success() {
    echo -e "${GREEN}✓${NC} $1"
}

error() {
    echo -e "${RED}✗${NC} $1"
}

warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Check if PostgreSQL service is running
info "Checking PostgreSQL service status..."
if systemctl is-active --quiet postgresql; then
    success "PostgreSQL service is running"
elif docker ps | grep -q postgres; then
    success "PostgreSQL Docker container is running"
    DOCKER_MODE=true
else
    error "PostgreSQL is not running"
    echo ""
    echo "To start PostgreSQL:"
    echo "• Service: sudo systemctl start postgresql"
    echo "• Docker: docker-compose -f docker-compose-pi5.yml up -d postgres"
    exit 1
fi

# Database connection parameters
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="jwt_security"
DB_USER="hungcop"
DB_PASSWORD="hungcop290987"

echo ""
info "Testing database connection..."
echo "Host: $DB_HOST:$DB_PORT"
echo "Database: $DB_NAME"
echo "User: $DB_USER"

# Test connection
if [ "$DOCKER_MODE" = true ]; then
    # Test via Docker
    CONNECTION_TEST=$(docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
        psql -h localhost -U $DB_USER -d $DB_NAME -c "SELECT 1;" 2>&1)
else
    # Test via system PostgreSQL
    CONNECTION_TEST=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT 1;" 2>&1)
fi

if echo "$CONNECTION_TEST" | grep -q "1 row"; then
    success "Database connection successful"
    
    echo ""
    info "Checking database schema..."
    
    # Check tables
    if [ "$DOCKER_MODE" = true ]; then
        TABLE_COUNT=$(docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
            psql -h localhost -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null | tr -d ' \n\r')
    else
        TABLE_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null | tr -d ' \n\r')
    fi
    
    if [ "$TABLE_COUNT" -gt 0 ]; then
        success "Database schema exists ($TABLE_COUNT tables found)"
        
        # List tables with details
        echo ""
        info "Database tables:"
        if [ "$DOCKER_MODE" = true ]; then
            TABLES=$(docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
                psql -h localhost -U $DB_USER -d $DB_NAME -t -c "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;" 2>/dev/null | tr -d '\r')
        else
            TABLES=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;" 2>/dev/null | tr -d '\r')
        fi
        
        echo "$TABLES" | while read -r table; do
            table=$(echo "$table" | xargs)  # trim whitespace
            if [ -n "$table" ]; then
                echo "• $table"
            fi
        done
        
        # Show detailed table structure and content
        if [ "$DETAILED_MODE" = true ]; then
            echo ""
            info "Detailed table analysis:"
            echo ""
            
            echo "$TABLES" | while read -r table; do
                table=$(echo "$table" | xargs)  # trim whitespace
                if [ -n "$table" ]; then
                    echo "=============================================="
                    echo "📋 TABLE: $table"
                    echo "=============================================="
                    
                    # Show table structure
                    echo ""
                    echo "🏗️ Structure:"
                    if [ "$DOCKER_MODE" = true ]; then
                        docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
                            psql -h localhost -U $DB_USER -d $DB_NAME -c "\d+ $table" 2>/dev/null
                    else
                        PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "\d+ $table" 2>/dev/null
                    fi
                    
                    # Show row count
                    echo ""
                    echo "📊 Row count:"
                    if [ "$DOCKER_MODE" = true ]; then
                        ROW_COUNT=$(docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
                            psql -h localhost -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM $table;" 2>/dev/null | tr -d ' \n\r')
                    else
                        ROW_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM $table;" 2>/dev/null | tr -d ' \n\r')
                    fi
                    echo "Total rows: $ROW_COUNT"
                    
                    # Show sample data (first 5 rows)
                    if [ "$ROW_COUNT" -gt 0 ]; then
                        echo ""
                        echo "📄 Sample data (first 5 rows):"
                        if [ "$DOCKER_MODE" = true ]; then
                            docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
                                psql -h localhost -U $DB_USER -d $DB_NAME -c "SELECT * FROM $table LIMIT 5;" 2>/dev/null
                        else
                            PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT * FROM $table LIMIT 5;" 2>/dev/null
                        fi
                    else
                        echo ""
                        echo "📄 No data in this table"
                    fi
                    
                    echo ""
                fi
            done
        fi
        
        # Summary statistics
        echo ""
        info "📈 Database Summary:"
        echo ""
        echo "$TABLES" | while read -r table; do
            table=$(echo "$table" | xargs)
            if [ -n "$table" ]; then
                if [ "$DOCKER_MODE" = true ]; then
                    ROW_COUNT=$(docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
                        psql -h localhost -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM $table;" 2>/dev/null | tr -d ' \n\r')
                else
                    ROW_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM $table;" 2>/dev/null | tr -d ' \n\r')
                fi
                printf "• %-25s : %s rows\n" "$table" "$ROW_COUNT"
            fi
        done
        
    else
        warning "Database exists but no tables found"
        echo "Run database initialization:"
        echo "• ./manage-postgres-pi5.sh init"
    fi
    
else
    error "Database connection failed"
    echo "Error details:"
    echo "$CONNECTION_TEST"
    echo ""
    echo "Troubleshooting steps:"
    echo "1. Check if PostgreSQL is running: systemctl status postgresql"
    echo "2. Check Docker containers: docker ps"
    echo "3. Check database logs: docker logs <postgres-container>"
    echo "4. Initialize database: ./manage-postgres-pi5.sh init"
    exit 1
fi

# Test application database configuration
echo ""
info "Checking application configuration..."

# Check if application.yml exists
if [ -f "src/main/resources/application-pi5.yml" ]; then
    success "Pi5 configuration file found"
    
    # Extract database URL from config
    DB_URL=$(grep "url:" src/main/resources/application-pi5.yml | awk '{print $2}')
    if [ -n "$DB_URL" ]; then
        echo "Configured URL: $DB_URL"
    fi
else
    error "Pi5 configuration file not found"
fi

echo ""
echo "=========================================="
echo "DATABASE CHECK COMPLETED"
echo "=========================================="
echo ""

if [ "$CONNECTION_TEST" ] && echo "$CONNECTION_TEST" | grep -q "1 row"; then
    success "Database is ready for Spring Boot application"
    echo ""
    echo "📋 Available commands:"
    echo "• Basic check:      ./check-database.sh"
    echo "• Detailed view:    ./check-database.sh --detailed"
    echo "• Summary only:     ./check-database.sh --summary"
    echo "• Specific table:   ./show-table.sh <table_name>"
    echo ""
    echo "🚀 Next steps:"
    echo "• Start application: ./run-on-pi5.sh"
    echo "• Test APIs: ./quick-function-test.sh"
    echo "• Full deployment: ./complete-pi5-deployment.sh"
else
    error "Database setup incomplete"
    echo ""
    echo "Fix steps:"
    echo "• Initialize database: ./manage-postgres-pi5.sh init"
    echo "• Check Docker logs: docker logs <postgres-container>"
    echo "• Restart PostgreSQL: sudo systemctl restart postgresql"
fi

echo ""