#!/bin/bash

# Script quản lý PostgreSQL Docker trên Pi 5
# File: manage-postgres-pi5.sh

case "$1" in
    start)
        echo "🚀 Starting PostgreSQL..."
        docker-compose up -d postgres
        echo "✅ PostgreSQL started"
        ;;
    stop)
        echo "🛑 Stopping PostgreSQL..."
        docker-compose stop postgres
        echo "✅ PostgreSQL stopped"
        ;;
    restart)
        echo "🔄 Restarting PostgreSQL..."
        docker-compose restart postgres
        echo "✅ PostgreSQL restarted"
        ;;
    status)
        echo "📊 PostgreSQL Status:"
        docker-compose ps postgres
        ;;
    logs)
        echo "📄 PostgreSQL Logs:"
        docker-compose logs -f postgres
        ;;
    backup)
        echo "💾 Creating backup..."
        BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
        docker exec postgres-jwt-pi5 pg_dump -U hungcop jwt_security > "backups/$BACKUP_FILE"
        echo "✅ Backup created: backups/$BACKUP_FILE"
        ;;
    restore)
        if [ -z "$2" ]; then
            echo "❌ Usage: $0 restore <backup_file>"
            exit 1
        fi
        echo "🔄 Restoring from backup: $2"
        docker exec -i postgres-jwt-pi5 psql -U hungcop jwt_security < "$2"
        echo "✅ Restore completed"
        ;;
    shell)
        echo "🐚 Connecting to PostgreSQL shell..."
        docker exec -it postgres-jwt-pi5 psql -U hungcop -d jwt_security
        ;;
    clean)
        echo "🧹 Cleaning up old containers and images..."
        docker system prune -f
        docker volume prune -f
        echo "✅ Cleanup completed"
        ;;
    admin)
        echo "🌐 Starting pgAdmin..."
        docker-compose --profile admin up -d pgadmin
        echo "✅ pgAdmin started at http://$(hostname -I | awk '{print $1}'):5050"
        echo "📝 Login: admin@admin.com / admin123"
        ;;
    monitor)
        ./monitor-postgres-pi5.sh
        ;;
    *)
        echo "📋 PostgreSQL Management Script for Raspberry Pi 5"
        echo "Usage: $0 {start|stop|restart|status|logs|backup|restore|shell|clean|admin|monitor}"
        echo ""
        echo "Commands:"
        echo "  start    - Start PostgreSQL container"
        echo "  stop     - Stop PostgreSQL container"
        echo "  restart  - Restart PostgreSQL container"
        echo "  status   - Show container status"
        echo "  logs     - Show PostgreSQL logs"
        echo "  backup   - Create database backup"
        echo "  restore  - Restore from backup file"
        echo "  shell    - Connect to PostgreSQL shell"
        echo "  clean    - Clean up Docker system"
        echo "  admin    - Start pgAdmin web interface"
        echo "  monitor  - Show performance statistics"
        exit 1
        ;;
esac
