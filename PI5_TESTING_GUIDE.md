# PI5 TESTING GUIDE - Hướng dẫn kiểm tra chuẩn trên Raspberry Pi 5

## 1. Chuẩn bị trước khi chuyển sang Pi5

### Trên laptop (hiện tại):
```bash
# Đảm bảo JAR đã build thành công
mvn clean package -DskipTests

# Kiểm tra JAR
ls -la target/security-0.0.1-SNAPSHOT.jar

# Copy toàn bộ project sang Pi5
scp -r . hungcop@<PI5_IP>:/home/hungcop/jwt-security-app/
```

## 2. Kiểm tra từng bước trên Pi5

### Bước 1: Kiểm tra cơ bản
```bash
# SSH vào Pi5
ssh hungcop@<PI5_IP>

# Chuyển vào thư mục project
cd /home/hungcop/jwt-security-app

# Kiểm tra files đã copy đủ chưa
ls -la

# Chạy script kiểm tra hệ thống
chmod +x pi5-production-test.sh
./pi5-production-test.sh
```

### Bước 2: Test nhanh các tính năng
```bash
# Chạy ứng dụng thủ công trước
java -Xms512m -Xmx1024m -jar target/security-0.0.1-SNAPSHOT.jar --spring.profiles.active=pi5 &

# Đợi 15-20 giây để app khởi động

# Chạy test tính năng
chmod +x quick-function-test.sh
./quick-function-test.sh
```

### Bước 3: Deployment hoàn chỉnh
```bash
# Dừng app thủ công (nếu đang chạy)
pkill -f "spring-boot"

# Chạy script deployment hoàn chỉnh
chmod +x complete-pi5-deployment.sh
sudo ./complete-pi5-deployment.sh
```

## 3. Kiểm tra kết quả cuối cùng

### A. Kiểm tra service đang chạy:
```bash
sudo systemctl status jwt-security
sudo journalctl -u jwt-security -f
```

### B. Kiểm tra kết nối database:
```bash
sudo -u postgres psql -d jwt_security -c "\dt"
sudo -u postgres psql -d jwt_security -c "SELECT * FROM _user LIMIT 5;"
```

### C. Test API endpoints:

#### 1. Test registration:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "Test",
    "lastname": "User", 
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### 2. Test login:
```bash
curl -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### 3. Test protected endpoint (với token từ login):
```bash
curl -H "Authorization: Bearer <YOUR_TOKEN>" \
  http://localhost:8080/api/v1/management/
```

## 4. Giám sát hiệu suất

### Kiểm tra memory usage:
```bash
# Tổng thể hệ thống
free -h

# Chi tiết cho Java process
ps aux | grep java
top -p $(pgrep java)
```

### Kiểm tra network:
```bash
# Test từ máy khác trong mạng
curl http://<PI5_IP>:8080/

# Kiểm tra port đang listen
netstat -tlnp | grep 8080
```

## 5. Troubleshooting

### Nếu app không start:
```bash
# Xem logs chi tiết
sudo journalctl -u jwt-security -n 100

# Kiểm tra database connection
sudo systemctl status postgresql
sudo -u postgres psql -d jwt_security -c "SELECT 1;"

# Kiểm tra port conflicts
sudo netstat -tlnp | grep 8080
```

### Nếu JWT errors:
```bash
# Kiểm tra hardcoded values trong JAR
jar tf target/security-0.0.1-SNAPSHOT.jar | grep JwtService
mkdir temp && cd temp
jar xf ../target/security-0.0.1-SNAPSHOT.jar BOOT-INF/classes/com/alibou/security/config/JwtService.class
hexdump -C BOOT-INF/classes/com/alibou/security/config/JwtService.class | grep -A5 -B5 "404E635266556A586E"
cd .. && rm -rf temp
```

### Nếu database errors:
```bash
# Reset database
sudo -u postgres dropdb jwt_security
sudo -u postgres createdb jwt_security
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE jwt_security TO hungcop;"

# Restart app
sudo systemctl restart jwt-security
```

## 6. Checklist cuối cùng

- [ ] JAR file build thành công (>50MB)
- [ ] PostgreSQL running và accessible
- [ ] App start without errors
- [ ] Registration endpoint works
- [ ] Login endpoint works
- [ ] JWT tokens generated correctly
- [ ] Protected endpoints accessible with valid token
- [ ] Database operations work (user creation/authentication)
- [ ] Systemd service enabled and auto-start
- [ ] Application accessible from other devices
- [ ] Memory usage under 1GB
- [ ] Response time under 1 second

## 7. Production URLs

Sau khi deploy thành công:
- **Main application**: http://<PI5_IP>:8080/
- **API base**: http://<PI5_IP>:8080/api/v1/
- **Registration**: http://<PI5_IP>:8080/api/v1/auth/register
- **Login**: http://<PI5_IP>:8080/api/v1/auth/authenticate

## 8. Maintenance Commands

```bash
# Start/Stop/Restart
sudo systemctl start jwt-security
sudo systemctl stop jwt-security  
sudo systemctl restart jwt-security

# View logs
sudo journalctl -u jwt-security -f          # Follow logs
sudo journalctl -u jwt-security -n 100      # Last 100 lines
sudo journalctl -u jwt-security --since "1 hour ago"

# Update application
sudo systemctl stop jwt-security
cp new-jar-file.jar /home/hungcop/jwt-security-app/
sudo systemctl start jwt-security
```

Với các script và hướng dẫn này, bạn có thể kiểm tra chuẩn từng bước trên Pi5!
