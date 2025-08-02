# PowerShell script để setup PostgreSQL cho development
# File: setup-postgres-dev.ps1

Write-Host "🐘 Setting up PostgreSQL for Development..." -ForegroundColor Green

# Kiểm tra Docker
if (Get-Command docker -ErrorAction SilentlyContinue) {
    Write-Host "✅ Docker đã được cài đặt" -ForegroundColor Green
    
    # Kiểm tra Docker đang chạy
    try {
        docker version | Out-Null
        Write-Host "✅ Docker đang chạy" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ Docker không chạy. Vui lòng khởi động Docker Desktop" -ForegroundColor Red
        exit 1
    }
    
    # Chạy PostgreSQL container
    Write-Host "🚀 Khởi động PostgreSQL container..." -ForegroundColor Yellow
    
    docker run --name postgres-jwt-dev `
        -e POSTGRES_USER=hungcop `
        -e POSTGRES_PASSWORD=hungcop290987 `
        -e POSTGRES_DB=jwt_security `
        -p 5432:5432 `
        -d postgres:15-alpine
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ PostgreSQL đã khởi động thành công!" -ForegroundColor Green
        Write-Host "📊 Connection details:" -ForegroundColor Cyan
        Write-Host "  Host: localhost" -ForegroundColor White
        Write-Host "  Port: 5432" -ForegroundColor White
        Write-Host "  Database: jwt_security" -ForegroundColor White
        Write-Host "  Username: hungcop" -ForegroundColor White
        Write-Host "  Password: hungcop290987" -ForegroundColor White
        
        # Đợi PostgreSQL khởi động hoàn toàn
        Write-Host "⏳ Đợi PostgreSQL khởi động hoàn toàn..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
        
        # Import schema
        Write-Host "📄 Import database schema..." -ForegroundColor Yellow
        Get-Content "database_schema.sql" | docker exec -i postgres-jwt-dev psql -U hungcop -d jwt_security
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Schema đã được import thành công!" -ForegroundColor Green
        }
        
        Write-Host "🎉 Setup hoàn tất! Bây giờ bạn có thể:" -ForegroundColor Green
        Write-Host "  1. Đổi application.yml profile thành 'dev-postgres'" -ForegroundColor White
        Write-Host "  2. Chạy: mvn spring-boot:run -Dspring-boot.run.profiles=dev-postgres" -ForegroundColor White
        Write-Host "  3. Hoặc set SPRING_PROFILES_ACTIVE=dev-postgres" -ForegroundColor White
    }
    else {
        Write-Host "❌ Không thể khởi động PostgreSQL container" -ForegroundColor Red
        Write-Host "💡 Có thể container đã tồn tại. Thử chạy:" -ForegroundColor Yellow
        Write-Host "  docker start postgres-jwt-dev" -ForegroundColor White
    }
}
else {
    Write-Host "❌ Docker chưa được cài đặt" -ForegroundColor Red
    Write-Host "💡 Các lựa chọn:" -ForegroundColor Yellow
    Write-Host "  1. Cài Docker Desktop: https://docs.docker.com/desktop/install/windows/" -ForegroundColor White
    Write-Host "  2. Hoặc cài PostgreSQL trực tiếp: https://www.postgresql.org/download/windows/" -ForegroundColor White
    Write-Host "  3. Hoặc tiếp tục dùng H2 database (hiện tại)" -ForegroundColor White
}

# Script dừng PostgreSQL
Write-Host "`n📄 Để dừng PostgreSQL sau này, chạy:" -ForegroundColor Cyan
Write-Host "  docker stop postgres-jwt-dev" -ForegroundColor White
Write-Host "📄 Để khởi động lại:" -ForegroundColor Cyan  
Write-Host "  docker start postgres-jwt-dev" -ForegroundColor White
