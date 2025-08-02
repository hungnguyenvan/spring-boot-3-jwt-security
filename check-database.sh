#!/bin/bash

# Database Content Checker Script for Spring Boot JWT Security
# Usage: ./check-database.sh [option]

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Database connection settings
DB_CONTAINER="postgres-container"
DB_NAME="jwt_security"
DB_USER="jwt_user"
DB_HOST="localhost"
DB_PORT="5432"

# Function to check if PostgreSQL container is running
check_container() {
    if ! docker ps | grep -q "$DB_CONTAINER"; then
        echo -e "${RED}PostgreSQL container '$DB_CONTAINER' is not running!${NC}"
        echo -e "${YELLOW}Start it with: docker-compose up -d${NC}"
        exit 1
    fi
    echo -e "${GREEN}PostgreSQL container is running${NC}"
}

# Function to execute SQL query
execute_sql() {
    local query="$1"
    docker exec -it "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "$query"
}

# Function to execute SQL query without interactive mode (for scripting)
execute_sql_quiet() {
    local query="$1"
    docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "$query"
}

# Function to show database overview
show_overview() {
    echo -e "${BLUE}=== DATABASE OVERVIEW ===${NC}"
    echo -e "${CYAN}Database: $DB_NAME${NC}"
    echo -e "${CYAN}User: $DB_USER${NC}"
    echo -e "${CYAN}Container: $DB_CONTAINER${NC}"
    echo ""
    
    echo -e "${YELLOW}Database size:${NC}"
    execute_sql_quiet "SELECT pg_size_pretty(pg_database_size('$DB_NAME')) as database_size;"
    echo ""
}

# Function to list all tables
show_tables() {
    echo -e "${BLUE}=== TABLES IN DATABASE ===${NC}"
    execute_sql_quiet "
        SELECT 
            schemaname,
            tablename,
            tableowner,
            pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
        FROM pg_tables 
        WHERE schemaname = 'public'
        ORDER BY tablename;
    "
    echo ""
}

# Function to show table structures
show_structure() {
    echo -e "${BLUE}=== TABLE STRUCTURES ===${NC}"
    
    for table in "_user" "token" "book"; do
        echo -e "${YELLOW}Structure of table: $table${NC}"
        execute_sql_quiet "
            SELECT 
                column_name,
                data_type,
                character_maximum_length,
                is_nullable,
                column_default
            FROM information_schema.columns 
            WHERE table_name = '$table' 
                AND table_schema = 'public'
            ORDER BY ordinal_position;
        "
        echo ""
    done
}

# Function to show sequences
show_sequences() {
    echo -e "${BLUE}=== SEQUENCES ===${NC}"
    execute_sql_quiet "
        SELECT 
            sequence_name,
            start_value,
            increment_by,
            max_value,
            min_value,
            last_value
        FROM information_schema.sequences 
        WHERE sequence_schema = 'public'
        ORDER BY sequence_name;
    "
    echo ""
    
    echo -e "${YELLOW}Current sequence values:${NC}"
    execute_sql_quiet "
        SELECT 
            'user_id_seq' as sequence_name,
            last_value,
            is_called 
        FROM _user_id_seq
        UNION ALL
        SELECT 
            'token_id_seq' as sequence_name,
            last_value,
            is_called 
        FROM token_id_seq
        UNION ALL
        SELECT 
            'book_id_seq' as sequence_name,
            last_value,
            is_called 
        FROM book_id_seq;
    "
    echo ""
}

# Function to show data counts
show_counts() {
    echo -e "${BLUE}=== DATA COUNTS ===${NC}"
    
    echo -e "${YELLOW}Record counts:${NC}"
    execute_sql_quiet "
        SELECT 
            'Users' as table_name,
            COUNT(*) as record_count
        FROM _user
        UNION ALL
        SELECT 
            'Tokens' as table_name,
            COUNT(*) as record_count
        FROM token
        UNION ALL
        SELECT 
            'Books' as table_name,
            COUNT(*) as record_count
        FROM book;
    "
    echo ""
}

# Function to show user data
show_users() {
    echo -e "${BLUE}=== USER DATA ===${NC}"
    execute_sql_quiet "
        SELECT 
            id,
            firstname,
            lastname,
            email,
            username,
            role,
            locked,
            created_date,
            last_modified_date
        FROM _user
        ORDER BY id;
    "
    echo ""
}

# Function to show token data
show_tokens() {
    echo -e "${BLUE}=== TOKEN DATA ===${NC}"
    execute_sql_quiet "
        SELECT 
            t.id,
            LEFT(t.token, 50) || '...' as token_preview,
            t.token_type,
            t.revoked,
            t.expired,
            u.username as user_name
        FROM token t
        LEFT JOIN _user u ON t.user_id = u.id
        ORDER BY t.id;
    "
    echo ""
}

# Function to show book data
show_books() {
    echo -e "${BLUE}=== BOOK DATA ===${NC}"
    execute_sql_quiet "
        SELECT 
            id,
            author,
            isbn,
            created_date,
            last_modified_date,
            created_by,
            last_modified_by
        FROM book
        ORDER BY id;
    "
    echo ""
}

# Function to show foreign key relationships
show_relationships() {
    echo -e "${BLUE}=== FOREIGN KEY RELATIONSHIPS ===${NC}"
    execute_sql_quiet "
        SELECT
            tc.table_name,
            kcu.column_name,
            ccu.table_name AS foreign_table_name,
            ccu.column_name AS foreign_column_name
        FROM
            information_schema.table_constraints AS tc
            JOIN information_schema.key_column_usage AS kcu
              ON tc.constraint_name = kcu.constraint_name
              AND tc.table_schema = kcu.table_schema
            JOIN information_schema.constraint_column_usage AS ccu
              ON ccu.constraint_name = tc.constraint_name
              AND ccu.table_schema = tc.table_schema
        WHERE tc.constraint_type = 'FOREIGN KEY'
            AND tc.table_schema = 'public'
        ORDER BY tc.table_name;
    "
    echo ""
}

# Function to show indexes
show_indexes() {
    echo -e "${BLUE}=== INDEXES ===${NC}"
    execute_sql_quiet "
        SELECT
            schemaname,
            tablename,
            indexname,
            indexdef
        FROM pg_indexes
        WHERE schemaname = 'public'
        ORDER BY tablename, indexname;
    "
    echo ""
}

# Function to test application connectivity
test_connectivity() {
    echo -e "${BLUE}=== CONNECTIVITY TEST ===${NC}"
    
    # Test container connectivity
    echo -e "${YELLOW}Testing container connectivity...${NC}"
    if docker exec "$DB_CONTAINER" pg_isready -U "$DB_USER" -d "$DB_NAME"; then
        echo -e "${GREEN}Database is ready and accepting connections${NC}"
    else
        echo -e "${RED}Database is not ready${NC}"
        return 1
    fi
    
    # Test from host
    echo -e "${YELLOW}Testing from host...${NC}"
    if command -v psql > /dev/null; then
        if PGPASSWORD="$DB_USER" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" > /dev/null 2>&1; then
            echo -e "${GREEN}Can connect from host${NC}"
        else
            echo -e "${RED}Cannot connect from host${NC}"
        fi
    else
        echo -e "${YELLOW}psql not installed on host, skipping host connectivity test${NC}"
    fi
    
    # Test Spring Boot health endpoint
    echo -e "${YELLOW}Testing Spring Boot health endpoint...${NC}"
    if curl -s -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}Spring Boot application is responding${NC}"
    else
        echo -e "${RED}Spring Boot application is not responding${NC}"
    fi
    echo ""
}

# Function to run custom SQL query
run_custom_query() {
    echo -e "${BLUE}=== CUSTOM SQL QUERY ===${NC}"
    echo -e "${YELLOW}Enter your SQL query (type 'exit' to quit):${NC}"
    
    while true; do
        read -p "SQL> " query
        if [ "$query" = "exit" ]; then
            break
        fi
        if [ -n "$query" ]; then
            execute_sql "$query"
        fi
    done
}

# Function to export data
export_data() {
    echo -e "${BLUE}=== EXPORTING DATA ===${NC}"
    
    local backup_dir="./db_backups"
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    
    mkdir -p "$backup_dir"
    
    echo -e "${YELLOW}Exporting database schema and data...${NC}"
    
    # Export schema
    docker exec "$DB_CONTAINER" pg_dump -U "$DB_USER" -d "$DB_NAME" --schema-only > "$backup_dir/schema_$timestamp.sql"
    
    # Export data
    docker exec "$DB_CONTAINER" pg_dump -U "$DB_USER" -d "$DB_NAME" --data-only > "$backup_dir/data_$timestamp.sql"
    
    # Export full backup
    docker exec "$DB_CONTAINER" pg_dump -U "$DB_USER" -d "$DB_NAME" > "$backup_dir/full_backup_$timestamp.sql"
    
    echo -e "${GREEN}Backup files created:${NC}"
    echo -e "${CYAN}Schema: $backup_dir/schema_$timestamp.sql${NC}"
    echo -e "${CYAN}Data: $backup_dir/data_$timestamp.sql${NC}"
    echo -e "${CYAN}Full: $backup_dir/full_backup_$timestamp.sql${NC}"
    echo ""
}

# Function to show usage
show_usage() {
    echo -e "${BLUE}Database Content Checker${NC}"
    echo ""
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  overview     Show database overview"
    echo "  tables       List all tables"
    echo "  structure    Show table structures"
    echo "  sequences    Show sequences"
    echo "  counts       Show record counts"
    echo "  users        Show user data"
    echo "  tokens       Show token data"
    echo "  books        Show book data"
    echo "  relationships Show foreign key relationships"
    echo "  indexes      Show indexes"
    echo "  connectivity Test database connectivity"
    echo "  query        Run custom SQL queries"
    echo "  export       Export database data"
    echo "  all          Show all information"
    echo "  help         Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 all                 # Show complete database analysis"
    echo "  $0 users               # Show only user data"
    echo "  $0 connectivity        # Test database connections"
    echo "  $0 query               # Interactive SQL mode"
}

# Function to show all information
show_all() {
    check_container
    show_overview
    show_tables
    show_structure
    show_sequences
    show_counts
    show_relationships
    show_indexes
    show_users
    show_tokens
    show_books
    test_connectivity
}

# Main script logic
case "$1" in
    overview)
        check_container
        show_overview
        ;;
    tables)
        check_container
        show_tables
        ;;
    structure)
        check_container
        show_structure
        ;;
    sequences)
        check_container
        show_sequences
        ;;
    counts)
        check_container
        show_counts
        ;;
    users)
        check_container
        show_users
        ;;
    tokens)
        check_container
        show_tokens
        ;;
    books)
        check_container
        show_books
        ;;
    relationships)
        check_container
        show_relationships
        ;;
    indexes)
        check_container
        show_indexes
        ;;
    connectivity)
        check_container
        test_connectivity
        ;;
    query)
        check_container
        run_custom_query
        ;;
    export)
        check_container
        export_data
        ;;
    all)
        show_all
        ;;
    help|--help|-h)
        show_usage
        ;;
    "")
        show_usage
        ;;
    *)
        echo -e "${RED}Unknown option: $1${NC}"
        echo ""
        show_usage
        exit 1
        ;;
esac

exit 0
