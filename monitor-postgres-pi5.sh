#!/bin/bash

# Script monitoring PostgreSQL trên Pi 5
# File: monitor-postgres-pi5.sh

echo "🔍 PostgreSQL Performance Monitor for Raspberry Pi 5"
echo "=================================================="

# Kiểm tra container status
echo "📊 Container Status:"
docker ps --filter "name=postgres-jwt-pi5" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo

# Resource usage
echo "💾 Resource Usage:"
docker stats postgres-jwt-pi5 --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.NetIO}}\t{{.BlockIO}}"
echo

# Database connections
echo "🔗 Active Connections:"
docker exec postgres-jwt-pi5 psql -U hungcop -d jwt_security -c "
SELECT 
    count(*) as total_connections,
    count(*) FILTER (WHERE state = 'active') as active,
    count(*) FILTER (WHERE state = 'idle') as idle
FROM pg_stat_activity 
WHERE datname = 'jwt_security';"
echo

# Database size
echo "💽 Database Size:"
docker exec postgres-jwt-pi5 psql -U hungcop -d jwt_security -c "
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE schemaname = 'public';"
echo

# System resources on Pi 5
echo "🖥️  Raspberry Pi 5 Resources:"
echo "CPU Usage:"
top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1
echo "Memory Usage:"
free -h | grep "Mem:"
echo "Disk Usage:"
df -h / | tail -1
echo "Temperature:"
vcgencmd measure_temp 2>/dev/null || echo "Temperature monitoring not available"

# PostgreSQL performance stats
echo "⚡ PostgreSQL Performance:"
docker exec postgres-jwt-pi5 psql -U hungcop -d jwt_security -c "
SELECT 
    'Cache Hit Ratio' as metric,
    round(sum(blks_hit)*100.0/sum(blks_hit+blks_read), 2) as value
FROM pg_stat_database 
WHERE datname = 'jwt_security'
UNION ALL
SELECT 
    'Transactions/sec',
    round(sum(xact_commit+xact_rollback)/extract(epoch from (now()-stats_reset)), 2)
FROM pg_stat_database 
WHERE datname = 'jwt_security';"
