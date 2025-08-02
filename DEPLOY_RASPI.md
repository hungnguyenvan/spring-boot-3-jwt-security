# 🚀 Hướng dẫn Deploy Spring Boot JWT Security lên Raspberry Pi 5

## 🔧 Yêu cầu hệ thống
- Raspberry Pi 5 với Raspberry Pi OS (64-bit)
- RAM: Tối thiểu 4GB (khuyến nghị 8GB)
- Storage: Tối thiểu 16GB SD Card (khuyến nghị 32GB+)
- Network: Kết nối Internet ổn định

## 📝 Các bước thực hiện

### 1. Chuẩn bị Raspberry Pi
```bash
# Cập nhật hệ thống
sudo apt update && sudo apt upgrade -y

# Cài đặt các gói cần thiết
sudo apt install -y openjdk-17-jdk maven git curl wget
```

### 2. Clone code từ repository
```bash
# Clone project
git clone https://github.com/hungnguyenvan/spring-boot-3-jwt-security.git
cd spring-boot-3-jwt-security

# Cấp quyền thực thi cho script
chmod +x start-app.sh
```

### 3. Thiết lập Database

#### Option A: Sử dụng Docker (Khuyến nghị)
```bash
# Cài đặt Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Logout và login lại
logout

# Khởi động PostgreSQL với Docker
docker-compose -f docker-compose-raspi.yml up -d postgres

# Kiểm tra
docker ps
```

#### Option B: Cài đặt trực tiếp
```bash
# Cài đặt PostgreSQL
sudo apt install postgresql postgresql-contrib -y

# Tạo database
sudo -u postgres createuser --interactive
sudo -u postgres createdb jwt_security

# Import schema
sudo -u postgres psql jwt_security < database_schema.sql
```

### 4. Build và chạy ứng dụng
```bash
# Build project
mvn clean compile

# Chạy với production profile
./start-app.sh

# Hoặc chạy thủ công
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 5. Kiểm tra ứng dụng
```bash
# Kiểm tra health
curl http://localhost:8080/actuator/health

# Test API đăng ký
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "Test",
    "lastname": "User",
    "email": "test@example.com",
    "password": "password123",
    "username": "testuser",
    "role": "USER"
  }'
```

## 🌐 Truy cập từ máy khác
```bash
# Lấy IP của Raspberry Pi
hostname -I

# Truy cập từ laptop
http://<raspi-ip>:8080
```

## 🔄 Auto-start khi boot
```bash
# Tạo systemd service
sudo nano /etc/systemd/system/jwt-security.service

# Nội dung file:
[Unit]
Description=Spring Boot JWT Security Application
After=network.target

[Service]
Type=forking
User=pi
ExecStart=/home/pi/spring-boot-3-jwt-security/start-app.sh
Restart=on-failure

[Install]
WantedBy=multi-user.target

# Enable service
sudo systemctl enable jwt-security.service
sudo systemctl start jwt-security.service
```

## 📊 Monitoring
```bash
# Xem logs
tail -f logs/spring-boot-app.log

# Kiểm tra resource usage
htop

# Memory usage
free -h

# Disk usage
df -h
```

## 🔧 Troubleshooting

### Lỗi Out of Memory
```bash
# Tăng swap space
sudo dphys-swapfile swapoff
sudo nano /etc/dphys-swapfile
# Thay đổi CONF_SWAPSIZE=1024
sudo dphys-swapfile setup
sudo dphys-swapfile swapon
```

### Lỗi Port đã sử dụng
```bash
# Tìm process sử dụng port 8080
sudo lsof -i :8080

# Kill process
sudo kill -9 <PID>
```

### Database connection issues
```bash
# Kiểm tra PostgreSQL
sudo systemctl status postgresql

# Restart PostgreSQL
sudo systemctl restart postgresql
```

## 📱 API Testing từ laptop
Sử dụng các file trong thư mục `http/` để test API từ laptop của bạn, chỉ cần thay `localhost` thành IP của Raspberry Pi.

Ví dụ:
```http
POST http://<raspi-ip>:8080/api/v1/auth/register
Content-Type: application/json

{
  "firstname": "Admin",
  "lastname": "User", 
  "email": "admin@example.com",
  "password": "AdminPassword123",
  "username": "admin123",
  "role": "ADMIN"
}
```
