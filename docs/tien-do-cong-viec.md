# Tien Do Cong Viec

Cap nhat ngay: 2026-04-04

## Trang thai TODO

- [x] Move frontend to webapp
- [x] Unify Docker structure scripts
- [x] Create professional VN docs
- [x] Migrate JS JSX to TS TSX
- [x] Apply naming convention
- [x] Build validate full stack

## Bang chung xac nhan

- Frontend da nam o `src/main/webapp`, khong con `quizizz-fe` doc lap.
- Docker scripts duoc chuan hoa tai `src/main/docker`:
  - `docker-up.ps1`, `docker-down.ps1`
  - `docker-up.sh`, `docker-down.sh`
- Tai lieu van hanh da cap nhat theo cau truc moi:
  - `docs/huong-dan-chay-local.md`
  - `docs/huong-dan-ci-cd.md`
  - `docs/huong-dan-deploy-server.md`
- Scope frontend `src/main/webapp/src` da migration sang `ts/tsx`.
- Lint frontend hien tai: `errors=0`, `warnings=0`.
- Da sua `tsconfig.json` de tuong thich TypeScript 6 (bo `baseUrl`).
- Build frontend + backend se duoc xac nhan bang command trong muc Validation.

## Validation Commands

```bash
# Frontend
cd src/main/webapp
npm run lint
npm run build

# Backend
cd ../../../
./mvnw clean package -DskipTests
```
