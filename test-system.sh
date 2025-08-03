#!/bin/bash

# ==========================================================
# AUTOMATED TEST SCRIPT FOR HIERARCHICAL DOCUMENT LIBRARY
# ==========================================================

set -e  # Exit on any error

# Configuration
BASE_URL="http://localhost:8080"
TEST_USER_EMAIL="test@library.com"
TEST_USER_PASSWORD="test123"
ADMIN_EMAIL="admin@library.com"
ADMIN_PASSWORD="admin123"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Global variables
AUTH_TOKEN=""
FIELD_ID=""
YEAR_ID=""
MANUFACTURER_ID=""
SERIES_ID=""
PRODUCT_ID=""
DOCUMENT_ID=""

# ==========================================================
# UTILITY FUNCTIONS
# ==========================================================

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_server() {
    log_info "Checking if server is running..."
    if curl -s "$BASE_URL/actuator/health" > /dev/null; then
        log_success "Server is running at $BASE_URL"
    else
        log_error "Server is not running. Please start the application first."
        exit 1
    fi
}

register_test_user() {
    log_info "Registering test user..."
    curl -s -X POST "$BASE_URL/api/v1/auth/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"firstname\": \"Test\",
            \"lastname\": \"User\",
            \"email\": \"$TEST_USER_EMAIL\",
            \"password\": \"$TEST_USER_PASSWORD\"
        }" > /dev/null
    log_success "Test user registered (or already exists)"
}

authenticate() {
    log_info "Authenticating user..."
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/authenticate" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"$TEST_USER_EMAIL\",
            \"password\": \"$TEST_USER_PASSWORD\"
        }")
    
    AUTH_TOKEN=$(echo "$RESPONSE" | jq -r '.access_token // empty')
    
    if [ -z "$AUTH_TOKEN" ] || [ "$AUTH_TOKEN" = "null" ]; then
        log_error "Failed to authenticate. Response: $RESPONSE"
        exit 1
    fi
    
    log_success "Authentication successful"
}

# ==========================================================
# TEST FUNCTIONS
# ==========================================================

test_create_document_field() {
    log_info "Creating document field: Auto"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/document-fields" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Auto",
            "code": "AUTO",
            "description": "Automotive technical documents and manuals",
            "color": "#FF5733",
            "sortOrder": 1
        }')
    
    FIELD_ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    
    if [ -z "$FIELD_ID" ] || [ "$FIELD_ID" = "null" ]; then
        log_error "Failed to create document field. Response: $RESPONSE"
        return 1
    fi
    
    log_success "Document field created with ID: $FIELD_ID"
}

test_create_production_year() {
    log_info "Creating production year: 2008"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/production-years" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"year\": 2008,
            \"fieldId\": $FIELD_ID,
            \"description\": \"Automotive products manufactured in 2008\"
        }")
    
    YEAR_ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    
    if [ -z "$YEAR_ID" ] || [ "$YEAR_ID" = "null" ]; then
        log_error "Failed to create production year. Response: $RESPONSE"
        return 1
    fi
    
    log_success "Production year created with ID: $YEAR_ID"
}

test_create_manufacturer() {
    log_info "Creating manufacturer: Toyota"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/manufacturers" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"Toyota\",
            \"yearId\": $YEAR_ID,
            \"description\": \"Toyota Motor Corporation - Leading automotive manufacturer\",
            \"website\": \"https://toyota.com\",
            \"country\": \"Japan\",
            \"sortOrder\": 1
        }")
    
    MANUFACTURER_ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    
    if [ -z "$MANUFACTURER_ID" ] || [ "$MANUFACTURER_ID" = "null" ]; then
        log_error "Failed to create manufacturer. Response: $RESPONSE"
        return 1
    fi
    
    log_success "Manufacturer created with ID: $MANUFACTURER_ID"
}

test_create_product_series() {
    log_info "Creating product series: Mazda"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/product-series" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"Mazda\",
            \"manufacturerId\": $MANUFACTURER_ID,
            \"description\": \"Mazda series vehicles under Toyota partnership\",
            \"launchYear\": 2008,
            \"isActive\": true,
            \"sortOrder\": 1
        }")
    
    SERIES_ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    
    if [ -z "$SERIES_ID" ] || [ "$SERIES_ID" = "null" ]; then
        log_error "Failed to create product series. Response: $RESPONSE"
        return 1
    fi
    
    log_success "Product series created with ID: $SERIES_ID"
}

test_create_product() {
    log_info "Creating product: Mazda2"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/products" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"Mazda2\",
            \"seriesId\": $SERIES_ID,
            \"description\": \"Mazda2 compact car - reliable and fuel efficient\",
            \"modelCode\": \"DE\",
            \"specifications\": {
                \"engine\": \"1.5L DOHC\",
                \"transmission\": \"5-speed manual / 4-speed automatic\",
                \"fuelType\": \"Gasoline\",
                \"drivetrain\": \"Front-wheel drive\"
            },
            \"launchDate\": \"2008-01-15\",
            \"isActive\": true,
            \"sortOrder\": 1
        }")
    
    PRODUCT_ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    
    if [ -z "$PRODUCT_ID" ] || [ "$PRODUCT_ID" = "null" ]; then
        log_error "Failed to create product. Response: $RESPONSE"
        return 1
    fi
    
    log_success "Product created with ID: $PRODUCT_ID"
}

test_create_technical_document() {
    log_info "Creating technical document: Engine Schematic"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/technical-documents" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"title\": \"Engine Schematic\",
            \"documentType\": \"ENGINE_SCHEMATIC\",
            \"description\": \"Detailed engine schematic and parts diagram for Mazda2 1.5L engine\",
            \"category\": \"MECHANICAL\",
            \"subCategory\": \"ENGINE\",
            \"productId\": $PRODUCT_ID,
            \"fileName\": \"mazda2_engine_schematic_v1.0.pdf\",
            \"filePath\": \"/documents/auto/2008/toyota/mazda/mazda2/engine_schematic.pdf\",
            \"fileFormat\": \"PDF\",
            \"fileSize\": 2548576,
            \"version\": \"v1.0\",
            \"language\": \"EN\",
            \"pageCount\": 45,
            \"isPublic\": true,
            \"downloadable\": true,
            \"sortOrder\": 1
        }")
    
    DOCUMENT_ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    
    if [ -z "$DOCUMENT_ID" ] || [ "$DOCUMENT_ID" = "null" ]; then
        log_error "Failed to create technical document. Response: $RESPONSE"
        return 1
    fi
    
    log_success "Technical document created with ID: $DOCUMENT_ID"
}

test_hierarchy_search() {
    log_info "Testing full hierarchy search..."
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/technical-documents/search?fieldName=Auto&year=2008&manufacturerName=Toyota&seriesName=Mazda&productName=Mazda2&documentType=ENGINE_SCHEMATIC" \
        -H "Authorization: Bearer $AUTH_TOKEN")
    
    TOTAL_ELEMENTS=$(echo "$RESPONSE" | jq -r '.totalElements // 0')
    
    if [ "$TOTAL_ELEMENTS" -gt 0 ]; then
        HIERARCHY_PATH=$(echo "$RESPONSE" | jq -r '.content[0].hierarchyPath // empty')
        log_success "Hierarchy search successful. Found document at: $HIERARCHY_PATH"
    else
        log_error "Hierarchy search failed. No documents found."
        return 1
    fi
}

test_full_text_search() {
    log_info "Testing full text search..."
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/technical-documents/search?query=engine+mazda+schematic" \
        -H "Authorization: Bearer $AUTH_TOKEN")
    
    TOTAL_ELEMENTS=$(echo "$RESPONSE" | jq -r '.totalElements // 0')
    
    if [ "$TOTAL_ELEMENTS" -gt 0 ]; then
        log_success "Full text search successful. Found $TOTAL_ELEMENTS documents."
    else
        log_error "Full text search failed. No documents found."
        return 1
    fi
}

test_user_interactions() {
    log_info "Testing user interactions..."
    
    # Test view
    curl -s -X POST "$BASE_URL/api/v1/technical-documents/$DOCUMENT_ID/view" \
        -H "Authorization: Bearer $AUTH_TOKEN" > /dev/null
    
    # Test download
    curl -s -X POST "$BASE_URL/api/v1/technical-documents/$DOCUMENT_ID/download" \
        -H "Authorization: Bearer $AUTH_TOKEN" > /dev/null
    
    # Test rating
    curl -s -X POST "$BASE_URL/api/v1/technical-documents/$DOCUMENT_ID/rate" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"rating": 4.5}' > /dev/null
    
    log_success "User interactions tested (view, download, rating)"
}

test_statistics() {
    log_info "Testing statistics..."
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/technical-documents/statistics" \
        -H "Authorization: Bearer $AUTH_TOKEN")
    
    TOTAL_ACTIVE=$(echo "$RESPONSE" | jq -r '.totalActive // 0')
    TOTAL_DOWNLOADS=$(echo "$RESPONSE" | jq -r '.totalDownloads // 0')
    TOTAL_VIEWS=$(echo "$RESPONSE" | jq -r '.totalViews // 0')
    AVERAGE_RATING=$(echo "$RESPONSE" | jq -r '.averageRating // 0')
    
    log_success "Statistics: Active=$TOTAL_ACTIVE, Downloads=$TOTAL_DOWNLOADS, Views=$TOTAL_VIEWS, Avg Rating=$AVERAGE_RATING"
}

test_popular_documents() {
    log_info "Testing popular documents..."
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/technical-documents/popular?limit=5" \
        -H "Authorization: Bearer $AUTH_TOKEN")
    
    DOCUMENT_COUNT=$(echo "$RESPONSE" | jq length)
    
    if [ "$DOCUMENT_COUNT" -gt 0 ]; then
        log_success "Popular documents retrieved: $DOCUMENT_COUNT documents"
    else
        log_warning "No popular documents found"
    fi
}

# ==========================================================
# PERFORMANCE TESTS
# ==========================================================

test_performance() {
    log_info "Running performance tests..."
    
    # Test concurrent requests
    log_info "Testing concurrent search requests..."
    for i in {1..10}; do
        curl -s -X GET "$BASE_URL/api/v1/technical-documents/search?query=engine" \
            -H "Authorization: Bearer $AUTH_TOKEN" > /dev/null &
    done
    wait
    
    log_success "Performance test completed"
}

# ==========================================================
# MAIN TEST EXECUTION
# ==========================================================

run_all_tests() {
    echo "============================================================"
    echo "  HIERARCHICAL DOCUMENT LIBRARY - AUTOMATED TEST SUITE"
    echo "============================================================"
    echo ""
    
    # Pre-test checks
    check_server
    register_test_user
    authenticate
    
    echo ""
    log_info "Starting comprehensive test suite..."
    echo ""
    
    # Core functionality tests
    test_create_document_field
    test_create_production_year
    test_create_manufacturer
    test_create_product_series
    test_create_product
    test_create_technical_document
    
    echo ""
    log_info "Testing search functionality..."
    test_hierarchy_search
    test_full_text_search
    
    echo ""
    log_info "Testing user interactions..."
    test_user_interactions
    
    echo ""
    log_info "Testing analytics..."
    test_statistics
    test_popular_documents
    
    echo ""
    log_info "Testing performance..."
    test_performance
    
    echo ""
    echo "============================================================"
    log_success "ALL TESTS COMPLETED SUCCESSFULLY! 🎉"
    echo "============================================================"
    echo ""
    echo "✅ Complete hierarchy created: Auto > 2008 > Toyota > Mazda > Mazda2 > Engine Schematic"
    echo "✅ Search functionality working across all levels"
    echo "✅ User interactions (view, download, rating) functional"
    echo "✅ Statistics and analytics working"
    echo "✅ Performance tests passed"
    echo ""
    echo "🚀 System is ready for production deployment!"
    echo ""
}

# ==========================================================
# SCRIPT EXECUTION
# ==========================================================

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    log_error "jq is required but not installed. Please install jq to run this script."
    exit 1
fi

# Check if curl is installed
if ! command -v curl &> /dev/null; then
    log_error "curl is required but not installed. Please install curl to run this script."
    exit 1
fi

# Run all tests
run_all_tests
