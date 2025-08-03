@echo off
echo 🚀 Starting Spring Boot JWT Security Application...

REM Start PostgreSQL
echo 🗄️ Starting PostgreSQL...
docker-compose -f docker-compose.yml up -d postgres

REM Wait for PostgreSQL
echo ⏳ Waiting for PostgreSQL to start...
timeout /t 10 /nobreak > nul

REM Build and run application
echo 🔨 Building and running application...
mvn spring-boot:run

pause
