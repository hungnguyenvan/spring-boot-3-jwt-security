# PowerShell script để chạy ứng dụng với profile khác nhau
# File: run-app.ps1

param(
    [string]$Profile = "dev"
)

Write-Host "🚀 Starting Spring Boot Application..." -ForegroundColor Green

switch ($Profile.ToLower()) {
    "dev" {
        Write-Host "📱 Running with H2 Database (Development)" -ForegroundColor Cyan
        Write-Host "🔗 H2 Console: http://localhost:8080/h2-console" -ForegroundColor Yellow
        $env:SPRING_PROFILES_ACTIVE = "dev"
    }
    "postgres" {
        Write-Host "🐘 Running with PostgreSQL (Production-like)" -ForegroundColor Cyan
        Write-Host "💾 Database: PostgreSQL on localhost:5432" -ForegroundColor Yellow
        $env:SPRING_PROFILES_ACTIVE = "dev-postgres"
        
        # Kiểm tra PostgreSQL có chạy không
        try {
            $result = docker ps --filter "name=postgres-jwt-dev" --format "table {{.Names}}\t{{.Status}}"
            if ($result -match "postgres-jwt-dev.*Up") {
                Write-Host "✅ PostgreSQL container đang chạy" -ForegroundColor Green
            }
            else {
                Write-Host "⚠️  PostgreSQL container không chạy. Đang khởi động..." -ForegroundColor Yellow
                docker start postgres-jwt-dev
                Start-Sleep -Seconds 5
            }
        }
        catch {
            Write-Host "❌ PostgreSQL chưa được setup. Chạy: .\setup-postgres-dev.ps1" -ForegroundColor Red
            exit 1
        }
    }
    "prod" {
        Write-Host "🏭 Running with Production Profile" -ForegroundColor Cyan
        $env:SPRING_PROFILES_ACTIVE = "prod"
    }
    default {
        Write-Host "❌ Unknown profile: $Profile" -ForegroundColor Red
        Write-Host "📋 Available profiles:" -ForegroundColor Yellow
        Write-Host "  dev      - H2 Database (default)" -ForegroundColor White
        Write-Host "  postgres - PostgreSQL Database" -ForegroundColor White
        Write-Host "  prod     - Production Profile" -ForegroundColor White
        Write-Host "💡 Usage: .\run-app.ps1 -Profile postgres" -ForegroundColor Cyan
        exit 1
    }
}

Write-Host "🔧 Active Profile: $env:SPRING_PROFILES_ACTIVE" -ForegroundColor Green
Write-Host "⏳ Starting application..." -ForegroundColor Yellow

# Chạy ứng dụng
mvn spring-boot:run

Write-Host "👋 Application stopped." -ForegroundColor Yellow
