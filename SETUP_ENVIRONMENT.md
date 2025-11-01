# HƯỚNG DẪN CÀI ĐẶT MÔI TRƯỜNG PHÁT TRIỂN

## Tổng quan dự án
- **Backend**: Spring Boot 3.2+ với Java 21+
- **Frontend**: React 18+ với Webpack
- **Database**: Oracle (Auth) và PostgreSQL (Product)
- **Testing**: JUnit 5, Mockito, Jest, Cypress

---

## 1. CÀI ĐẶT JAVA (JDK 21+)

### Windows:
1. Tải JDK 21 từ: https://www.oracle.com/java/technologies/downloads/#java21
   - Hoặc sử dụng OpenJDK: https://adoptium.net/
2. Cài đặt và thêm vào PATH:
   ```powershell
   # Kiểm tra sau khi cài
   java -version
   javac -version
   ```

### Cấu hình JAVA_HOME:
```powershell
# Mở System Environment Variables
# Thêm JAVA_HOME = C:\Program Files\Java\jdk-21
# Thêm %JAVA_HOME%\bin vào PATH
```

---

## 2. CÀI ĐẶT MAVEN

### Windows:
1. Tải Maven từ: https://maven.apache.org/download.cgi
2. Giải nén vào C:\Program Files\Apache\maven
3. Thêm vào PATH:
   ```powershell
   # Thêm C:\Program Files\Apache\maven\bin vào PATH
   # Kiểm tra
   mvn -version
   ```

### Hoặc sử dụng Maven Wrapper (đã có trong dự án):
```powershell
cd backend
.\mvnw.cmd -version
```

---

## 3. CÀI ĐẶT NODE.JS VÀ NPM

### Windows:
1. Tải Node.js LTS từ: https://nodejs.org/
   - Khuyến nghị: Node.js 20.x LTS
2. Cài đặt (npm được cài cùng)
3. Kiểm tra:
   ```powershell
   node -v   # v20.x.x
   npm -v    # 10.x.x
   ```

---

## 4. CÀI ĐẶT DATABASE

### Oracle Database (cho Auth):
1. **Option 1**: Oracle XE (Express Edition)
   - Tải từ: https://www.oracle.com/database/technologies/xe-downloads.html
   
2. **Option 2**: Docker (Khuyến nghị)
   ```powershell
   docker pull container-registry.oracle.com/database/express:latest
   docker run -d -p 1521:1521 -e ORACLE_PWD=password container-registry.oracle.com/database/express:latest
   ```

3. Tạo user và schema:
   ```sql
   CREATE USER auth_user IDENTIFIED BY auth_password;
   GRANT CONNECT, RESOURCE TO auth_user;
   GRANT CREATE SESSION TO auth_user;
   GRANT CREATE TABLE TO auth_user;
   ```

### PostgreSQL (cho Product):
1. **Option 1**: Cài đặt trực tiếp
   - Tải từ: https://www.postgresql.org/download/
   
2. **Option 2**: Docker (Khuyến nghị)
   ```powershell
   docker pull postgres:16
   docker run -d -p 5432:5432 -e POSTGRES_USER=product_user -e POSTGRES_PASSWORD=product_password -e POSTGRES_DB=products postgres:16
   ```

---

## 5. CÀI ĐẶT DEPENDENCIES

### Backend:
```powershell
cd backend
.\mvnw.cmd clean install -DskipTests
```

### Frontend:
```powershell
cd frontend
npm install
```

---

## 6. CẤU HÌNH BIẾN MÔI TRƯỜNG

Tạo file `.env` trong thư mục gốc (hoặc cấu hình trong IDE):

```properties
# Oracle Auth Database
AUTH_DB_URL=jdbc:oracle:thin:@//localhost:1521/FLOGIN
AUTH_DB_USERNAME=auth_user
AUTH_DB_PASSWORD=auth_password

# PostgreSQL Product Database
PRODUCT_DB_URL=jdbc:postgresql://localhost:5432/products
PRODUCT_DB_USERNAME=product_user
PRODUCT_DB_PASSWORD=product_password
```

---

## 7. CHẠY ỨNG DỤNG

### Backend:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
Backend chạy tại: http://localhost:8081

### Frontend:
```powershell
cd frontend
npm start
```
Frontend chạy tại: http://localhost:8080

---

## 8. CHẠY TESTS

### Backend Tests (JUnit 5 + Mockito):
```powershell
cd backend
.\mvnw.cmd test
```

### Frontend Tests (Jest):
```powershell
cd frontend
npm test
```

### Cypress E2E Tests:
```powershell
# TODO: Cài đặt Cypress sau khi hoàn thành ứng dụng
cd frontend
npm install cypress --save-dev
npx cypress open
```

---

## 9. CÔNG CỤ PHÁT TRIỂN KHUYẾN NGHỊ

### IDE:
- **Backend**: IntelliJ IDEA Community/Ultimate hoặc Eclipse
- **Frontend**: VS Code với extensions:
  - ESLint
  - Prettier
  - Jest Runner
  - React Developer Tools

### Extensions VS Code:
- Java Extension Pack
- Spring Boot Extension Pack
- ES7+ React/Redux/React-Native snippets
- GitLens

### Database Tools:
- DBeaver (Universal)
- Oracle SQL Developer (Oracle)
- pgAdmin (PostgreSQL)

---

## 10. KIỂM TRA MÔI TRƯỜNG

Chạy script kiểm tra:
```powershell
# Kiểm tra tất cả công cụ
java -version
mvn -version
node -v
npm -v
docker --version  # nếu sử dụng Docker

# Kiểm tra kết nối database
# Backend sẽ tự động tạo bảng khi chạy lần đầu
```

---

## 11. CẤU TRÚC DỰ ÁN SAU KHI XÓA MÃ NGUỒN

```
FloginFE_BE/
├── backend/
│   ├── src/
│   │   ├── main/java/com/flogin/
│   │   │   ├── Application.java (rỗng)
│   │   │   ├── SecurityConfig.java (rỗng)
│   │   │   ├── controller/ (rỗng)
│   │   │   ├── dto/ (rỗng)
│   │   │   ├── entity/ (rỗng)
│   │   │   ├── repository/ (rỗng)
│   │   │   └── service/ (rỗng)
│   │   └── test/java/com/flogin/ (rỗng)
│   └── pom.xml (giữ nguyên dependencies)
├── frontend/
│   ├── src/
│   │   ├── components/ (rỗng)
│   │   ├── services/ (rỗng)
│   │   ├── tests/ (rỗng)
│   │   └── utils/ (rỗng)
│   └── package.json (giữ nguyên dependencies)
└── README.md
```

---

## 12. BƯỚC TIẾP THEO - PHÁT TRIỂN TDD

### Quy trình TDD:
1. **Red**: Viết test trước (test fail)
2. **Green**: Viết code tối thiểu để pass test
3. **Refactor**: Cải thiện code

### Thứ tự phát triển đề xuất:

#### Phase 1: Backend - Authentication
1. Viết test cho `User` entity
2. Implement `User` entity
3. Viết test cho `UserRepository`
4. Implement `UserRepository`
5. Viết test cho `AuthService`
6. Implement `AuthService`
7. Viết test cho `AuthController`
8. Implement `AuthController`

#### Phase 2: Backend - Product CRUD
1. Viết test cho `Product` entity
2. Implement tương tự như Authentication

#### Phase 3: Frontend - Components
1. Viết test cho `LoginForm`
2. Implement `LoginForm`
3. Viết test cho `ProductList`
4. Implement tiếp...

#### Phase 4: Integration & E2E Tests
1. JUnit Integration Tests
2. Jest Integration Tests
3. Cypress E2E Tests

---

## 13. TÀI LIỆU THAM KHẢO

- Spring Boot Docs: https://spring.io/projects/spring-boot
- React Docs: https://react.dev/
- JUnit 5: https://junit.org/junit5/
- Jest: https://jestjs.io/
- Cypress: https://www.cypress.io/
- TDD Best Practices: https://testdriven.io/

---

## 14. GHI CHÚ QUAN TRỌNG

⚠️ **MÃ NGUỒN ĐÃ ĐƯỢC XÓA SẠCH**
- Tất cả file Java và React đều là file rỗng với TODO comments
- Dependencies vẫn được giữ nguyên trong pom.xml và package.json
- Cấu trúc thư mục được giữ nguyên
- Sẵn sàng cho phát triển TDD từ đầu

✅ **CHECKLIST TRƯỚC KHI BẮT ĐẦU:**
- [ ] Java 21+ đã cài đặt
- [ ] Maven hoặc Maven Wrapper hoạt động
- [ ] Node.js 20+ và npm đã cài đặt
- [ ] Oracle Database đang chạy (port 1521)
- [ ] PostgreSQL đang chạy (port 5432)
- [ ] Backend dependencies đã install: `.\mvnw.cmd clean install -DskipTests`
- [ ] Frontend dependencies đã install: `npm install`
- [ ] IDE/Editor đã cài đặt extensions cần thiết

---

**Chúc bạn thành công với dự án TDD!**
