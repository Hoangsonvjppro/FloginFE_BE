# FloginFE_BE - Full Stack Web Application with TDD

## 📋 Tổng quan dự án

Dự án xây dựng ứng dụng web full-stack với **Test-Driven Development (TDD)** bao gồm 2 chức năng chính:
- **Authentication (Đăng nhập/Đăng ký)**: Quản lý người dùng
- **Product Management (CRUD)**: Quản lý sản phẩm

## 🏗️ Kiến trúc kỹ thuật

### Backend
- **Framework**: Spring Boot 3.5.7
- **Java**: JDK 21
- **Build Tool**: Maven
- **Databases**: 
  - Oracle Database (User Authentication)
  - PostgreSQL (Product Management)
- **Security**: Spring Security
- **Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers

### Frontend
- **Library**: React 18.3.1
- **Build Tool**: Webpack 5
- **Language**: JavaScript (JSX)
- **HTTP Client**: Axios
- **Testing**: Jest, React Testing Library
- **E2E Testing**: Cypress (planned)

## 📁 Cấu trúc dự án

```
FloginFE_BE/
├── backend/                    # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/flogin/
│   │   │   │   ├── Application.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── AuthDataSourceConfig.java
│   │   │   │   ├── ProductDataSourceConfig.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── ProductController.java
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   │   └── RegisterRequest.java
│   │   │   │   │   └── product/
│   │   │   │   │       ├── ProductRequest.java
│   │   │   │   │       ├── ProductResponse.java
│   │   │   │   │       └── ProductMapper.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── auth/User.java
│   │   │   │   │   └── product/Product.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── auth/UserRepository.java
│   │   │   │   │   └── product/ProductRepository.java
│   │   │   │   └── service/
│   │   │   │       ├── auth/AuthService.java
│   │   │   │       ├── product/ProductService.java
│   │   │   │       ├── BadRequestException.java
│   │   │   │       └── NotFoundException.java
│   │   │   └── resources/
│   │   │       ├── application.yaml
│   │   │       └── application-test.yml
│   │   └── test/java/com/flogin/
│   │       ├── ApplicationTests.java
│   │       ├── integration/
│   │       │   ├── AuthIntegrationTest.java
│   │       │   └── ProductIntegrationTest.java
│   │       └── unit/
│   │           ├── controller/ProductControllerTest.java
│   │           └── service/
│   │               ├── auth/AuthServiceTest.java
│   │               └── product/ProductServiceTest.java
│   └── pom.xml
│
├── frontend/                   # React application
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── components/
│   │   │   ├── App.jsx
│   │   │   ├── auth/
│   │   │   │   ├── LoginForm.jsx
│   │   │   │   └── RegisterForm.jsx
│   │   │   └── product/
│   │   │       ├── ProductForm.jsx
│   │   │       ├── ProductItem.jsx
│   │   │       └── ProductList.jsx
│   │   ├── services/
│   │   │   ├── httpClient.js
│   │   │   ├── authApi.js
│   │   │   └── productApi.js
│   │   ├── tests/
│   │   │   ├── unit/
│   │   │   │   ├── LoginForm.test.jsx
│   │   │   │   └── ProductForm.test.jsx
│   │   │   └── integration/
│   │   │       └── ProductFlow.test.jsx
│   │   ├── utils/
│   │   │   ├── constants.js
│   │   │   └── validators.js
│   │   ├── index.jsx
│   │   └── styles.css
│   ├── babel.config.cjs
│   ├── jest.config.cjs
│   ├── jest.setup.js
│   ├── webpack.config.js
│   └── package.json
│
├── SETUP_ENVIRONMENT.md       # Hướng dẫn cài đặt môi trường
└── README.md                  # File này
```

## 🚀 Bắt đầu nhanh

### 1. Yêu cầu hệ thống
- Java JDK 21+
- Maven 3.8+ (hoặc sử dụng Maven Wrapper đi kèm)
- Node.js 20+ và npm
- Oracle Database (port 1521)
- PostgreSQL (port 5432)

📖 **Chi tiết cài đặt**: Xem file [SETUP_ENVIRONMENT.md](./SETUP_ENVIRONMENT.md)

### 2. Cài đặt dependencies

#### Backend:
```bash
cd backend
./mvnw clean install -DskipTests  # Linux/Mac
.\mvnw.cmd clean install -DskipTests  # Windows
```

#### Frontend:
```bash
cd frontend
npm install
```

### 3. Cấu hình Database

Tạo file `.env` hoặc cấu hình biến môi trường:

```properties
# Oracle
AUTH_DB_URL=jdbc:oracle:thin:@//localhost:1521/FLOGIN
AUTH_DB_USERNAME=auth_user
AUTH_DB_PASSWORD=auth_password

# PostgreSQL
PRODUCT_DB_URL=jdbc:postgresql://localhost:5432/products
PRODUCT_DB_USERNAME=product_user
PRODUCT_DB_PASSWORD=product_password
```

### 4. Chạy ứng dụng

#### Backend (Terminal 1):
```bash
cd backend
./mvnw spring-boot:run  # Linux/Mac
.\mvnw.cmd spring-boot:run  # Windows
```
→ API running at: http://localhost:8081

#### Frontend (Terminal 2):
```bash
cd frontend
npm start
```
→ Web running at: http://localhost:8080

## 🧪 Testing

### Backend Tests (JUnit 5 + Mockito)
```bash
cd backend
./mvnw test                    # Chạy tất cả tests
./mvnw test -Dtest=AuthServiceTest  # Chạy 1 test cụ thể
```

### Frontend Tests (Jest)
```bash
cd frontend
npm test                       # Interactive mode
npm test -- --coverage         # Với coverage report
```

### Test Coverage Goals
- **Unit Tests**: ≥ 80% coverage
- **Integration Tests**: Core flows
- **E2E Tests**: Critical user journeys

## 📊 API Endpoints

### Authentication
- `POST /api/auth/register` - Đăng ký người dùng mới
- `POST /api/auth/login` - Đăng nhập

### Products
- `GET /api/products` - Lấy danh sách sản phẩm
- `POST /api/products` - Tạo sản phẩm mới
- `PUT /api/products/{id}` - Cập nhật sản phẩm
- `DELETE /api/products/{id}` - Xóa sản phẩm

## 🎯 Phương pháp TDD

Dự án này áp dụng **Test-Driven Development**:

### Quy trình:
1. **Red** 🔴: Viết test trước (test fail)
2. **Green** 🟢: Viết code tối thiểu để pass test
3. **Refactor** 🔵: Cải thiện code

### Thứ tự phát triển:
1. ✅ Entity layer (User, Product)
2. ✅ Repository layer (JPA Repositories)
3. ✅ Service layer (Business logic)
4. ✅ Controller layer (REST APIs)
5. ✅ Frontend components
6. ✅ Integration tests
7. ⏳ E2E tests (Cypress)

## 📝 Coding Standards

### Backend (Java)
- Follow Java Code Conventions
- Use Lombok để giảm boilerplate
- Package structure theo domain
- Exception handling với custom exceptions

### Frontend (React)
- Functional components với Hooks
- PropTypes cho type checking
- Component composition
- Separation of concerns (components/services/utils)

## 🔧 Công cụ phát triển

- **IDE Backend**: IntelliJ IDEA / Eclipse
- **IDE Frontend**: VS Code
- **Database Tools**: DBeaver, SQL Developer, pgAdmin
- **API Testing**: Postman / Insomnia
- **Version Control**: Git

## 📚 Tài liệu tham khảo

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Jest Documentation](https://jestjs.io/)
- [TDD Best Practices](https://testdriven.io/)

## 🐛 Troubleshooting

### Backend không chạy được?
- Kiểm tra Java version: `java -version`
- Kiểm tra database đang chạy
- Xem logs trong terminal

### Frontend không build được?
- Xóa `node_modules` và `package-lock.json`
- Chạy lại `npm install`
- Kiểm tra Node version: `node -v`

### Tests fail?
- Đảm bảo dependencies đã install đầy đủ
- Kiểm tra cấu hình test environment
- Xem chi tiết lỗi trong test output

## 📄 License

This project is for educational purposes.

## 👥 Contributors

- Hoangson Le (Developer)

---

**Note**: Mã nguồn hiện tại đã được xóa sạch để bắt đầu phát triển theo TDD từ đầu. Tất cả file đều là template rỗng với TODO comments.

**Bắt đầu phát triển**: Xem [SETUP_ENVIRONMENT.md](./SETUP_ENVIRONMENT.md) để cài đặt môi trường đầy đủ.
