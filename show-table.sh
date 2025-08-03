#!/bin/bash

# Show specific table details
# File: show-table.sh
# Usage: ./show-table.sh [table_name]

if [ $# -eq 0 ]; then
    echo "Usage: $0 <table_name>"
    echo ""
    echo "Available tables:"
    echo "• _user"
    echo "• book"
    echo "• book_type"
    echo "• token"
    echo "• user_profile"
    echo "• editor_book_type_permission"
    echo ""
    echo "Examples:"
    echo "  $0 _user"
    echo "  $0 book"
    exit 1
fi

TABLE_NAME=$1

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

success() {
    echo -e "${GREEN}✓${NC} $1"
}

warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Database connection parameters
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="jwt_security"
DB_USER="hungcop"
DB_PASSWORD="hungcop290987"

# Check if Docker mode
if docker ps | grep -q postgres; then
    DOCKER_MODE=true
    success "Using Docker PostgreSQL"
else
    DOCKER_MODE=false
    success "Using system PostgreSQL"
fi

echo ""
echo "=============================================="
echo "📋 TABLE DETAILS: $TABLE_NAME"
echo "=============================================="

# Check if table exists
echo ""
info "Checking if table exists..."
if [ "$DOCKER_MODE" = true ]; then
    TABLE_EXISTS=$(docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
        psql -h localhost -U $DB_USER -d $DB_NAME -t -c "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = '$TABLE_NAME');" 2>/dev/null | tr -d ' \n\r')
else
    TABLE_EXISTS=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = '$TABLE_NAME');" 2>/dev/null | tr -d ' \n\r')
fi

if [ "$TABLE_EXISTS" = "t" ]; then
    success "Table '$TABLE_NAME' exists"
else
    echo "❌ Table '$TABLE_NAME' does not exist"
    exit 1
fi

# Show table structure
echo ""
info "🏗️ Table Structure:"
if [ "$DOCKER_MODE" = true ]; then
    docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
        psql -h localhost -U $DB_USER -d $DB_NAME -c "\d+ $TABLE_NAME" 2>/dev/null
else
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "\d+ $TABLE_NAME" 2>/dev/null
fi

# Show row count
echo ""
info "📊 Row Count:"
if [ "$DOCKER_MODE" = true ]; then
    ROW_COUNT=$(docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
        psql -h localhost -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM $TABLE_NAME;" 2>/dev/null | tr -d ' \n\r')
else
    ROW_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM $TABLE_NAME;" 2>/dev/null | tr -d ' \n\r')
fi
echo "Total rows: $ROW_COUNT"

# Show all data if row count is reasonable (< 50)
if [ "$ROW_COUNT" -gt 0 ]; then
    echo ""
    if [ "$ROW_COUNT" -le 50 ]; then
        info "📄 All Data:"
        if [ "$DOCKER_MODE" = true ]; then
            docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
                psql -h localhost -U $DB_USER -d $DB_NAME -c "SELECT * FROM $TABLE_NAME ORDER BY id;" 2>/dev/null
        else
            PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT * FROM $TABLE_NAME ORDER BY id;" 2>/dev/null
        fi
    else
        info "📄 Sample Data (first 20 rows):"
        if [ "$DOCKER_MODE" = true ]; then
            docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
                psql -h localhost -U $DB_USER -d $DB_NAME -c "SELECT * FROM $TABLE_NAME ORDER BY id LIMIT 20;" 2>/dev/null
        else
            PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT * FROM $TABLE_NAME ORDER BY id LIMIT 20;" 2>/dev/null
        fi
        echo ""
        warning "Showing only first 20 rows (total: $ROW_COUNT)"
        echo "Use LIMIT and OFFSET for pagination:"
        echo "  SELECT * FROM $TABLE_NAME ORDER BY id LIMIT 20 OFFSET 20;"
    fi
else
    echo ""
    warning "📄 No data in this table"
fi

# Show indexes
echo ""
info "🔍 Indexes:"
if [ "$DOCKER_MODE" = true ]; then
    docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
        psql -h localhost -U $DB_USER -d $DB_NAME -c "SELECT indexname, indexdef FROM pg_indexes WHERE tablename = '$TABLE_NAME';" 2>/dev/null
else
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT indexname, indexdef FROM pg_indexes WHERE tablename = '$TABLE_NAME';" 2>/dev/null
fi

# Show constraints
echo ""
info "🔒 Constraints:"
if [ "$DOCKER_MODE" = true ]; then
    docker exec -it $(docker ps --format "table {{.Names}}" | grep postgres | head -1) \
        psql -h localhost -U $DB_USER -d $DB_NAME -c "SELECT conname, contype, pg_get_constraintdef(oid) FROM pg_constraint WHERE conrelid = '$TABLE_NAME'::regclass;" 2>/dev/null
else
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT conname, contype, pg_get_constraintdef(oid) FROM pg_constraint WHERE conrelid = '$TABLE_NAME'::regclass;" 2>/dev/null
fi

echo ""
echo "=============================================="
echo "✅ TABLE ANALYSIS COMPLETED"
echo "=============================================="
