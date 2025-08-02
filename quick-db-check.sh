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
DB_USER="hungcop"

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
    'User Profiles' as table_name, COUNT(*) as count FROM user_profile
UNION ALL
SELECT 
    'Book Types' as table_name, COUNT(*) as count FROM book_type
UNION ALL
SELECT 
    'Books' as table_name, COUNT(*) as count FROM book
UNION ALL
SELECT 
    'Editor Permissions' as table_name, COUNT(*) as count FROM editor_book_type_permission
UNION ALL
SELECT 
    'Tokens' as table_name, COUNT(*) as count FROM token;
"

echo ""
echo -e "${YELLOW}👥 Users:${NC}"
query "SELECT id, firstname, lastname, email, username, role, locked FROM _user ORDER BY id;"

echo ""
echo -e "${YELLOW}👤 User Profiles:${NC}"
query "
SELECT 
    up.id,
    u.username,
    up.full_name,
    up.phone_number,
    up.city,
    up.country,
    up.activity_status
FROM user_profile up
JOIN _user u ON up.user_id = u.id
ORDER BY up.id;
"

echo ""
echo -e "${YELLOW}📖 Book Types:${NC}"
query "
SELECT 
    id,
    name,
    category,
    active,
    sort_order,
    color_code
FROM book_type 
ORDER BY sort_order, id;
"

echo ""
echo -e "${YELLOW}📚 Books:${NC}"
query "
SELECT 
    b.id,
    b.title,
    b.author,
    bt.name as book_type,
    b.is_free,
    b.downloadable,
    b.download_count,
    b.rating
FROM book b
LEFT JOIN book_type bt ON b.book_type_id = bt.id
ORDER BY b.id;
"

echo ""
echo -e "${YELLOW}🔐 Editor Permissions:${NC}"
query "
SELECT 
    ebtp.id,
    u.username as editor,
    bt.name as book_type,
    ebtp.can_edit,
    ebtp.can_delete,
    ebtp.active
FROM editor_book_type_permission ebtp
JOIN _user u ON ebtp.user_id = u.id
JOIN book_type bt ON ebtp.book_type_id = bt.id
WHERE ebtp.active = true
ORDER BY u.username, bt.name;
"

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
    'user_profile_id_seq' as seq_name, last_value FROM user_profile_id_seq
UNION ALL
SELECT 
    'book_type_id_seq' as seq_name, last_value FROM book_type_id_seq
UNION ALL
SELECT 
    'book_id_seq' as seq_name, last_value FROM book_id_seq
UNION ALL
SELECT 
    'editor_permission_id_seq' as seq_name, last_value FROM editor_permission_id_seq
UNION ALL
SELECT 
    'token_id_seq' as seq_name, last_value FROM token_id_seq;
"

echo ""
echo -e "${YELLOW}📈 Statistics:${NC}"
query "
SELECT 
    'Free Books' as metric, COUNT(*) as value FROM book WHERE is_free = true AND active = true
UNION ALL
SELECT 
    'Paid Books' as metric, COUNT(*) as value FROM book WHERE is_free = false AND active = true
UNION ALL
SELECT 
    'Downloadable Books' as metric, COUNT(*) as value FROM book WHERE downloadable = true AND active = true
UNION ALL
SELECT 
    'Active Book Types' as metric, COUNT(*) as value FROM book_type WHERE active = true
UNION ALL
SELECT 
    'Active User Profiles' as metric, COUNT(*) as value FROM user_profile WHERE activity_status = 'ACTIVE';
"

echo ""
echo -e "${GREEN}✅ Quick check completed!${NC}"
