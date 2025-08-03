# Xử lý Lỗi Broken Pipe trên Raspberry Pi 5

## 🔍 Nguyên nhân lỗi

Lỗi "Broken pipe" xuất hiện khi:
- Output stream của Maven bị ngắt kết nối đột ngột
- Terminal session bị terminate trong quá trình build
- Memory hoặc CPU overload trên Pi5

## 🛠️ Giải pháp

### 1. Sử dụng Maven Wrapper tối ưu

```bash
# Chạy script tối ưu Maven
chmod +x mvn-pi5.sh
./mvn-pi5.sh clean compile
```

### 2. Sử dụng Safe Maven Script

```bash
# Tạo các script tối ưu
chmod +x fix-maven-pi5.sh
./fix-maven-pi5.sh

# Sử dụng safe Maven wrapper
./maven-safe.sh clean package -DskipTests
```

### 3. Chạy trong Screen Session

```bash
# Cài đặt screen
sudo apt install screen -y

# Tạo screen session
screen -S spring-boot

# Chạy application trong screen
./run-on-pi5.sh

# Detach: Ctrl+A, D
# Reattach: screen -r spring-boot
```

### 4. Sử dụng Systemd Service

```bash
# Cài đặt và chạy service
./start-app-pi5.sh

# Kiểm tra status
sudo systemctl status spring-boot-jwt

# Xem logs
sudo journalctl -u spring-boot-jwt -f
```

### 5. Manual Build với Error Handling

```bash
# Set environment variables
export MAVEN_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -Djava.awt.headless=true"
export _JAVA_OPTIONS="-Djava.awt.headless=true"

# Build với batch mode
mvn --batch-mode --no-transfer-progress clean compile

# Nếu vẫn lỗi, build từng phase
mvn clean
mvn compile
mvn package -DskipTests
```

## 🎯 Tối ưu hóa Pi5

### Memory Settings

```bash
# Kiểm tra memory
free -h

# Tăng swap nếu cần
sudo dphys-swapfile swapoff
sudo nano /etc/dphys-swapfile
# CONF_SWAPSIZE=2048
sudo dphys-swapfile setup
sudo dphys-swapfile swapon
```

### JVM Tuning

```bash
# Optimal JVM settings cho Pi5 4GB RAM
export JAVA_OPTS="-Xmx1536m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Docker Optimization

```bash
# Giới hạn memory cho PostgreSQL
docker run -d \
  --name postgres-jwt-optimized \
  --memory=512m \
  --cpus=1.0 \
  -e POSTGRES_USER=hungcop \
  -e POSTGRES_PASSWORD=hungcop123 \
  -e POSTGRES_DB=jwt_security \
  -p 5432:5432 \
  postgres:15-alpine
```

## 🚀 Recommended Workflow

1. **Preparation**
   ```bash
   ./fix-maven-pi5.sh
   ```

2. **Build & Deploy**
   ```bash
   ./start-app-pi5.sh
   ```

3. **Monitor**
   ```bash
   sudo systemctl status spring-boot-jwt
   sudo journalctl -u spring-boot-jwt -f
   ```

4. **Test**
   ```bash
   curl http://localhost:8080/api/v1/auth/register
   ```

## ⚡ Quick Fixes

```bash
# Nếu gặp broken pipe
pkill -f maven
./maven-safe.sh clean compile

# Nếu Out of Memory
export MAVEN_OPTS="-Xmx768m"
./mvn-pi5.sh clean compile

# Nếu Docker issues
docker system prune -f
./run-on-pi5.sh
```
