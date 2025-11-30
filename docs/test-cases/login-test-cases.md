# Login - Test Cases Chi tiết

## TC_LOGIN_001: Đăng nhập thành công với credentials hợp lệ

| Thuộc tính | Giá trị |
|------------|---------|
| **Test Case ID** | TC_LOGIN_001 |
| **Test Name** | Đăng nhập thành công với credentials hợp lệ |
| **Priority** | 🔴 Critical |
| **Feature** | Authentication - Login |
| **Test Type** | Functional, Positive |
| **Prerequisites** | - User account đã tồn tại trong database<br>- Backend API đang chạy trên port 8081<br>- Frontend đang chạy trên port 8080<br>- Database có sẵn test user: test@example.com |
| **Test Data** | **Email**: `test@example.com`<br>**Password**: `Test1234` |

### Test Steps

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to login page: `http://localhost:8080/login` | - Login form hiển thị<br>- Email field visible<br>- Password field visible<br>- Login button visible |
| 2 | Nhập email: `test@example.com` vào Email field | - Email được hiển thị trong field<br>- Không có validation error |
| 3 | Nhập password: `Test1234` vào Password field | - Password được masked (hiển thị ****)<br>- Không có validation error |
| 4 | Click nút "Login" | - Loading indicator hiển thị<br>- Button disabled trong quá trình xử lý |
| 5 | Chờ API response | - API call tới `POST /api/auth/login` thành công<br>- Response status: 200 OK<br>- Response body chứa token và user info |
| 6 | Verify localStorage | - Token được lưu vào localStorage với key `token`<br>- Token có format JWT hợp lệ |
| 7 | Verify redirect | - User được redirect tới dashboard (`/` hoặc `/dashboard`)<br>- URL không còn `/login` |
| 8 | Verify UI state | - User menu/avatar hiển thị<br>- Logout button có sẵn |

### Expected Results

**API Response**:
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "test@example.com",
  "fullName": "Test User"
}
```

**HTTP Status**: 200 OK  
**Response Time**: < 2 seconds  
**UI Behavior**: Success message hiển thị, redirect sau 0.5-1 giây

### Actual Result
_(Sẽ được điền sau khi execute test)_

### Test Evidence
_(Screenshots sẽ được đính kèm)_
- Screenshot 1: Login form
- Screenshot 2: Filled credentials
- Screenshot 3: API response in Network tab
- Screenshot 4: LocalStorage với token
- Screenshot 5: Dashboard after redirect

### Status
- [ ] Not Run
- [ ] Pass
- [ ] Fail
- [ ] Blocked

### Test Date: ___________
### Tested By: ___________
### Notes: ___________

---

## TC_LOGIN_002: Email hoặc password sai - Hiển thị lỗi authentication

| Thuộc tính | Giá trị |
|------------|---------|
| **Test Case ID** | TC_LOGIN_002 |
| **Test Name** | Login thất bại với credentials không đúng |
| **Priority** | 🔴 Critical |
| **Feature** | Authentication - Login |
| **Test Type** | Functional, Negative |
| **Prerequisites** | - Backend API đang chạy<br>- Frontend đang chạy<br>- User chưa login |
| **Test Data** | **Scenario 1**:<br>Email: `wrong@example.com`<br>Password: `Test1234`<br><br>**Scenario 2**:<br>Email: `test@example.com`<br>Password: `WrongPassword123` |

### Test Steps

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to `/login` | Login form hiển thị |
| 2 | Nhập email: `wrong@example.com` | Email filled |
| 3 | Nhập password: `Test1234` | Password filled |
| 4 | Click "Login" button | - Loading indicator hiển thị<br>- Button disabled |
| 5 | Chờ API response | - API trả về 400 Bad Request<br>- Response chứa error message |
| 6 | Verify error display | - Error message hiển thị: "Email hoặc mật khẩu không đúng"<br>- Message có thể ở dạng toast hoặc inline |
| 7 | Verify UI state | - Vẫn ở trang login<br>- Form fields vẫn giữ giá trị<br>- Button được enable lại |
| 8 | Verify localStorage | - Không có token trong localStorage<br>- User vẫn ở trạng thái logged out |
| 9 | Repeat với Scenario 2 | Kết quả tương tự |

### Expected Results

**API Response**:
```json
{
  "message": "Invalid email or password"
}
```

**HTTP Status**: 400 Bad Request  
**UI Behavior**: 
- Error message hiển thị trong 3-5 giây (nếu là toast)
- Hoặc hiển thị persistent cho đến khi user nhập lại

**Security Note**: 
- Message KHÔNG tiết lộ email có tồn tại hay không
- Message giống nhau cho cả email sai và password sai

### Actual Result
_(To be filled)_

### Status
- [ ] Not Run
- [ ] Pass
- [ ] Fail

---

## TC_LOGIN_003: Validation - Email rỗng

| Thuộc tính | Giá trị |
|------------|---------|
| **Test Case ID** | TC_LOGIN_003 |
| **Test Name** | Validation error khi email để trống |
| **Priority** | 🔴 Critical |
| **Feature** | Form Validation |
| **Test Type** | Functional, Negative |
| **Prerequisites** | - Frontend đang chạy<br>- User ở trang login |
| **Test Data** | **Email**: _(empty)_<br>**Password**: `Test1234` |

### Test Steps

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to `/login` | Form hiển thị |
| 2 | Để trống Email field | Field empty |
| 3 | Nhập password: `Test1234` | Password filled |
| 4 | Click "Login" | - Client-side validation chặn submit<br>- Hoặc HTML5 validation hiển thị |
| 5 | Verify error message | Error hiển thị: "Email không được để trống" hoặc "Email is required" |
| 6 | Verify API call | - API KHÔNG được gọi<br>- Verify trong Network tab |
| 7 | Verify field highlighting | Email field được highlight (border đỏ hoặc tương tự) |

### Expected Results

**Validation Method**: Client-side (React form validation)  
**Error Message**: "Email không được để trống" hoặc "Email is required"  
**Error Display**: Ngay dưới Email field  
**API Call**: Không có (validation chặn trước)  
**Form Submit**: Blocked

### Actual Result
_(To be filled)_

### Status
- [ ] Not Run
- [ ] Pass
- [ ] Fail

---

## TC_LOGIN_004: Validation - Password rỗng

| Thuộc tính | Giá trị |
|------------|---------|
| **Test Case ID** | TC_LOGIN_004 |
| **Test Name** | Validation error khi password để trống |
| **Priority** | 🔴 Critical |
| **Feature** | Form Validation |
| **Test Type** | Functional, Negative |
| **Prerequisites** | - Frontend đang chạy |
| **Test Data** | **Email**: `test@example.com`<br>**Password**: _(empty)_ |

### Test Steps

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to `/login` | Form hiển thị |
| 2 | Nhập email: `test@example.com` | Email filled |
| 3 | Để trống Password field | Field empty |
| 4 | Click "Login" | Validation error hiển thị |
| 5 | Verify error message | "Mật khẩu không được để trống" hoặc "Password is required" |
| 6 | Verify API not called | No network request trong Network tab |

### Expected Results

**Validation**: Client-side  
**Error Message**: "Mật khẩu không được để trống"  
**Form Behavior**: Submit blocked  
**API**: Not called

### Actual Result
_(To be filled)_

### Status
- [ ] Not Run
- [ ] Pass
- [ ] Fail

---

## TC_LOGIN_005: Validation - Email sai định dạng

| Thuộc tính | Giá trị |
|------------|---------|
| **Test Case ID** | TC_LOGIN_005 |
| **Test Name** | Validation error với email không đúng format |
| **Priority** | 🟠 High |
| **Feature** | Form Validation |
| **Test Type** | Functional, Negative |
| **Prerequisites** | - Frontend đang chạy |
| **Test Data** | **Test cases**:<br>1. `user` (no @ or domain)<br>2. `user@` (no domain)<br>3. `@domain.com` (no local part)<br>4. `user @domain.com` (space in email)<br>5. `user@domain` (no TLD)<br><br>**Password**: `Test1234` |

### Test Steps

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to `/login` | Form hiển thị |
| 2 | Nhập invalid email (test case 1): `user` | Email filled |
| 3 | Nhập password: `Test1234` | Password filled |
| 4 | Click "Login" hoặc blur khỏi email field | Validation error hiển thị |
| 5 | Verify error | "Email không đúng định dạng" hoặc "Invalid email format" |
| 6 | Verify submit blocked | Form không submit nếu click Login |
| 7 | Repeat bước 2-6 cho các test cases khác | Tất cả đều show validation error |

### Expected Results

**Validation Rule**: Regex pattern `/^[^\s@]+@[^\s@]+\.[^\s@]+$/`  
**Error Message**: "Email không đúng định dạng"  
**All Test Cases**: Phải fail validation  
**API**: Not called for any invalid format

### Test Cases Coverage

| Input | Valid? | Expected Error |
|-------|--------|----------------|
| `user` | ❌ | Invalid format |
| `user@` | ❌ | Invalid format |
| `@domain.com` | ❌ | Invalid format |
| `user @domain.com` | ❌ | Invalid format (space) |
| `user@domain` | ❌ | Invalid format (no TLD) |
| `user@domain.com` | ✅ | No error |

### Actual Result
_(To be filled)_

### Status
- [ ] Not Run
- [ ] Pass
- [ ] Fail

---

## Test Summary Table

| Test Case ID | Test Name | Priority | Type | Expected Outcome | Actual Status |
|--------------|-----------|----------|------|------------------|---------------|
| TC_LOGIN_001 | Login thành công | Critical | Positive | Pass | Not Run |
| TC_LOGIN_002 | Login thất bại - Wrong credentials | Critical | Negative | Error displayed | Not Run |
| TC_LOGIN_003 | Validation - Email empty | Critical | Negative | Validation error | Not Run |
| TC_LOGIN_004 | Validation - Password empty | Critical | Negative | Validation error | Not Run |
| TC_LOGIN_005 | Validation - Email invalid format | High | Negative | Validation error | Not Run |

---

## Test Execution Notes

### Environment
- **Frontend URL**: http://localhost:8080
- **Backend API**: http://localhost:8081
- **Database**: Oracle DB (auth_user table)
- **Browser**: Chrome/Firefox/Edge (specify)

### Test Data Setup
```sql
-- Verify test user exists
SELECT * FROM users WHERE email = 'test@example.com';
```

### Pre-test Checklist
- [ ] Backend server running
- [ ] Frontend server running
- [ ] Test user exists in database
- [ ] Browser cache cleared
- [ ] No existing token in localStorage

---

**Người tạo**: Nhóm FloginFE_BE  
**Ngày tạo**: 30/11/2024  
**Version**: 1.0  
**Last Updated**: 30/11/2024
