# Login - Phân tích Yêu cầu Chức năng

## 1. Tổng quan
Chức năng Login cho phép người dùng đăng nhập vào hệ thống FloginFE_BE bằng email và mật khẩu. Hệ thống sử dụng JWT token để xác thực và quản lý phiên làm việc.

## 2. Yêu cầu Chức năng

### 2.1 Authentication Flow

```
Người dùng → Nhập Email & Password → Validation → API Call → Backend Authentication
                                          ↓                           ↓
                                    Validation Error           Database Check
                                          ↓                           ↓
                                    Show Error Message     Password Verification
                                                                      ↓
                                                            Success / Failure
                                                                      ↓
                                                        Return JWT Token / Error
                                                                      ↓
                                                        Save Token → Redirect
```

### 2.2 Validation Rules

#### Email Validation
- **Bắt buộc**: Email không được để trống
- **Format**: Phải đúng định dạng email (có @ và domain)
  - Regex: `/^[^\s@]+@[^\s@]+\.[^\s@]+$/`
  - Ví dụ hợp lệ: `user@example.com`, `test.user@domain.co.uk`
  - Ví dụ không hợp lệ: `user`, `user@`, `@domain.com`, `user @domain.com`
- **Normalization**: Email được chuyển về lowercase trước khi xử lý
- **Độ dài**: Tối đa 255 ký tự

#### Password Validation
- **Bắt buộc**: Password không được để trống
- **Độ dài**: Tối thiểu 8 ký tự
- **Độ phức tạp**: 
  - Phải chứa ít nhất 1 chữ cái (a-z, A-Z)
  - Phải chứa ít nhất 1 chữ số (0-9)
  - Không yêu cầu ký tự đặc biệt (tùy chọn)
- **Ví dụ hợp lệ**: `Password123`, `Test1234`, `MyPass99`
- **Ví dụ không hợp lệ**: `pass` (quá ngắn), `password` (không có số), `12345678` (không có chữ)

## 3. API Specification

### 3.1 Login Endpoint

**Endpoint**: `POST /api/auth/login`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

**Success Response** (200 OK):
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com",
  "fullName": "User Name"
}
```

**Error Responses**:

1. **400 Bad Request** - Validation Error:
```json
{
  "message": "Validation failed",
  "errors": {
    "email": "Email is required",
    "password": "Password must be at least 8 characters"
  }
}
```

2. **400 Bad Request** - Invalid Credentials:
```json
{
  "message": "Invalid email or password"
}
```

3. **500 Internal Server Error**:
```json
{
  "message": "An error occurred during login",
  "error": "Internal server error"
}
```

## 4. Error Handling

### 4.1 Client-side Validation Errors
- Email rỗng: "Email không được để trống"
- Email sai format: "Email không đúng định dạng"
- Password rỗng: "Mật khẩu không được để trống"
- Password quá ngắn: "Mật khẩu phải có ít nhất 8 ký tự"
- Password không đủ phức tạp: "Mật khẩu phải chứa chữ và số"

### 4.2 Server-side Errors
- Email không tồn tại: "Email hoặc mật khẩu không đúng" (security: không tiết lộ email tồn tại hay không)
- Password sai: "Email hoặc mật khẩu không đúng"
- Network error: "Không thể kết nối đến server. Vui lòng thử lại"
- Server error: "Đã xảy ra lỗi. Vui lòng thử lại sau"

## 5. Security Requirements

### 5.1 Password Security
- Password được hash bằng BCrypt trên backend (không lưu plain text)
- Salt rounds: 10
- Password không được hiển thị trong response

### 5.2 Token Management
- JWT token được trả về sau khi login thành công
- Token được lưu trong localStorage với key `token`
- Token có thời gian hết hạn (expiration time)
- Token phải được gửi kèm trong header cho các API cần authentication

### 5.3 CORS Configuration
- Backend cho phép CORS từ frontend origin
- Credentials được phép gửi kèm request

## 6. User Experience Requirements

### 6.1 UI/UX
- Form hiển thị rõ ràng với labels cho Email và Password
- Password field dạng masked (****)
- Submit button disabled khi đang xử lý request
- Loading indicator hiển thị trong quá trình login
- Error messages hiển thị ngay dưới field tương ứng
- Success message có thể hiển thị trước khi redirect

### 6.2 Navigation
- Sau khi login thành công, redirect đến trang Dashboard hoặc Home
- Link đến trang Register nếu chưa có account
- Link "Forgot Password" (nếu có)

### 6.3 Performance
- Login request phải hoàn thành trong vòng 2 giây (normal conditions)
- Timeout cho API call: 10 giây
- Retry mechanism: Không tự động retry (user phải click lại)

## 7. Business Rules

### 7.1 Login Attempts
- Không giới hạn số lần thử (hiện tại)
- Có thể implement rate limiting trong tương lai

### 7.2 Session Management
- Một user có thể login từ nhiều devices cùng lúc
- Mỗi login tạo một token mới
- Token cũ vẫn valid cho đến khi hết hạn

### 7.3 Remember Me
- Không có tính năng "Remember Me" trong version hiện tại
- Token được lưu trong localStorage (persistent)

## 8. Test Data Requirements

### 8.1 Valid Test User
```
Email: test@example.com
Password: Test1234
Expected: Login successful
```

### 8.2 Invalid Credentials
```
Email: wrong@example.com
Password: WrongPass123
Expected: Login failed
```

### 8.3 Edge Cases
- Very long email (> 255 chars)
- Special characters in password
- Unicode characters (tiếng Việt) trong email/password
- SQL injection attempts
- XSS attempts

## 9. Dependencies

### 9.1 Frontend Dependencies
- React 18.3.1
- Axios (HTTP client)
- React Router (navigation)

### 9.2 Backend Dependencies
- Spring Boot 3.5.7
- Spring Security
- BCrypt (password hashing)
- JWT library
- Oracle Database (user data storage)

## 10. Acceptance Criteria

✅ User có thể login thành công với email và password hợp lệ  
✅ Validation errors được hiển thị rõ ràng cho input không hợp lệ  
✅ JWT token được lưu vào localStorage sau khi login thành công  
✅ User được redirect đến dashboard sau khi login thành công  
✅ Error message được hiển thị khi credentials không đúng  
✅ Submit button disabled trong quá trình xử lý  
✅ API call timeout sau 10 giây và hiển thị error message  
✅ Password được mask trong input field  
✅ Email được normalize (lowercase) trước khi gửi lên server  

---

**Người tạo**: Nhóm FloginFE_BE  
**Ngày tạo**: 30/11/2024  
**Version**: 1.0
