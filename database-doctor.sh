#!/bin/bash

# Database Health Check and Repair Script
# Checks database status and fixes common issues

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

DB_CONTAINER="postgres-jwt-optimized"
DB_NAME="jwt_security"
DB_USER="hungcop"

echo -e "${BLUE}=== Database Health Check and Repair ===${NC}"

# Function to check if container is running
check_container() {
    if ! docker ps | grep -q "$DB_CONTAINER"; then
        echo -e "${RED}❌ PostgreSQL container '$DB_CONTAINER' is not running!${NC}"
        return 1
    fi
    echo -e "${GREEN}✅ Container is running${NC}"
    return 0
}

# Function to test database connection
test_connection() {
    echo -e "${BLUE}Testing database connection...${NC}"
    
    if docker exec "$DB_CONTAINER" pg_isready -U "$DB_USER" -d "$DB_NAME" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Database server is ready${NC}"
    else
        echo -e "${RED}❌ Database server is not ready${NC}"
        return 1
    fi
    
    # Test actual connection with query
    if docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Database connection successful${NC}"
        return 0
    else
        echo -e "${RED}❌ Database connection failed${NC}"
        return 1
    fi
}

# Function to check if tables exist
check_tables() {
    echo -e "${BLUE}Checking database tables...${NC}"
    
    local tables=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c "
        SELECT string_agg(tablename, ', ') 
        FROM pg_tables 
        WHERE schemaname = 'public' AND tablename IN ('_user', 'token', 'book');
    " 2>/dev/null | tr -d ' ')
    
    if [ -z "$tables" ] || [ "$tables" = "" ]; then
        echo -e "${RED}❌ Required tables not found${NC}"
        return 1
    else
        echo -e "${GREEN}✅ Found tables: $tables${NC}"
        return 0
    fi
}

# Function to check sequences
check_sequences() {
    echo -e "${BLUE}Checking database sequences...${NC}"
    
    local sequences=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c "
        SELECT string_agg(sequence_name, ', ') 
        FROM information_schema.sequences 
        WHERE sequence_schema = 'public' AND sequence_name IN ('_user_id_seq', 'token_id_seq', 'book_id_seq');
    " 2>/dev/null | tr -d ' ')
    
    if [ -z "$sequences" ] || [ "$sequences" = "" ]; then
        echo -e "${RED}❌ Required sequences not found${NC}"
        return 1
    else
        echo -e "${GREEN}✅ Found sequences: $sequences${NC}"
        return 0
    fi
}

# Function to check sample data
check_data() {
    echo -e "${BLUE}Checking sample data...${NC}"
    
    local user_count=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM _user;" 2>/dev/null | tr -d ' ')
    local book_count=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM book;" 2>/dev/null | tr -d ' ')
    
    echo -e "${CYAN}   Users: $user_count${NC}"
    echo -e "${CYAN}   Books: $book_count${NC}"
    
    if [ "$user_count" -gt 0 ] && [ "$book_count" -gt 0 ]; then
        echo -e "${GREEN}✅ Sample data found${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  No sample data found${NC}"
        return 1
    fi
}

# Function to reload database schema
reload_schema() {
    echo -e "${YELLOW}🔄 Reloading database schema...${NC}"
    
    if [ ! -f "database_schema.sql" ]; then
        echo -e "${RED}❌ database_schema.sql not found${NC}"
        return 1
    fi
    
    echo -e "${BLUE}Executing schema script...${NC}"
    if docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" < database_schema.sql; then
        echo -e "${GREEN}✅ Schema reloaded successfully${NC}"
        return 0
    else
        echo -e "${RED}❌ Schema reload failed${NC}"
        return 1
    fi
}

# Function to show repair options
show_repair_options() {
    echo ""
    echo -e "${YELLOW}=== Repair Options ===${NC}"
    echo "1. Reload schema only (reload-schema)"
    echo "2. Restart container (restart-container)"
    echo "3. Full database reset (full-reset)"
    echo "4. Check logs (logs)"
    echo "5. Manual SQL mode (sql)"
    echo ""
}

# Function to restart container
restart_container() {
    echo -e "${YELLOW}🔄 Restarting PostgreSQL container...${NC}"
    
    docker restart "$DB_CONTAINER"
    
    echo -e "${BLUE}Waiting for container to be ready...${NC}"
    for i in {1..30}; do
        if docker exec "$DB_CONTAINER" pg_isready -U "$DB_USER" -d "$DB_NAME" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Container restarted successfully${NC}"
            return 0
        fi
        sleep 1
    done
    
    echo -e "${RED}❌ Container restart failed or timed out${NC}"
    return 1
}

# Function to do full reset
full_reset() {
    echo -e "${RED}⚠️  WARNING: This will completely reset the database!${NC}"
    read -p "Are you sure? (type 'yes' to continue): " confirm
    
    if [ "$confirm" != "yes" ]; then
        echo -e "${YELLOW}Operation cancelled${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}🔄 Performing full database reset...${NC}"
    
    if [ -f "./reload-database-pi5.sh" ]; then
        chmod +x ./reload-database-pi5.sh
        ./reload-database-pi5.sh
    else
        echo -e "${RED}❌ reload-database-pi5.sh not found${NC}"
        return 1
    fi
}

# Function to show logs
show_logs() {
    echo -e "${BLUE}=== Container Logs (last 50 lines) ===${NC}"
    docker logs --tail 50 "$DB_CONTAINER"
}

# Function to enter SQL mode
sql_mode() {
    echo -e "${BLUE}=== Manual SQL Mode ===${NC}"
    echo -e "${YELLOW}Type 'exit' to quit${NC}"
    
    docker exec -it "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME"
}

# Main health check function
run_health_check() {
    echo -e "${BLUE}Running complete health check...${NC}"
    echo ""
    
    local issues=0
    
    if ! check_container; then
        ((issues++))
    fi
    
    if ! test_connection; then
        ((issues++))
    fi
    
    if ! check_tables; then
        ((issues++))
    fi
    
    if ! check_sequences; then
        ((issues++))
    fi
    
    if ! check_data; then
        ((issues++))
    fi
    
    echo ""
    if [ $issues -eq 0 ]; then
        echo -e "${GREEN}🎉 Database is healthy! No issues found.${NC}"
    else
        echo -e "${RED}⚠️  Found $issues issue(s)${NC}"
        show_repair_options
    fi
    
    return $issues
}

# Main script logic
case "$1" in
    check|"")
        run_health_check
        ;;
    reload-schema)
        check_container && reload_schema
        ;;
    restart-container)
        restart_container
        ;;
    full-reset)
        full_reset
        ;;
    logs)
        show_logs
        ;;
    sql)
        check_container && sql_mode
        ;;
    repair)
        run_health_check
        if [ $? -gt 0 ]; then
            echo ""
            echo -e "${YELLOW}Would you like to try automatic repair? (y/n)${NC}"
            read -p "> " auto_repair
            
            if [ "$auto_repair" = "y" ] || [ "$auto_repair" = "Y" ]; then
                echo -e "${BLUE}Attempting automatic repair...${NC}"
                if ! check_tables || ! check_sequences; then
                    reload_schema
                fi
                if ! test_connection; then
                    restart_container
                fi
                echo ""
                echo -e "${BLUE}Re-running health check...${NC}"
                run_health_check
            fi
        fi
        ;;
    help|--help|-h)
        echo "Database Health Check and Repair Script"
        echo ""
        echo "Usage: $0 [option]"
        echo ""
        echo "Options:"
        echo "  check           Run health check (default)"
        echo "  reload-schema   Reload database schema only"
        echo "  restart-container Restart PostgreSQL container"
        echo "  full-reset      Complete database reset (DANGEROUS)"
        echo "  logs            Show container logs"
        echo "  sql             Enter manual SQL mode"
        echo "  repair          Run health check and offer repair options"
        echo "  help            Show this help"
        echo ""
        ;;
    *)
        echo -e "${RED}Unknown option: $1${NC}"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac
