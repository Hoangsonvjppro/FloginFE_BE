# Development Guide

## Local Development with Hot Reload

### Frontend Development (Recommended)

**Start local dev server với hot reload:**

```bash
cd frontend
npm start
```

Server sẽ chạy tại: **http://localhost:3000**

**Ưu điểm:**
- ⚡ Hot reload - tự động refresh khi code thay đổi
- 🚀 Rebuild nhanh (< 1 giây)
- 💾 Không cần rebuild Docker mỗi lần sửa code
- 🔧 DevTools support tốt hơn

**Lưu ý:**
- Backend API vẫn chạy ở Docker port 8081
- Nginx proxy `/api` đã được config sẵn

---

### Docker Development (For Production-like Testing)

**Build và run toàn bộ stack:**

```bash
docker compose up --build -d
```

Services:
- Frontend: http://localhost:3000 (Nginx)
- Backend: http://localhost:8081
- Oracle: localhost:1521
- PostgreSQL: localhost:5432

**Rebuild chỉ frontend:**

```bash
docker compose up --build frontend -d
```

**Stop all containers:**

```bash
docker compose down
```

**Clean restart (xóa volumes):**

```bash
docker compose down -v
docker compose up --build -d
```

---

## Testing

### Backend Tests

```bash
cd backend
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
.\mvnw.cmd clean test
```

### Frontend Tests

**Windows:**
```bash
cd frontend
wsl bash -c "npm test"
```

**Linux/Mac:**
```bash
cd frontend
npm test
```

---

## Recommended Workflow

1. **Start Docker containers lần đầu:**
   ```bash
   docker compose up -d
   ```

2. **Develop frontend với hot reload:**
   ```bash
   cd frontend
   npm start
   ```

3. **Code thay đổi → tự động reload** ✨

4. **Test trước khi commit:**
   ```bash
   # Backend
   cd backend && .\mvnw.cmd test
   
   # Frontend
   cd frontend && wsl bash -c "npm test"
   ```

5. **Test production build:**
   ```bash
   docker compose up --build frontend -d
   ```

---

## Ports Summary

| Service | Port | URL |
|---------|------|-----|
| Frontend Dev (Hot Reload) | 3000 | http://localhost:3000 |
| Frontend Prod (Docker) | 3000 | http://localhost:3000 |
| Backend API | 8081 | http://localhost:8081/api |
| Oracle DB | 1521 | jdbc:oracle:thin:@localhost:1521/XEPDB1 |
| PostgreSQL | 5432 | jdbc:postgresql://localhost:5432/products |

---

## Tips

- Dùng **npm start** cho development (nhanh, hot reload)
- Dùng **docker compose** cho testing tích hợp hoặc demo
- Commit code thường xuyên
- Chạy tests trước khi push
