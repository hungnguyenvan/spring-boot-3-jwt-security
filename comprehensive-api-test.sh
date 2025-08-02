#!/bin/bash

# Complete API Testing Script for Spring Boot JWT Security
# File: comprehensive-api-test.sh

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
NC='\033[0m'

BASE_URL="http://localhost:8080"
TOKEN=""
CURRENT_USER_EMAIL=""

echo -e "${BLUE}=== Spring Boot JWT Security - Complete API Testing ===${NC}"

# Helper function to make authenticated requests
auth_request() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    
    if [ -z "$TOKEN" ]; then
        echo -e "${RED}❌ No token available. Please authenticate first.${NC}"
        return 1
    fi
    
    if [ -n "$data" ]; then
        curl -s -X "$method" "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data"
    else
        curl -s -X "$method" "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json"
    fi
}

# 1. AUTHENTICATION APIS
test_register() {
    echo -e "${YELLOW}🔐 Testing User Registration...${NC}"
    
    read -p "First name: " fname
    read -p "Last name: " lname
    read -p "Email: " email
    read -p "Password: " password
    
    local response=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"firstname\":\"$fname\",\"lastname\":\"$lname\",\"email\":\"$email\",\"password\":\"$password\"}")
    
    echo "Response: $response"
    echo ""
}

test_authenticate() {
    local email="$1"
    local password="$2"
    
    if [ -z "$email" ]; then
        read -p "Email: " email
        read -p "Password: " password
    fi
    
    echo -e "${YELLOW}🔑 Authenticating: $email${NC}"
    
    local response=$(curl -s -X POST "$BASE_URL/api/v1/auth/authenticate" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$email\",\"password\":\"$password\"}")
    
    echo "Response: $response"
    
    TOKEN=$(echo "$response" | jq -r '.access_token // .token // empty' 2>/dev/null)
    
    if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        echo -e "${GREEN}✅ Authentication successful${NC}"
        echo "Token: ${TOKEN:0:50}..."
        CURRENT_USER_EMAIL="$email"
    else
        echo -e "${RED}❌ Authentication failed${NC}"
    fi
    echo ""
}

test_refresh_token() {
    echo -e "${YELLOW}🔄 Testing Token Refresh...${NC}"
    
    curl -s -X POST "$BASE_URL/api/v1/auth/refresh-token" \
        -H "Authorization: Bearer $TOKEN"
    echo ""
}

test_reset_password() {
    echo -e "${YELLOW}🔒 Testing Password Reset...${NC}"
    
    read -p "Email to reset: " email
    read -p "New password: " new_password
    
    local response=$(curl -s -X POST "$BASE_URL/api/v1/auth/reset-password" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$email\",\"newPassword\":\"$new_password\"}")
    
    echo "Response: $response"
    echo ""
}

test_delete_user() {
    echo -e "${YELLOW}❌ Testing User Deletion (Admin only)...${NC}"
    
    read -p "Email to delete: " email
    
    local response=$(auth_request "POST" "/api/v1/auth/delete-user" "{\"email\":\"$email\"}")
    echo "Response: $response"
    echo ""
}

# 2. USER MANAGEMENT APIS
test_change_password() {
    echo -e "${YELLOW}🔐 Testing Change Password...${NC}"
    
    read -p "Current password: " current_pass
    read -p "New password: " new_pass
    read -p "Confirm new password: " confirm_pass
    
    local response=$(auth_request "PATCH" "/api/v1/users" \
        "{\"currentPassword\":\"$current_pass\",\"newPassword\":\"$new_pass\",\"confirmationPassword\":\"$confirm_pass\"}")
    
    echo "Response: $response"
    echo ""
}

test_get_users() {
    echo -e "${YELLOW}👥 Testing Get Users...${NC}"
    
    read -p "Search term (optional): " search
    read -p "Page (default 0): " page
    read -p "Size (default 10): " size
    
    page=${page:-0}
    size=${size:-10}
    
    local endpoint="/api/v1/users?page=$page&size=$size"
    if [ -n "$search" ]; then
        endpoint="$endpoint&search=$search"
    fi
    
    local response=$(auth_request "GET" "$endpoint")
    echo "Response: $response" | jq '.' 2>/dev/null || echo "$response"
    echo ""
}

test_update_user_role() {
    echo -e "${YELLOW}👑 Testing Update User Role...${NC}"
    
    read -p "User ID: " user_id
    read -p "New role (USER/EDITOR/ADMIN): " role_name
    
    local response=$(auth_request "PUT" "/api/v1/users/role" \
        "{\"userId\":$user_id,\"roleName\":\"$role_name\"}")
    
    echo "Response: $response"
    echo ""
}

test_lock_user() {
    echo -e "${YELLOW}🔒 Testing Lock/Unlock User (Admin only)...${NC}"
    
    read -p "User ID: " user_id
    read -p "Lock user? (true/false): " lock_status
    
    local response=$(auth_request "PATCH" "/api/v1/users/lock" \
        "{\"userId\":$user_id,\"lock\":$lock_status}")
    
    echo "Response: $response"
    echo ""
}

# 3. BOOK MANAGEMENT APIS
test_create_book() {
    echo -e "${YELLOW}📚 Testing Create Book...${NC}"
    
    read -p "Author: " author
    read -p "ISBN: " isbn
    
    local response=$(auth_request "POST" "/api/v1/books" \
        "{\"author\":\"$author\",\"isbn\":\"$isbn\"}")
    
    echo "Response: $response"
    echo ""
}

test_get_books() {
    echo -e "${YELLOW}📖 Testing Get All Books...${NC}"
    
    local response=$(auth_request "GET" "/api/v1/books")
    echo "Response: $response" | jq '.' 2>/dev/null || echo "$response"
    echo ""
}

# 4. DEMO/TESTING APIS
test_demo_controller() {
    echo -e "${YELLOW}🎯 Testing Demo Controller...${NC}"
    
    local response=$(auth_request "GET" "/api/v1/demo-controller")
    echo "Response: $response"
    echo ""
}

test_admin_controller() {
    echo -e "${YELLOW}👨‍💼 Testing Admin Controller (Admin only)...${NC}"
    
    echo "GET:"
    auth_request "GET" "/api/v1/admin"
    echo ""
    
    echo "POST:"
    auth_request "POST" "/api/v1/admin"
    echo ""
    
    echo "PUT:"
    auth_request "PUT" "/api/v1/admin"
    echo ""
    
    echo "DELETE:"
    auth_request "DELETE" "/api/v1/admin"
    echo ""
}

test_management_controller() {
    echo -e "${YELLOW}🏢 Testing Management Controller (Editor/Admin)...${NC}"
    
    echo "GET:"
    auth_request "GET" "/api/v1/management"
    echo ""
    
    echo "POST:"
    auth_request "POST" "/api/v1/management"
    echo ""
    
    echo "PUT:"
    auth_request "PUT" "/api/v1/management"
    echo ""
    
    echo "DELETE:"
    auth_request "DELETE" "/api/v1/management"
    echo ""
}

# 5. SYSTEM HEALTH APIS
test_health() {
    echo -e "${YELLOW}🏥 Testing Health Endpoint...${NC}"
    
    curl -s "$BASE_URL/actuator/health" | jq '.' 2>/dev/null || curl -s "$BASE_URL/actuator/health"
    echo ""
}

test_actuator_info() {
    echo -e "${YELLOW}ℹ️ Testing Actuator Info...${NC}"
    
    curl -s "$BASE_URL/actuator/info" | jq '.' 2>/dev/null || curl -s "$BASE_URL/actuator/info"
    echo ""
}

# 6. BATCH TESTING
test_all_endpoints() {
    echo -e "${PURPLE}🚀 Running All Endpoint Tests...${NC}"
    
    # System health (no auth needed)
    test_health
    test_actuator_info
    
    if [ -z "$TOKEN" ]; then
        echo -e "${RED}❌ No authentication token. Please authenticate first.${NC}"
        return 1
    fi
    
    # Basic protected endpoints
    test_demo_controller
    test_get_books
    test_get_users
    
    # Role-specific endpoints
    echo -e "${CYAN}Testing role-specific endpoints...${NC}"
    test_management_controller
    test_admin_controller
    
    echo -e "${GREEN}✅ Batch testing completed!${NC}"
}

# Quick authentication presets
auth_admin() {
    test_authenticate "admin@mail.com" "password"
}

auth_editor() {
    test_authenticate "editor@mail.com" "password"
}

auth_user() {
    test_authenticate "user@mail.com" "password"
}

# Show current status
show_status() {
    echo -e "${BLUE}=== Current Status ===${NC}"
    if [ -n "$TOKEN" ]; then
        echo -e "${GREEN}✅ Authenticated as: $CURRENT_USER_EMAIL${NC}"
        echo -e "${CYAN}Token: ${TOKEN:0:30}...${NC}"
    else
        echo -e "${RED}❌ Not authenticated${NC}"
    fi
    echo ""
}

# Main menu
show_menu() {
    echo ""
    echo -e "${BLUE}=== API Test Menu ===${NC}"
    echo ""
    echo -e "${YELLOW}Authentication:${NC}"
    echo "1.  Register New User"
    echo "2.  Login as Admin"
    echo "3.  Login as Editor"
    echo "4.  Login as User"
    echo "5.  Custom Login"
    echo "6.  Refresh Token"
    echo "7.  Reset Password"
    echo "8.  Delete User (Admin)"
    echo ""
    echo -e "${YELLOW}User Management:${NC}"
    echo "9.  Change Password"
    echo "10. Get Users List"
    echo "11. Update User Role"
    echo "12. Lock/Unlock User"
    echo ""
    echo -e "${YELLOW}Book Management:${NC}"
    echo "13. Create Book"
    echo "14. Get All Books"
    echo ""
    echo -e "${YELLOW}Demo/Testing:${NC}"
    echo "15. Demo Controller"
    echo "16. Admin Controller"
    echo "17. Management Controller"
    echo ""
    echo -e "${YELLOW}System:${NC}"
    echo "18. Health Check"
    echo "19. Actuator Info"
    echo ""
    echo -e "${YELLOW}Batch Testing:${NC}"
    echo "20. Test All Endpoints"
    echo ""
    echo -e "${YELLOW}Utilities:${NC}"
    echo "21. Show Status"
    echo "22. Clear Token"
    echo "0.  Exit"
    echo ""
}

# Interactive mode
while true; do
    show_status
    show_menu
    read -p "Choose option (0-22): " choice
    
    case $choice in
        1) test_register ;;
        2) auth_admin ;;
        3) auth_editor ;;
        4) auth_user ;;
        5) test_authenticate ;;
        6) test_refresh_token ;;
        7) test_reset_password ;;
        8) test_delete_user ;;
        9) test_change_password ;;
        10) test_get_users ;;
        11) test_update_user_role ;;
        12) test_lock_user ;;
        13) test_create_book ;;
        14) test_get_books ;;
        15) test_demo_controller ;;
        16) test_admin_controller ;;
        17) test_management_controller ;;
        18) test_health ;;
        19) test_actuator_info ;;
        20) test_all_endpoints ;;
        21) show_status ;;
        22) TOKEN=""; CURRENT_USER_EMAIL=""; echo "Token cleared" ;;
        0) echo "Exiting..."; break ;;
        *) echo "Invalid option" ;;
    esac
done
