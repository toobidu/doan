# Hướng Dẫn Chạy Local

Tài liệu này mô tả cách chạy dự án theo 2 chế độ:

- Chế độ phát triển tách riêng frontend và backend.
- Chế độ full-stack bằng Docker (mô hình tương tự JHipster).

## 1. Điều Kiện Tiên Quyết

- JDK 21.
- Maven Wrapper (đã có sẵn: `mvnw`, `mvnw.cmd`).
- Node.js 20 trở lên.
- Docker Desktop (nếu chạy full-stack).

## 2. Chạy Local Cho Phát Triển Frontend + Backend

### Bước 1: Khởi động backend

```bash
./mvnw spring-boot:run -Pdev-no-frontend-build
```

Backend mặc định chạy tại `http://localhost:8080`.

### Bước 2: Khởi động frontend

```bash
cd src/main/webapp
npm ci
npm run dev
```

Frontend mặc định chạy tại `http://localhost:5173`.

Vite đã cấu hình proxy API `/api` về backend local (`http://localhost:8080`).

## 3. Chạy Full-Stack Bằng Docker

Tất cả tệp Docker nằm trong thư mục `src/main/docker`.

### Windows PowerShell

```powershell
./src/main/docker/docker-up.ps1
```

### Linux/macOS

```bash
chmod +x src/main/docker/docker-up.sh src/main/docker/docker-down.sh
./src/main/docker/docker-up.sh
```

Lệnh trên sẽ khởi động:

- PostgreSQL.
- Redis.
- MinIO.
- Ứng dụng Quizizz (Spring Boot + frontend React đã build tĩnh).

Sau khi container chạy:

- App: `http://localhost:8080`
- MinIO Console: `http://localhost:9001`

### Dừng hệ thống Docker

Windows:

```powershell
./src/main/docker/docker-down.ps1
```

Linux/macOS:

```bash
./src/main/docker/docker-down.sh
```

## 4. Build Một Artifact (JAR Đã Nhúng Frontend)

```bash
./mvnw clean package -DskipTests
```

Kết quả:

- File jar: `target/quizizz-0.0.1-SNAPSHOT.jar`.
- Frontend được build từ `src/main/webapp` và copy vào thư mục static bên trong jar.

## 5. Sự Cố Thường Gặp

- Lỗi `vite is not recognized`: chưa cài dependencies frontend. Chạy `npm ci` trong `src/main/webapp`.
- Lỗi kết nối DB khi chạy local: kiểm tra biến môi trường `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Lỗi CORS khi phát triển: đảm bảo gọi API qua tiền tố `/api` để Vite proxy đúng.
