# Product - Phân tích Yêu cầu Chức năng

## 1. Tổng quan
Chức năng Product Management cho phép người dùng quản lý sản phẩm với đầy đủ các thao tác CRUD (Create, Read, Update, Delete). Hệ thống lưu trữ thông tin sản phẩm trong PostgreSQL database.

## 2. CRUD Requirements

### 2.1 CREATE - Tạo sản phẩm mới

**Chức năng**: Cho phép user tạo sản phẩm mới với đầy đủ thông tin

**Input Fields**:
- Product Name (required)
- Description (optional)
- Price (required)
- Quantity (required)

**Process Flow**:
```
User clicks "Add Product" → Form modal opens → User fills data → 
Submit → Validation → API Call → Database Insert → Success/Error Response
```

**API Endpoint**: `POST /api/products`

**Success Criteria**:
- Product được lưu vào database
- Product có ID mới (auto-generated)
- Success message hiển thị
- Product hiển thị trong danh sách

---

### 2.2 READ - Xem danh sách và chi tiết sản phẩm

**2.2.1 Get All Products**

**Chức năng**: Hiển thị danh sách tất cả sản phẩm

**API Endpoint**: `GET /api/products`

**Response**: Array of products với thông tin:
- ID, Name, Description, Price, Quantity
- Created date, Updated date

**UI Requirements**:
- Hiển thị dạng table hoặc grid
- Show product image (nếu có)
- Actions: Edit, Delete buttons

**2.2.2 Get Single Product**

**Chức năng**: Xem chi tiết một sản phẩm

**API Endpoint**: `GET /api/products/{id}`

**Use Case**: 
- Click vào product trong list để xem detail
- Pre-fill form khi edit

---

### 2.3 UPDATE - Cập nhật thông tin sản phẩm

**Chức năng**: Sửa thông tin sản phẩm đã tồn tại

**Process Flow**:
```
User clicks Edit button → Form pre-filled với data hiện tại → 
User modifies data → Submit → Validation → API Call → 
Database Update → Success Response
```

**API Endpoint**: `PUT /api/products/{id}`

**Rules**:
- Chỉ update các fields được thay đổi
- ID không được thay đổi
- Created date không được thay đổi
- Updated date tự động cập nhật

**Optimistic Lock**: Có thể implement version checking để tránh concurrent updates

---

### 2.4 DELETE - Xóa sản phẩm

**Chức năng**: Xóa sản phẩm khỏi hệ thống

**Process Flow**:
```
User clicks Delete → Confirmation dialog → Confirm → 
API Call → Database Delete → Success → Remove from UI list
```

**API Endpoint**: `DELETE /api/products/{id}`

**Safety**:
- Phải có confirmation dialog trước khi xóa
- Soft delete vs Hard delete: Hard delete (xóa hẳn khỏi DB)

**Error Cases**:
- Product không tồn tại → 404 Not Found
- Product đang được sử dụng (nếu có foreign key) → 400 Bad Request

---

## 3. Validation Rules

### 3.1 Product Name
- **Bắt buộc**: Không được để trống
- **Độ dài**: 
  - Tối thiểu: 3 ký tự
  - Tối đa: 100 ký tự
- **Ký tự cho phép**: Chữ, số, space, dấu câu cơ bản
- **Ví dụ hợp lệ**: 
  - `Laptop Dell XPS 15`
  - `iPhone 15 Pro Max`
  - `Bàn phím cơ Keychron K2`
- **Ví dụ không hợp lệ**:
  - _(empty)_ → "Tên sản phẩm không được để trống"
  - `AB` → "Tên sản phẩm phải có ít nhất 3 ký tự"
  - _(string > 100 chars)_ → "Tên sản phẩm không quá 100 ký tự"

---

### 3.2 Description
- **Tùy chọn**: Có thể để trống
- **Độ dài tối đa**: 500 ký tự
- **Ký tự cho phép**: Tất cả (UTF-8)
- **Sanitization**: Escape HTML/script tags để tránh XSS
- **Ví dụ**: 
  ```
  Laptop cao cấp dành cho lập trình viên và designers. 
  Màn hình 15.6 inch, CPU Intel Core i7 gen 12, RAM 16GB.
  ```

---

### 3.3 Price (Giá)
- **Bắt buộc**: Không được để trống
- **Kiểu dữ liệu**: Number (BigDecimal trong Java)
- **Giá trị**:
  - **Minimum**: > 0 (VNĐ)
  - **Maximum**: ≤ 999,999,999 (1 tỷ VNĐ)
- **Format**: 
  - Backend: BigDecimal
  - Frontend display: Có dấu phẩy (15,000,000)
  - Input: Number field
- **Examples**:
  - ✅ `100` (100 VNĐ)
  - ✅ `15000000` (15 triệu VNĐ)
  - ✅ `999999999` (Max)
  - ❌ `0` → "Giá phải lớn hơn 0"
  - ❌ `-100` → "Giá phải lớn hơn 0"
  - ❌ `1000000000` → "Giá không vượt quá 999,999,999"

---

### 3.4 Quantity (Số lượng)
- **Bắt buộc**: Không được để trống
- **Kiểu dữ liệu**: Integer
- **Giá trị**:
  - **Minimum**: ≥ 0 (cho phép out of stock)
  - **Maximum**: ≤ 99,999
- **Examples**:
  - ✅ `0` (hết hàng)
  - ✅ `10`
  - ✅ `99999` (Max)
  - ❌ `-1` → "Số lượng không được âm"
  - ❌ `100000` → "Số lượng không quá 99,999"

---

## 4. API Specification

### 4.1 Create Product

**Request**:
```http
POST /api/products
Content-Type: application/json

{
  "name": "Laptop Dell XPS 15",
  "description": "Laptop cao cấp cho developers",
  "price": 35000000,
  "quantity": 5
}
```

**Success Response (201 Created)**:
```json
{
  "id": 101,
  "name": "Laptop Dell XPS 15",
  "description": "Laptop cao cấp cho developers",
  "price": 35000000,
  "quantity": 5,
  "createdAt": "2024-11-30T10:30:00",
  "updatedAt": "2024-11-30T10:30:00"
}
```

**Error Response (400 Bad Request)**:
```json
{
  "message": "Validation failed",
  "errors": {
    "name": "Tên sản phẩm không được để trống",
    "price": "Giá phải lớn hơn 0"
  }
}
```

---

### 4.2 Get All Products

**Request**:
```http
GET /api/products
```

**Success Response (200 OK)**:
```json
[
  {
    "id": 1,
    "name": "Laptop Dell",
    "description": "...",
    "price": 15000000,
    "quantity": 10
  },
  {
    "id": 2,
    "name": "Mouse Logitech",
    "price": 500000,
    "quantity": 50
  }
]
```

---

### 4.3 Get Product by ID

**Request**:
```http
GET /api/products/1
```

**Success Response (200 OK)**:
```json
{
  "id": 1,
  "name": "Laptop Dell",
  "description": "High performance laptop",
  "price": 15000000,
  "quantity": 10,
  "createdAt": "2024-11-01T10:00:00",
  "updatedAt": "2024-11-20T15:30:00"
}
```

**Error Response (404 Not Found)**:
```json
{
  "message": "Product not found with id: 1"
}
```

---

### 4.4 Update Product

**Request**:
```http
PUT /api/products/1
Content-Type: application/json

{
  "name": "Laptop Dell XPS 15 (Updated)",
  "price": 32000000,
  "quantity": 8
}
```

**Success Response (200 OK)**:
```json
{
  "id": 1,
  "name": "Laptop Dell XPS 15 (Updated)",
  "description": "High performance laptop",
  "price": 32000000,
  "quantity": 8,
  "updatedAt": "2024-11-30T11:00:00"
}
```

---

### 4.5 Delete Product

**Request**:
```http
DELETE /api/products/1
```

**Success Response (204 No Content)**:
```
(Empty body)
```

**Error Response (404 Not Found)**:
```json
{
  "message": "Product not found with id: 1"
}
```

---

## 5. UI/UX Requirements

### 5.1 Product List Page
- **Display**: Table or Grid layout
- **Columns**: Name, Price, Quantity, Actions
- **Actions per row**:
  - View button (optional)
  - Edit button
  - Delete button
- **Top actions**:
  - "Add New Product" button
  - Search/Filter (nice to have)
- **Empty State**: "Chưa có sản phẩm nào" với button "Thêm sản phẩm đầu tiên"

### 5.2 Product Form (Create/Edit)
- **Display**: Modal dialog hoặc separate page
- **Form Fields**:
  - Product Name (text input)
  - Description (textarea)
  - Price (number input)
  - Quantity (number input)
- **Buttons**:
  - "Create Product" / "Update Product"
  - "Cancel"
- **Error Display**: Inline errors dưới mỗi field
- **Loading State**: Disable form và hiển thị spinner khi submit

### 5.3 Delete Confirmation
- **Dialog**: Modal confirmation
- **Message**: "Bạn có chắc muốn xóa sản phẩm '[Product Name]'?"
- **Buttons**: 
  - "Xóa" (destructive - màu đỏ)
  - "Hủy"

---

## 6. Security Requirements

### 6.1 Authentication
- User phải đăng nhập mới được quản lý products
- JWT token phải được gửi trong header: `Authorization: Bearer <token>`

### 6.2 Authorization
- Tất cả users đã login đều có quyền CRUD products (hiện tại)
- Có thể thêm role-based access control sau (Admin only)

### 6.3 Input Sanitization
- Escape HTML trong description để tránh XSS
- Validate data type trước khi lưu database
- Prevent SQL injection (sử dụng prepared statements)

---

## 7. Performance Requirements

### 7.1 Response Time
- Get all products: < 500ms (với < 1000 products)
- CRUD operations: < 1 second
- UI should show loading indicators

### 7.2 Pagination (Future)
- Khi số lượng products > 100: implement pagination
- Page size: 20 products per page

---

## 8. Test Data Requirements

### Valid Product Examples
```json
{
  "name": "Laptop Dell XPS 15",
  "description": "High-end laptop for developers",
  "price": 35000000,
  "quantity": 5
}

{
  "name": "Mouse Logitech MX Master 3",
  "price": 2000000,
  "quantity": 20
}

{
  "name": "Bàn phím Keychron K2",
  "description": "",
  "price": 1500000,
  "quantity": 0
}
```

### Invalid Examples
```json
{
  "name": "",     // Empty name - invalid
  "price": -100,  // Negative price - invalid
  "quantity": -5  // Negative quantity - invalid
}
```

---

## 9. Acceptance Criteria

✅ User có thể tạo product mới với thông tin hợp lệ  
✅ Product list hiển thị tất cả products từ database  
✅ User có thể edit product và thay đổi được lưu  
✅ User có thể delete product sau khi confirm  
✅ Validation errors hiển thị rõ ràng cho invalid inputs  
✅ Form có loading state khi submit  
✅ Confirmation dialog xuất hiện trước khi delete  
✅ Empty state hiển thị khi chưa có product  
✅ Price và quantity được validate theo boundaries  
✅ Description tùy chọn (có thể để trống)  

---

**Người tạo**: Nhóm FloginFE_BE  
**Ngày tạo**: 30/11/2024  
**Version**: 1.0
