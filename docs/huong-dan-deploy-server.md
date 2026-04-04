# Hướng Dẫn Deploy Lên Server (Ưu Tiên JAR + run.sh)

Tài liệu này hướng dẫn mô hình deploy ổn định, dễ vận hành: build jar tại CI/local, copy lên server, chạy bằng script `run.sh` và quản lý tiến trình bằng systemd.

## 1. Kiến Trúc Deploy

- Artifact: `target/quizizz-0.0.1-SNAPSHOT.jar`.
- Runtime: Java 21.
- Dịch vụ phụ trợ: PostgreSQL, Redis, MinIO.
- Process manager: systemd (khuyến nghị).

## 2. Chuẩn Bị Server

Cần cài đặt:

- OpenJDK 21.
- tar/unzip, curl.
- systemd.

Tạo user chạy app:

```bash
sudo useradd -r -s /bin/false quizizz || true
sudo mkdir -p /opt/quizizz /var/log/quizizz
sudo chown -R quizizz:quizizz /opt/quizizz /var/log/quizizz
```

## 3. Copy Artifact Lên Server

Ví dụ copy từ máy build:

```bash
scp target/quizizz-0.0.1-SNAPSHOT.jar user@server:/opt/quizizz/app.jar
```

## 4. Tạo File Môi Trường Production

Tạo `/opt/quizizz/.env`:

```env
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DB_URL=jdbc:postgresql://<db-host>:5432/quizizz
DB_USERNAME=postgres
DB_PASSWORD=<strong-password>
REDIS_HOST=<redis-host>
REDIS_PORT=6379
MINIO_ENDPOINT=http://<minio-host>:9000
MINIO_PUBLIC_ENDPOINT=https://<public-minio-domain>
MINIO_ACCESS_KEY=<minio-user>
MINIO_SECRET_KEY=<minio-password>
OPENAI_API_KEY=
MAIL_USERNAME=
MAIL_PASSWORD=
```

## 5. Tạo Script run.sh

Tạo `/opt/quizizz/run.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

cd /opt/quizizz
set -a
source ./.env
set +a

exec /usr/bin/java -Xms512m -Xmx1024m -jar /opt/quizizz/app.jar
```

Cấp quyền:

```bash
chmod +x /opt/quizizz/run.sh
chown quizizz:quizizz /opt/quizizz/run.sh /opt/quizizz/app.jar /opt/quizizz/.env
```

## 6. Tạo Systemd Service

Tạo `/etc/systemd/system/quizizz.service`:

```ini
[Unit]
Description=Quizizz Backend Service
After=network.target

[Service]
Type=simple
User=quizizz
Group=quizizz
WorkingDirectory=/opt/quizizz
ExecStart=/opt/quizizz/run.sh
Restart=always
RestartSec=5
StandardOutput=append:/var/log/quizizz/app.log
StandardError=append:/var/log/quizizz/app-error.log

[Install]
WantedBy=multi-user.target
```

Khởi động dịch vụ:

```bash
sudo systemctl daemon-reload
sudo systemctl enable quizizz
sudo systemctl start quizizz
sudo systemctl status quizizz
```

## 7. Cập Nhật Phiên Bản Mới

Khi có jar mới:

```bash
sudo systemctl stop quizizz
sudo cp /tmp/app.jar /opt/quizizz/app.jar
sudo chown quizizz:quizizz /opt/quizizz/app.jar
sudo systemctl start quizizz
```

## 8. Kiểm Tra Sau Deploy

- Health check: `GET /actuator/health`.
- Swagger UI: `/swagger-ui.html`.
- Các route frontend bất kỳ sẽ được forward về `index.html`.
