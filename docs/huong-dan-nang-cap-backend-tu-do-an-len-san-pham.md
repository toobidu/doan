# Hướng Dẫn Nâng Cấp Backend Từ Đồ Án Lên Sản Phẩm

Tài liệu này tổng hợp các vấn đề đã phát hiện trong backend hiện tại và chuyển chúng thành một lộ trình nâng cấp có thể triển khai theo sprint. Mục tiêu là đưa hệ thống từ mức "chạy được cho demo" lên mức "vận hành được, bảo trì được, chịu lỗi được".

Tài liệu này cũng bao gồm phần bổ sung monitoring với Spring Boot Actuator, Prometheus và Grafana ngay trong repo.

## 1. Mục tiêu nâng cấp

Backend của game quiz hiện tại đã có nền tảng tốt:

- Spring Boot 3, Java 21
- PostgreSQL, Redis, MinIO
- JWT authentication
- Role/Permission
- Socket.IO cho real-time game
- Actuator cho observability

Nhưng để thành sản phẩm thật, cần bổ sung 4 nhóm năng lực:

1. Đúng nghiệp vụ và chống gian lận.
2. Bảo mật và phân quyền thực chiến.
3. Vận hành được ở production.
4. Quan sát được hệ thống khi có lỗi hoặc tải tăng cao.

## 2. Những vấn đề chính đã phát hiện

### 2.1. P0: Anti-cheat và rò rỉ dữ liệu đáp án

Các vấn đề cần xử lý trước tiên:

- Một số API trả về dữ liệu đáp án quá giàu thông tin, dễ làm lộ `isCorrect`.
- Luồng chấm điểm chưa kiểm tra chặt chẽ `answerId` có thực sự thuộc `questionId` hay không.
- Cơ chế shuffle đáp án và cơ chế resolve đáp án có thể lệch thứ tự giữa client và server.

Hệ quả:

- Người chơi có thể gian lận bằng cách soi payload.
- Kết quả game có thể sai dù UI có vẻ hoạt động bình thường.
- Về mặt sản phẩm, chỉ cần 1 người khai thác được là game mất công bằng ngay.

### 2.2. P0: Monitoring có cấu hình nhưng chưa hoàn chỉnh

Hiện tại cấu hình Spring Boot đã bật endpoint Prometheus trong `src/main/resources/config/application.yml`, nhưng backend chưa có dependency `micrometer-registry-prometheus`.

Hệ quả:

- `/actuator/prometheus` có thể không hoạt động đúng như mong đợi.
- Prometheus/Grafana chưa thể scrape metrics ổn định.

### 2.3. P1: Bảo mật production còn lỏng

Các điểm cần nâng:

- Actuator hiện expose nhiều endpoint nhạy cảm hơn mức cần thiết.
- CORS và Socket.IO origin đang để rất rộng.
- JWT secret đang có giá trị mặc định, không phù hợp production.
- Data seeding tạo sẵn user hệ thống là tiện cho dev nhưng rủi ro nếu lọt sang môi trường thật.

### 2.4. P1: Game state chưa đủ mạnh để scale

Game state và timer hiện phụ thuộc nhiều vào memory/process hiện tại.

Hệ quả:

- Restart app là mất timer.
- Chạy nhiều instance sẽ khó đồng bộ.
- Reconnect, resume, failover chưa ổn định ở production.

### 2.5. P1: Một số flow nghiệp vụ còn dang dở

Ví dụ:

- `invitePlayer(...)` chưa hoàn thiện.
- `updateRoom(...)` còn `return null`.
- Một số chỗ dùng `RuntimeException` hoặc `orElseThrow()` trần, khó chuẩn hóa lỗi.

### 2.6. P2: Test coverage còn mỏng

Repo đã có Testcontainers, nhưng test hiện tại chưa bao phủ các luồng quan trọng:

- auth
- room lifecycle
- game lifecycle
- anti-cheat
- websocket/game event flow

## 3. Thứ tự ưu tiên theo sprint

## Sprint 1: Chặn lỗi gây hỏng sản phẩm

Mục tiêu:

- Không lộ đáp án.
- Không chấm sai.
- Không để người chơi gửi payload giả.

Việc cần làm:

1. Tách DTO cho player và DTO cho teacher/admin.
2. Không trả `isCorrect` cho API/player socket event.
3. Khi submit answer, xác minh:
   - người chơi đang ở trong room
   - room đang `PLAYING`
   - câu hỏi hiện tại đúng với room/game session
   - `answerId` thuộc `questionId`
   - người chơi chưa trả lời câu đó
4. Chuẩn hóa thứ tự đáp án:
   - hoặc server gửi `answerId` trực tiếp
   - hoặc server lưu order đã shuffle theo session/player
5. Viết test anti-cheat.

## Sprint 2: Siết bảo mật và production hygiene

Mục tiêu:

- Production không lộ config nội bộ.
- Authentication/logout/reset password an toàn hơn.

Việc cần làm:

1. Bỏ secret mặc định ở production.
2. Giới hạn actuator của môi trường `prod` chỉ còn:
   - `health`
   - `info`
   - `prometheus`
3. Đổi reset password sang flow token-based:
   - generate reset token
   - gửi link
   - user tự đặt mật khẩu mới
4. Siết CORS và Socket.IO origin bằng env.
5. Review lại seeded admin/test account để chỉ tồn tại trong dev/local.

## Sprint 3: Ổn định game engine và scaling

Mục tiêu:

- Game session chịu được reconnect/restart tốt hơn.

Việc cần làm:

1. Đưa timer và trạng thái câu hỏi sang Redis hoặc persistent state.
2. Thiết kế state machine rõ ràng:
   - `WAITING`
   - `PLAYING`
   - `SHOWING_RESULT`
   - `FINISHED`
   - `ARCHIVED`
3. Làm idempotent cho:
   - start game
   - next question
   - end game
   - submit answer
4. Tách host command và player command rõ ràng.

## Sprint 4: Chất lượng dữ liệu và maintainability

Mục tiêu:

- Code dễ bảo trì hơn.
- Query đúng và có thể scale tốt hơn.

Việc cần làm:

1. Thay các `RuntimeException` bằng `ApiException` hoặc exception domain-specific.
2. Hoàn thiện các flow còn `TODO` hoặc `return null`.
3. Rà lại query/filter đang làm in-memory.
4. Bổ sung migration bằng Flyway hoặc Liquibase.
5. Viết integration test theo business flow.

## 4. Monitoring đã được bổ sung trong repo

Các thay đổi đã thêm:

- Thêm dependency Prometheus registry vào `pom.xml`.
- Giữ Spring Boot Actuator metrics export cho Prometheus.
- Thêm stack monitoring trong Docker Compose:
  - Prometheus
  - Grafana
- Thêm file scrape config:
  - `src/main/docker/monitoring/prometheus.yml`
- Thêm Grafana datasource provisioning:
   - `src/main/docker/monitoring/grafana/provisioning/datasources/datasource.yml`
- Giới hạn actuator của profile `prod` còn `health`, `info`, `prometheus`.

## 5. Cách chạy monitoring local

### 5.1. Chạy backend local trên máy host

Chạy app như bình thường:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Sau đó chạy monitoring stack:

```bash
docker compose -f src/main/docker/services.yml --profile monitoring up -d
```

Prometheus sẽ scrape app qua:

```text
http://host.docker.internal:8080/actuator/prometheus
```

### 5.2. Chạy full stack bằng Docker

```bash
docker compose -f src/main/docker/app.yml --profile monitoring up -d --build
```

## 6. URL quan trọng sau khi bật monitoring

- Backend health: `http://localhost:8080/actuator/health`
- Backend metrics: `http://localhost:8080/actuator/prometheus`
- Prometheus UI: `http://localhost:9090`
- Grafana UI: `http://localhost:3000`

Mặc định Grafana:

- user: `admin`
- password: `admin`

Có thể đổi bằng biến môi trường trong `src/main/docker/.env.example`:

- `GRAFANA_ADMIN_USER`
- `GRAFANA_ADMIN_PASSWORD`

## 7. Những metrics nên theo dõi đầu tiên

### 7.1. HTTP/API

- `http_server_requests_seconds_count`
- `http_server_requests_seconds_sum`
- `http_server_requests_seconds_max`

Theo dõi:

- số request
- latency p95/p99
- tỷ lệ 4xx/5xx

### 7.2. JVM

- `jvm_memory_used_bytes`
- `jvm_gc_pause_seconds`
- `process_cpu_usage`
- `system_cpu_usage`

Theo dõi:

- memory tăng bất thường
- GC pause lớn
- CPU tăng đột ngột khi nhiều room/game cùng lúc

### 7.3. Connection pool và backend service

- datasource / hikari metrics
- redis cache metrics
- custom metrics cho game session nếu bổ sung sau

Theo dõi:

- DB pool có bị cạn không
- cache hit/miss
- số room active
- số game đang chạy

## 8. Dashboard nên có trong Grafana

Dashboard tối thiểu nên gồm:

1. Tổng request theo phút.
2. Error rate 4xx/5xx.
3. Latency p50/p95/p99.
4. JVM heap/non-heap.
5. GC pause.
6. CPU process/system.
7. Số room đang active.
8. Số game session đang `IN_PROGRESS`.
9. Số kết nối socket đang mở.

## 9. Alert nên thêm sau Prometheus

Alert tối thiểu:

1. `5xx rate` vượt ngưỡng trong 5 phút.
2. `p95 latency` tăng bất thường.
3. JVM heap > 80%.
4. DB readiness down.
5. Redis down.
6. Tỷ lệ submit answer lỗi tăng đột biến.
7. Số room `PLAYING` tăng nhưng số game finish không tăng tương ứng.

## 10. Checklist production sau khi có monitoring

Trước khi coi là "sản phẩm thật", nên đạt checklist sau:

- Có Prometheus scrape được metrics ổn định.
- Có Grafana dashboard cơ bản.
- Có alert cho health, error rate, latency, JVM.
- Không lộ `isCorrect` cho API/player payload.
- Không dùng password reset qua email bằng plain password.
- Không dùng JWT secret mặc định trong production.
- Có migration script DB.
- Có integration test cho auth, room, game, anti-cheat.
- Có log correlation hoặc request id.
- Có quy trình rollback đơn giản.

## 11. Đề xuất kỹ thuật tiếp theo sau tài liệu này

Nếu tiếp tục triển khai theo thứ tự an toàn, nên làm như sau:

1. Refactor DTO dành riêng cho player để bỏ toàn bộ dữ liệu đáp án nhạy cảm.
2. Hardening `submitAnswer`.
3. Chuẩn hóa state machine cho room/game.
4. Viết integration test cho `start-game`, `submit-answer`, `end-game`.
5. Thêm custom Micrometer metrics cho:
   - room created
   - room joined
   - game started
   - game finished
   - answer submitted
   - answer rejected

## 12. Kết luận

Backend hiện tại đã có nền móng để đi tiếp, nhưng chưa đủ chặt để coi là production-ready. Hướng nâng cấp đúng không phải là thêm nhiều tính năng trước, mà là:

1. chặn gian lận và sai dữ liệu
2. siết bảo mật production
3. ổn định hóa game state
4. bổ sung monitoring và cảnh báo
5. tăng test coverage

Sau khi hoàn thành các bước đó, hệ thống mới thực sự chuyển từ "đồ án chạy được" sang "sản phẩm có thể vận hành".
