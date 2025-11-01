# 🚀 QUICK START GUIDE

## Bắt đầu trong 5 phút

### Bước 1: Kiểm tra môi trường
```powershell
# Kiểm tra các công cụ cần thiết
java -version    # Cần: Java 21+
mvn -version     # Cần: Maven 3.8+
node -v          # Cần: Node 20+
npm -v           # Cần: npm 10+
```

❌ **Nếu thiếu công cụ**: Xem [SETUP_ENVIRONMENT.md](./SETUP_ENVIRONMENT.md)

---

### Bước 2: Setup Database

#### Option 1: Docker (Khuyến nghị - Nhanh nhất)
```powershell
# Oracle
docker run -d --name oracle-auth `
  -p 1521:1521 `
  -e ORACLE_PWD=password `
  container-registry.oracle.com/database/express:latest

# PostgreSQL
docker run -d --name postgres-product `
  -p 5432:5432 `
  -e POSTGRES_USER=product_user `
  -e POSTGRES_PASSWORD=product_password `
  -e POSTGRES_DB=products `
  postgres:16
```

#### Option 2: Local Installation
- Cài Oracle XE và PostgreSQL thủ công
- Tạo user/database theo config trong `application.yaml`

---

### Bước 3: Cài đặt Dependencies

```powershell
# Backend
cd backend
.\mvnw.cmd clean install -DskipTests

# Frontend (terminal mới)
cd frontend
npm install
```

---

### Bước 4: Chạy ứng dụng

#### Terminal 1 - Backend:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
✅ Backend: http://localhost:8081

#### Terminal 2 - Frontend:
```powershell
cd frontend
npm start
```
✅ Frontend: http://localhost:8080

---

### Bước 5: Chạy Tests

```powershell
# Backend tests
cd backend
.\mvnw.cmd test

# Frontend tests
cd frontend
npm test
```

---

## 🎯 Bắt đầu phát triển TDD

### 1. Chọn feature để phát triển
- [ ] User Authentication (Login/Register)
- [ ] Product CRUD (Create, Read, Update, Delete)

### 2. Theo chu trình TDD

```
🔴 RED (Viết test - Test fails)
      ↓
🟢 GREEN (Viết code - Test passes)
      ↓
🔵 REFACTOR (Cải thiện code)
      ↓
    Repeat
```

### 3. Ví dụ: Tạo User Entity

#### Step 1: Tạo test (RED)
**File**: `backend/src/test/java/com/flogin/unit/entity/UserTest.java`
```java
package com.flogin.unit.entity;

import com.flogin.entity.auth.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void shouldCreateUserWithValidData() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        
        // Then
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
    }
}
```

Chạy test: `.\mvnw.cmd test -Dtest=UserTest`
→ ❌ Test sẽ FAIL (chưa có implementation)

#### Step 2: Implement code (GREEN)
**File**: `backend/src/main/java/com/flogin/entity/auth/User.java`
```java
package com.flogin.entity.auth;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
}
```

Chạy lại test: `.\mvnw.cmd test -Dtest=UserTest`
→ ✅ Test PASS

#### Step 3: Refactor (nếu cần)
- Thêm validation
- Thêm timestamps
- Optimize code

---

## 📂 Cấu trúc files quan trọng

```
FloginFE_BE/
├── README.md                    ← Tổng quan dự án
├── SETUP_ENVIRONMENT.md         ← Hướng dẫn cài đặt môi trường
├── TDD_WORKFLOW.md              ← Hướng dẫn TDD chi tiết
├── QUICK_START.md               ← File này
│
├── backend/
│   ├── pom.xml                  ← Dependencies (giữ nguyên)
│   ├── src/main/resources/
│   │   ├── application.yaml     ← Config database
│   │   └── application-test.yml ← Config test (H2)
│   └── src/
│       ├── main/java/com/flogin/    ← Production code (rỗng)
│       └── test/java/com/flogin/    ← Test code (rỗng)
│
└── frontend/
    ├── package.json             ← Dependencies (giữ nguyên)
    ├── webpack.config.js        ← Build config
    ├── jest.config.cjs          ← Test config
    └── src/
        ├── components/          ← React components (rỗng)
        ├── services/            ← API services (rỗng)
        └── tests/               ← Test files (rỗng)
```

---

## 🎓 Learning Path

### Day 1-2: Backend Authentication
1. User Entity + tests
2. UserRepository + tests
3. AuthService + tests
4. AuthController + tests
5. Integration test cho Auth flow

### Day 3-4: Backend Product CRUD
1. Product Entity + tests
2. ProductRepository + tests
3. ProductService + tests
4. ProductController + tests
5. Integration test cho Product CRUD

### Day 5-6: Frontend Components
1. LoginForm + tests
2. RegisterForm + tests
3. ProductList + tests
4. ProductForm + tests
5. ProductItem + tests

### Day 7: Integration & E2E
1. Backend integration tests
2. Frontend integration tests
3. Cypress E2E tests (optional)

---

## 🆘 Troubleshooting nhanh

### Backend không chạy?
```powershell
# Kiểm tra port 8081 có bị chiếm không
netstat -ano | findstr :8081

# Xóa cache Maven
.\mvnw.cmd clean

# Rebuild
.\mvnw.cmd clean install -DskipTests
```

### Frontend lỗi?
```powershell
# Xóa node_modules
rm -r node_modules
rm package-lock.json

# Cài lại
npm install

# Clear cache
npm cache clean --force
```

### Database lỗi?
```powershell
# Kiểm tra Docker containers
docker ps

# Restart containers
docker restart oracle-auth
docker restart postgres-product

# Xem logs
docker logs oracle-auth
docker logs postgres-product
```

---

## 📊 Checklist hoàn thành

### Môi trường
- [ ] Java 21+ installed
- [ ] Maven working
- [ ] Node.js 20+ installed
- [ ] npm working
- [ ] Oracle Database running (port 1521)
- [ ] PostgreSQL running (port 5432)

### Dependencies
- [ ] Backend dependencies installed
- [ ] Frontend dependencies installed
- [ ] Backend tests can run
- [ ] Frontend tests can run

### Application
- [ ] Backend starts successfully
- [ ] Frontend starts successfully
- [ ] Can access http://localhost:8080
- [ ] Can access http://localhost:8081

### Development
- [ ] Hiểu chu trình TDD (Red-Green-Refactor)
- [ ] Đã đọc TDD_WORKFLOW.md
- [ ] Sẵn sàng viết test đầu tiên

---

## 🎯 Next Steps

1. ✅ Đã setup xong? → Đọc [TDD_WORKFLOW.md](./TDD_WORKFLOW.md)
2. ✅ Hiểu TDD? → Bắt đầu với User Entity
3. ✅ Stuck? → Xem examples trong TDD_WORKFLOW.md
4. ✅ Cần help? → Review [SETUP_ENVIRONMENT.md](./SETUP_ENVIRONMENT.md)

---

## 🔥 Pro Tips

1. **Chạy tests liên tục**: Sử dụng watch mode
   ```bash
   # Backend (trong IDE)
   # Frontend
   npm test -- --watch
   ```

2. **Commit thường xuyên**: Mỗi khi 1 test pass
   ```bash
   git add .
   git commit -m "test: add user entity test"
   git commit -m "feat: implement user entity"
   ```

3. **Focus vào 1 test tại 1 thời điểm**: Đừng viết nhiều tests cùng lúc

4. **Refactor sau khi GREEN**: Đừng bỏ qua bước refactor

5. **Test coverage**: Aim for ≥80% nhưng quality > quantity

---

**Chúc bạn coding vui vẻ! 🎉**

Bắt đầu ngay: Mở file `TDD_WORKFLOW.md` và follow Phase 1!
