# Product - Test Scenarios

## Danh sách Test Scenarios

Tổng số: **12 scenarios**  
Phân loại: Critical (5), High (4), Medium (2), Low (1)

---

## A. CREATE - Tạo sản phẩm mới

### TS_PRODUCT_001: Tạo sản phẩm thành công với đầy đủ thông tin
- **Priority**: 🔴 **Critical**
- **Mô tả**: User tạo sản phẩm mới với tất cả fields hợp lệ
- **Test Data**:
  - Name: `Laptop Dell XPS 15`
  - Description: `High-end laptop for developers`
  - Price: `35000000`
  - Quantity: `10`
- **Expected**: Product created, ID returned, shown in list

### TS_PRODUCT_002: Tạo sản phẩm với description rỗng (optional field)
- **Priority**: 🟠 **High**
- **Mô tả**: Description là optional, có thể để trống
- **Test Data**:
  - Name: `Mouse Logitech`
  - Description: _(empty)_
  - Price: `500000`
  - Quantity: `20`
- **Expected**: Product created successfully

### TS_PRODUCT_003: Validation - Tên sản phẩm rỗng
- **Priority**: 🔴 **Critical**
- **Test Data**: Name empty, other fields valid
- **Expected**: "Tên sản phẩm không được để trống"

### TS_PRODUCT_004: Validation - Giá = 0 hoặc âm
- **Priority**: 🔴 **Critical**
- **Test Data**: Price = `0` hoặc `-100`
- **Expected**: "Giá phải lớn hơn 0"

### TS_PRODUCT_005: Validation - Số lượng âm
- **Priority**: 🟠 **High**
- **Test Data**: Quantity = `-5`
- **Expected**: "Số lượng không được âm"

---

## B. READ - Xem danh sách và chi tiết

### TS_PRODUCT_006: Hiển thị danh sách tất cả sản phẩm
- **Priority**: 🔴 **Critical**
- **Mô tả**: GET all products và hiển thị trong UI
- **Expected**: All products shown, correct data

### TS_PRODUCT_007: Xem chi tiết một sản phẩm
- **Priority**: 🟠 **High**
- **Mô tả**: GET /api/products/{id}
- **Expected**: Product details displayed correctly

---

## C. UPDATE - Cập nhật sản phẩm

### TS_PRODUCT_008: Cập nhật sản phẩm thành công
- **Priority**: 🔴 **Critical**
- **Mô tả**: User edit product và thay đổi name, price
- **Test Steps**:
  1. Click Edit on existing product
  2. Change name and price
  3. Submit
- **Expected**: Product updated, changes reflected

### TS_PRODUCT_009: Cập nhật sản phẩm không tồn tại
- **Priority**: 🟡 **Medium**
- **Test Data**: PUT /api/products/99999 (ID không tồn tại)
- **Expected**: 404 Not Found error

---

## D. DELETE - Xóa sản phẩm

### TS_PRODUCT_010: Xóa sản phẩm thành công
- **Priority**: 🔴 **Critical**
- **Mô tả**: User delete product after confirmation
- **Test Steps**:
  1. Click Delete button
  2. Confirm in dialog
  3. Product deleted
- **Expected**: Product removed from list, 204 response

### TS_PRODUCT_011: Hủy xóa sản phẩm
- **Priority**: 🟠 **High**
- **Mô tả**: User clicks Cancel trong confirmation dialog
- **Expected**: Product NOT deleted, still in list

### TS_PRODUCT_012: Xóa sản phẩm không tồn tại
- **Priority**: 🟡 **Medium**
- **Test Data**: DELETE /api/products/99999
- **Expected**: 404 Not Found error

---

## E. BOUNDARY TESTS

### Test với giá trị boundary
- **Name**: 
  - Min (3 chars): `ABC` ✅
  - Max (100 chars): _(100 character string)_ ✅
  - Too short (2 chars): `AB` ❌
- **Price**:
  - Min valid: `1` ✅
  - Max: `999999999` ✅
  - Zero: `0` ❌
  - Over max: `1000000000` ❌
- **Quantity**:
  - Zero (out of stock): `0` ✅
  - Max: `99999` ✅
  - Negative: `-1` ❌

---

## Priority Summary

### 🔴 Critical (5): 
TS_PRODUCT_001, 003, 004, 006, 008, 010

### 🟠 High (4):
TS_PRODUCT_002, 005, 007, 011

### 🟡 Medium (2):
TS_PRODUCT_009, 012

---

**Người tạo**: Nhóm FloginFE_BE  
**Ngày tạo**: 30/11/2024
