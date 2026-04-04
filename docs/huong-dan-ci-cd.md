# Hướng Dẫn CI/CD Chi Tiết

## 1. Mục Tiêu Của Quy Trình CI/CD

Quy trình CI/CD cho dự án này được thiết kế để giải quyết các vấn đề vận hành thực tế:

- Giảm lỗi tích hợp giữa frontend và backend bằng cách build trong cùng một pipeline.
- Đảm bảo artifact triển khai luôn nhất quán (một file jar đã nhúng frontend).
- Rút ngắn thời gian phát hành phiên bản mới.
- Tăng khả năng truy vết và rollback khi có sự cố production.

## 2. Quy Trình Tổng Quan Và Giá Trị Mang Lại

Quy trình được chia thành 3 pha:

1. CI (Continuous Integration): kiểm tra và đóng gói code ngay khi có thay đổi.
2. CD (Continuous Delivery/Deployment): triển khai tự động hoặc bán tự động lên môi trường đích.
3. Verify & Rollback: xác nhận hệ thống sau deploy và quay lại phiên bản ổn định nếu cần.

Các vấn đề được giải quyết:

- Tránh tình trạng "chạy được ở máy dev nhưng lỗi trên server".
- Phát hiện lỗi sớm ở pull request thay vì đến khi phát hành.
- Chuẩn hóa cách deploy, giảm phụ thuộc thao tác thủ công.

## 3. Chi Tiết Pha CI (Nhánh PR Và Nhánh Develop/Main)

### 3.1 Trigger

- Pull request vào `main`.
- Push lên `develop` hoặc `main`.

### 3.2 Các bước bắt buộc

1. Checkout source.
2. Cài Java 21 (Temurin).
3. Cache Maven để tăng tốc pipeline.
4. Build và kiểm tra frontend qua Maven lifecycle.
5. Chạy lint frontend (`src/main/webapp`).
6. Chạy test backend.
7. Tạo artifact jar.
8. Upload artifact để dùng cho bước deploy.

### 3.3 Vấn đề được xử lý ở pha CI

- Lỗi cú pháp/lint từ frontend sau refactor JS sang TS.
- Lỗi biên dịch backend hoặc sai dependency.
- Lỗi unit test làm giảm chất lượng release.

## 4. Chi Tiết Pha CD (Triển Khai Lên Server)

### 4.1 Điều kiện deploy

- Chỉ deploy từ `main`.
- Job `build` phải thành công 100%.
- Có thể thêm bước manual approval trước production.

### 4.2 Các bước deploy chuẩn

1. Download artifact từ CI.
2. Copy jar lên server qua SSH/SCP.
3. Dừng service cũ (`quizizz`).
4. Ghi đè hoặc cập nhật symlink jar mới.
5. Khởi động lại service.
6. Chạy smoke test endpoint health.

### 4.3 Vấn đề được xử lý ở pha CD

- Giảm rủi ro quên bước deploy thủ công.
- Đảm bảo bản chạy thực tế đúng artifact đã qua kiểm thử.
- Giảm downtime bằng quy trình restart có kiểm soát.

## 5. Verify Sau Deploy Và Tiêu Chí Thành Công

Sau deploy, bắt buộc kiểm tra:

- `GET /actuator/health` trả trạng thái UP.
- Truy cập giao diện frontend không lỗi 500.
- Các API chính hoạt động: đăng nhập, danh sách phòng, tạo phòng.
- Log service không có lỗi nghiêm trọng liên tục.

Tiêu chí hoàn tất deploy:

- Health check đạt.
- Không phát sinh lỗi nghiêm trọng trong 5-10 phút đầu.
- Người dùng có thể thao tác luồng chính.

## 6. Rollback Khi Có Sự Cố

### 6.1 Chiến lược

- Lưu tối thiểu 2 phiên bản jar gần nhất trong `/opt/quizizz/releases`.
- Dùng symlink `/opt/quizizz/app.jar` trỏ đến phiên bản đang chạy.

### 6.2 Các bước rollback

1. Dừng service hiện tại.
2. Chuyển symlink về bản jar ổn định trước đó.
3. Khởi động lại service.
4. Chạy lại health check.

### 6.3 Vấn đề được xử lý

- Khôi phục dịch vụ nhanh khi release mới bị lỗi.
- Giảm thời gian gián đoạn hệ thống.

## 7. Quản Lý Secrets Và An Toàn Pipeline

Không commit secret vào source code. Sử dụng GitHub Secrets hoặc hệ thống vault:

- `SSH_HOST`, `SSH_USER`, `SSH_KEY`
- `DB_PASSWORD`
- `MAIL_PASSWORD`
- `OPENAI_API_KEY`

Thiết lập thêm:

- Bật branch protection cho `main`.
- Bắt buộc pull request review.
- Bắt buộc pipeline CI pass trước khi merge.

## 8. Ví Dụ GitHub Actions Tham Khảo

Tạo file `.github/workflows/ci-cd.yml`:

```yaml
name: ci-cd

on:
  push:
    branches: ["main", "develop"]
  pull_request:
    branches: ["main"]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Java 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-maven-

      - name: Lint frontend
        working-directory: src/main/webapp
        run: npm ci && npm run lint

      - name: Build and test backend
        run: ./mvnw clean verify

      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: quizizz-jar
          path: target/*.jar

  deploy:
    if: github.ref == 'refs/heads/main'
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Download artifact
        uses: actions/download-artifact@v4
        with:
          name: quizizz-jar
          path: dist

      - name: Copy artifact to server
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.SSH_HOST }}
          username: ${{ secrets.SSH_USER }}
          key: ${{ secrets.SSH_KEY }}
          source: "dist/*.jar"
          target: "/tmp"

      - name: Deploy and smoke test
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.SSH_HOST }}
          username: ${{ secrets.SSH_USER }}
          key: ${{ secrets.SSH_KEY }}
          script: |
            sudo systemctl stop quizizz
            sudo cp /tmp/dist/*.jar /opt/quizizz/app.jar
            sudo chown quizizz:quizizz /opt/quizizz/app.jar
            sudo systemctl start quizizz
            curl -f http://localhost:8080/actuator/health
```

## 9. Khuyến Nghị Nâng Cao

- Thêm SAST (CodeQL/Semgrep) để phát hiện lỗ hổng sớm.
- Thêm dependency scan (OWASP Dependency Check, Trivy).
- Tách môi trường `staging` và `production` với approval độc lập.
- Tạo thông báo deploy lên Slack/Teams để theo dõi vận hành.
