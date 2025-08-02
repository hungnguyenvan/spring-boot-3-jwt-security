#!/bin/bash

# Quick Database Check Script
# Simple script to quickly view database content

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

DB_CONTAINER="postgres-jwt-optimized"
DB_NAME="jwt_security"
DB_USER="jwt_user"

echo -e "${BLUE}=== Quick Database Check ===${NC}"

# Quick query function
query() {
    docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "$1"
}

# Check if container is running
if ! docker ps | grep -q "$DB_CONTAINER"; then
    echo "❌ PostgreSQL container not running"
    exit 1
fi

echo "✅ PostgreSQL container is running"
echo ""

# Show tables and counts
echo -e "${YELLOW}📊 Table Counts:${NC}"
query "
SELECT 
    'Users' as table_name, COUNT(*) as count FROM _user
UNION ALL
SELECT 
    'Tokens' as table_name, COUNT(*) as count FROM token  
UNION ALL
SELECT 
    'Books' as table_name, COUNT(*) as count FROM book;
"

echo ""
echo -e "${YELLOW}👥 Users:${NC}"
query "SELECT id, firstname, lastname, email, role FROM _user ORDER BY id;"

echo ""
echo -e "${YELLOW}📚 Books:${NC}"
query "SELECT id, author, isbn FROM book ORDER BY id;"

echo ""
echo -e "${YELLOW}🔑 Active Tokens:${NC}"
query "
SELECT 
    t.id,
    u.username,
    t.token_type,
    t.revoked,
    t.expired
FROM token t
JOIN _user u ON t.user_id = u.id
ORDER BY t.id;
"

echo ""
echo -e "${YELLOW}🔢 Sequences:${NC}"
query "
SELECT 
    '_user_id_seq' as seq_name, last_value FROM _user_id_seq
UNION ALL
SELECT 
    'token_id_seq' as seq_name, last_value FROM token_id_seq
UNION ALL
SELECT 
    'book_id_seq' as seq_name, last_value FROM book_id_seq;
"

echo ""
echo -e "${GREEN}✅ Quick check completed!${NC}"
