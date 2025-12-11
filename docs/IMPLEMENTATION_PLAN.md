# 📋 BÁO CÁO KẾ HOẠCH TRIỂN KHAI
## Dự án FloginFE_BE - Software Testing Assignment

**Ngày tạo**: 05/12/2025  
**Phiên bản**: 1.0  
**Trạng thái**: Chờ phê duyệt

---

## 📑 MỤC LỤC

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Phân tích Gap (Thiếu hụt)](#2-phân-tích-gap-thiếu-hụt)
3. [Kế hoạch triển khai](#3-kế-hoạch-triển-khai)
4. [Chi tiết từng Phase](#4-chi-tiết-từng-phase)
5. [Ước tính thời gian](#5-ước-tính-thời-gian)
6. [Rủi ro và giải pháp](#6-rủi-ro-và-giải-pháp)

---

## 1. TỔNG QUAN DỰ ÁN

### 1.1 Mục tiêu
Hoàn thiện dự án FloginFE_BE đáp ứng **100%** yêu cầu của assignment kiểm thử phần mềm, bao gồm:
- 80 điểm bắt buộc (Câu 1-5)
- 20 điểm bonus (Performance & Security Testing)

### 1.2 Quyết định kỹ thuật đã thống nhất

| Hạng mục | Quyết định |
|----------|------------|
| Login Field | **Username** (không dùng Email cho login) |
| Password Rule | 6-100 ký tự, bắt buộc có chữ VÀ số |
| Product Category | Thêm field enum với 7 categories |
| Test Database | H2 In-Memory (Unit/Integration), H2 File (Dev) |
| Production DB | Oracle (Auth) + PostgreSQL (Product) via Docker |
| E2E Test Server | Frontend dev server (port 3000) |
| Bonus | ✅ Thực hiện Performance & Security Testing |
| Môi trường chạy | WSL Ubuntu 24 (đã có Java, Maven, Node.js) |

---

## 2. PHÂN TÍCH GAP (THIẾU HỤT)

### 2.1 Backend Changes Required

| File/Component | Hiện tại | Cần thay đổi | Mức độ |
|----------------|----------|--------------|--------|
| `User.java` | Chỉ có email | Thêm `username` field | 🔴 Major |
| `LoginRequest.java` | `email`, `password` | Đổi thành `username`, `password` | 🔴 Major |
| `RegisterRequest.java` | `email`, `password`, `fullName` | Thêm `username` | 🟡 Medium |
| `AuthService.java` | Login by email | Login by username | 🔴 Major |
| `AuthService.java` | Password min 8 chars | 6-100 chars + chữ + số | 🟡 Medium |
| `Product.java` | Không có category | Thêm `category` enum | 🟡 Medium |
| `ProductRequest.java` | Không có category | Thêm `category` field | 🟡 Medium |
| `ProductService.java` | Không validate category | Validate category trong list | 🟡 Medium |
| `application.yaml` | Oracle + PostgreSQL | Thêm profile H2 cho test/dev | 🟡 Medium |

### 2.2 Frontend Changes Required

| File/Component | Hiện tại | Cần thay đổi | Mức độ |
|----------------|----------|--------------|--------|
| `LoginForm.jsx` | Input email | Input username | 🔴 Major |
| `RegisterForm.jsx` | Không có username | Thêm username input | 🟡 Medium |
| `validators.js` | `validateUsername` có sẵn | Giữ nguyên, cập nhật message | 🟢 Minor |
| `validators.js` | Password rule | Đồng bộ với backend (6-100) | 🟢 Minor |
| `authApi.js` | Login với email | Login với username | 🔴 Major |
| `ProductForm.jsx` | Không có category | Thêm category dropdown | 🟡 Medium |
| `productApi.js` | Không có category | Cập nhật request body | 🟢 Minor |

### 2.3 Test Files Status

#### Backend Tests

| File | Status | Action |
|------|--------|--------|
| `AuthServiceTest.java` | ✅ 758 lines | Cập nhật theo username login |
| `ProductServiceTest.java` | ✅ 842 lines | Thêm tests cho category |
| `LoginControllerTest.java` | ❌ Placeholder | **Viết mới hoàn chỉnh** |
| `ProductControllerTest.java` | ❌ Placeholder | **Viết mới hoàn chỉnh** |
| `AuthIntegrationTest.java` | ✅ 417 lines | Cập nhật theo username |
| `ProductIntegrationTest.java` | ❌ Placeholder | **Viết mới hoàn chỉnh** |

#### Frontend Tests

| File | Status | Action |
|------|--------|--------|
| `validators.test.js` | ✅ 571 lines | Cập nhật password rules |
| `LoginForm.test.jsx` | ✅ 423 lines | Cập nhật username tests |
| `ProductForm.test.jsx` | ✅ 576 lines | Thêm category tests |
| `authApi.test.js` | ✅ Exists | Cập nhật mock tests |
| `ProductFlow.test.jsx` | ❌ Placeholder | **Viết mới hoàn chỉnh** |

#### E2E Cypress Tests

| File | Status | Action |
|------|--------|--------|
| `login.cy.js` | ✅ 265 lines | Cập nhật username selectors |
| `product.cy.js` | ✅ 251 lines | Thêm category tests |
| `ProductPage.js` (POM) | ✅ 162 lines | Thêm category methods |

### 2.4 Documentation Status

| File | Status | Action |
|------|--------|--------|
| `login-requirements.md` | ✅ Exists | Cập nhật username rules |
| `login-scenarios.md` | ✅ Exists | Review & update |
| `login-test-cases.md` | ✅ Exists | Thêm format TC_LOGIN_XXX |
| `product-requirements.md` | ✅ Exists | Thêm category requirement |
| `product-scenarios.md` | ✅ Exists | Review & update |
| `product-test-cases.md` | ✅ Exists | Thêm format TC_PRODUCT_XXX |

### 2.5 CI/CD Status

| Component | Status | Action |
|-----------|--------|--------|
| Frontend tests | ✅ Running | - |
| Backend tests | ✅ Running | - |
| E2E Cypress | ❌ Not in CI | **Thêm vào workflow** |
| Coverage report | ❌ Not generated | **Thêm coverage upload** |

### 2.6 Bonus Features (Chưa có)

| Feature | Status | Action |
|---------|--------|--------|
| Performance Testing | ❌ Chưa có | **Tạo k6 scripts** |
| Security Testing | ❌ Chưa có | **Tạo security test suite** |

---

## 3. KẾ HOẠCH TRIỂN KHAI

### 3.1 Tổng quan Phases

```
┌─────────────────────────────────────────────────────────────────────┐
│                        IMPLEMENTATION PLAN                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  PHASE 1: Infrastructure & Core Changes                             │
│  ├── Sprint 1.1: Database & Config Setup                            │
│  └── Sprint 1.2: Core Entity Changes                                │
│                                                                     │
│  PHASE 2: Backend Implementation                                    │
│  ├── Sprint 2.1: Auth Module Changes                                │
│  ├── Sprint 2.2: Product Module Changes                             │
│  └── Sprint 2.3: Backend Unit Tests                                 │
│                                                                     │
│  PHASE 3: Frontend Implementation                                   │
│  ├── Sprint 3.1: Auth Components                                    │
│  ├── Sprint 3.2: Product Components                                 │
│  └── Sprint 3.3: Frontend Unit Tests                                │
│                                                                     │
│  PHASE 4: Integration & E2E Testing                                 │
│  ├── Sprint 4.1: Backend Integration Tests                          │
│  ├── Sprint 4.2: Frontend Integration Tests                         │
│  └── Sprint 4.3: Cypress E2E Tests                                  │
│                                                                     │
│  PHASE 5: CI/CD & Documentation                                     │
│  ├── Sprint 5.1: CI/CD Pipeline                                     │
│  └── Sprint 5.2: Documentation Update                               │
│                                                                     │
│  PHASE 6: Bonus - Performance & Security                            │
│  ├── Sprint 6.1: Performance Testing (k6)                           │
│  └── Sprint 6.2: Security Testing                                   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 4. CHI TIẾT TỪNG PHASE

### 📦 PHASE 1: Infrastructure & Core Changes

#### Sprint 1.1: Database & Config Setup

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T1.1.1 | Tạo Category enum | `backend/.../entity/product/Category.java` | High |
| T1.1.2 | Tạo application-test.yml với H2 | `backend/src/main/resources/application-test.yml` | High |
| T1.1.3 | Tạo application-dev.yml với H2 | `backend/src/main/resources/application-dev.yml` | High |
| T1.1.4 | Tạo docker-compose.db.yml | `docker-compose.db.yml` | Medium |
| T1.1.5 | Cập nhật TestConfig | `backend/.../TestConfig.java` | High |

#### Sprint 1.2: Core Entity Changes

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T1.2.1 | Thêm username vào User entity | `backend/.../entity/auth/User.java` | Critical |
| T1.2.2 | Thêm category vào Product entity | `backend/.../entity/product/Product.java` | High |
| T1.2.3 | Cập nhật UserRepository | `backend/.../repository/auth/UserRepository.java` | Critical |

---

### 📦 PHASE 2: Backend Implementation

#### Sprint 2.1: Auth Module Changes

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T2.1.1 | Cập nhật LoginRequest (username) | `backend/.../dto/auth/LoginRequest.java` | Critical |
| T2.1.2 | Cập nhật RegisterRequest (+username) | `backend/.../dto/auth/RegisterRequest.java` | Critical |
| T2.1.3 | Cập nhật AuthService validation | `backend/.../service/auth/AuthService.java` | Critical |
| T2.1.4 | Cập nhật AuthController | `backend/.../controller/AuthController.java` | High |

**Validation Rules cho Username:**
```java
// Username: 3-50 ký tự, chỉ chứa a-z, A-Z, 0-9, -, ., _
private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");
```

**Validation Rules cho Password:**
```java
// Password: 6-100 ký tự, bắt buộc có chữ VÀ số
private void validatePassword(String password) {
    if (password.length() < 6 || password.length() > 100) {
        throw new BadRequestException("Password must be 6-100 characters");
    }
    if (!password.matches(".*[a-zA-Z].*")) {
        throw new BadRequestException("Password must contain at least one letter");
    }
    if (!password.matches(".*[0-9].*")) {
        throw new BadRequestException("Password must contain at least one number");
    }
}
```

#### Sprint 2.2: Product Module Changes

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T2.2.1 | Cập nhật ProductRequest (+category) | `backend/.../dto/product/ProductRequest.java` | High |
| T2.2.2 | Cập nhật ProductResponse (+category) | `backend/.../dto/product/ProductResponse.java` | High |
| T2.2.3 | Cập nhật ProductMapper | `backend/.../dto/product/ProductMapper.java` | High |
| T2.2.4 | Cập nhật ProductService validation | `backend/.../service/product/ProductService.java` | High |

**Category Enum:**
```java
public enum Category {
    ELECTRONICS,    // Điện tử
    CLOTHING,       // Thời trang
    FOOD,           // Thực phẩm
    BOOKS,          // Sách
    SPORTS,         // Thể thao
    HOME,           // Gia dụng
    OTHER           // Khác
}
```

#### Sprint 2.3: Backend Unit Tests

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T2.3.1 | Cập nhật AuthServiceTest | `backend/.../service/auth/AuthServiceTest.java` | Critical |
| T2.3.2 | Cập nhật ProductServiceTest | `backend/.../service/product/ProductServiceTest.java` | High |
| T2.3.3 | **Viết mới** LoginControllerTest | `backend/.../unit/controller/LoginControllerTest.java` | Critical |
| T2.3.4 | **Viết mới** ProductControllerTest | `backend/.../unit/controller/ProductControllerTest.java` | High |

---

### 📦 PHASE 3: Frontend Implementation

#### Sprint 3.1: Auth Components

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T3.1.1 | Cập nhật LoginForm (username input) | `frontend/src/components/auth/LoginForm.jsx` | Critical |
| T3.1.2 | Cập nhật RegisterForm (+username) | `frontend/src/components/auth/RegisterForm.jsx` | Critical |
| T3.1.3 | Cập nhật authApi service | `frontend/src/services/authApi.js` | Critical |
| T3.1.4 | Cập nhật validators (password rules) | `frontend/src/utils/validators.js` | High |

#### Sprint 3.2: Product Components

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T3.2.1 | Thêm category dropdown vào ProductForm | `frontend/src/components/product/ProductForm.jsx` | High |
| T3.2.2 | Hiển thị category trong ProductItem | `frontend/src/components/product/ProductItem.jsx` | Medium |
| T3.2.3 | Cập nhật productApi | `frontend/src/services/productApi.js` | High |
| T3.2.4 | Cập nhật validateProduct | `frontend/src/utils/validators.js` | High |

#### Sprint 3.3: Frontend Unit Tests

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T3.3.1 | Cập nhật validators.test.js | `frontend/src/tests/unit/validators.test.js` | High |
| T3.3.2 | Cập nhật LoginForm.test.jsx | `frontend/src/tests/unit/LoginForm.test.jsx` | High |
| T3.3.3 | Cập nhật ProductForm.test.jsx | `frontend/src/tests/unit/ProductForm.test.jsx` | High |
| T3.3.4 | Cập nhật authApi.test.js | `frontend/src/tests/unit/authApi.test.js` | High |

---

### 📦 PHASE 4: Integration & E2E Testing

#### Sprint 4.1: Backend Integration Tests

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T4.1.1 | Cập nhật AuthIntegrationTest | `backend/.../integration/AuthIntegrationTest.java` | High |
| T4.1.2 | **Viết mới** ProductIntegrationTest | `backend/.../integration/ProductIntegrationTest.java` | Critical |

#### Sprint 4.2: Frontend Integration Tests

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T4.2.1 | **Viết mới** ProductFlow.test.jsx | `frontend/src/tests/integration/ProductFlow.test.jsx` | Critical |
| T4.2.2 | **Viết mới** LoginFlow.test.jsx | `frontend/src/tests/integration/LoginFlow.test.jsx` | High |

#### Sprint 4.3: Cypress E2E Tests

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T4.3.1 | Cập nhật login.cy.js (username) | `frontend/cypress/e2e/login.cy.js` | High |
| T4.3.2 | Cập nhật product.cy.js (+category) | `frontend/cypress/e2e/product.cy.js` | High |
| T4.3.3 | Cập nhật ProductPage.js POM | `frontend/cypress/pages/ProductPage.js` | Medium |
| T4.3.4 | Tạo LoginPage.js POM | `frontend/cypress/pages/LoginPage.js` | Medium |
| T4.3.5 | Cập nhật cypress.config.js (baseUrl 3000) | `frontend/cypress.config.js` | High |

---

### 📦 PHASE 5: CI/CD & Documentation

#### Sprint 5.1: CI/CD Pipeline

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T5.1.1 | Thêm E2E tests vào CI | `.github/workflows/ci.yml` | High |
| T5.1.2 | Thêm coverage report | `.github/workflows/ci.yml` | High |
| T5.1.3 | Tạo workflow riêng cho login tests | `.github/workflows/login-tests.yml` | Medium |
| T5.1.4 | Tạo workflow riêng cho product tests | `.github/workflows/product-tests.yml` | Medium |

#### Sprint 5.2: Documentation Update

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T5.2.1 | Cập nhật login-requirements.md | `docs/test-cases/login-requirements.md` | High |
| T5.2.2 | Cập nhật login-scenarios.md | `docs/test-cases/login-scenarios.md` | High |
| T5.2.3 | Cập nhật login-test-cases.md | `docs/test-cases/login-test-cases.md` | High |
| T5.2.4 | Cập nhật product-requirements.md | `docs/test-cases/product-requirements.md` | High |
| T5.2.5 | Cập nhật product-scenarios.md | `docs/test-cases/product-scenarios.md` | High |
| T5.2.6 | Cập nhật product-test-cases.md | `docs/test-cases/product-test-cases.md` | High |
| T5.2.7 | Cập nhật coverage reports | `docs/coverage/*.md` | Medium |
| T5.2.8 | Cập nhật README.md | `README.md` | Medium |
| T5.2.9 | Cập nhật DEVELOPMENT.md | `DEVELOPMENT.md` | Medium |

---

### 📦 PHASE 6: Bonus - Performance & Security Testing

#### Sprint 6.1: Performance Testing (k6)

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T6.1.1 | Setup k6 configuration | `performance/k6.config.js` | High |
| T6.1.2 | Login API load test (100, 500, 1000 users) | `performance/login-load-test.js` | High |
| T6.1.3 | Login API stress test | `performance/login-stress-test.js` | High |
| T6.1.4 | Product API load test | `performance/product-load-test.js` | High |
| T6.1.5 | Product API stress test | `performance/product-stress-test.js` | High |
| T6.1.6 | Performance analysis report | `docs/performance-report.md` | High |

#### Sprint 6.2: Security Testing

| Task ID | Task | Files | Priority |
|---------|------|-------|----------|
| T6.2.1 | SQL Injection tests | `security/sql-injection.test.js` | Critical |
| T6.2.2 | XSS (Cross-Site Scripting) tests | `security/xss.test.js` | Critical |
| T6.2.3 | CSRF tests | `security/csrf.test.js` | High |
| T6.2.4 | Authentication bypass tests | `security/auth-bypass.test.js` | Critical |
| T6.2.5 | Input validation tests | `security/input-validation.test.js` | High |
| T6.2.6 | Security headers tests | `security/security-headers.test.js` | Medium |
| T6.2.7 | Password security tests | `security/password-security.test.js` | High |
| T6.2.8 | Security analysis report | `docs/security-report.md` | High |

---

## 5. ƯỚC TÍNH THỜI GIAN

### 5.1 Tổng quan

| Phase | Sprints | Tasks | Estimated Time |
|-------|---------|-------|----------------|
| Phase 1 | 2 | 8 | ~2 giờ |
| Phase 2 | 3 | 12 | ~4 giờ |
| Phase 3 | 3 | 12 | ~3 giờ |
| Phase 4 | 3 | 9 | ~4 giờ |
| Phase 5 | 2 | 13 | ~3 giờ |
| Phase 6 | 2 | 14 | ~4 giờ |
| **TOTAL** | **15** | **68** | **~20 giờ** |

### 5.2 Thứ tự ưu tiên thực hiện

```
[CRITICAL] ──────────────────────────────────────────────────────────
    │
    ├── Phase 1: Infrastructure (PHẢI làm trước)
    │   └── Không thể test nếu chưa có database config
    │
    ├── Phase 2.1-2.2: Backend Core Changes
    │   └── Frontend phụ thuộc vào API
    │
    └── Phase 3.1-3.2: Frontend Core Changes
        └── Tests phụ thuộc vào components

[HIGH] ────────────────────────────────────────────────────────────────
    │
    ├── Phase 2.3: Backend Unit Tests
    ├── Phase 3.3: Frontend Unit Tests
    └── Phase 4: Integration & E2E Tests

[MEDIUM] ──────────────────────────────────────────────────────────────
    │
    ├── Phase 5: CI/CD & Documentation
    └── Phase 6: Bonus Testing
```

---

## 6. RỦI RO VÀ GIẢI PHÁP

| Rủi ro | Mức độ | Giải pháp |
|--------|--------|-----------|
| H2 không tương thích với Oracle syntax | Medium | Sử dụng Hibernate dialect phù hợp |
| Cypress fails vì API chưa ready | High | Mock API trong E2E tests |
| WSL network issues với localhost | Medium | Sử dụng `host.docker.internal` hoặc IP |
| Coverage không đạt 85%+ | Medium | Viết thêm edge case tests |
| k6 không chạy được trên WSL | Low | Cài k6 native trên WSL |

---

## 7. CHECKLIST PHÊ DUYỆT

Vui lòng xác nhận các mục sau trước khi bắt đầu:

- [ ] Đồng ý với cấu trúc Phases và Sprints
- [ ] Đồng ý với danh sách Tasks
- [ ] Đồng ý với ước tính thời gian
- [ ] Đồng ý với thứ tự ưu tiên
- [ ] Sẵn sàng bắt đầu Phase 1

---

## 8. LỆNH CHẠY (WSL Ubuntu)

### Chạy Backend Tests
```bash
cd /mnt/c/Users/Hoangson\ Le/Documents/SoftwareTesting/assignment2/FloginFE_BE/backend
mvn clean test -Dspring.profiles.active=test
```

### Chạy Frontend Tests
```bash
cd /mnt/c/Users/Hoangson\ Le/Documents/SoftwareTesting/assignment2/FloginFE_BE/frontend
npm test
```

### Chạy Cypress E2E
```bash
cd /mnt/c/Users/Hoangson\ Le/Documents/SoftwareTesting/assignment2/FloginFE_BE/frontend
npm run start &  # Start dev server
npm run cy:run   # Run Cypress tests
```

### Chạy Docker Databases
```bash
cd /mnt/c/Users/Hoangson\ Le/Documents/SoftwareTesting/assignment2/FloginFE_BE
docker-compose -f docker-compose.db.yml up -d
```

---

**Nếu bạn đồng ý với kế hoạch này, hãy trả lời "APPROVED" để tôi bắt đầu triển khai từ Phase 1!**
