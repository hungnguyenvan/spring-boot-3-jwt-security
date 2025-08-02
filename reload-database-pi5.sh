#!/bin/bash

# Script để reload database với schema đã fix
# File: reload-database-pi5.sh

echo "🔄 Reloading PostgreSQL database with corrected schema..."

# Stop và remove container cũ
echo "Stopping and removing old PostgreSQL container..."
docker stop postgres-jwt-optimized 2>/dev/null || echo "Container not running"
docker rm postgres-jwt-optimized 2>/dev/null || echo "Container not found"

# Xóa data cũ
echo "Cleaning old data..."
sudo rm -rf /opt/docker-data/postgres/*
sudo chown hungcop:hungcop /opt/docker-data/postgres

# Start container mới với schema đã fix
echo "Starting fresh PostgreSQL container..."
docker run -d \
  --name postgres-jwt-optimized \
  -e POSTGRES_USER=hungcop \
  -e POSTGRES_PASSWORD=hungcop290987 \
  -e POSTGRES_DB=jwt_security \
  -v /opt/docker-data/postgres:/var/lib/postgresql/data \
  -v $(pwd)/database_schema.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro \
  -p 5432:5432 \
  --restart unless-stopped \
  postgres:15-alpine

echo "⏳ Waiting for PostgreSQL to initialize..."
sleep 20

# Kiểm tra container status
echo "📊 Container status:"
docker ps | grep postgres-jwt-optimized

# Kiểm tra PostgreSQL ready
echo "🔍 Checking PostgreSQL readiness..."
for i in {1..30}; do
    if docker exec postgres-jwt-optimized pg_isready -U hungcop -d jwt_security > /dev/null 2>&1; then
        echo "✅ PostgreSQL is ready"
        break
    fi
    echo "⏳ Waiting... ($i/30)"
    sleep 2
done

# Kiểm tra schema
echo "📋 Verifying database schema..."
docker exec postgres-jwt-optimized psql -U hungcop -d jwt_security -c "\dt"

echo ""
echo "👥 Checking users table structure:"
docker exec postgres-jwt-optimized psql -U hungcop -d jwt_security -c "\d _user"

echo ""
echo "📚 Checking books table structure:"
docker exec postgres-jwt-optimized psql -U hungcop -d jwt_security -c "\d book"

echo ""
echo "📊 Checking sample data:"
docker exec postgres-jwt-optimized psql -U hungcop -d jwt_security -c "SELECT COUNT(*) as users FROM _user;"
docker exec postgres-jwt-optimized psql -U hungcop -d jwt_security -c "SELECT COUNT(*) as books FROM book;"

echo ""
echo "✅ Database reloaded with corrected schema!"
echo "🚀 Now you can run the Spring Boot application:"
echo "   export SPRING_PROFILES_ACTIVE=pi5"
echo "   mvn spring-boot:run -Dspring-boot.run.profiles=pi5"
