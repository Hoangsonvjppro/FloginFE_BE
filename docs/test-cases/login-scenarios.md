# Login - Test Scenarios

## Danh sách Test Scenarios

Tổng số: **15 scenarios**  
Phân loại: Critical (4), High (5), Medium (4), Low (2)

---

## A. HAPPY PATH - Đăng nhập thành công

### TS_LOGIN_001: Đăng nhập thành công với credentials hợp lệ
- **Priority**: 🔴 **Critical**
- **Mô tả**: User nhập email và password hợp lệ, hệ thống xác thực thành công
- **Preconditions**: 
  - User account tồn tại trong database
  - Application đang chạy
- **Test Steps**:
  1. Truy cập trang login
  2. Nhập email hợp lệ: `test@example.com`
  3. Nhập password hợp lệ: `Test1234`
  4. Click nút "Login"
- **Expected Result**:
  - API trả về 200 OK
  - JWT token được lưu vào localStorage
  - Success message hiển thị
  - Redirect đến dashboard
- **Actual Result**: (Sẽ được điền sau khi test)
- **Status**: Not Run

---

### TS_LOGIN_002: Đăng nhập với email uppercase được normalize
- **Priority**: 🟠 **High**
- **Mô tả**: Email viết hoa được chuyển về lowercase và login thành công
- **Test Steps**:
  1. Nhập email: `TEST@EXAMPLE.COM` (uppercase)
  2. Nhập password: `Test1234`
  3. Click Login
- **Expected Result**:
  - Email được normalize thành `test@example.com`
  - Login thành công như bình thường
- **Status**: Not Run

---

## B. NEGATIVE TESTS - Validation Errors

### TS_LOGIN_003: Email rỗng - hiển thị lỗi validation
- **Priority**: 🔴 **Critical**
- **Mô tả**: User submit form mà không nhập email
- **Test Steps**:
  1. Để trống email field
  2. Nhập password: `Test1234`
  3. Click Login
- **Expected Result**:
  - Form validation chặn submit
  - Error message: "Email không được để trống"
  - API không được gọi
  - Vẫn ở trang login
- **Status**: Not Run

---

### TS_LOGIN_004: Password rỗng - hiển thị lỗi validation
- **Priority**: 🔴 **Critical**
- **Mô tả**: User submit form mà không nhập password
- **Test Steps**:
  1. Nhập email: `test@example.com`
  2. Để trống password field
  3. Click Login
- **Expected Result**:
  - Form validation chặn submit
  - Error message: "Mật khẩu không được để trống"
  - API không được gọi
- **Status**: Not Run

---

### TS_LOGIN_005: Email sai định dạng - validation error
- **Priority**: 🟠 **High**
- **Mô tả**: Email không có @ hoặc domain
- **Test Data**:
  - `user` (không có @)
  - `user@` (không có domain)
  - `@domain.com` (không có local part)
  - `user @domain.com` (có space)
- **Test Steps**:
  1. Nhập email không hợp lệ
  2. Nhập password: `Test1234`
  3. Click Login
- **Expected Result**:
  - Validation error: "Email không đúng định dạng"
  - Submit bị chặn
- **Status**: Not Run

---

### TS_LOGIN_006: Password quá ngắn (< 8 ký tự)
- **Priority**: 🟠 **High**
- **Mô tả**: Password có độ dài nhỏ hơn 8 ký tự
- **Test Data**: `Pass12` (6 ký tự)
- **Test Steps**:
  1. Nhập email: `test@example.com`
  2. Nhập password: `Pass12`
  3. Click Login
- **Expected Result**:
  - Client validation: "Mật khẩu phải có ít nhất 8 ký tự"
  - Hoặc server trả về 400 Bad Request
- **Status**: Not Run

---

### TS_LOGIN_007: Password không chứa chữ (only numbers)
- **Priority**: 🟡 **Medium**
- **Mô tả**: Password chỉ có số, không có chữ cái
- **Test Data**: `12345678`
- **Test Steps**:
  1. Nhập email: `test@example.com`
  2. Nhập password: `12345678`
  3. Click Login
- **Expected Result**:
  - Validation error: "Mật khẩu phải chứa cả chữ và số"
- **Status**: Not Run

---

### TS_LOGIN_008: Password không chứa số (only letters)
- **Priority**: 🟡 **Medium**
- **Mô tả**: Password chỉ có chữ, không có số
- **Test Data**: `password`
- **Test Steps**:
  1. Nhập email: `test@example.com`
  2. Nhập password: `password`
  3. Click Login
- **Expected Result**:
  - Validation error: "Mật khẩu phải chứa cả chữ và số"
- **Status**: Not Run

---

### TS_LOGIN_009: Cả email và password đều rỗng
- **Priority**: 🟠 **High**
- **Mô tả**: User click submit mà không nhập gì
- **Test Steps**:
  1. Để trống tất cả fields
  2. Click Login
- **Expected Result**:
  - Multiple validation errors hiển thị
  - Email error: "Email không được để trống"
  - Password error: "Mật khẩu không được để trống"
- **Status**: Not Run

---

## C. AUTHENTICATION FAILURES

### TS_LOGIN_010: Email không tồn tại trong database
- **Priority**: 🔴 **Critical**
- **Mô tả**: User nhập email chưa được đăng ký
- **Test Data**: `nonexistent@example.com`
- **Test Steps**:
  1. Nhập email: `nonexistent@example.com`
  2. Nhập password: `Test1234`
  3. Click Login
- **Expected Result**:
  - API trả về 400 Bad Request
  - Error message: "Email hoặc mật khẩu không đúng"
  - Không tiết lộ email tồn tại hay không (security)
  - Vẫn ở trang login
- **Status**: Not Run

---

### TS_LOGIN_011: Password sai
- **Priority**: 🟠 **High**
- **Mô tả**: Email đúng nhưng password sai
- **Test Steps**:
  1. Nhập email hợp lệ: `test@example.com`
  2. Nhập password sai: `WrongPassword123`
  3. Click Login
- **Expected Result**:
  - API trả về 400 Bad Request
  - Error message: "Email hoặc mật khẩu không đúng"
  - Token không được lưu
  - Không redirect
- **Status**: Not Run

---

## D. BOUNDARY TESTS

### TS_LOGIN_012: Email có độ dài tối đa (255 ký tự)
- **Priority**: 🟡 **Medium**
- **Mô tả**: Test với email dài nhất có thể
- **Test Data**: Email 255 ký tự hợp lệ
- **Test Steps**:
  1. Nhập email 255 ký tự
  2. Nhập password hợp lệ
  3. Click Login
- **Expected Result**:
  - Nếu email tồn tại: Login thành công
  - Nếu không: "Email hoặc mật khẩu không đúng"
  - Không có lỗi về độ dài
- **Status**: Not Run

---

### TS_LOGIN_013: Password có độ dài tối thiểu (8 ký tự)
- **Priority**: 🟡 **Medium**
- **Mô tả**: Test boundary condition - password đúng 8 ký tự
- **Test Data**: `Test1234` (8 ký tự)
- **Test Steps**:
  1. Nhập email: `test@example.com`
  2. Nhập password: `Test1234`
  3. Click Login
- **Expected Result**:
  - Validation pass
  - Login thành công
- **Status**: Not Run

---

## E. EDGE CASES & SECURITY

### TS_LOGIN_014: SQL Injection attempt
- **Priority**: ⚪ **Low** (security test)
- **Mô tả**: Thử inject SQL code trong email field
- **Test Data**: 
  - Email: `' OR '1'='1`
  - Email: `admin'--`
  - Email: `test@example.com'; DROP TABLE users;--`
- **Test Steps**:
  1. Nhập SQL injection payload vào email
  2. Nhập password bất kỳ
  3. Click Login
- **Expected Result**:
  - Input được sanitized
  - Login failed với "Email không đúng định dạng" hoặc "Email hoặc mật khẩu không đúng"
  - Database không bị ảnh hưởng
- **Status**: Not Run

---

### TS_LOGIN_015: XSS attempt
- **Priority**: ⚪ **Low** (security test)
- **Mô tả**: Thử inject JavaScript code
- **Test Data**: 
  - Email: `<script>alert('XSS')</script>@test.com`
  - Password: `<script>alert('XSS')</script>`
- **Test Steps**:
  1. Nhập XSS payload
  2. Click Login
- **Expected Result**:
  - Script không được execute
  - Input được escape/sanitize
  - Login failed với validation error
- **Status**: Not Run

---

## Tổng kết Priority

### 🔴 Critical (4 scenarios)
- TS_LOGIN_001: Login thành công
- TS_LOGIN_003: Email rỗng
- TS_LOGIN_004: Password rỗng
- TS_LOGIN_010: Email không tồn tại

### 🟠 High (5 scenarios)
- TS_LOGIN_002: Email normalize
- TS_LOGIN_005: Email sai format
- TS_LOGIN_006: Password quá ngắn
- TS_LOGIN_009: Cả 2 field rỗng
- TS_LOGIN_011: Password sai

### 🟡 Medium (4 scenarios)
- TS_LOGIN_007: Password không có chữ
- TS_LOGIN_008: Password không có số
- TS_LOGIN_012: Email max length
- TS_LOGIN_013: Password min length

### ⚪ Low (2 scenarios)
- TS_LOGIN_014: SQL Injection
- TS_LOGIN_015: XSS attempt

---

## Test Coverage Analysis

**Functional Coverage**:
- ✅ Happy path (login thành công)
- ✅ Validation errors (email, password)
- ✅ Authentication failures (wrong credentials)
- ✅ Boundary conditions (min/max length)
- ✅ Security tests (injection attacks)

**Non-functional Coverage**:
- ⏳ Performance (sẽ test riêng)
- ⏳ Load testing (sẽ test riêng)
- ⏳ Concurrent logins (sẽ test riêng)

---

**Người tạo**: Nhóm FloginFE_BE  
**Ngày tạo**: 30/11/2024  
**Version**: 1.0
