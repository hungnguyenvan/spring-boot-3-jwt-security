# Script để build và chạy Spring Boot app với Docker trên Windows
# File: run-app-windows.ps1

Write-Host "🚀 Starting Spring Boot JWT Security Application..." -ForegroundColor Green

# Thiết lập Docker paths
$dockerPath = "C:\Program Files\Docker\Docker\resources\bin\docker.exe"
$dockerComposePath = "C:\Program Files\Docker\Docker\resources\bin\docker-compose.exe"

# Kiểm tra Docker
Write-Host "🔍 Checking Docker..." -ForegroundColor Yellow
if (Test-Path $dockerPath) {
    Write-Host "✅ Docker found" -ForegroundColor Green
} else {
    Write-Host "❌ Docker not found. Please install Docker Desktop" -ForegroundColor Red
    exit 1
}

# Kiểm tra Java
Write-Host "🔍 Checking Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "✅ Java found: $($javaVersion[0])" -ForegroundColor Green
} catch {
    Write-Host "❌ Java not found. Please install Java 17+" -ForegroundColor Red
    exit 1
}

# Kiểm tra Maven
Write-Host "🔍 Checking Maven..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version | Select-Object -First 1
    Write-Host "✅ Maven found: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven not found. Please install Maven" -ForegroundColor Red
    exit 1
}

# Start PostgreSQL container
Write-Host "🗄️ Starting PostgreSQL container..." -ForegroundColor Yellow
try {
    & $dockerComposePath -f docker-compose.yml up -d postgres
    Write-Host "✅ PostgreSQL container started" -ForegroundColor Green
    
    # Đợi PostgreSQL khởi động
    Write-Host "⏳ Waiting for PostgreSQL to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
} catch {
    Write-Host "❌ Failed to start PostgreSQL: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Kiểm tra PostgreSQL container
Write-Host "🔍 Checking PostgreSQL status..." -ForegroundColor Yellow
$postgresContainer = & $dockerPath ps --filter "name=postgres-sql" --format "table {{.Names}}\t{{.Status}}"
Write-Host $postgresContainer

# Build application
Write-Host "🔨 Building application..." -ForegroundColor Yellow
try {
    mvn clean compile
    Write-Host "✅ Application compiled successfully" -ForegroundColor Green
} catch {
    Write-Host "❌ Build failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Initialize database (if needed)
Write-Host "🗃️ Checking database..." -ForegroundColor Yellow
if (Test-Path "database_schema.sql") {
    Write-Host "📄 Database schema found - you may need to initialize database manually" -ForegroundColor Yellow
}

# Run application
Write-Host "🚀 Starting Spring Boot application..." -ForegroundColor Green
Write-Host "📍 Application will be available at: http://localhost:8080" -ForegroundColor Cyan
Write-Host "📊 Swagger UI: http://localhost:8080/swagger-ui/index.html" -ForegroundColor Cyan
Write-Host "🗄️ Database: localhost:5432 (user: hungcop, password: hungcop290987)" -ForegroundColor Cyan

try {
    mvn spring-boot:run
} catch {
    Write-Host "❌ Application start failed: $($_.Exception.Message)" -ForegroundColor Red
    
    # Show container logs for debugging
    Write-Host "🔍 PostgreSQL logs:" -ForegroundColor Yellow
    & $dockerPath logs postgres-sql --tail 20
}

Write-Host "👋 Application stopped" -ForegroundColor Yellow
