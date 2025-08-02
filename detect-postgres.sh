#!/bin/bash

# Auto-detect PostgreSQL container script
# This script automatically finds the running PostgreSQL container

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=== PostgreSQL Container Detection ===${NC}"

# Function to find PostgreSQL container
find_postgres_container() {
    # Look for containers with postgres image
    local containers=$(docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}" | grep postgres)
    
    if [ -z "$containers" ]; then
        echo -e "${RED}❌ No running PostgreSQL containers found${NC}"
        echo ""
        echo -e "${YELLOW}Available containers:${NC}"
        docker ps -a --format "table {{.Names}}\t{{.Image}}\t{{.Status}}"
        return 1
    fi
    
    echo -e "${GREEN}✅ Found PostgreSQL containers:${NC}"
    echo "$containers"
    echo ""
    
    # Get just the container name
    local container_name=$(docker ps --format "{{.Names}}" | grep postgres | head -1)
    
    if [ -n "$container_name" ]; then
        echo -e "${GREEN}Using container: ${YELLOW}$container_name${NC}"
        
        # Test connection
        echo -e "${BLUE}Testing connection...${NC}"
        if docker exec "$container_name" pg_isready -U hungcop -d jwt_security > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Database is ready${NC}"
            
            # Show database info
            echo ""
            echo -e "${YELLOW}Database Information:${NC}"
            docker exec "$container_name" psql -U hungcop -d jwt_security -c "
                SELECT 
                    current_database() as database,
                    current_user as user,
                    version() as postgres_version;
            "
            
            # Show table counts
            echo ""
            echo -e "${YELLOW}Table Counts:${NC}"
            docker exec "$container_name" psql -U hungcop -d jwt_security -c "
                SELECT 
                    'Users' as table_name, COUNT(*) as count FROM _user
                UNION ALL
                SELECT 
                    'Tokens' as table_name, COUNT(*) as count FROM token  
                UNION ALL
                SELECT 
                    'Books' as table_name, COUNT(*) as count FROM book;
            "
        else
            echo -e "${RED}❌ Database connection failed${NC}"
        fi
        
        echo ""
        echo -e "${BLUE}To use this container in scripts, update the DB_CONTAINER variable:${NC}"
        echo -e "${YELLOW}DB_CONTAINER=\"$container_name\"${NC}"
        
        return 0
    else
        echo -e "${RED}❌ Could not determine container name${NC}"
        return 1
    fi
}

# Function to update scripts with correct container name
update_scripts() {
    local container_name="$1"
    
    if [ -z "$container_name" ]; then
        echo -e "${RED}No container name provided${NC}"
        return 1
    fi
    
    # Detect database user from container environment
    local db_user=$(docker exec "$container_name" printenv POSTGRES_USER 2>/dev/null || echo "hungcop")
    
    echo -e "${BLUE}=== Updating Scripts ===${NC}"
    echo -e "${YELLOW}Container: $container_name${NC}"
    echo -e "${YELLOW}Database User: $db_user${NC}"
    
    # List of scripts to update
    local scripts=("check-database.sh" "quick-db-check.sh" "monitor-database.sh")
    
    for script in "${scripts[@]}"; do
        if [ -f "$script" ]; then
            echo -e "${YELLOW}Updating $script...${NC}"
            
            # Create backup
            cp "$script" "$script.backup"
            
            # Update container name and user
            sed -i "s/DB_CONTAINER=\".*\"/DB_CONTAINER=\"$container_name\"/" "$script"
            sed -i "s/DB_USER=\".*\"/DB_USER=\"$db_user\"/" "$script"
            
            echo -e "${GREEN}✅ Updated $script${NC}"
        else
            echo -e "${YELLOW}⚠️  Script $script not found${NC}"
        fi
    done
    
    echo ""
    echo -e "${GREEN}✅ All scripts updated with:${NC}"
    echo -e "${CYAN}   Container: $container_name${NC}"
    echo -e "${CYAN}   User: $db_user${NC}"
}

# Main script logic
case "$1" in
    detect|"")
        find_postgres_container
        ;;
    update)
        # Auto-detect and update
        container_name=$(docker ps --format "{{.Names}}" | grep postgres | head -1)
        if [ -n "$container_name" ]; then
            update_scripts "$container_name"
        else
            echo -e "${RED}No PostgreSQL container found to update scripts${NC}"
            exit 1
        fi
        ;;
    update-manual)
        if [ -z "$2" ]; then
            echo "Usage: $0 update-manual <container_name>"
            exit 1
        fi
        update_scripts "$2"
        ;;
    help|--help|-h)
        echo "PostgreSQL Container Detection Script"
        echo ""
        echo "Usage: $0 [option] [container_name]"
        echo ""
        echo "Options:"
        echo "  detect         Detect running PostgreSQL containers (default)"
        echo "  update         Auto-detect and update all database scripts"
        echo "  update-manual  Update scripts with specified container name"
        echo "  help           Show this help"
        echo ""
        echo "Examples:"
        echo "  $0                                    # Detect containers"
        echo "  $0 update                             # Auto-update scripts"
        echo "  $0 update-manual postgres-jwt-optimized  # Manual update"
        ;;
    *)
        echo -e "${RED}Unknown option: $1${NC}"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac
