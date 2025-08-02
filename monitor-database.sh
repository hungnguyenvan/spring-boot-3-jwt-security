#!/bin/bash

# Database Monitor Script
# Real-time monitoring of database activity

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

DB_CONTAINER="postgres-container"
DB_NAME="jwt_security"
DB_USER="jwt_user"

# Function to execute query
query() {
    docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c "$1" 2>/dev/null
}

# Function to clear screen and show header
show_header() {
    clear
    echo -e "${BLUE}=== Database Monitor - JWT Security ===${NC}"
    echo -e "${CYAN}Press Ctrl+C to exit${NC}"
    echo -e "${CYAN}Refresh every 5 seconds${NC}"
    echo ""
    echo -e "${YELLOW}Time: $(date)${NC}"
    echo ""
}

# Function to show statistics
show_stats() {
    echo -e "${GREEN}📊 Database Statistics:${NC}"
    
    # Connection count
    local connections=$(query "SELECT count(*) FROM pg_stat_activity WHERE datname='$DB_NAME';")
    echo -e "   Active connections: ${YELLOW}$connections${NC}"
    
    # Database size
    local db_size=$(query "SELECT pg_size_pretty(pg_database_size('$DB_NAME'));")
    echo -e "   Database size: ${YELLOW}$db_size${NC}"
    
    # Record counts
    local user_count=$(query "SELECT count(*) FROM _user;")
    local token_count=$(query "SELECT count(*) FROM token;")
    local book_count=$(query "SELECT count(*) FROM book;")
    
    echo -e "   Users: ${YELLOW}$user_count${NC} | Tokens: ${YELLOW}$token_count${NC} | Books: ${YELLOW}$book_count${NC}"
    echo ""
}

# Function to show recent activity
show_activity() {
    echo -e "${GREEN}🔄 Recent Activity:${NC}"
    
    # Show recent user logins (tokens created)
    echo -e "${CYAN}Recent token activity:${NC}"
    query "
    SELECT 
        u.username,
        t.token_type,
        CASE WHEN t.revoked THEN 'REVOKED' ELSE 'ACTIVE' END as status
    FROM token t
    JOIN _user u ON t.user_id = u.id
    ORDER BY t.id DESC
    LIMIT 5;
    " | while read line; do
        if [ -n "$line" ]; then
            echo "   $line"
        fi
    done
    echo ""
}

# Function to show running queries
show_queries() {
    echo -e "${GREEN}🔍 Active Queries:${NC}"
    
    local active_queries=$(query "
    SELECT 
        pid,
        usename,
        application_name,
        state,
        substring(query, 1, 50) || '...' as query_preview
    FROM pg_stat_activity 
    WHERE datname='$DB_NAME' 
        AND state = 'active' 
        AND query NOT LIKE '%pg_stat_activity%'
    ORDER BY query_start DESC;
    ")
    
    if [ -n "$active_queries" ]; then
        echo "$active_queries" | while read line; do
            if [ -n "$line" ]; then
                echo "   $line"
            fi
        done
    else
        echo "   No active queries"
    fi
    echo ""
}

# Function to show locks
show_locks() {
    echo -e "${GREEN}🔒 Database Locks:${NC}"
    
    local locks=$(query "
    SELECT 
        l.mode,
        l.locktype,
        l.relation::regclass as table_name,
        a.usename,
        a.application_name
    FROM pg_locks l
    JOIN pg_stat_activity a ON l.pid = a.pid
    WHERE a.datname = '$DB_NAME'
        AND l.granted = true
    ORDER BY l.relation;
    ")
    
    if [ -n "$locks" ]; then
        echo "$locks" | head -10 | while read line; do
            if [ -n "$line" ]; then
                echo "   $line"
            fi
        done
    else
        echo "   No locks detected"
    fi
    echo ""
}

# Main monitoring loop
monitor_database() {
    while true; do
        # Check if container is running
        if ! docker ps | grep -q "$DB_CONTAINER"; then
            show_header
            echo -e "${RED}❌ PostgreSQL container '$DB_CONTAINER' is not running!${NC}"
            echo -e "${YELLOW}Start it with: docker-compose up -d${NC}"
            sleep 5
            continue
        fi
        
        show_header
        show_stats
        show_activity
        show_queries
        show_locks
        
        sleep 5
    done
}

# Function to show performance stats
show_performance() {
    echo -e "${BLUE}=== Performance Statistics ===${NC}"
    echo ""
    
    echo -e "${GREEN}📈 Table Statistics:${NC}"
    query "
    SELECT 
        schemaname,
        tablename,
        n_tup_ins as inserts,
        n_tup_upd as updates,
        n_tup_del as deletes,
        n_live_tup as live_rows,
        n_dead_tup as dead_rows
    FROM pg_stat_user_tables
    ORDER BY tablename;
    "
    
    echo ""
    echo -e "${GREEN}💾 Cache Hit Ratio:${NC}"
    query "
    SELECT 
        round(
            100.0 * sum(blks_hit) / (sum(blks_hit) + sum(blks_read)), 2
        ) as cache_hit_ratio
    FROM pg_stat_database 
    WHERE datname = '$DB_NAME';
    "
    
    echo ""
    echo -e "${GREEN}🔍 Index Usage:${NC}"
    query "
    SELECT 
        schemaname,
        tablename,
        indexname,
        idx_tup_read,
        idx_tup_fetch
    FROM pg_stat_user_indexes
    ORDER BY idx_tup_read DESC;
    "
}

# Usage information
show_usage() {
    echo "Database Monitor Script"
    echo ""
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  monitor      Start real-time monitoring (default)"
    echo "  performance  Show performance statistics"
    echo "  help         Show this help"
    echo ""
}

# Main script
case "$1" in
    monitor|"")
        monitor_database
        ;;
    performance)
        if ! docker ps | grep -q "$DB_CONTAINER"; then
            echo -e "${RED}PostgreSQL container not running${NC}"
            exit 1
        fi
        show_performance
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        echo -e "${RED}Unknown option: $1${NC}"
        show_usage
        exit 1
        ;;
esac
