# Frontend Module

Frontend da duoc tich hop truc tiep vao backend theo cau truc monorepo:

- Source frontend: src/main/webapp
- Build output: target/classes/static

## Lenh su dung nhanh

```bash
# Chay frontend dev server
cd src/main/webapp
npm ci
npm run dev

# Build frontend
npm run build

# Lint frontend
npm run lint

# Preview production build
npm run preview
```

## Luu y quan trong

- Vite proxy /api ve backend local qua http://localhost:8080.
- Frontend duoc Maven build tu dong khi chay ./mvnw clean package tai thu muc goc project.
- Khong su dung Dockerfile/docker-compose rieng trong webapp nua; he thong docker da duoc chuan hoa tai src/main/docker.
