@echo off
echo 🚀 Quick Start Spring Boot with H2 Database...
echo.
echo ℹ️ This will start the app with in-memory H2 database
echo 📊 H2 Console: http://localhost:8080/h2-console
echo 🌐 API: http://localhost:8080
echo 📖 Swagger: http://localhost:8080/swagger-ui/index.html
echo.

echo 🔨 Building application...
mvn clean compile -q

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Build failed! Check errors above.
    pause
    exit /b 1
)

echo ✅ Build successful!
echo 🚀 Starting application...
mvn spring-boot:run

pause
